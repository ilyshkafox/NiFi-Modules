package ru.ilyshkafox.nifi.vk.client.controllers.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.ilyshkafox.nifi.vk.client.controllers.CheckBackClient;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.*;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.WebClient;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto.ContentType;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto.HttpResponse;

import java.io.IOException;
import java.net.URI;
import java.time.OffsetDateTime;
import java.util.Iterator;
import java.util.Map;


@RequiredArgsConstructor
public class CheckBackClientImpl implements CheckBackClient {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final static String CHECKBACK_INDEX_URL = "https://static.checkback.vkforms.ru/vkapps/index.html";
    private static final int DEFAULT_PAGE_SIZE = 100;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final String xVkAuth;
    private final Map<HeadersType, Headers> headersMap;
    private final WebClient webClient;
    @Getter
    private OffsetDateTime lastRequestTime = OffsetDateTime.now();

    public boolean hasCheckBackLogin(String xVkAuth) {
        try {
            Map<String, String> header = headersMap.get(HeadersType.CHECKBACK_INDEX).toMap();
            HttpResponse htmlPage = webClient.get(URI.create(CHECKBACK_INDEX_URL + xVkAuth), header);
            lastRequestTime = OffsetDateTime.now();
            return htmlPage.getStatusCode() == 200;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
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
        requestDelay();
        try {
            Map<String, String> header = headersMap.get(HeadersType.CHECKBACK_REST_HEADER).toMap();
            header.put("X-vk-sign", xVkAuth);
            HttpResponse htmlPage = webClient.get(URI.create("https://static.checkback.vkforms.ru/api/v1/scan?page=" + page + "&limit=" + DEFAULT_PAGE_SIZE + "&x2=0&notifications_allowed=true&mobile=1&android=0&category_id=12"), header);
            lastRequestTime = OffsetDateTime.now();
            return objectMapper.readValue(htmlPage.getBody(), ScanResponse.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public PostScanResponse postScan(final String qrString) {
        requestDelay();
        try {
            Map<String, String> header = headersMap.get(HeadersType.CHECKBACK_REST_HEADER).toMap();
            header.put("referer", CHECKBACK_INDEX_URL + xVkAuth);
            header.put("X-vk-sign", xVkAuth);
            HttpResponse htmlPage = webClient.post(URI.create("https://static.checkback.vkforms.ru/api/v1/scan"), header,
                    OBJECT_MAPPER.writeValueAsString(PostScanRequest.of(qrString)), ContentType.APPLICATION_JSON);

            lastRequestTime = OffsetDateTime.now();
            PostScanResponse response = htmlPage.getBody(PostScanResponse.class);
            response.setResponseString(htmlPage.getBody());
            return response;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private void requestDelay() {
        if (lastRequestTime.plusSeconds(5).isAfter(OffsetDateTime.now())) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ignore) {
            }
        }
    }
}
