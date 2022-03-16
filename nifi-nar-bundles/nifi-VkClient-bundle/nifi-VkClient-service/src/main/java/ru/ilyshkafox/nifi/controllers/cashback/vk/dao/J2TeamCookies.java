package ru.ilyshkafox.nifi.controllers.cashback.vk.dao;

import lombok.Data;

import java.util.List;

@Data
public class J2TeamCookies {
    private String url;
    private List<J2TeamCookiesItem> cookies;
}
