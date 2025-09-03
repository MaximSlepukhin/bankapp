//package com.github.maximslepukhin.controller;
//
//import org.springframework.web.bind.annotation.GetMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.math.BigDecimal;
//
//import static jdk.internal.org.jline.reader.impl.LineReaderImpl.CompletionType.List;
//
//@RestController
//@RequestMapping("/api")
//public class RateController {
//
//    @GetMapping("/rates")
//    public List<RateDto> getRates() {
//        return List.of(
//                new RateDto("Доллар", "USD", new BigDecimal("95.50")),
//                new RateDto("Рубль", "RUB", BigDecimal.ONE),
//                new RateDto("Евро", "EUR", new BigDecimal("102.30"))
//        );
//    }
//}