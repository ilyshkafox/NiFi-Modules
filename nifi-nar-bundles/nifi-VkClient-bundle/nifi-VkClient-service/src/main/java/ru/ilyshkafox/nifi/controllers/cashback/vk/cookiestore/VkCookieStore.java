package ru.ilyshkafox.nifi.controllers.cashback.vk.cookiestore;

import lombok.extern.slf4j.Slf4j;
import ru.ilyshkafox.nifi.controllers.cashback.vk.CookieEncoder;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.CookieEntity;
import ru.ilyshkafox.nifi.controllers.cashback.vk.repo.CookieRepo;

import java.net.*;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
public class VkCookieStore implements CookieStore {
    private final CookieStore cookieStore = new CookieManager().getCookieStore();
    private final CookieRepo repo;
    private final CookieEncoder encoder;

    public VkCookieStore(CookieRepo repo, CookieEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
        init();
    }

    private void init() {
        List<CookieEntity> all = repo.findAll();
        all.forEach(vkCookieEntity -> {
            URI uri = URI.create(vkCookieEntity.getUri());
            HttpCookie cookie = map(vkCookieEntity);
            if (cookie != null) {
                cookieStore.add(uri, cookie);
            }
        });
        log.info("Проинициализировано {} записей Cookie", getCookies().size());
    }

    @Override
    public void add(URI uri, HttpCookie cookie) {
        cookieStore.add(uri, cookie);
        repo.delete(cookie.getName(), cookie.getDomain(), cookie.getPath());
        if (cookie.getMaxAge() > 0) {
            repo.save(map(cookie, getEffectiveURI(uri)));
        }
    }

    @Override
    public boolean remove(URI uri, HttpCookie cookie) {
        boolean res = cookieStore.remove(uri, cookie);
        repo.delete(cookie.getName(), cookie.getDomain(), cookie.getPath());
        return res;
    }

    @Override
    public boolean removeAll() {
        boolean res = cookieStore.removeAll();
        repo.deleteAll();
        return res;
    }

    @Override
    public List<HttpCookie> get(URI uri) {
        return cookieStore.get(uri);
    }

    @Override
    public List<HttpCookie> getCookies() {
        return cookieStore.getCookies();
    }

    @Override
    public List<URI> getURIs() {
        return cookieStore.getURIs();
    }


    private CookieEntity map(HttpCookie cookie, URI uri) {

        CookieEntity dto = new CookieEntity();
        if (uri != null) {
            dto.setUri(uri.toString());
        }
        dto.setName(cookie.getName());
        dto.setDomain(cookie.getDomain());
        dto.setPath(cookie.getPath());
        dto.setValue(encoder.encode(cookie.getValue()));
        dto.setComment(cookie.getComment());
        dto.setCommentURL(cookie.getCommentURL());
        dto.setDiscard(cookie.getDiscard());
        dto.setPortlist(cookie.getPortlist());
        dto.setSecure(cookie.getSecure());
        dto.setHttpOnly(cookie.isHttpOnly());
        dto.setVersion(cookie.getVersion());
        if (cookie.getMaxAge() <= 0) {
            throw new RuntimeException("Данное куки нельзя сохранять в репозитории.");
        }
        dto.setMaxAge(OffsetDateTime.now().plusSeconds(cookie.getMaxAge()));
        return dto;

    }

    private HttpCookie map(CookieEntity dto) {
        long l = dto.getMaxAge().toEpochSecond() - OffsetDateTime.now().toEpochSecond();
        if (l <= 0) {
            repo.delete(dto);
            return null;
        }

        HttpCookie cookie = new HttpCookie(
                dto.getName(),
                encoder.decode(dto.getValue())
        );
        cookie.setDomain(dto.getDomain());
        cookie.setPath(dto.getPath());
        cookie.setComment(dto.getComment());
        cookie.setCommentURL(dto.getCommentURL());
        cookie.setDiscard(dto.isDiscard());
        cookie.setMaxAge(l);
        cookie.setPortlist(dto.getPortlist());
        cookie.setSecure(dto.isSecure());
        cookie.setHttpOnly(dto.isHttpOnly());
        cookie.setVersion(dto.getVersion());
        return cookie;
    }

    private URI getEffectiveURI(URI uri) {
        URI effectiveURI = null;
        try {
            effectiveURI = new URI("http",
                    uri.getHost(),
                    null,  // path component
                    null,  // query component
                    null   // fragment component
            );
        } catch (URISyntaxException ignored) {
            effectiveURI = uri;
        }

        return effectiveURI;
    }
}
