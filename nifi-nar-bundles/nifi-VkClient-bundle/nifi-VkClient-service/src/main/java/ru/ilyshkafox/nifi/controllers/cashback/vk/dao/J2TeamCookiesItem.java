package ru.ilyshkafox.nifi.controllers.cashback.vk.dao;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class J2TeamCookiesItem {
    private String domain;
    private BigDecimal expirationDate;
    private Boolean hostOnly;
    private Boolean httpOnly;
    private String name;
    private String path;
    private String sameSite;
    private Boolean secure;
    private Boolean session;
    private String storeId;
    private String value;
}
