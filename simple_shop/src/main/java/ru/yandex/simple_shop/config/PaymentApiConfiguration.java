package ru.yandex.simple_shop.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.simple_shop.ApiClient;
import ru.yandex.simple_shop.api.PaymentApi;;

@Configuration
@RequiredArgsConstructor
public class PaymentApiConfiguration {

    @Value("${simple_shop.payment_api.base_url}")
    private String baseUrl;

    @Bean
    public PaymentApi paymentApi(ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {
        if (System.getenv().containsKey("PAYMENT_APP_HOST")) {
            String host = System.getenv("PAYMENT_APP_HOST");
            baseUrl = "https://" + host +":8081";
        }
        ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
                new ServerOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager);
        oauth2Client.setDefaultClientRegistrationId("keycloak");

        WebClient webClient = WebClient.builder().baseUrl(baseUrl).filter(oauth2Client).build();
        ApiClient apiClient = new ApiClient(webClient);
        apiClient.setBasePath(baseUrl);
        return new PaymentApi(apiClient);
    }
}
