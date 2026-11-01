package com.ailearning.courseservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
    "KEYCLOAK_ISSUER_URI=http://localhost:8080/realms/test"
})
class CourseServiceApplicationTests {

    @Test
    void contextLoads() {
    }

}
