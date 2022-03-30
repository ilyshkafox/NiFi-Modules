package ru.ilyshkafox.nifi.controllers.cashback.vk.cookieencoder;


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
