package ru.ilyshka_fox.service.cashback.vk.services;

import reactor.core.publisher.Mono;

public interface CheckbackWebServices {

    Mono<String> getScan(int page, int limit);

    Mono<String> getScan(long id);

    Mono<String> postScan(String qrString);
}
