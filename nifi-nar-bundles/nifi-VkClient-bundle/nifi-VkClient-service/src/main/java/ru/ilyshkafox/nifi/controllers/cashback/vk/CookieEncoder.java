package ru.ilyshkafox.nifi.controllers.cashback.vk;

public interface CookieEncoder {
    default String getName() {
        return getClass().getSimpleName();
    }

    String getPassword();

    String decode(String text);

    String encode(String text);
}
