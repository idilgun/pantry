package com.localbanana.pantry;

import org.springframework.boot.SpringApplication;

public class TestPantryApplication {

	public static void main(String[] args) {
		SpringApplication.from(PantryApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
