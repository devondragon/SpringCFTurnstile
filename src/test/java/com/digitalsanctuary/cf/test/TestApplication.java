package com.digitalsanctuary.cf.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

/**
 * Test application class for running tests with Spring Boot.
 */
@Slf4j
@SpringBootApplication
public class TestApplication {
    // This class doesn't need any content, it just needs the @SpringBootApplication annotation
    /**
     * Default constructor for TestApplication.
     */
    public TestApplication() {
        log.debug("empty constructor");
    }
}
