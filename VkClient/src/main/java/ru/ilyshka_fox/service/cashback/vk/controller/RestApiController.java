package ru.ilyshka_fox.service.cashback.vk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.ilyshka_fox.service.cashback.vk.api.dto.ScanResponseItem;
import ru.ilyshka_fox.service.cashback.vk.mapstract.ScanResponseMap;
import ru.ilyshka_fox.service.cashback.vk.services.CheckbackWebServices;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class RestApiController {
    @Value("${ru.ilyshka_fox.controller.api-key}")
    private final String apiKey;
    private final CheckbackWebServices services;
    private final ScanResponseMap mapper;

    @GetMapping
    public Flux<ScanResponseItem> getReceipts(int page, int size, String key) {
        if (!apiKey.equals(key)) {
            throw new RuntimeException("Неверный ключ api!");
        }
        return services.getReceipts(page, size)
                .map(mapper::map)
                .flatMapIterable(x -> x);

    }

    @GetMapping("/{id}")
    public Mono<ScanResponseItem> getReceipt(@PathVariable("id") long id, String key) {
        if (!apiKey.equals(key)) {
            throw new RuntimeException("Неверный ключ api!");
        }
        return services.getReceipt(id)
                .map(mapper::map);

    }
}
