package com.github.maximslepukhin.model.record;

import java.math.BigDecimal;

public record BlockerRequest(String login, String currency, BigDecimal amount) {
}
