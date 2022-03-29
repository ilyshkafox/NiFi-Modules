package ru.ilyshkafox.nifi.controllers.cashback.vk.utils;

import org.flywaydb.core.internal.util.StringUtils;

import java.net.HttpCookie;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

public abstract class HashUtils {
    public static long getCookieHash(List<HttpCookie> httpCookie) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES * httpCookie.size());

        httpCookie.stream()
                .sorted(Comparator.comparing(HttpCookie::getDomain)
                        .thenComparing(HttpCookie::getName)
                        .thenComparing(HttpCookie::getPath)
                        .thenComparing(HttpCookie::getValue)
                )
                .map(HashUtils::getHashCode)
                .forEach(buffer::putLong);
        return getCRC32Checksum(buffer.array());
    }

    private static long getHashCode(HttpCookie cookie) {
        String name = format(cookie.getName());
        String domain = format(cookie.getDomain());
        String path = format(cookie.getPath());
        String value = format(cookie.getValue());
        return getCRC32Checksum((name + domain + path + value).getBytes(StandardCharsets.ISO_8859_1));
    }

    private static String format(String val) {
        return val == null ? "" : val.toLowerCase();
    }

    private static long getCRC32Checksum(byte[] bytes) {
        Checksum crc32 = new CRC32();
        crc32.update(bytes, 0, bytes.length);
        return crc32.getValue();
    }

    public static long getPasswordHash(String password) {
        return StringUtils.hasText(password) ? getCRC32Checksum(password.getBytes(StandardCharsets.ISO_8859_1)) : 0;
    }

}
