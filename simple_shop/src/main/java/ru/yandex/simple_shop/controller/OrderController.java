package ru.yandex.simple_shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
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
import ru.yandex.simple_shop.service.UserService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final UserService userService;

    @PostMapping("/buy")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> buyItems(Principal principal) {
        String username = principal.getName();
        return userService.findByUsername(username).flatMap(user -> orderService.createOrder(user).flatMap(orderEntity ->
                paymentService.processOrderPayment(orderEntity.getTotalPrice())
                        .map(paymentSuccess -> {
                            if (paymentSuccess) {
                                return "redirect:/orders/" + orderEntity.getId() + "?newOrder=true";
                            } else {
                                return "redirect:/main/items";
                            }
                        })));

    }

    @GetMapping("/orders")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> getOrders(Model model, Principal principal) {
        String username = principal.getName();
        return userService.findByUsername(username).flatMap(user ->
                orderService.getOrders(user.getId()).collectList().map(orders -> {
            model.addAttribute("orders", orders);
            return "orders.html";
        }));
    }

    @GetMapping("/orders/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
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
