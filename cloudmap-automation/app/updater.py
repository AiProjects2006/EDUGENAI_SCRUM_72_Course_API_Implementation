
import logging
import os
import time

import boto3
from botocore.exceptions import BotoCoreError, ClientError
from kubernetes import client, config
from kubernetes.config.config_exception import ConfigException

# --------------------------------------------------
# Configuration
# --------------------------------------------------

REGION = os.getenv("AWS_REGION", "us-east-1")

CLOUD_MAP_SERVICE_ID = os.getenv(
    "CLOUD_MAP_SERVICE_ID",
    "srv-jmlnaub3w4gugan2",
)

K8S_NAMESPACE = os.getenv("K8S_NAMESPACE", "default")

POD_LABEL_SELECTOR = os.getenv(
    "POD_LABEL_SELECTOR",
    "app=course-service",
)

PORT = int(os.getenv("COURSE_SERVICE_PORT", "8082"))

SYNC_INTERVAL_SECONDS = int(
    os.getenv("SYNC_INTERVAL_SECONDS", "30")
)

OPERATION_TIMEOUT_SECONDS = int(
    os.getenv("OPERATION_TIMEOUT_SECONDS", "120")
)

INSTANCE_PREFIX = "course-service-pod-"


logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)s %(message)s",
)

logger = logging.getLogger("cloud-map-updater")


# --------------------------------------------------
# Kubernetes client
# --------------------------------------------------

def create_kubernetes_client():
    """Create a Kubernetes client using in-cluster credentials."""

    try:
        config.load_incluster_config()
    except ConfigException as exc:
        raise RuntimeError(
            "Cannot load Kubernetes in-cluster configuration. "
            "Deploy this updater inside EKS."
        ) from exc

    return client.CoreV1Api()


def get_ready_course_pod(k8s):
    """
    Find a ready Course Service pod.

    If Kubernetes cannot be queried, the API exception propagates.
    If no ready pod exists, raise an error and keep Cloud Map unchanged.
    """

    pods = k8s.list_namespaced_pod(
        namespace=K8S_NAMESPACE,
        label_selector=POD_LABEL_SELECTOR,
    ).items

    ready_pods = []

    for pod in pods:
        status = pod.status
        metadata = pod.metadata

        if status is None or metadata is None:
            continue

        if (
            status.phase != "Running"
            or not status.pod_ip
            or not metadata.name
        ):
            continue

        conditions = status.conditions or []

        is_ready = any(
            condition.type == "Ready"
            and condition.status == "True"
            for condition in conditions
        )

        if is_ready:
            ready_pods.append(pod)

    if not ready_pods:
        raise RuntimeError(
            "No ready Course Service pod found. "
            "Keeping Cloud Map unchanged."
        )

    # Deterministic choice for a single-replica setup.
    # For multiple replicas, use the multi-pod approach described below.
    pod = min(
        ready_pods,
        key=lambda item: item.metadata.name or "",
    )

    return pod.metadata.name, pod.status.pod_ip


# --------------------------------------------------
# Cloud Map helpers
# --------------------------------------------------

def create_cloud_map_client():
    """Create a Cloud Map client using the pod's AWS credentials."""

    return boto3.client(
        "servicediscovery",
        region_name=REGION,
    )


def wait_for_operation(cloud_map, operation_id):
    """Wait for an AWS Cloud Map operation to finish."""

    deadline = time.monotonic() + OPERATION_TIMEOUT_SECONDS

    while time.monotonic() < deadline:
        result = cloud_map.get_operation(
            OperationId=operation_id
        )

        operation = result["Operation"]
        status = operation["Status"]

        if status == "SUCCESS":
            return

        if status == "FAIL":
            raise RuntimeError(
                f"Cloud Map operation {operation_id} failed: "
                f"{operation.get('ErrorMessage', 'unknown error')}"
            )

        time.sleep(3)

    raise TimeoutError(
        f"Cloud Map operation {operation_id} did not finish "
        f"within {OPERATION_TIMEOUT_SECONDS} seconds."
    )


def list_instances(cloud_map):
    """Return all registrations for the configured Cloud Map service."""

    instances = []

    paginator = cloud_map.get_paginator("list_instances")

    for page in paginator.paginate(
        ServiceId=CLOUD_MAP_SERVICE_ID
    ):
        instances.extend(page.get("Instances", []))

    return instances


def make_instance_id(pod_ip):
    """Create a stable instance ID for the current pod IP."""

    return INSTANCE_PREFIX + pod_ip.replace(".", "-")


