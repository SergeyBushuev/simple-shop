package ru.yandex.payment_service.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.model.BalanceResponse;
import ru.yandex.payment_service.model.PaymentRequest;
import ru.yandex.payment_service.model.PaymentResponse;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {
    private final ConcurrentHashMap<String, BigDecimal> userBalances = new ConcurrentHashMap<>();

    public PaymentService() {
        userBalances.put("test", new BigDecimal("1500.00"));
    }

    public Mono<BalanceResponse> getBalance(String userId) {
        return Mono.fromCallable(() -> {
            BigDecimal balance = userBalances.computeIfAbsent(userId, this::getRandomUserBalance);
            return new BalanceResponse(userId, balance);
        });
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return Mono.fromCallable(() -> {
            BigDecimal currentBalance = userBalances.get(request.getUserId());

            if (currentBalance == null) {
                return new PaymentResponse(
                        request.getUserId(),
                        request.getAmount(),
                        BigDecimal.ZERO,
                        PaymentResponse.StatusEnum.FAILED,
                        "User not found " + request.getUserId()
                );
            }

            if (currentBalance.compareTo(request.getAmount()) < 0) {
                return new PaymentResponse(
                        request.getUserId(),
                        request.getAmount(),
                        currentBalance,
                        PaymentResponse.StatusEnum.FAILED,
                        "Insufficient funds " + request.getAmount()
                );
            }

            BigDecimal newBalance = currentBalance.subtract(request.getAmount());
            userBalances.put(request.getUserId(), newBalance);

            return new PaymentResponse(
                    request.getUserId(),
                    request.getAmount(),
                    newBalance,
                    PaymentResponse.StatusEnum.SUCCESS,
                    "Платеж успешно обработан"
            );
        });
    }

    private BigDecimal getRandomUserBalance(String userId) {
        double rndDouble = ThreadLocalRandom.current().nextDouble(0, 100000);
        return new BigDecimal(rndDouble).setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }
}
