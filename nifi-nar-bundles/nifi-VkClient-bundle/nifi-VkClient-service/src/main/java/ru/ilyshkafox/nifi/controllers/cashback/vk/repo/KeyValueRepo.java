package ru.ilyshkafox.nifi.controllers.cashback.vk.repo;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.generated.tables.Storage;
import org.jooq.impl.DSL;

import java.sql.SQLException;

import static org.jooq.generated.tables.Storage.STORAGE;


@RequiredArgsConstructor
public class KeyValueRepo {
    public final Storage TABLE = STORAGE.as(DSL.name(STORAGE.getName()));

    private final DSLContext dsl;

    public String getValue(final String key) throws SQLException {
        return dsl
                .select(TABLE.VALUE)
                .from(TABLE)
                .where(TABLE.KEY.eq(key))
                .fetchOne(TABLE.VALUE);
    }


    public void setValue(final String key, final String value) throws SQLException {
        dsl
                .insertInto(TABLE, TABLE.KEY, TABLE.VALUE)
                .values(key, value)
                .onDuplicateKeyUpdate()
                .set(TABLE.VALUE, value)
                .where(TABLE.KEY.eq(key))
                .execute();
    }
}
