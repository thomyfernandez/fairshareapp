package com.example.fairshareapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class FairshareappApplication {

	public static void main(String[] args) {
		SpringApplication.run(FairshareappApplication.class, args);
	}

	@GetMapping("/")
	public String home() {
		return "La aplicación funciona correctamente";
	}

}
