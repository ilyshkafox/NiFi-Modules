package ru.ilyshka_fox.service.cashback.vk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
