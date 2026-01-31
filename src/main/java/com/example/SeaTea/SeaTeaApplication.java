package com.example.SeaTea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// createdAt, updatedAt, deletedAt 자동 생성 어노테이션 추가
@EnableJpaAuditing
@SpringBootApplication
public class SeaTeaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SeaTeaApplication.class, args);
	}

}
