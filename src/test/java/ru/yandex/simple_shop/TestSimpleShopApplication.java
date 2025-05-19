package ru.yandex.simple_shop;

import org.springframework.boot.SpringApplication;

public class TestSimpleShopApplication {

	public static void main(String[] args) {
		SpringApplication.from(SimpleShopApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
