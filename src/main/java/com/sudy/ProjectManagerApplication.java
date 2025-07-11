package com.sudy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan("com.sudy.*")

public class ProjectManagerApplication {
    public static void main(String[] args) {


        SpringApplication.run(ProjectManagerApplication.class, args);
    }
}