package ru.ilyshkafox.nifi.controllers.cashback.vk.repo;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.generated.tables.Cookie;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.CookieEntity;

import java.util.List;

import static org.jooq.generated.Tables.COOKIE;


@RequiredArgsConstructor
public class CookieRepo {
    public final Cookie TABLE = COOKIE.as(DSL.name(COOKIE.getName()));

    private final DSLContext dsl;

    public List<CookieEntity> findAll() {
        return dsl.selectFrom(TABLE)
                .fetchInto(CookieEntity.class);

    }

    public void delete(CookieEntity entity) {
        delete(entity.getName(), entity.getDomain(), entity.getPath());
    }

    public void delete(String name, String domain, String path) {
        dsl.deleteFrom(TABLE)
                .where(TABLE.NAME.eq(name))
                .and(TABLE.DOMAIN.eq(domain))
                .and(TABLE.PATH.eq(path))
                .execute();
    }

    public void save(CookieEntity entity) {
        if (entity.getId() == null) {
            create(entity);
        } else {
            update(entity);
        }
    }

    public void create(CookieEntity entity) {
        dsl.insertInto(TABLE,
                        TABLE.URL,
                        TABLE.NAME,
                        TABLE.DOMAIN,
                        TABLE.PATH,
                        TABLE.VALUE,
                        TABLE.COMMENT,
                        TABLE.COMMENT_URL,
                        TABLE.DISCARD,
                        TABLE.MAX_AGE,
                        TABLE.PORTLIST,
                        TABLE.SECURE,
                        TABLE.HTTP_ONLY,
                        TABLE.VERSION
                )
                .values(entity.getUrl(),
                        entity.getName(),
                        entity.getDomain(),
                        entity.getPath(),
                        entity.getValue(),
                        entity.getComment(),
                        entity.getCommentURL(),
                        entity.isDiscard(),
                        entity.getMaxAge(),
                        entity.getPortlist(),
                        entity.isSecure(),
                        entity.isHttpOnly(),
                        entity.getVersion())
                .execute();
    }


    private void update(CookieEntity entity) {
        dsl.update(TABLE)
                .set(TABLE.URL, entity.getUrl())
                .set(TABLE.NAME, entity.getName())
                .set(TABLE.DOMAIN, entity.getDomain())
                .set(TABLE.PATH, entity.getPath())
                .set(TABLE.VALUE, entity.getValue())
                .set(TABLE.COMMENT, entity.getComment())
                .set(TABLE.COMMENT_URL, entity.getCommentURL())
                .set(TABLE.DISCARD, entity.isDiscard())
                .set(TABLE.MAX_AGE, entity.getMaxAge())
                .set(TABLE.PORTLIST, entity.getPortlist())
                .set(TABLE.SECURE, entity.isSecure())
                .set(TABLE.HTTP_ONLY, entity.isHttpOnly())
                .set(TABLE.VERSION, entity.getVersion())
                .where(TABLE.ID.eq(entity.getId()))
                .execute();
    }

    public void deleteAll() {
        dsl.deleteFrom(TABLE)
//                .where(DSL.sql("1=1"))
                .execute();
    }
}
