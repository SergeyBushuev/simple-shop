package ru.yandex.simple_shop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class SimpleShopApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleShopApplication.class, args);
	}

}
