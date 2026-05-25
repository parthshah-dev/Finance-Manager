package com.example.moneymanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class MoneymanagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(MoneymanagerApplication.class, args);
	}

}
