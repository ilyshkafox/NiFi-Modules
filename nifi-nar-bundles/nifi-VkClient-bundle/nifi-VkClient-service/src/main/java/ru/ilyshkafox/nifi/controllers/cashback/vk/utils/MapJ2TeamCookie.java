package ru.ilyshkafox.nifi.controllers.cashback.vk.utils;

import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookiesItem;

import java.net.HttpCookie;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public abstract class MapJ2TeamCookie {
    public static List<HttpCookie> toHttpCookie(J2TeamCookies cookies) {
        return cookies.getCookies().stream()
                .map(MapJ2TeamCookie::toHttpCookie)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }


    private static HttpCookie toHttpCookie(J2TeamCookiesItem dto) {
        HttpCookie cookie = new HttpCookie(
                dto.getName(),
                dto.getValue()
        );
        cookie.setDomain(dto.getDomain());
        cookie.setPath(dto.getPath());
        cookie.setSecure(dto.getSecure());
        cookie.setHttpOnly(dto.getHttpOnly());
        cookie.setVersion(0);
        if (dto.getExpirationDate() != null) {
            long l = dto.getExpirationDate().longValue() - System.currentTimeMillis() / 1000;
            if (l <= 0) {
                return null;
            }
            cookie.setMaxAge(l);
        }
        return cookie;
    }

}
