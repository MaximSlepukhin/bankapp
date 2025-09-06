package com.github.maximslepukhin.clients;

import org.springframework.stereotype.Service;

@Service
public class BlockerClient {

    public boolean isBlocked(String accountId) {
        // REST-запрос к Blocker Service
        return false; // пример
    }
}
