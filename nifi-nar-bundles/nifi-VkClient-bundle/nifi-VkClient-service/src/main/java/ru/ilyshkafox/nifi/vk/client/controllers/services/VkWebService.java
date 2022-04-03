package ru.ilyshkafox.nifi.vk.client.controllers.services;

import com.jayway.jsonpath.JsonPath;
import org.apache.nifi.logging.ComponentLog;
import org.jsoup.Jsoup;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.Headers;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.HeadersType;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.WebClient;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto.HttpResponse;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

public class VkWebService implements Closeable {
    private final static URI VK_URL = URI.create("https://m.vk.com");
    private final static URI VK_LOGIN_URL = URI.create("https://login.vk.com/");
    private final static URI VK_CHECKBACK_CATALOG_URL = URI.create(VK_URL + "/checkback?ref=catalog_recent");
    private final static String VK_PATCH_FEED = "/feed";
    private final WebClient webClient;
    private final Map<HeadersType, Headers> headersMap;
    private final ComponentLog log;


    public VkWebService(final ComponentLog logger, final Map<HeadersType, Headers> headers, final WebClient webClient) {
        this.log = logger;
        this.headersMap = headers;
        this.webClient = webClient;
    }

    public boolean checkLogin() {
        try {
            return checkLogin0();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


    private boolean checkLogin0() throws IOException, InterruptedException {
        var headers = headersMap.get(HeadersType.VK_LOGIN).toMap();
        HttpResponse startPage = webClient.get(VK_URL, headers);
        return startPage.getUri().getPath().equals(VK_PATCH_FEED);
    }

    public String getCheckBackXAuth() {
        try {
            Map<String, String> headers = headersMap.get(HeadersType.CHECKBACK_LOGIN).toMap();
            HttpResponse htmlPage = webClient.get(VK_CHECKBACK_CATALOG_URL, headers);
            String jsonString = Jsoup.parse(htmlPage.getBody()).body().getElementById("page_script").html().split("\n")[2].split("=", 2)[1].trim();
            jsonString = jsonString.substring(0, jsonString.length() - 1);
            String result = JsonPath.read(jsonString, "$.vk_app_url").toString().split("\\?", 2)[1].trim();
            return "?" + result;
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    @Override
    public void close() throws IOException {
        webClient.close();
    }
}

