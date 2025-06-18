package ru.yandex.simple_shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.service.ItemService;
import ru.yandex.simple_shop.service.OrderService;
import ru.yandex.simple_shop.service.PaymentService;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final ItemService itemService;

    @PostMapping("/items/{id}")
    public Mono<String> modifyCartItem(@PathVariable Long id,
                                       ServerWebExchange exchange) {
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType))
                .then(Mono.just("redirect:/cart/items"));
    }

    @GetMapping("/items")
    public Mono<String> getCartItems(Model model) {
        return itemService.getItemsFromCart()
                .map(itemList -> {
                    double total = itemList.stream()
                            .map(item -> item.getPrice() * item.getCount())
                            .reduce(0.0, Double::sum);
                    model.addAttribute("total", total);
                    model.addAttribute("items", itemList);
                    model.addAttribute("empty", itemList.isEmpty());
                    return "cart.html";
                });

    }
}
