package com.fighthub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class FightHubApplication {

	public static void main(String[] args) {
		SpringApplication.run(FightHubApplication.class, args);
	}

}
