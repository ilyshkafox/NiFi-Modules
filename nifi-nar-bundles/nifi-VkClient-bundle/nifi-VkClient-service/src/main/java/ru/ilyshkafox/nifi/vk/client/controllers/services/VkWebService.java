package ru.ilyshkafox.nifi.vk.client.controllers.services;

import com.jayway.jsonpath.JsonPath;
import org.apache.nifi.logging.ComponentLog;
import org.jsoup.Jsoup;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.Headers;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.HeadersType;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class VkWebService {
    private final static String VK_URL = "https://m.vk.com";
    private final static String VK_LOGIN_URL = "https://login.vk.com/";
    private final static String VK_PATCH_FEED = "/feed";
    private final static String VK_CHECKBACK_CATALOG_URL = VK_URL + "/checkback?ref=catalog_recent";
    private final HttpClient httpClient;
    private final Map<HeadersType, Headers> headersMap;
    private final ComponentLog log;


    public VkWebService(final ComponentLog logger, final Map<HeadersType, Headers> headers, final CookieManager cookieManager) {
        this.log = logger;
        this.headersMap = headers;
        this.httpClient = HttpClient.newBuilder().cookieHandler(cookieManager).build();
    }

    public boolean checkLogin() {
        try {
            return checkLogin0();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }


    private boolean checkLogin0() throws IOException, InterruptedException {
        Headers headers = headersMap.get(HeadersType.VK_LOGIN);
        HttpRequest request = HttpRequest.newBuilder(URI.create(VK_URL)).GET()
                .header("accept", headers.getAccept())
                .header("accept-language", headers.getAcceptLanguage())
                .header("cache-control", headers.getCacheControl())
                .header("referer", headers.getReferer())
                .header("sec-ch-ua", headers.getSecChUa())
                .header("sec-ch-ua-mobile", headers.getSecChUaMobile())
                .header("sec-fetch-dest", headers.getSecFetchDest())
                .header("sec-fetch-mode", headers.getSecFetchMode())
                .header("sec-fetch-site", headers.getSecFetchSite())
                .header("upgrade-insecure-requests", headers.getUpgradeInsecureRequests())
                .header("user-agent", headers.getUserAgent())
                .build();

        HttpResponse<String> startPage = send(request);
        if (startPage.statusCode() == 200) {
            log.debug("При вроверки авторизации был получен код 200. Пользователь не авторизирован!");
            return false;
        }
        if (startPage.statusCode() == 302) {
            String location = startPage.headers().firstValue("location")
                    .orElseThrow(() -> new RuntimeException("При проверке авторизации, не получил ссылку переадресации при коде 302."));
            if (VK_PATCH_FEED.equals(location)) {
                log.debug("При проверке авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь авторизирован!");
                return true;
            } else {
                log.debug("При проверке авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
                return false;
            }
        } else {
            throw new RuntimeException("Получен неожидаемый статус код " + startPage.statusCode());
        }
    }

    public String getCheckBackXAuth() {
        try {
            return getCheckBackXAuth0();
        } catch (Exception exception) {
            throw new RuntimeException(exception);
        }
    }

    private String getCheckBackXAuth0() throws IOException, InterruptedException {
        Headers headers = headersMap.get(HeadersType.CHECKBACK_LOGIN);
        HttpRequest request = HttpRequest.newBuilder(URI.create(VK_CHECKBACK_CATALOG_URL)).GET()
                .header("accept", headers.getAccept())
                .header("accept-language", headers.getAcceptLanguage())
                .header("cache-control", headers.getCacheControl())
                .header("referer", headers.getReferer())
                .header("sec-ch-ua", headers.getSecChUa())
                .header("sec-ch-ua-mobile", headers.getSecChUaMobile())
                .header("sec-fetch-dest", headers.getSecFetchDest())
                .header("sec-fetch-mode", headers.getSecFetchMode())
                .header("sec-fetch-site", headers.getSecFetchSite())
                .header("upgrade-insecure-requests", headers.getUpgradeInsecureRequests())
                .header("user-agent", headers.getUserAgent())
                .build();

        HttpResponse<String> htmlPage = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8));
        String jsonString = Jsoup.parse(htmlPage.body()).body().getElementById("page_script").html().split("\n")[2].split("=", 2)[1].trim();
        jsonString = jsonString.substring(0, jsonString.length() - 1);

        String result = JsonPath.read(jsonString, "$.vk_app_url").toString().split("\\?", 2)[1].trim();
        return "?" + result;
    }


    public HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        HttpResponse<String> page = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8));

        if (page.statusCode() == 302) {
            String location = page.headers().firstValue("location").orElseThrow(() -> new RuntimeException("При проверке авторизации, не получил ссылку переадресации при коде 302."));
            if (location.startsWith(VK_LOGIN_URL)) {
                if (!restoreAuth(location)) {
                    throw new RuntimeException("Не получилось продлить сессию пользователя!");
                }
            }
            page = httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8));
        }
        return page;
    }


    private boolean restoreAuth(String loginUrl) throws IOException, InterruptedException {
        loginUrl = restoreAuth1(loginUrl);
        return restoreAuth2(loginUrl);
    }

    private String restoreAuth1(String loginUrl) throws IOException, InterruptedException {
        HttpResponse<String> page = sendRequestVkLoginHeader(loginUrl);
        if (page.statusCode() == 302) {
            String location = getLocation(page);
            if (!location.startsWith("https://m.vk.com/login")) {
                throw new RuntimeException("Шаг 1. При продлении авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
            }
            return location;
        } else {
            throw new RuntimeException("Шаг 1. При продлении авторизации был получен код " + page.statusCode() + ". Не понятный для программы статус. Пользователь не авторизирован!");
        }
    }

    private boolean restoreAuth2(String loginUrl) throws IOException, InterruptedException {
        HttpResponse<String> page = sendRequestVkLoginHeader(loginUrl);
        if (page.statusCode() == 302) {
            String location = getLocation(page);
            if (!(location.equals("\\") || location.equals("/"))) {
                throw new RuntimeException("Шаг 2. При продлении авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
            }
            log.debug("Продление авторизации произошло успешно. Пользователь авторизирован!");
            return true;
        } else {
            throw new RuntimeException("Шаг 2. При продлении авторизации был получен код " + page.statusCode() + ". Не понятный для программы статус. Пользователь не авторизирован!");
        }
    }

    private HttpResponse<String> sendRequestVkLoginHeader(String loginUrl) throws IOException, InterruptedException {
        Headers headers = headersMap.get(HeadersType.VK_LOGIN);
        HttpRequest request = HttpRequest.newBuilder(URI.create(loginUrl)).GET()
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

        return httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8));
    }

    private String getLocation(HttpResponse<String> response) {
        return response.headers().firstValue("location").orElseThrow(() -> new RuntimeException("При проверке авторизации, не получил ссылку переадресации."));
    }


    public Headers getHeaders(HeadersType headersType) {
        return headersMap.get(headersType);
    }
}

