package ru.ilyshka_fox.service.cashback.vk.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.net.URIBuilder;
import org.openqa.selenium.By;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import ru.ilyshka_fox.service.cashback.vk.dto.ScanResponse;
import ru.ilyshka_fox.service.cashback.vk.services.CheckbackWebServices;
import ru.ilyshka_fox.service.cashback.vk.services.VkWebService;

import java.net.URI;
import java.net.URISyntaxException;

@Service
@RequiredArgsConstructor
public class CheckbackWebServicesImpl implements CheckbackWebServices {
    public static final String VK_CHECKBACK_PAGE = "https://m.vk.com/checkback?ref=catalog_recent#";
    public static final String SCAN_PAGE = "https://static.checkback.vkforms.ru/api/v1/scan";

    private final VkWebService vk;
    private final WebClient webClient;

    public ScanResponse getRecipes(int page, int limit) {
        vk.open(VK_CHECKBACK_PAGE);
        String XVkSign = "?" + vk.findElement(By.tagName("iframe")).getAttribute("src").split("\\?", 2)[1];
        try {
            URI postUrl = new URIBuilder(SCAN_PAGE)
                    .addParameter("page", String.valueOf(page))
                    .addParameter("limit", String.valueOf(limit))
                    .build();


            return webClient.get()
                    .uri(postUrl)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("X-vk-sign", XVkSign)
                    .retrieve()
                    .bodyToMono(ScanResponse.class)
                    .block();


        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }
}
