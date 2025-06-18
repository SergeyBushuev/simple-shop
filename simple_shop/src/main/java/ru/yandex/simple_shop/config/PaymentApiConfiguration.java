package ru.yandex.simple_shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.simple_shop.ApiClient;
import ru.yandex.simple_shop.api.PaymentApi;;

@Configuration
@RequiredArgsConstructor
public class PaymentApiConfiguration {

    @Value("${simple_shop.payment_api.base_url}")
    private String baseUrl;

    @Bean
    public PaymentApi paymentApi() {
        if (System.getenv().containsKey("PAYMENT_APP_HOST")) {
            String host = System.getenv("PAYMENT_APP_HOST");
            baseUrl = "https://" + host +":8081";
        }
        WebClient webClient = WebClient.builder().baseUrl(baseUrl).build();
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(baseUrl);
        return new PaymentApi(apiClient);
    }
}
