package com.example.librashare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class LibrashareApplication {

	public static void main(String[] args) {
		SpringApplication.run(LibrashareApplication.class, args);
	}

}
