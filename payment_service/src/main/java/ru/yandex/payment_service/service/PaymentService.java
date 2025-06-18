package ru.yandex.payment_service.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.model.BalanceResponse;
import ru.yandex.payment_service.model.PaymentRequest;
import ru.yandex.payment_service.model.PaymentResponse;
import ru.yandex.payment_service.utils.ResponseTemplates;

import java.math.BigDecimal;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class PaymentService {
    private final ConcurrentHashMap<String, BigDecimal> userBalances = new ConcurrentHashMap<>();

    public Mono<BalanceResponse> getBalance(String userId) {
        return Mono.fromCallable(() -> {
            BigDecimal balance = userBalances.computeIfAbsent(userId, this::getRandomUserBalance);
//            userBalances.put(userId, balance);
            return new BalanceResponse(userId, balance);
        });
    }

    public Mono<PaymentResponse> processPayment(PaymentRequest request) {
        return Mono.fromCallable(() -> {

            if (!userBalances.containsKey(request.getUserId())) {
                return ResponseTemplates.userNotFound(request.getUserId(), request.getAmount());
            }

            BigDecimal currentBalance = userBalances.get(request.getUserId());

            if (currentBalance.compareTo(request.getAmount()) < 0) {
                return ResponseTemplates.insufficientFunds(request.getUserId(), request.getAmount(), currentBalance);
            }

            BigDecimal newBalance = currentBalance.subtract(request.getAmount());
            userBalances.put(request.getUserId(), newBalance);

            return ResponseTemplates.success(request.getUserId(), request.getAmount(), newBalance);
        });
    }

    private BigDecimal getRandomUserBalance(String userId) {
        double rndDouble = ThreadLocalRandom.current().nextDouble(100, 1000);
        return new BigDecimal(rndDouble).setScale(2, BigDecimal.ROUND_HALF_DOWN);
    }
}
