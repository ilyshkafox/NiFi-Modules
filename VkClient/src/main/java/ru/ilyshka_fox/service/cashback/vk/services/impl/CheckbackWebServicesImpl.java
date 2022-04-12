package ru.ilyshka_fox.service.cashback.vk.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.hc.core5.net.URIBuilder;
import org.openqa.selenium.By;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.ilyshka_fox.service.cashback.vk.services.CheckbackWebServices;
import ru.ilyshka_fox.service.cashback.vk.services.VkWebService;

import java.net.URI;

@Service
@RequiredArgsConstructor
public class CheckbackWebServicesImpl implements CheckbackWebServices {
    public static final String VK_CHECKBACK_PAGE = "https://m.vk.com/checkback?ref=catalog_recent#";
    public static final String SCAN_PAGE = "https://static.checkback.vkforms.ru/api/v1/scan";

    private final VkWebService vkService;
    private final WebClient webClient;

    public Mono<String> getScan(int page, int limit) {
        return getXVkSign()
                .flatMap(XVkSign -> {
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
                                .bodyToMono(String.class);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });


    }

    @Override
    public Mono<String> getScan(long id) {
        return getXVkSign()
                .flatMap(XVkSign -> {
                    try {
                        return webClient.get()
                                .uri(SCAN_PAGE + "/{id}", id)
                                .accept(MediaType.APPLICATION_JSON)
                                .header("X-vk-sign", XVkSign)
                                .retrieve()
                                .bodyToMono(String.class);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }

//    {"response":{"validation":2}} - чек уже есть в системе.
    @Override
    public Mono<String> postScan(final String qrString) {

        return getXVkSign()
                .flatMap(XVkSign -> {
                    try {
                        return webClient.post()
                                .uri(SCAN_PAGE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header("X-vk-sign", XVkSign)
                                .bodyValue("{\"action_id\":-1,\"first_name\":null,\"qr_string\":\"" + qrString + "\",\"source\":\"handed\"}")
                                .retrieve()
                                .bodyToMono(String.class);
                    } catch (Exception e) {
                        return Mono.error(e);
                    }
                });
    }


    private Mono<String> getXVkSign() {
        return vkService.blockResource(vk -> {
            vk.open(VK_CHECKBACK_PAGE);
            return "?" + vk.findElement(By.tagName("iframe")).getAttribute("src").split("\\?", 2)[1];
        });
    }
}
