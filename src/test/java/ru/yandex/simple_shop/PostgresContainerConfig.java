package ru.yandex.simple_shop;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PostgresContainerConfig {

	private static final PostgreSQLContainer<?> postgres;

	static {
		postgres = new PostgreSQLContainer<>("postgres:15").withDatabaseName("testdb") // Название базы данных
				.withUsername("junit")      // Логин
				.withPassword("junit")
				.withInitScript("testSchema.sql");
		// Имя и версия образа .withDatabaseName("testdb") // Название базы данных .withUsername("junit")      // Логин                .withPassword("junit");     // Пароль        postgres.start();
	}

	@BeforeAll
	static void initPostgres() {
		postgres.start();
	}

	@AfterAll
	static void stopPostgres() {
		postgres.close();
	}

	@DynamicPropertySource
	static void configureProperties(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

}
