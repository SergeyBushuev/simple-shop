package ru.yandex.payment_service.utils;

import ru.yandex.payment_service.model.PaymentResponse;

import java.math.BigDecimal;

public class ResponseTemplates {

    static public PaymentResponse success(String userId, BigDecimal amount, BigDecimal remainingBalance) {
        return new PaymentResponse(
                userId,
                amount,
                remainingBalance,
                PaymentResponse.StatusEnum.SUCCESS,
                "Payment successful"
        );
    }

    static public PaymentResponse insufficientFunds(String userId, BigDecimal amount, BigDecimal remainingBalance) {
        return new PaymentResponse(
                userId,
                amount,
                remainingBalance,
                PaymentResponse.StatusEnum.FAILED,
                "Insufficient funds: required " + amount
        );
    }

    static public PaymentResponse userNotFound(String userId, BigDecimal amount) {
        return new PaymentResponse(
                userId,
                amount,
                BigDecimal.ZERO,
                PaymentResponse.StatusEnum.FAILED,
                "User not found " + userId
        );
    }
}
