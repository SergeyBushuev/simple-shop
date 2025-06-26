package ru.yandex.simple_shop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.simple_shop.model.User;
import ru.yandex.simple_shop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
