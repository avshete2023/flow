package com.flow.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.flow")
public class FlowApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(FlowApiApplication.class, args);
	}
}

