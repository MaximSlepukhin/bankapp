package com.github.maximslepukhin.controller;

import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import com.github.maximslepukhin.service.BlockerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/blocker")
public class BlockerController {

    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    @PostMapping("/check")
    public ResponseEntity<BlockerStatus> check(@Valid @RequestBody BlockerRequest request) {
        BlockerStatus status = blockerService.checkBlock(request);
        HttpStatus httpStatus = status.isBlocked() ? HttpStatus.FORBIDDEN : HttpStatus.OK;
        return ResponseEntity.status(httpStatus).body(status);
    }
}
