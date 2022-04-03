package ru.ilyshkafox.nifi.vk.client.controllers.webclient;


import ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto.ContentType;
import ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto.HttpResponse;

import java.io.Closeable;
import java.net.URI;
import java.util.Map;

public interface WebClient extends Closeable {
    HttpResponse send(String method, URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse send(String method, URI url, Map<String, String> headers, String body);

    HttpResponse send(String method, URI url, Map<String, String> headers);

    HttpResponse get(URI url, Map<String, String> headers);

    HttpResponse post(URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse post(URI url, Map<String, String> headers, String body);

    HttpResponse put(URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse put(URI url, Map<String, String> headers, String body);

    HttpResponse delete(URI url, Map<String, String> headers);
}
