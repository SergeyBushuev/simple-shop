package ru.yandex.simple_shop.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import ru.yandex.simple_shop.PostgresContainerConfig;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ShopControllerIT extends PostgresContainerConfig {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @SneakyThrows
    void redirectToShowcase_OkTest() {
        webTestClient.get()
                .uri("/")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");
    }

    @Test
    @SneakyThrows
    void showcase_OkTest() {
        webTestClient.get()
                .uri("/main/items")
                .attribute("search", "")
                .attribute("sort", "ALPHA")
                .attribute("pageNumber", "1")
                .attribute("pageSize", "10")
                .exchange().expectStatus().isOk().expectBody(String.class);

    }

    @Test
    @SneakyThrows
    void itemShowcase_OkTest() {
        webTestClient.get()
                .uri("/items/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//p[2]/b[1]").isEqualTo("Big cat");
    }

    @Test
    @SneakyThrows
    void emptyCart_OkTest() {
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr").nodeCount(2);

    }

    @Test
    @SneakyThrows
    void addToCart_OkTest() {
        MultiValueMap<String, String> bodyPlus = new LinkedMultiValueMap<>();
        bodyPlus.add("action", "plus");


        webTestClient.post()
                .uri("/main/items/1")
                .bodyValue(bodyPlus)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().location("/main/items");

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr[1]//tr[2]/td[1]/b").isEqualTo("Big cat");

    }

    @Test
    @SneakyThrows
    void cartInteraction_OkTest() {

        MultiValueMap<String, String> bodyPlus = new LinkedMultiValueMap<>();
        bodyPlus.add("action", "plus");

        MultiValueMap<String, String> bodyMinus = new LinkedMultiValueMap<>();
        bodyMinus.add("action", "minus");

        MultiValueMap<String, String> bodyDelete = new LinkedMultiValueMap<>();
        bodyDelete.add("action", "delete");

        webTestClient.post()
                .uri("/items/1")
                .bodyValue(bodyPlus)
                .exchange().expectStatus().is3xxRedirection();
        webTestClient.post()
                .uri("/cart/items/1")
                .bodyValue(bodyPlus)
                .exchange().expectStatus().is3xxRedirection();
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr[1]//tr[2]/td[1]/b").isEqualTo("Big cat")
                .xpath("//tr[1]//tr[4]//span").isEqualTo(2.0);

        webTestClient.post()
                .uri("/cart/items/1")
                .bodyValue(bodyMinus)
                .exchange().expectStatus().is3xxRedirection();

        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr[1]//tr[4]//span").isEqualTo(1.0);

        webTestClient.post()
                .uri("/cart/items/1")
                .bodyValue(bodyDelete)
                .exchange().expectStatus().is3xxRedirection();
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr").nodeCount(2);
    }

    @Test
    @SneakyThrows
    void buyItems_OkTest() {
        MultiValueMap<String, String> bodyPlus = new LinkedMultiValueMap<>();
        bodyPlus.add("action", "plus");

        webTestClient.post()
                .uri("/items/1")
                .bodyValue(bodyPlus)
                .exchange().expectStatus().is3xxRedirection();
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr[1]//tr[2]/td[1]/b").isEqualTo("Big cat")
                .xpath("//tr[1]//tr[4]//span").isEqualTo(1.0);

        webTestClient.post().uri("/buy")
                .exchange().expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/1?newOrder=true");
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr").nodeCount(2);

    }

        @Test
    @SneakyThrows
    void ordersPage_OkTest() {
            MultiValueMap<String, String> bodyPlus = new LinkedMultiValueMap<>();
            bodyPlus.add("action", "plus");

            webTestClient.post()
                    .uri("/items/1")
                    .bodyValue(bodyPlus)
                    .exchange().expectStatus().is3xxRedirection();
            webTestClient.post().uri("/buy")
                    .exchange().expectStatus().is3xxRedirection();

            webTestClient.post()
                    .uri("/items/1")
                    .bodyValue(bodyPlus)
                    .exchange().expectStatus().is3xxRedirection();
            webTestClient.post().uri("/buy")
                    .exchange().expectStatus().is3xxRedirection();

            webTestClient.get().uri("/orders")
                            .exchange().expectStatus().isOk()
                    .expectBody().xpath("//table/tr[1]/td").nodeCount(3);

    }

    @Test
    @SneakyThrows
    void orderPage_OkTest() {
        MultiValueMap<String, String> bodyPlus = new LinkedMultiValueMap<>();
        bodyPlus.add("action", "plus");

        webTestClient.post()
                .uri("/items/1")
                .bodyValue(bodyPlus)
                .exchange().expectStatus().is3xxRedirection();
        webTestClient.get()
                .uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//tr[1]//tr[2]/td[1]/b").isEqualTo("Big cat")
                .xpath("//tr[1]//tr[4]//span").isEqualTo(1.0);

        webTestClient.post().uri("/buy")
                .exchange().expectStatus().is3xxRedirection()
                .expectHeader().location("/orders/1?newOrder=true");
        webTestClient.get().uri("/orders/1")
                .exchange()
                .expectStatus().isOk()
                .expectBody().xpath("//table//table//tr[2]/td[1]/b").isEqualTo("Big cat");
    }


}
