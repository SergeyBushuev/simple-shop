package ru.yandex.simple_shop.dto;

public record Paging(int pageNumber,
                     int pageSize,
                     boolean hasNext,
                     boolean hasPrevious) {
}
