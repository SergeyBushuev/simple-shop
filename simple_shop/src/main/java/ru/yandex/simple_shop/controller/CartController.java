package ru.yandex.simple_shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.service.ItemService;
import ru.yandex.simple_shop.service.OrderService;
import ru.yandex.simple_shop.service.PaymentService;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final ItemService itemService;

    @PostMapping("/items/{id}")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> modifyCartItem(@PathVariable Long id,
                                       ServerWebExchange exchange,
                                       Principal principal) {
        String username = principal.getName();
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType, username))
                .then(Mono.just("redirect:/cart/items"));
    }

    @GetMapping("/items")
    @PreAuthorize("isAuthenticated() and hasAuthority('USER')")
    public Mono<String> getCartItems(Model model, Principal principal) {
        String username = principal.getName();
        return itemService.getItemsFromCart(username)
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
