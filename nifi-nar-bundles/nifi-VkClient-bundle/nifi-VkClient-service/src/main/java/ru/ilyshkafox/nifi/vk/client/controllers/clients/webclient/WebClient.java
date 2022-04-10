package ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient;


import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.ContentType;

import java.io.Closeable;
import java.net.URI;
import java.util.Map;

public interface WebClient extends Closeable {
    HttpResponse send(String method, URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse send(String method, URI url, Map<String, String> headers, String body);

    HttpResponse send(String method, URI url, Map<String, String> headers);

    HttpResponse get(URI url);

    HttpResponse get(URI url, Map<String, String> headers);

    HttpResponse post(URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse post(URI url, Map<String, String> headers, String body);

    HttpResponse put(URI url, Map<String, String> headers, String body, ContentType contentType);

    HttpResponse put(URI url, Map<String, String> headers, String body);

    HttpResponse delete(URI url, Map<String, String> headers);
}
