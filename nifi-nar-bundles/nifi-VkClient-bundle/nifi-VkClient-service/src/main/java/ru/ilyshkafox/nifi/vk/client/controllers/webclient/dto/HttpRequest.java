package ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto;

import lombok.Data;

import java.util.Map;

@Data
public class HttpRequest {
    private final String url;
    private final Map<String, String> headers;
    private final String method;
    private final String body;
}
