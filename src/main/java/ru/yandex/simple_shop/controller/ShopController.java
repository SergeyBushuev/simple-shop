package ru.yandex.simple_shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.simple_shop.model.ActionType;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.model.SortType;
import ru.yandex.simple_shop.service.ItemService;


@RestController
@RequiredArgsConstructor
public class ShopController {

    private final ItemService itemService;

    @GetMapping("/")
    public String redirectToMain() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           Model model) {
        ItemEntity item = itemService.getById(1L); //test

        return "main.html";
    }

    @PostMapping("/main/items/{id}")
    public String modifyItemInCart(@PathVariable Long id,
                                   @RequestParam ActionType action) {

        return "redirect:/main/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        return "cart.html";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyCartItem(@PathVariable Long id,
                                 @RequestParam ActionType action) {
        return "redirect:/cart/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        return "item.html";
    }

    @PostMapping("/items/{id}")
    public String modifyItemFromCard(@PathVariable Long id,
                                     @RequestParam ActionType action) {
        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buyItems() {
        return "redirect:/orders/{id}?newOrder=true";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        return "orders.html";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable Long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        return "order.html";
    }
}
