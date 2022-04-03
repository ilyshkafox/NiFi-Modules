package ru.ilyshkafox.nifi.vk.client.controllers.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import ru.ilyshkafox.nifi.vk.client.controllers.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.vk.client.controllers.dao.J2TeamCookiesItem;

import java.net.HttpCookie;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class J2TeamCookiesMapper {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static J2TeamCookies jsonDecode(String jsonString) {
        try {
            return objectMapper.readValue(jsonString, J2TeamCookies.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("При получении куки из файла произошла ошибка.", e);
        }
    }

    public static List<HttpCookie> map(J2TeamCookies cookies) {
        return cookies.getCookies().stream()
                .map(J2TeamCookiesMapper::map)
                .filter(Objects::nonNull)
                .collect(Collectors.toUnmodifiableList());
    }


    public static HttpCookie map(J2TeamCookiesItem dto) {
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


    public static List<Cookie> mapHttp5(J2TeamCookies cookies) {
        return cookies.getCookies().stream()
                .map(J2TeamCookiesMapper::mapHttp5)
                .collect(Collectors.toUnmodifiableList());
    }


    public static Cookie mapHttp5(J2TeamCookiesItem dto) {
        BasicClientCookie cookie = new BasicClientCookie(dto.getName(), dto.getValue());
        cookie.setDomain(dto.getDomain());
        cookie.setPath(dto.getPath());
        cookie.setValue(dto.getValue());
        cookie.setSecure(dto.getSecure() != null && dto.getSecure());
        cookie.setExpiryDate(new Date(dto.getExpirationDate().longValue() * 1000));
        cookie.setAttribute("http-only", String.valueOf(dto.getHttpOnly() != null && dto.getHttpOnly()));
        return cookie;
    }
}
