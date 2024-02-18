package com.unbidden.jvtaskmanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestJvTaskManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.from(JvTaskManagementSystemApplication::main)
                .with(TestJvTaskManagementSystemApplication.class).run(args);
    }

}
