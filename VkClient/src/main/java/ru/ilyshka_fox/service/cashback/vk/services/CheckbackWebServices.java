package ru.ilyshka_fox.service.cashback.vk.services;

import ru.ilyshka_fox.service.cashback.vk.dto.ScanResponse;

public interface CheckbackWebServices {

    ScanResponse getRecipes(int page, int limit);

}
