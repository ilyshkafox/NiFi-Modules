package ru.ilyshkafox.nifi.vk.client.controllers.cookiestore;

import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.nifi.logging.ComponentLog;
import ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder.CookieEncoder;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.CookieEntity;
import ru.ilyshkafox.nifi.vk.client.controllers.repo.CookieRepo;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

public class VkCookieStore5 implements CookieStore {
    private transient ReadWriteLock lock ;

    private final CookieRepo repo;
    private final CookieEncoder encoder;
    private final ComponentLog log;

    public VkCookieStore5(CookieRepo repo, CookieEncoder encoder, ComponentLog logger) {
        this.repo = repo;
        this.encoder = encoder;
        this.log = logger;
        this.lock = new ReentrantReadWriteLock();
        init();
    }

    private void init() {
        clearExpired(new Date());
        log.info("Cookie: Проинициализировано {} записей", getCookies().size());
    }


    @Override
    public void addCookie(final Cookie cookie) {
        if (cookie != null) {
            lock.writeLock().lock();
            try {
                repo.delete(cookie.getName(), cookie.getDomain(), cookie.getPath());
                if (!cookie.isExpired(new Date())) {
                    repo.save(map(cookie));
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
    }


    @Override
    public List<Cookie> getCookies() {
        lock.readLock().lock();
        try {
            return repo.findAll().stream().map(this::map).collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    @Override
    public boolean clearExpired(final Date date) {
        if (date == null) {
            return false;
        }
        lock.writeLock().lock();
        try {
            long count = repo.findAll().stream()
                    .map(this::map)
                    .filter(cookie -> cookie.isExpired(date))
                    .peek(cookie -> repo.delete(cookie.getName(), cookie.getDomain(), cookie.getPath()))
                    .count();
            return count > 0;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Clears all cookies.
     */
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            repo.deleteAll();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        return getCookies().toString();
    }


    private CookieEntity map(Cookie cookie) {
        CookieEntity dto = new CookieEntity();
        dto.setUrl("--HttpClient5--");
        dto.setName(cookie.getName());
        dto.setDomain(cookie.getDomain());
        dto.setPath(cookie.getPath());
        dto.setValue(encoder.encode(cookie.getValue()));
        dto.setSecure(cookie.isSecure());
        dto.setHttpOnly("true".equalsIgnoreCase(cookie.getAttribute("http-only")));
        dto.setMaxAge(OffsetDateTime.ofInstant(cookie.getExpiryDate().toInstant(), ZoneOffset.systemDefault()));
        return dto;
    }

    private Cookie map(CookieEntity cookieEntity) {
        BasicClientCookie cookie = new BasicClientCookie(cookieEntity.getName(), cookieEntity.getValue());
        cookie.setDomain(cookieEntity.getDomain());
        cookie.setPath(cookieEntity.getPath());
        cookie.setValue(encoder.decode(cookieEntity.getValue()));
        cookie.setSecure(cookieEntity.isSecure());
        cookie.setExpiryDate(Date.from(cookieEntity.getMaxAge().toInstant()));
        cookie.setAttribute("http-only", String.valueOf(cookieEntity.isHttpOnly()));
        return cookie;
    }
}
