package ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient;

import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.BaseHttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.ContentType;

import java.io.IOException;
import java.net.CookieManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpWebClient implements WebClient {
    private final HttpClient httpClient;


    public HttpWebClient(final CookieManager cookieManager) {
        this.httpClient = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .followRedirects(HttpClient.Redirect.NEVER)
                .build();
    }


    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse send(String method, URI url, Map<String, String> headers, String body, ContentType contentType) {
        HttpRequest.Builder builder = HttpRequest.newBuilder(url);
        headers.forEach((name, value) -> {
            if (!name.equalsIgnoreCase("Content-Type")) builder.header(name, value);
        });
        if (body == null) {
            builder.method(method, null);

        } else {
            builder.method(method, HttpRequest.BodyPublishers.ofString(body));
            builder.header("Content-Type", getHttpContentType(contentType));
        }
        try {
            return mapHttpResponse(this.send(builder.build()));
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse send(String method, URI url, Map<String, String> headers, String body) {
        return send(method, url, headers, body, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse send(String method, URI url, Map<String, String> headers) {
        return send(method, url, headers, null, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse get(URI url) {
        return send("GET", url, Map.of(), null, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse get(URI url, Map<String, String> headers) {
        return send("GET", url, headers, null, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse post(URI url, Map<String, String> headers, String body, ContentType contentType) {
        return send("POST", url, headers, body, contentType);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse post(URI url, Map<String, String> headers, String body) {
        return send("POST", url, headers, body, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse put(URI url, Map<String, String> headers, String body, ContentType contentType) {
        return send("PUT", url, headers, body, contentType);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse put(URI url, Map<String, String> headers, String body) {
        return send("PUT", url, headers, body, null);
    }

    @Override
    public ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse delete(URI url, Map<String, String> headers) {
        return send("DELETE", url, headers, null, null);
    }

    private ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse mapHttpResponse(HttpResponse<String> response) {
        return new BaseHttpResponse(
                response.uri(),
                response.statusCode(),
                response.body(),
                response.headers().map()
        );
    }

    private HttpResponse<String> send(HttpRequest request) throws IOException, InterruptedException {
        return httpClient.send(request, responseInfo -> HttpResponse.BodySubscribers.ofString(StandardCharsets.UTF_8));
    }


    @Override
    public void close() throws IOException {
        // Ignore
    }


    private String getHttpContentType(ContentType contentType) {
        switch (contentType) {
            case APPLICATION_ATOM_XML:
                return "application/atom+xml";
            case APPLICATION_FORM_URLENCODED:
                return "application/x-www-form-urlencoded";
            case APPLICATION_JSON:
                return "application/json";
            case APPLICATION_SVG_XML:
                return "application/svg+xml";
            case APPLICATION_XHTML_XML:
                return "application/xhtml+xml";
            case APPLICATION_XML:
                return "application/xml";
            case IMAGE_BMP:
                return "image/bmp";
            case IMAGE_GIF:
                return "image/gif";
            case IMAGE_JPEG:
                return "image/jpeg";
            case IMAGE_PNG:
                return "image/png";
            case IMAGE_SVG:
                return "image/svg+xml";
            case IMAGE_TIFF:
                return "image/tiff";
            case IMAGE_WEBP:
                return "image/webp";
            case MULTIPART_FORM_DATA:
                return "multipart/form-data";
            case TEXT_HTML:
                return "text/html";
            case TEXT_PLAIN:
                return "text/plain";
            case TEXT_XML:
                return "text/xml";
            default:
                throw new RuntimeException("Нереализованный ContentType");
        }
    }

}

