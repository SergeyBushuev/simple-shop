package ru.yandex.payment_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.payment_service.model.PaymentRequest;

import java.math.BigDecimal;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void UnauthorizedGetBalance_ErrorTest() {
        String userId = "test";

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void getBalance_OkTest() {
        String userId = "test";

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.balance").exists();
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void processPayment_okTest() {
        String userId = "test";

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(new BigDecimal("100.00"));


        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.amount").isEqualTo(100.00)
                .jsonPath("$.remainingBalance").exists()
                .jsonPath("$.status").isEqualTo("SUCCESS")
                .jsonPath("$.message").isEqualTo("Payment successful");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void processPayment_insufficientFundTest() {
        String userId = "test";

        webTestClient.get()
                .uri("/payments/balance/{userId}", userId)
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk();

        PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(new BigDecimal("10000.00"));


        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.amount").isEqualTo(10000.00)
                .jsonPath("$.remainingBalance").exists()
                .jsonPath("$.status").isEqualTo("FAILED")
                .jsonPath("$.message").isEqualTo("Insufficient funds: required 10000.00");
    }

    @Test
    @WithMockUser(username = "testuser", authorities = "USER")
    void processPayment_unknownUserErrorTest() {
        String userId = "test";

        PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(new BigDecimal("100.00"));


        webTestClient.post()
                .uri("/payments/pay")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(paymentRequest)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.userId").isEqualTo(userId)
                .jsonPath("$.amount").isEqualTo(100.00)
                .jsonPath("$.remainingBalance").isEqualTo(BigDecimal.ZERO)
                .jsonPath("$.status").isEqualTo("FAILED")
                .jsonPath("$.message").isEqualTo("User not found test");
    }
}