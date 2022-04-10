package ru.ilyshka_fox.service.cashback.vk.services;

import reactor.core.publisher.Mono;
import ru.ilyshka_fox.service.cashback.vk.dto.ScanResponse;

public interface CheckbackWebServices {

    Mono<ScanResponse> getRecipes(int page, int limit);

}
