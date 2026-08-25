package io.github.jair.anderson.souza.capavailability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CapAvailabilityApplication {

	public static void main(String[] args) {
		SpringApplication.run(CapAvailabilityApplication.class, args);
	}

}
