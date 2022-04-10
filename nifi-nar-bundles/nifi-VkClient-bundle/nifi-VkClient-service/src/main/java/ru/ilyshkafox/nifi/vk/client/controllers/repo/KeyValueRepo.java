package ru.ilyshkafox.nifi.vk.client.controllers.repo;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.generated.tables.Storage;

import java.util.Optional;

import static org.jooq.generated.tables.Storage.STORAGE;


@RequiredArgsConstructor
public class KeyValueRepo {
    public final Storage TABLE = STORAGE;

    private final DSLContext dsl;

    public Optional<String> get(final String key) {
        return Optional.ofNullable(dsl
                .select(TABLE.VALUE)
                .from(TABLE)
                .where(TABLE.KEY.eq(key))
                .fetchOne(TABLE.VALUE));
    }


    public void set(final String key, final String value) {
        dsl
                .insertInto(TABLE, TABLE.KEY, TABLE.VALUE)
                .values(key, value)
                .onDuplicateKeyUpdate()
                .set(TABLE.VALUE, value)
                .where(TABLE.KEY.eq(key))
                .execute();
    }
}
