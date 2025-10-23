package com.github.maximslepukhin.service;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class BlockerServiceImpl implements BlockerService {

    @Override
    public BlockerStatus checkBlock(BlockerRequest request) {
        LocalTime now = LocalTime.now();
        if (now.isAfter(LocalTime.of(0, 0)) && now.isBefore(LocalTime.of(0, 5))) {
            return new BlockerStatus(true, "Operations are blocked for maintenance (00:00–00:05)");
        }

        return new BlockerStatus(false, "Operation allowed");
    }
}
