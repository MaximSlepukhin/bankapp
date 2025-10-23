package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocker")
public class BlockerController {

    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    @PostMapping("/check")
    public BlockerStatus check(@RequestBody BlockerRequest request) {
        return blockerService.checkBlock(request);
    }
}
