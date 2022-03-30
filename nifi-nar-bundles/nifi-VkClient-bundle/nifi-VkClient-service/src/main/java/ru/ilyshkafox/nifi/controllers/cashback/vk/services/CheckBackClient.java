package ru.ilyshkafox.nifi.controllers.cashback.vk.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.Headers;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.HeadersType;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.ScanResponse;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.OffsetDateTime;
import java.util.Iterator;


@RequiredArgsConstructor
public class CheckBackClient {
    private final static String CHECKBACK_INDEX_URL = "https://static.checkback.vkforms.ru/vkapps/index.html";

    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String xVkAuth;
    private final VkWebService vkWebService;
    @Getter
    private OffsetDateTime lastRequestTime = OffsetDateTime.now();

    public boolean hasCheckBackLogin(String xVkAuth) {
        try {
            boolean hasLogin = hasCheckBackLogin0(xVkAuth);
            lastRequestTime = OffsetDateTime.now();
            return hasLogin;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private boolean hasCheckBackLogin0(String xVkAuth) throws IOException, InterruptedException {
        Headers headers = vkWebService.getHeaders(HeadersType.CHECKBACK_INDEX);
        HttpRequest request = HttpRequest.newBuilder(URI.create(CHECKBACK_INDEX_URL + xVkAuth)).GET()
                .header("accept", headers.getAccept())
                .header("accept-language", headers.getAcceptLanguage())
                .header("referer", headers.getReferer())
                .header("sec-ch-ua", headers.getSecChUa())
                .header("sec-ch-ua-mobile", headers.getSecChUaMobile())
                .header("sec-fetch-dest", headers.getSecFetchDest())
                .header("sec-fetch-mode", headers.getSecFetchMode())
                .header("sec-fetch-site", headers.getSecFetchSite())
                .header("upgrade-insecure-requests", headers.getUpgradeInsecureRequests())
                .header("user-agent", headers.getUserAgent())
                .build();

        HttpResponse<String> htmlPage = vkWebService.send(request);
        return htmlPage.statusCode() == 200;
    }


    public Iterator<ScanResponse.DataItem> getScanDataItem() {
        return new Iterator<>() {
            private ScanResponse current = getScan(1);
            private Iterator<ScanResponse.DataItem> iterator = current.getResponse().getItems().getData().iterator();

            @Override
            public boolean hasNext() {
                if (iterator.hasNext()) {
                    return true;
                }
                ScanResponse.Items items = current.getResponse().getItems();
                return items.getCurrentPage() < items.getLastPage();
            }

            @Override
            public ScanResponse.DataItem next() {
                if (!iterator.hasNext()) {
                    ScanResponse.Items items = current.getResponse().getItems();
                    if (items.getCurrentPage() < items.getLastPage()) {
                        nextPage();
                    }
                }
                return iterator.next();
            }

            private void nextPage() {
                current = getScan(current.getResponse().getItems().getCurrentPage() + 1);
                iterator = current.getResponse().getItems().getData().iterator();
            }
        };
    }

    public ScanResponse getScan(int page) {
        if (lastRequestTime.plusSeconds(5).isAfter(OffsetDateTime.now())) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignore) {
            }
        }
        try {
            String scan = getScan0(xVkAuth, page, DEFAULT_PAGE_SIZE);
            lastRequestTime = OffsetDateTime.now();
            return objectMapper.readValue(scan, ScanResponse.class);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public String getScan0(String xVkAuth, int page, int limit) throws IOException, InterruptedException {
        Headers headers = vkWebService.getHeaders(HeadersType.CHECKBACK_REST_HEADER);
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://static.checkback.vkforms.ru/api/v1/scan?page=" + page + "&limit=" + limit + "&x2=0&notifications_allowed=true&mobile=1&android=0&category_id=12")).GET()
                .header("accept", headers.getAccept())
                .header("accept-language", headers.getAcceptLanguage())
                .header("Cache-Control", headers.getCacheControl())
                .header("referer", CHECKBACK_INDEX_URL + xVkAuth)
                .header("sec-ch-ua", headers.getSecChUa())
                .header("sec-ch-ua-mobile", headers.getSecChUaMobile())
                .header("Sec-Fetch-Dest", headers.getSecFetchDest())
                .header("Sec-Fetch-Mode", headers.getSecFetchMode())
                .header("Sec-Fetch-Site", headers.getSecFetchSite())
                .header("user-agent", headers.getUserAgent())
                .header("X-vk-sign", xVkAuth)
                .build();
        HttpResponse<String> htmlPage = vkWebService.send(request);
        return htmlPage.body();
    }
}
