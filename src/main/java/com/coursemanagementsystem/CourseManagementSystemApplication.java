package com.coursemanagementsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class CourseManagementSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourseManagementSystemApplication.class, args);
    }

}
