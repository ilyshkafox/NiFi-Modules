package ru.ilyshka_fox.service.cashback.vk.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ScanOneResponse {
    private ScanResponse.DataItem response;
}
