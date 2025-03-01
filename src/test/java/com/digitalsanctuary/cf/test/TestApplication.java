package com.digitalsanctuary.cf.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import lombok.extern.slf4j.Slf4j;

@Slf4j

@SpringBootApplication
public class TestApplication {
    // This class doesn't need any content, it just needs the @SpringBootApplication annotation
    public TestApplication() {
        log.debug("empty constructor");
    }
}
