package com.github.maximslepukhin.service;


import com.github.maximslepukhin.model.dto.TransferRequest;
import com.github.maximslepukhin.model.dto.TransferResponse;
;

public interface TransferService {
    TransferResponse transfer(TransferRequest request);
}