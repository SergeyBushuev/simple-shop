package ru.yandex.simple_shop;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PostgresContainerConfig implements RedisConfig {

	private static final PostgreSQLContainer<?> postgres;

	static {
		postgres = new PostgreSQLContainer<>("postgres:15").withDatabaseName("testdb") // Название базы данных
				.withUsername("junit")      // Логин
				.withPassword("junit")
				.withInitScript("testSchema.sql")
				.withReuse(true);
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
		String r2dbcUrl = String.format(
				"r2dbc:postgresql://%s:%d/%s",
				postgres.getHost(),
				postgres.getMappedPort(5432),
				postgres.getDatabaseName()
		);
		registry.add("spring.r2dbc.url", () -> r2dbcUrl);
		registry.add("spring.r2dbc.username", postgres::getUsername);
		registry.add("spring.r2dbc.password", postgres::getPassword);
	}

}
