package com.github.maximslepukhin.config.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Configuration
public class FeignErrorDecoderConfig {

    @Bean
    public ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new ErrorDecoder() {
            @Override
            public Exception decode(String methodKey, Response response) {
                String body = null;
                try {
                    if (response.body() != null) {
                        body = new String(response.body().asInputStream().readAllBytes(), StandardCharsets.UTF_8);
                    }
                } catch (IOException ignored) {}

                // ✅ конвертируем headers в HttpHeaders
                HttpHeaders headers = new HttpHeaders();
                for (Map.Entry<String, Collection<String>> entry : response.headers().entrySet()) {
                    headers.put(entry.getKey(), List.copyOf(entry.getValue()));
                }

                return new HttpClientErrorException(
                        HttpStatus.resolve(response.status()) != null
                                ? HttpStatus.valueOf(response.status())
                                : HttpStatus.INTERNAL_SERVER_ERROR,
                        response.reason(),
                        headers,
                        body != null ? body.getBytes(StandardCharsets.UTF_8) : null,
                        StandardCharsets.UTF_8
                );
            }
        };
    }
}
