package ru.ilyshkafox.nifi.vk.client.controllers.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

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

    public Map<String, String> toMap() {
        return Map.ofEntries(
                Map.entry("accept", accept),
                Map.entry("accept-language", acceptLanguage),
                Map.entry("cache-control", cacheControl),
                Map.entry("referer", referer),
                Map.entry("sec-ch-ua", secChUa),
                Map.entry("sec-ch-ua-mobile", secChUaMobile),
                Map.entry("sec-fetch-dest", secFetchDest),
                Map.entry("sec-fetch-mode", secFetchMode),
                Map.entry("sec-fetch-site", secFetchSite),
                Map.entry("upgrade-insecure-requests", upgradeInsecureRequests),
                Map.entry("user-agent", userAgent)
        );
    }
}
