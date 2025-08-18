package com.gooners.watguessr;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class WatGuessr {
	public static void main(String[] args) {
		SpringApplication.run(WatGuessr.class, args);
	}

	@Autowired
	private Environment env;

	@PostConstruct
	public void checkEnv() {
		System.out.println("Mail host = " + env.getProperty("SPRING_MAIL_HOST"));
	}
}

