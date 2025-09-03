package com.github.maximslepukhin.dto;

public enum Currency {
    RUB("Рубль"),
    USD("Доллар"),
    EUR("Евро");

    private final String title;

    Currency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}