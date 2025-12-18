package com.github.maximslepukhin.model.enums;

public enum Currency {
    RUB("Рубль"),
    USD("Доллар США"),
    CNY("Юань");

    private final String title;

    Currency(String title) {
        this.title = title;
    }

}

