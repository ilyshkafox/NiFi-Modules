package ru.ilyshka_fox.service.cashback.vk.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import ru.ilyshka_fox.service.cashback.vk.services.VkWebClient;
import ru.ilyshka_fox.service.cashback.vk.services.VkWebService;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
public class VkWebServiceImpl implements VkWebService {
    private static final ReentrantLock lock = new ReentrantLock();
    private static final Scheduler scheduler = Schedulers.newSingle("VkWeb");

    private final VkWebClient vkWebClient;

    @Override
    public <T> Mono<T> blockResource(Function<VkWebClient, T> function) {
        return Mono.just(vkWebClient)
                .publishOn(scheduler)
                .map(vkWebClient -> {
                    lock.lock();
                    try {
                        return function.apply(vkWebClient);
                    } finally {
                        lock.unlock();
                    }
                });
    }
}
