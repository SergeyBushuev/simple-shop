package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.api.PaymentApi;
import ru.yandex.simple_shop.model.BalanceResponse;
import ru.yandex.simple_shop.model.PaymentRequest;
import ru.yandex.simple_shop.model.PaymentResponse;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentApi paymentsApi;

    public Mono<Boolean> processOrderPayment(Double totalPrice) {
        final String userId = UUID.randomUUID().toString();
        return checkBalance(totalPrice, userId)
                .filter(balanceCheck -> balanceCheck)
                .flatMap(balanceCheck -> processPayment(totalPrice, userId));
    }

    private Mono<Boolean> checkBalance(Double totalPrice, String userId) {
        return paymentsApi.getBalance(userId)
                .map(balance ->
                        balance != null && balance.getBalance().compareTo(new BigDecimal(totalPrice)) >= 0);
    }

    private Mono<Boolean> processPayment(Double totalPrice, String userId) {
        final PaymentRequest paymentRequest = new PaymentRequest()
                .userId(userId)
                .amount(BigDecimal.valueOf(totalPrice));

        return paymentsApi.processPayment(paymentRequest)
                .map(this::isSuccessfulPayment);
    }

    private Boolean isSuccessfulPayment(PaymentResponse payment) {
        return payment != null && PaymentResponse.StatusEnum.SUCCESS.equals(payment.getStatus());
    }
}
