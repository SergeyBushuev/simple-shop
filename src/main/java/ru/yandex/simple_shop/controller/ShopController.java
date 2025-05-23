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
    public String redirectToMain() {
        return "redirect:/main/items";
    }

    @GetMapping("/main/items")
    public String getItems(@RequestParam(defaultValue = "") String search,
                           @RequestParam(defaultValue = "NO") SortType sort,
                           @RequestParam(defaultValue = "10") int pageSize,
                           @RequestParam(defaultValue = "1") int pageNumber,
                           Model model) {
        Page<ItemEntity> page = itemService.getShowcase(search, sort, pageNumber, pageSize);

        model.addAttribute("search", search);
        model.addAttribute("paging", new Paging(pageNumber, pageSize, page.hasNext(), page.hasPrevious()));
        model.addAttribute("items", Lists.partition(page.getContent(), 3));

        return "main.html";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable Long id, Model model) {
        ItemEntity item = itemService.findById(id);
        model.addAttribute("item", item);
        return "item.html";
    }

    @PostMapping("/main/items/{id}")
    public String addItemInCart(@PathVariable Long id,
                                @RequestParam ActionType action) {
        itemService.addItemInCart(id, action);
        return "redirect:/main/items";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyCartItem(@PathVariable Long id,
                                 @RequestParam ActionType action) {
        itemService.addItemInCart(id, action);
        return "redirect:/cart/items";
    }

    @GetMapping("/cart/items")
    public String getCartItems(Model model) {
        List<ItemEntity> itemsFromCart = itemService.getItemsFromCart();
        double total = itemsFromCart.stream()
                .map(item -> item.getPrice() * item.getCount())
                .reduce(0.0, Double::sum);
        model.addAttribute("total", total);
        model.addAttribute("items", itemsFromCart);
        model.addAttribute("empty", itemsFromCart.isEmpty());
        return "cart.html";
    }

    @PostMapping("/items/{id}")
    public String addCartItemFromItem(@PathVariable Long id,
                                      @RequestParam ActionType action) {
        itemService.addItemInCart(id, action);
        return "redirect:/items/" + id;
    }

    @PostMapping("/buy")
    public String buyItems() {
        OrderEntity orderEntity = orderService.createOrder();
        return "redirect:/orders/" + orderEntity.getId() + "?newOrder=true";
    }

    @GetMapping("/orders")
    public String getOrders(Model model) {
        List<OrderEntity> orders = orderService.getOrders();
        model.addAttribute("orders", orders);
        return "orders.html";
    }

    @GetMapping("/orders/{id}")
    public String getOrder(@PathVariable Long id,
                           @RequestParam(defaultValue = "false") boolean newOrder,
                           Model model) {
        OrderEntity orderEntity = orderService.findById(id);
        model.addAttribute("order", orderEntity);
        model.addAttribute("newOrder", newOrder);
        return "order.html";
    }
}
