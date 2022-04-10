package ru.ilyshkafox.nifi.vk.client.controllers.services;

import com.jayway.jsonpath.JsonPath;
import lombok.AllArgsConstructor;
import org.apache.nifi.logging.ComponentLog;
import org.jsoup.Jsoup;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.vkclient.VkClient;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.Headers;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.HeadersType;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

@AllArgsConstructor
public class VkWebService implements Closeable {
    private final static String VK_CHECKBACK_CATALOG_URL = "/checkback?ref=catalog_recent";

    private final VkClient vkClient;
    private final Map<HeadersType, Headers> headersMap;
    private final ComponentLog log;


    public boolean checkLogin() {
        return vkClient.isLogin();
    }


    public String getCheckBackXAuth() {
        log.info("Получение CheckBackXAuth токена.");
        HttpResponse htmlPage = vkClient.send(VK_CHECKBACK_CATALOG_URL, headersMap.get(HeadersType.CHECKBACK_LOGIN).toMap());
        String jsonString = Jsoup.parse(htmlPage.getBody()).body().getElementById("page_script").html().split("\n")[2].split("=", 2)[1].trim();
        jsonString = jsonString.substring(0, jsonString.length() - 1);
        String result = JsonPath.read(jsonString, "$.vk_app_url").toString().split("\\?", 2)[1].trim();
        return "?" + result;
    }

    @Override
    public void close() throws IOException {
        vkClient.close();
    }
}

