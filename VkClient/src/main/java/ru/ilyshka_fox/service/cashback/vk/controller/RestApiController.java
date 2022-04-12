package ru.ilyshka_fox.service.cashback.vk.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.ilyshka_fox.service.cashback.vk.services.CheckbackWebServices;

@RestController
@RequestMapping("/api/scan")
@RequiredArgsConstructor
public class RestApiController {
    @Value("${ru.ilyshka_fox.controller.api-key}")
    private final String apiKey;
    private final CheckbackWebServices services;

    @GetMapping(produces = "application/json")
    public Mono<String> getReceipts(int page, int size, String key) {
        if (!apiKey.equals(key)) {
            throw new RuntimeException("Неверный ключ api!");
        }
        return services.getScan(page, size);
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public Mono<String> getReceipt(@PathVariable("id") long id, String key) {
        if (!apiKey.equals(key)) {
            throw new RuntimeException("Неверный ключ api!");
        }
        return services.getScan(id);
    }


    // case  1: "Изображение некорректно, на фото не чек"
    // case  2: "Ваш чек отклонён, так как данный чек уже присылался ранее"
    // case  3: "Ваш чек не принят. Чекбэк принимает только чеки, полученные за последние 30 дней"
    // case  4: "Введены некорректные данные чека"
    // case  5: "Чек не принят. Запрещено загружать более двух чеков из одного магазина в день"
    // case  6: "Ваш аккаунт был заблокирован. К сожалению, мы больше не сможем принимать Ваши чеки"
    // case  7: "Ваш чек отклонен, так как он недействителен"
    // case  8: "Чек не принят. Запрещено загружать более десяти чеков в день"
    // case  9: "Чек не принят. Кешбэк можно получить только по чекам о покупке (тип операции — приход)."
    // case 10: "Чек не отправлен. Запрещено загружать более двух чеков из одного магазина в день. Попробуйте позже"
    // case 11: "Чек не отправлен. Запрещено загружать более десяти чеков в день. Попробуйте позже."
    // default: "Не удалось отсканировать чек. Попробуйте еще раз."
    @PostMapping(produces = "application/json")
    public Mono<String> getReceipt(
            String qrString
            , String key) {
        if (!apiKey.equals(key)) {
            throw new RuntimeException("Неверный ключ api!");
        }
        return services.postScan(qrString);
    }
}
