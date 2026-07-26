package com.allforland.automation;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class AllforlandAutomationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AllforlandAutomationApplication.class, args);
	}
}
