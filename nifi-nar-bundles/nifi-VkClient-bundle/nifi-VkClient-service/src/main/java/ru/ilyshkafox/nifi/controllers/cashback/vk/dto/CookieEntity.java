package ru.ilyshkafox.nifi.controllers.cashback.vk.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CookieEntity {
    private Long id;
    private String url;
    private String name;
    private String domain;
    private String path;
    private String value;
    private String comment;
    private String commentURL;
    private boolean discard;
    private OffsetDateTime maxAge;
    private String portlist;
    private boolean secure;
    private boolean httpOnly;
    private int version = 1;
}
