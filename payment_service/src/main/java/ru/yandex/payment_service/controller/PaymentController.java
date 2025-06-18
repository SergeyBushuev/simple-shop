package ru.yandex.payment_service.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.payment_service.api.PaymentApi;
import ru.yandex.payment_service.model.BalanceResponse;
import ru.yandex.payment_service.model.PaymentRequest;
import ru.yandex.payment_service.model.PaymentResponse;
import ru.yandex.payment_service.service.PaymentService;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final PaymentService paymentService;

    @Override
    public Mono<ResponseEntity<BalanceResponse>> getBalance(String userId, ServerWebExchange exchange) {
        return paymentService.getBalance(userId)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    @Override
    public Mono<ResponseEntity<PaymentResponse>> processPayment(Mono<PaymentRequest> paymentRequestMono, ServerWebExchange exchange) {
        return paymentRequestMono
                .flatMap(paymentService::processPayment)
                .map(this::constructResponse)
                .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    private ResponseEntity<PaymentResponse> constructResponse(PaymentResponse paymentResponse) {
        if (paymentResponse != null && paymentResponse.getStatus().equals(PaymentResponse.StatusEnum.SUCCESS)) {
            return ResponseEntity.ok(paymentResponse);
        }
        return ResponseEntity.badRequest().body(paymentResponse);
    }

}
