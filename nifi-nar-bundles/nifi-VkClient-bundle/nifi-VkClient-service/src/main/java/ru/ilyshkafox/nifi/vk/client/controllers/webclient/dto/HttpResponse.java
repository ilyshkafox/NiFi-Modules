package ru.ilyshkafox.nifi.vk.client.controllers.webclient.dto;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.net.URI;
import java.util.List;
import java.util.Map;

public interface HttpResponse {
    URI getUri();

    Integer getStatusCode();

    String getBody();

    <T> T getBody(Class<T> tClass) throws JsonProcessingException;

    Map<String, List<String>> getHeaders();
}
