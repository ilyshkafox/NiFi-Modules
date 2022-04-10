package ru.ilyshka_fox.service.cashback.vk.api;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import ru.ilyshka_fox.service.cashback.vk.ConfigurationBean;
import ru.ilyshka_fox.service.cashback.vk.VkClientApplication;
import ru.ilyshka_fox.service.cashback.vk.services.impl.CheckbackWebServicesImpl;
import ru.ilyshka_fox.service.cashback.vk.dto.ScanResponse;
import ru.ilyshka_fox.service.cashback.vk.services.impl.VkWebClientImpl;

import java.io.IOException;


@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {VkClientApplication.class})
@WebFluxTest
@WebAppConfiguration
class VkWebAdapterTest {


    @Test
    void name() throws InterruptedException, IOException {
//        var conf = new ConfigurationBean();
//
//        var captcha = conf.getTwoCaptcha("9b65c6f3637309baff75b0d5758d0b0b");
//        var webDriver = conf.getWebDriver();
//        var webClient = conf.getWebClient(16 * 1024 * 1024);
//        var vkWebAdapter = new VkWebClientImpl(captcha, webDriver, "ilya_moslov@mail.ru", "");
//        vkWebAdapter.init();
//        var checkbackWebAdapter = new CheckbackWebServicesImpl(vkWebAdapter, webClient);
//
//
//        ScanResponse recipes = checkbackWebAdapter.getRecipes(1, 10);
//        recipes.getResponse().getItems().getData()
//                .forEach(System.out::println);
//
//        vkWebAdapter.close();
//        webDriver.close();
    }
}