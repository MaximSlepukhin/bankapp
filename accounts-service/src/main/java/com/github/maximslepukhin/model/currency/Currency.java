package com.github.maximslepukhin.model.currency;

public enum Currency {
    RUB("Рубль"),
    USD("Доллар"),
    CNY("Юань");

    private final String title;

    Currency(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}