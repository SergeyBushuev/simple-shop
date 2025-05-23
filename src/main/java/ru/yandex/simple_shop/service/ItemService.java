package ru.yandex.simple_shop.service;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.simple_shop.model.ItemEntity;
import ru.yandex.simple_shop.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public ItemEntity getById(Long id) {
        return itemRepository.findById(id).orElseThrow(EntityNotFoundException::new);
    }


}