def register_current_pod(cloud_map, instance_id, pod_ip):
    """Register the current pod and wait for AWS confirmation."""

    response = cloud_map.register_instance(
        ServiceId=CLOUD_MAP_SERVICE_ID,
        InstanceId=instance_id,
        Attributes={
            "AWS_INSTANCE_IPV4": pod_ip,
            "AWS_INSTANCE_PORT": str(PORT),
        },
    )

    operation_id = response.get("OperationId")

    if not operation_id:
        raise RuntimeError(
            "Cloud Map did not return an operation ID "
            "for the registration."
        )

    wait_for_operation(cloud_map, operation_id)

    logger.info(
        "Registration succeeded: instance=%s ip=%s port=%s",
        instance_id,
        pod_ip,
        PORT,
    )


def deregister_instance(cloud_map, instance_id):
    """Remove an obsolete registration and wait for confirmation."""

    response = cloud_map.deregister_instance(
        ServiceId=CLOUD_MAP_SERVICE_ID,
        InstanceId=instance_id,
    )

    operation_id = response.get("OperationId")

    if not operation_id:
        raise RuntimeError(
            f"No operation ID returned while removing {instance_id}."
        )

    wait_for_operation(cloud_map, operation_id)

    logger.info("Removed obsolete registration: %s", instance_id)


# --------------------------------------------------
# Synchronization
# --------------------------------------------------

def sync_once(k8s, cloud_map):
    """
    Synchronize one ready pod with Cloud Map.

    Safety properties:
    - Kubernetes lookup failure stops cleanup.
    - No ready pod stops cleanup.
    - Registration failure stops cleanup.
    - Registration is verified before old updater-managed entries
      are removed.
    - Registrations outside INSTANCE_PREFIX are never removed.
    """

    try:
        pod_name, pod_ip = get_ready_course_pod(k8s)
    except Exception:
        logger.exception(
            "Cannot identify a ready Course Service pod. "
            "Existing Cloud Map registrations will be preserved."
        )
        return

    desired_instance_id = make_instance_id(pod_ip)

    logger.info(
        "Ready Course Service pod: name=%s ip=%s",
        pod_name,
        pod_ip,
    )

    try:
        instances = list_instances(cloud_map)

        existing = next(
            (
                instance
                for instance in instances
                if instance.get("Id") == desired_instance_id
            ),
            None,
        )

        attributes = (
            existing.get("Attributes", {})
            if existing
            else {}
        )

        registration_is_correct = (
            attributes.get("AWS_INSTANCE_IPV4") == pod_ip
            and attributes.get("AWS_INSTANCE_PORT") == str(PORT)
        )

        if not registration_is_correct:
            logger.info(
                "Registering current pod address in Cloud Map."
            )

            register_current_pod(
                cloud_map,
                desired_instance_id,
                pod_ip,
            )

        # Re-read Cloud Map after registration and verify the address.
        verified_instances = list_instances(cloud_map)

        verified = next(
            (
                instance
                for instance in verified_instances
                if instance.get("Id") == desired_instance_id
            ),
            None,
        )

        verified_attributes = (
            verified.get("Attributes", {})
            if verified
            else {}
        )

        if (
            verified is None
            or verified_attributes.get("AWS_INSTANCE_IPV4") != pod_ip
            or verified_attributes.get("AWS_INSTANCE_PORT") != str(PORT)
        ):
            logger.error(
                "Registration verification failed. "
                "No old registrations will be removed."
            )
            return

        logger.info(
            "Current pod registration is verified: %s -> %s",
            desired_instance_id,
            pod_ip,
        )

        # Cleanup only entries owned by this updater.
        # Do not remove registrations with other naming conventions.
        for instance in verified_instances:
            instance_id = instance.get("Id", "")

            if (
                instance_id.startswith(INSTANCE_PREFIX)
                and instance_id != desired_instance_id
            ):
                logger.info(
                    "Removing old updater-managed registration: %s",
                    instance_id,
                )

                deregister_instance(
                    cloud_map,
                    instance_id,
                )

        logger.info("Cloud Map synchronization completed.")

    except (BotoCoreError, ClientError, TimeoutError, RuntimeError):
        logger.exception(
            "Cloud Map synchronization failed. "
            "Cleanup stopped; check AWS and Kubernetes permissions."
        )

    except Exception:
        logger.exception(
            "Unexpected synchronization error. "
            "No further cleanup will be attempted in this cycle."
        )


# --------------------------------------------------
# Main loop
# --------------------------------------------------

def main():
    logger.info("Starting Course Service Cloud Map updater.")
    logger.info("AWS region: %s", REGION)
    logger.info("Cloud Map service ID: %s", CLOUD_MAP_SERVICE_ID)
    logger.info("Kubernetes namespace: %s", K8S_NAMESPACE)
    logger.info("Pod selector: %s", POD_LABEL_SELECTOR)
    logger.info("Course Service port: %s", PORT)

    k8s = create_kubernetes_client()
    cloud_map = create_cloud_map_client()

    while True:
        sync_once(k8s, cloud_map)
        time.sleep(SYNC_INTERVAL_SECONDS)


if __name__ == "__main__":
    main()
