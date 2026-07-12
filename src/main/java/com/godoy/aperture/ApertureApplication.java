package com.godoy.aperture;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ApertureApplication {

	public static void main(String[] args) {
		SpringApplication.run(ApertureApplication.class, args);
	}

}
