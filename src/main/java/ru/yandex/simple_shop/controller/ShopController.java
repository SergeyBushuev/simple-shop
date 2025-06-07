package ru.yandex.simple_shop.controller;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.dto.Paging;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.OrderEntity;
import ru.yandex.simple_shop.model.SortType;
import ru.yandex.simple_shop.service.ItemService;
import ru.yandex.simple_shop.service.OrderService;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ItemService itemService;
    private final OrderService orderService;

    @GetMapping("/")
    public Mono<String> redirectToMain() {
        return Mono.just("redirect:/main/items");
    }

    @GetMapping("/main/items")
    public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           Model model) {

        return itemService.getShowcase(search, sort, pageNumber, pageSize)
                .map(tuple2 -> {
                    List<ItemEntity> items = tuple2.getT1();
                    long total = tuple2.getT2();
                    boolean hasNext = (long) pageNumber * pageSize < total;
                    boolean hasPrevious = pageNumber > 1;

                    model.addAttribute("search", search);
                    model.addAttribute("paging", new Paging(pageNumber, pageSize, hasNext, hasPrevious));
                    model.addAttribute("items", Lists.partition(items, 3));
                    model.addAttribute("sort", sort);
                    return "main.html";
                });

    }

    @GetMapping("/items/{id}")
    public Mono<String> getItem(@PathVariable Long id, Model model) {
        return itemService.findById(id)
                .doOnNext(item -> model.addAttribute("item", item))
                .then(Mono.just("item.html"));
    }

    @PostMapping("/main/items/{id}")
    public Mono<String> addItemInCart(@PathVariable Long id,
                                ServerWebExchange exchange) {
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType))
                .then(Mono.just("redirect:/main/items"));
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> modifyCartItem(@PathVariable Long id,
                                       ServerWebExchange exchange) {
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType))
                .then(Mono.just("redirect:/cart/items"));
//        return itemService.addItemInCart(id, action).map(item -> "redirect:/cart/items");
    }

    @GetMapping("/cart/items")
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

    @PostMapping("/items/{id}")
    public Mono<String> addCartItemFromItem(@PathVariable Long id,
                                            ServerWebExchange exchange) {
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType))
                .then(Mono.just("redirect:/items/" + id));
    }

    @PostMapping("/buy")
    public Mono<String> buyItems() {
        return orderService.createOrder()
                .map(orderEntity -> "redirect:/orders/" + orderEntity.getId() + "?newOrder=true");
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
