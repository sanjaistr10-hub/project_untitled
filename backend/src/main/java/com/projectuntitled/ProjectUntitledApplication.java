package com.projectuntitled;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProjectUntitledApplication {

    public static void main(String[] args) {
        SpringApplication.run(ProjectUntitledApplication.class, args);
    }
}
