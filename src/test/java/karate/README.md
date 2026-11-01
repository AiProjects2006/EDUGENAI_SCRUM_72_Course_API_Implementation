# Karate API tests

This suite tests the Course Service API declared by the Spring controllers. It uses Karate 1.4.1 and the existing JUnit 5 runner; no Maven configuration changes are required.

## Run

From `D:\Projects\course-service` in PowerShell, start the application and run:

```powershell
mvn test "-Dtest=CourseApiRunner" "-DbaseUrl=http://localhost:8082"
```

Override `-DbaseUrl` to test another local or test deployment. Do not point this suite at production: lifecycle scenarios create, update, and delete uniquely named records.

## Endpoints covered

- `GET /api/v1/courses`, `GET /api/v1/courses/{id}`, `GET /api/v1/courses/subject/{subjectId}`, `GET /api/v1/courses/creator/{creatorId}`
- `POST /api/v1/courses`, `PUT /api/v1/courses/{id}`, `DELETE /api/v1/courses/{id}`
- `GET /api/v1/subjects`, `GET /api/v1/subjects/{id}`, `POST /api/v1/subjects`, `PUT /api/v1/subjects/{id}`, `DELETE /api/v1/subjects/{id}`
- `GET /api/v1/modules`, `GET /api/v1/modules/{id}`, `POST /api/v1/modules`, `PUT /api/v1/modules/{id}`, `DELETE /api/v1/modules/{id}`

The scenarios assert the `success`, `message`, and `data` response envelope, entity fields, successful status codes, validation errors, and not-found behavior. They use random names and only delete records created within their own scenario.

## Prerequisites and authentication

Start the application with its configured MySQL `course_db` available. The application properties also reference `KEYCLOAK_ISSUER_URI`; supply the issuer configuration needed for application startup. `SecurityConfig` permits all requests, so these tests do not require a token and do not send credentials.

The base URL defaults to `http://localhost:8082` (the configured application port). Set `-DbaseUrl=...` to override it.
