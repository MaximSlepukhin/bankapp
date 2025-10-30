package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;

public interface BlockerService {
    BlockerStatus checkBlock(BlockerRequest request);
}