package ru.yandex.simple_shop.controller;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.PostgresContainerConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class ShopControllerIT extends PostgresContainerConfig {
    @Autowired
    private MockMvc mockMvc;

    @Test
    @SneakyThrows
    void redirectToShowcase_OkTest() {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));
    }

    @Test
    @SneakyThrows
    void showcase_OkTest() {
        mockMvc.perform(get("/main/items")
                        .param("search", "")
                        .param("sort", "ALPHA")
                        .param("pageNumber", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(view().name("main.html"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("sort"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    @SneakyThrows
    void itemShowcase_OkTest() {
        mockMvc.perform(get("/items/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("item.html"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    @SneakyThrows
    void emptyCart_OkTest() {
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart.html"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"))
                .andExpect(model().attribute("empty", true));
    }

    @Test
    @SneakyThrows
    void addToCart_OkTest() {
        mockMvc.perform(post("/main/items/1")
                        .param("action", "plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/main/items"));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart.html"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("total"))
                .andExpect(model().attributeExists("empty"))
                .andExpect(model().attribute("empty", false));
    }

    @Test
    @SneakyThrows
    void cartInteraction_OkTest() {
        mockMvc.perform(post("/items/1")
                .param("action", "plus"));
        mockMvc.perform(post("/cart/items/1")
                        .param("action", "plus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("empty", false))
                .andExpect(xpath("//table/tr[1]/td").nodeCount(2));

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "minus"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("empty", false))
                .andExpect(xpath("//table/tr[1]/td").nodeCount(2));

        mockMvc.perform(post("/cart/items/1")
                        .param("action", "delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cart/items"));
        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("empty", true));
    }

    @Test
    @SneakyThrows
    void buyItems_OkTest() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("empty", false));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("empty", true));
    }

    @Test
    @SneakyThrows
    void ordersPage_OkTest() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        mockMvc.perform(post("/main/items/2")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders.html"))
                .andExpect(model().attributeExists("orders"))
                .andExpect(xpath("//table/tr[1]/td").nodeCount(3));
    }

    @Test
    @SneakyThrows
    void orderPage_OkTest() {
        mockMvc.perform(post("/main/items/1")
                .param("action", "plus"));

        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/orders/*?newOrder=true"));

        mockMvc.perform(get("/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("order.html"))
                .andExpect(model().attributeExists("order"))
                .andExpect(model().attributeExists("newOrder"));
    }



}
