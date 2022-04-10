package ru.ilyshka_fox.service.cashback.vk.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import reactor.core.publisher.Mono;

import java.util.function.Function;


public interface VkWebClient {
    boolean isLogin();

    void open(String url);

    <T> T open(String url, Function<VkWebClient, T> result);

    WebElement findElement(By by);
}
