package ru.yandex.simple_shop.controller;

import com.google.common.collect.Lists;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.dto.Paging;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.SortType;
import ru.yandex.simple_shop.service.ItemService;

import java.util.List;


@Controller
@RequiredArgsConstructor
public class ShopController {

    private final ItemService itemService;

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

    @PostMapping("/items/{id}")
    public Mono<String> addCartItemFromItem(@PathVariable Long id,
                                            ServerWebExchange exchange) {
        return exchange.getFormData()
                .mapNotNull(data -> data.getFirst("action"))
                .map(ActionType::valueOf)
                .flatMap(actionType -> itemService.addItemInCart(id, actionType))
                .then(Mono.just("redirect:/items/" + id));
    }

}
