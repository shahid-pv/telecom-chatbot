package com.skytel.support;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** Starts the SkyTel support application and its Spring services. */
@SpringBootApplication
public class SkyTelApplication {
	/** Starts the application using the supplied command-line arguments. */
	public static void main(String[] args) {
		SpringApplication.run(SkyTelApplication.class, args);
	}
}
