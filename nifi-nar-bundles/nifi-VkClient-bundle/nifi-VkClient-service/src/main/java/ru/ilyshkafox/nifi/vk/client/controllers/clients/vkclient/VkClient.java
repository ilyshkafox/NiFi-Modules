package ru.ilyshkafox.nifi.vk.client.controllers.clients.vkclient;

import lombok.AllArgsConstructor;
import org.apache.hc.core5.net.URIBuilder;
import org.apache.nifi.logging.ComponentLog;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.WebClient;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.Headers;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.HeadersType;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.Assert;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@AllArgsConstructor
public class VkClient implements Closeable {
    private final static URI VK_URL = URI.create("https://m.vk.com/");
    private final static URI VK_LOGIN_URL = URI.create("https://login.vk.com/");
    private final static String VK_PATCH_FEED = "/feed";

    private WebClient webClient;
    private final Map<HeadersType, Headers> headersMap;
    private ComponentLog log;


    public boolean isLogin() {
        var httpResponse = send("/", headersMap.get(HeadersType.VK_LOGIN).toMap());
        var statusCode = httpResponse.getStatusCode();

        if (statusCode == 200) {
            log.info("При вроверки авторизации был получен код 200. Пользователь не авторизирован!");
            return false;
        }

        if (statusCode == 302) {
            var location = getLocation(httpResponse);
            Assert.notNull(location, "При проверке авторизации, не получил ссылку переадресации при коде 302.");
            if (VK_PATCH_FEED.equals(location)) {
                log.info("При проверке авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь авторизирован!");
                return true;
            } else {
                log.info("При проверке авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
                return false;
            }
        } else {
            throw new RuntimeException("Получен неожидаемый статус код " + statusCode);
        }

    }

    public HttpResponse send(String path, Map<String, String> headers) {
        try {
            HttpResponse httpResponse;
            while (true) {
                httpResponse = webClient.get(new URIBuilder(VK_URL).setPath(path).build(), headers);
                if (httpResponse.getStatusCode() != 302) {
                    String location = httpResponse.getHeaders().getOrDefault("location", List.of("")).get(0);
                    if (location.startsWith(VK_LOGIN_URL.toString())) {
                        if (!restoreAuth(URI.create(location))) {
                            throw new RuntimeException("Не получилось продлить сессию пользователя!");
                        }
                        continue;
                    }
                }
                break;
            }

            if (httpResponse.is2xxStatus()) {
                return httpResponse;
            }

            throw new RuntimeException("Получен неожидаемый статус код " + httpResponse.getStatusCode());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }


    }


    private boolean restoreAuth(URI loginUrl) {
        loginUrl = restoreAuth1(loginUrl);
        return restoreAuth2(loginUrl);
    }


    private URI restoreAuth1(URI loginUrl) {
        HttpResponse httpResponse = webClient.get(loginUrl);
        if (httpResponse.getStatusCode() == 302) {
            String location = httpResponse.getHeaders().getOrDefault("location", List.of("")).get(0);
            if (!location.startsWith("https://m.vk.com/login")) {
                throw new RuntimeException("Шаг 1. При продлении авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
            }
            return URI.create(location);
        } else {
            throw new RuntimeException("Шаг 1. При продлении авторизации был получен код " + httpResponse.getStatusCode() + ". Не понятный для программы статус. Пользователь не авторизирован!");
        }
    }

    private boolean restoreAuth2(URI loginUrl) {
        HttpResponse httpResponse = webClient.get(loginUrl);
        if (httpResponse.getStatusCode() == 302) {
            String location = getLocation(httpResponse);
            if (!(Objects.equals(location, "\\") || Objects.equals(location, "/"))) {
                throw new RuntimeException("Шаг 2. При продлении авторизации был получен код 302 и ссылка \"" + location + "\" Пользователь не авторизирован!");
            }
            log.debug("Продление авторизации произошло успешно. Пользователь авторизирован!");
            return true;
        } else {
            throw new RuntimeException("Шаг 2. При продлении авторизации был получен код " + httpResponse.getStatusCode() + ". Не понятный для программы статус. Пользователь не авторизирован!");
        }
    }

    private String getLocation(HttpResponse httpResponse) {
        List<String> location = httpResponse.getHeaders().get("location");
        if (location != null && location.size() > 0) {
            return location.get(0);
        }
        return null;
    }

    public void close() throws IOException {
        webClient.close();
    }
}
