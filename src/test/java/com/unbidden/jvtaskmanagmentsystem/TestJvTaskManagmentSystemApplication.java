package com.unbidden.jvtaskmanagmentsystem;

import com.unbidden.jvtaskmanagementsystem.JvTaskManagmentSystemApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.TestConfiguration;

@TestConfiguration(proxyBeanMethods = false)
public class TestJvTaskManagmentSystemApplication {

    public static void main(String[] args) {
        SpringApplication.from(JvTaskManagmentSystemApplication::main)
                .with(TestJvTaskManagmentSystemApplication.class).run(args);
    }

}
