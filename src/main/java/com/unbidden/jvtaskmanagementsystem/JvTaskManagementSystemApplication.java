package com.unbidden.jvtaskmanagementsystem;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
        info = @Info(
            title = "Task Management System",
            version = "1.0"
        )
)
@SpringBootApplication
public class JvTaskManagementSystemApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(JvTaskManagementSystemApplication.class, args);
    }
}
