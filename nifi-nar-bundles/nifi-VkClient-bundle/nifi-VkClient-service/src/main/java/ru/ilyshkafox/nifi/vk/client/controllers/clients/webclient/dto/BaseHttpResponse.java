package ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;

import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Data
public class BaseHttpResponse implements HttpResponse {
    private final static ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private final URI uri;
    private final Integer statusCode;
    private final String body;
    private final Map<String, List<String>> headers;

    public BaseHttpResponse(URI uri, Integer statusCode, String body, Map<String, List<String>> headers) {
        this.uri = uri;
        this.statusCode = statusCode;
        this.body = body;
        headers.forEach((s, strings) -> headers.put(s, Collections.unmodifiableList(strings)));
        this.headers = Collections.unmodifiableMap(headers);
    }


    @Override
    public <T> T getBody(Class<T> tClass) throws JsonProcessingException {
        return OBJECT_MAPPER.readValue(body, tClass);
    }
}
