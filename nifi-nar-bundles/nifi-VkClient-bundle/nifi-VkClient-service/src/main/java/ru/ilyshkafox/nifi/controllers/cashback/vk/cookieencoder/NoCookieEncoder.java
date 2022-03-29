package ru.ilyshkafox.nifi.controllers.cashback.vk.cookieencoder;

import ru.ilyshkafox.nifi.controllers.cashback.vk.CookieEncoder;


public class NoCookieEncoder implements CookieEncoder {
    @Override
    public String getPassword() {
        return "";
    }

    public String decode(String text) {
        return text;
    }

    public String encode(String text) {
        return text;
    }
}
