package com.github.maximslepukhin.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Статус блокировки пользователя")
public class BlockerStatus {

    @Schema(description = "Признак блокировки", example = "false")
    private boolean blocked;

    @Schema(description = "Причина блокировки (если заблокирован)", example = "Подозрительная активность")
    private String reason;
}
