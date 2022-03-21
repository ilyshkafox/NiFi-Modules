package ru.ilyshkafox.nifi.controllers.cashback.vk.migrations;

import org.apache.nifi.reporting.InitializationException;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.scripts.V001__CreateVkCookie;
import ru.ilyshkafox.nifi.controllers.cashback.vk.repo.KeyValueRepo;

import java.sql.Connection;
import java.sql.SQLException;

import static ru.ilyshkafox.nifi.controllers.cashback.vk.repo.KeyValueRepo.*;

public abstract class UpdateDataBaseUtils {
    private static final String KEY_MIGRATION = "migration";

    public static void migrate(final Connection connection, final String schema, final SQLDialect dialect) throws SQLException {
        DSLContext dsl = DSL.using(connection, dialect);
        KeyValueRepo repo = new KeyValueRepo(dsl);

        beforeMigration(dsl, connection, schema);
        migration(dsl, repo);
    }

    private static void beforeMigration(final DSLContext dsl, Connection connection, String schema) throws SQLException {
        createAndChangeSchema(dsl, connection, schema);
        createIfNotExistsKeyValueStorage(dsl);
    }

    private static void migration(final DSLContext dsl, final KeyValueRepo repo) throws SQLException {
        String value = repo.getValue(KEY_MIGRATION);
        int version = value == null ? 0 : Integer.parseInt(value);
        int newVersion = 0;

        // Migration
        if (version <= newVersion++) V001__CreateVkCookie.migrate(dsl);

        repo.setValue(KEY_MIGRATION, String.valueOf(newVersion));
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


    public static void truncateTable(Connection connection, String name) throws InitializationException {
        DSL.using(connection)
                .truncateTable(DSL.name(name))
                .execute();
    }
}
