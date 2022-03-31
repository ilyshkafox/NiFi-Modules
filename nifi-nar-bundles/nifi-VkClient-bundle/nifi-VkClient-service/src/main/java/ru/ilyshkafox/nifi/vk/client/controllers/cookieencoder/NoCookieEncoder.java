package ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder;


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
