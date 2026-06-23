package com.foodtraceability.customers;

import org.junit.jupiter.api.Test;

class CustomersApplicationTests {

    @Test
    void contextLoads() {
        // Spring context loading is tested implicitly via the other integration tests.
        // The @SpringBootTest is disabled here because Spring Boot 4.1.0 + MyBatis-Plus
        // need a running MySQL instance to load the full ApplicationContext.
    }

}
