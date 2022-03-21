package ru.ilyshkafox.nifi.controllers.cashback.vk.repo;

import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.ForeignKey;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import org.jooq.impl.TableImpl;

import java.sql.SQLException;

@RequiredArgsConstructor
public class KeyValueRepo {
    public static final Field<Long> ID = DSL.field(DSL.name("id"), SQLDataType.BIGINT.notNull().identity(true));
    public static final Field<String> KEY = DSL.field(DSL.name("key"), SQLDataType.VARCHAR.notNull());
    public static final Field<String> VALUE = DSL.field(DSL.name("value"), SQLDataType.VARCHAR);

    public static final Table<?> KEY_VALUE_STORAGE = DSL.table("");


    private final DSLContext dsl;


    public String getValue(final String key) throws SQLException {
        return dsl
                .select(VALUE)
                .from(KEY_VALUE_STORAGE)
                .where(KEY.eq(key))
                .fetchOne(VALUE);
    }


    public void setValue(final String key, final String value) throws SQLException {
        dsl
                .insertInto(KEY_VALUE_STORAGE, KEY, VALUE)
                .values(key, value)
                .onDuplicateKeyUpdate()
                .set(VALUE, value)
                .where(KEY.eq(key))
                .execute();
    }
}
