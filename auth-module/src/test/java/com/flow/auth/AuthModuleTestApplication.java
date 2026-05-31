package com.flow.auth;

import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

@SuppressWarnings("unused")
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "com.flow.auth.api")
public class AuthModuleTestApplication {
}




