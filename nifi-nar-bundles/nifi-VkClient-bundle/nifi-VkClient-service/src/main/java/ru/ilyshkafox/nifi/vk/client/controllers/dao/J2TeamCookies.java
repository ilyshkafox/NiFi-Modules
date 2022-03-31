package ru.ilyshkafox.nifi.vk.client.controllers.dao;

import lombok.Data;

import java.util.List;

@Data
public class J2TeamCookies {
    private String url;
    private List<J2TeamCookiesItem> cookies;
}