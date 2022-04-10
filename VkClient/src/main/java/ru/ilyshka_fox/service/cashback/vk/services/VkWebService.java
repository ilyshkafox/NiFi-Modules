package ru.ilyshka_fox.service.cashback.vk.services;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import java.io.Closeable;


public interface VkWebService {
    boolean isLogin();

    void open(String url);

    WebElement findElement(By by);
}
