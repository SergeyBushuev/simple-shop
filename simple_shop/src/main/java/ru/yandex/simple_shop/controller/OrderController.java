package ru.yandex.simple_shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.service.ItemService;
import ru.yandex.simple_shop.service.OrderService;
import ru.yandex.simple_shop.service.PaymentService;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;

    @PostMapping("/buy")
    public Mono<String> buyItems() {

        return orderService.createOrder().flatMap(orderEntity ->
                paymentService.processOrderPayment(orderEntity.getTotalPrice())
                .map(paymentSuccess -> {
                    if (paymentSuccess) {
                        return "redirect:/orders/" + orderEntity.getId() + "?newOrder=true";
                    } else {
                        return "redirect:/main/items";
                    }
                }));

    }

    @GetMapping("/orders")
    public Mono<String> getOrders(Model model) {
        return orderService.getOrders().collectList().map(orders -> {
            model.addAttribute("orders", orders);
            return "orders.html";
        });
    }

    @GetMapping("/orders/{id}")
    public Mono<String> getOrder(@PathVariable Long id,
                                 @RequestParam(defaultValue = "false") boolean newOrder,
                                 Model model) {
        return orderService.findById(id).map(orderEntity -> {
            model.addAttribute("order", orderEntity);
            model.addAttribute("newOrder", newOrder);
            return "order.html";
        });
    }
}
