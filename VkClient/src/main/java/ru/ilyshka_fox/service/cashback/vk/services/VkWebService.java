package ru.ilyshka_fox.service.cashback.vk.services;

import reactor.core.publisher.Mono;

import java.util.function.Function;


public interface VkWebService {
    <T> Mono<T> blockResource(Function<VkWebClient, T> result);
}
