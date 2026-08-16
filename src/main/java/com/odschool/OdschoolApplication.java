package com.odschool;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class OdschoolApplication {

	public static void main(String[] args) {
		SpringApplication.run(OdschoolApplication.class, args);
	}

}
