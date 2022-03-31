package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Headers {
    private String accept;
    private String acceptLanguage;
    private String cacheControl;
    private String referer;
    private String secChUa;
    private String secChUaMobile;
    private String secFetchDest;
    private String secFetchMode;
    private String secFetchSite;
    private String upgradeInsecureRequests;
    private String userAgent;
}
