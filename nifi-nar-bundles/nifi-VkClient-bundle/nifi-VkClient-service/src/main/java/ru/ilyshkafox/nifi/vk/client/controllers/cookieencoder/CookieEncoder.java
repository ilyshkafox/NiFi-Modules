package ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder;

public interface CookieEncoder {
    default String getName() {
        return getClass().getSimpleName();
    }

    String getPassword();

    String decode(String text);

    String encode(String text);
}
