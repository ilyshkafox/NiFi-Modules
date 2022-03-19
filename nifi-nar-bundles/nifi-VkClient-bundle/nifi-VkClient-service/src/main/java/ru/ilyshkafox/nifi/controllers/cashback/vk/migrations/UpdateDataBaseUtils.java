package ru.ilyshkafox.nifi.controllers.cashback.vk.migrations;

import org.apache.nifi.reporting.InitializationException;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.SQLDialect;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;
import ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.scripts.V001__CreateVkCookie;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class UpdateDataBaseUtils {
    public static final Table<?> KEY_VALUE_STORAGE = DSL.table(DSL.name("key_value_storage"));
    public static final Field<Long> ID = DSL.field(DSL.name("id"), SQLDataType.BIGINT.notNull());
    public static final Field<String> KEY = DSL.field(DSL.name("key"), SQLDataType.VARCHAR.notNull());
    public static final Field<String> VALUE = DSL.field(DSL.name("value"), SQLDataType.VARCHAR);
    private static final String KEY_MIGRATION = "migration";

    public static void migrate(final Connection connection, final String schema, final SQLDialect dialect) throws SQLException {
        DSLContext dsl = DSL.using(connection, dialect);

        beforeMigration(dsl, connection, schema);
        migration(dsl);
    }

    private static void beforeMigration(final DSLContext dsl, Connection connection, String schema) throws SQLException {
        createAndChangeSchema(dsl, connection, schema);
        createIfNotExistsKeyValueStorage(dsl);
    }

    private static void migration(final DSLContext dsl) throws SQLException {
        int version = getCurrentMigrationVersion(dsl);
        int newVersion = 0;

        // Migration
        if (version <= newVersion++) V001__CreateVkCookie.migrate(dsl);

        setCurrentMigrationVersion(dsl, newVersion);
    }


    private static void createAndChangeSchema(final DSLContext dsl, Connection connection, String schema) throws SQLException {
        dsl.createSchemaIfNotExists(DSL.name(schema)).execute();
        connection.setSchema(schema);
    }

    private static void createIfNotExistsKeyValueStorage(DSLContext dsl) throws SQLException {
        dsl.createTableIfNotExists(KEY_VALUE_STORAGE)
                .columns(ID, KEY, VALUE)
                .primaryKey(ID)
                .constraint(DSL.unique(KEY))
                .execute();
    }


    private static int getCurrentMigrationVersion(final DSLContext dsl) throws SQLException {
        String value = dsl
                .select(VALUE)
                .from(KEY_VALUE_STORAGE)
                .where(KEY.eq(KEY_MIGRATION))
                .fetchOne(VALUE);
        return value == null ? 0 : Integer.parseInt(value);
    }


    private static void setCurrentMigrationVersion(final DSLContext dsl, final int version) throws SQLException {
        dsl
                .insertInto(KEY_VALUE_STORAGE, KEY, VALUE)
                .values(KEY_MIGRATION, String.valueOf(version))
                .onDuplicateKeyUpdate()
                .set(VALUE, String.valueOf(version))
                .where(KEY.eq(KEY_MIGRATION))
                .execute();
    }

    public static void truncateTable(Connection connection, String name) throws InitializationException {
        DSL.using(connection)
                .truncateTable(DSL.name(name))
                .execute();
    }
}
