package com.github.maximslepukhin.clients;


import org.springframework.stereotype.Service;

@Service
public class NotificationsClient {

    public void notify(String accountId, String message) {
        // REST-запрос к Notifications Service
        System.out.println("Notify " + accountId + ": " + message);
    }
}
