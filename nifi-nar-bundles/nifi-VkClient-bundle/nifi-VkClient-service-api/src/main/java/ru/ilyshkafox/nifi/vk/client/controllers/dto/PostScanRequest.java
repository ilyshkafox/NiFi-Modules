package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.http.HttpRequest;
import java.nio.charset.StandardCharsets;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PostScanRequest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    @JsonProperty("action_id")
    private Integer actionId = -1;
    @JsonProperty("first_name")
    private String firstName;
    @JsonProperty("qr_string")
    private String qrString;
    private String source = "handed";

    public static PostScanRequest of(String qrString) {
        return new PostScanRequest(-1, null, qrString, "handed");
    }

    public HttpRequest.BodyPublisher toBodyPublishers() {
        try {
            return HttpRequest.BodyPublishers.ofString(OBJECT_MAPPER.writeValueAsString(this));
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Не удалось сформировать запрос.", e);
        }
    }
}
