package com.github.maximslepukhin.service;

import com.github.maximslepukhin.config.MaintenanceProperties;
import com.github.maximslepukhin.model.dto.BlockerRequest;
import com.github.maximslepukhin.model.dto.BlockerStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalTime;

@Service
public class BlockerServiceImpl implements BlockerService {

    private static final Logger log = LoggerFactory.getLogger(BlockerServiceImpl.class);
    private final MaintenanceProperties props;

    public BlockerServiceImpl(MaintenanceProperties props) {
        this.props = props;
    }

    @Override
    public BlockerStatus checkBlock(BlockerRequest request) {
        log.info("Проверка блокировки для аккаунта={}", request.getLogin());
        LocalTime now = LocalTime.now();
        if (now.isAfter(props.getStart()) && now.isBefore(props.getEnd())) {
            return new BlockerStatus(true,
                    String.format("Операции временно недоступны: техобслуживание (%s–%s)", props.getStart(), props.getEnd()));
        }
        return new BlockerStatus(false, "Операция разрешена");
    }
}
