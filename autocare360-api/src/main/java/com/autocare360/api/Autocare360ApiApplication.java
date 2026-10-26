package com.autocare360.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class Autocare360ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(Autocare360ApiApplication.class, args);
    	}
	}
