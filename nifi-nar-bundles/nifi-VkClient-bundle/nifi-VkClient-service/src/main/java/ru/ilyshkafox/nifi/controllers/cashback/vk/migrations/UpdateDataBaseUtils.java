package ru.ilyshkafox.nifi.controllers.cashback.vk.migrations;

import org.apache.nifi.reporting.InitializationException;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.scripts.V001__CreateVkCookie;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class UpdateDataBaseUtils {

    public static int migrate(Integer version, Connection connection, String schema, SQLDialect dialect) throws SQLException {
        preMigration(connection, schema, dialect);
        if (version < 1) V001__CreateVkCookie.migrate(connection, dialect);
        return 1;
    }

    private static void preMigration(Connection connection, String schema, SQLDialect dialect) throws SQLException {
        DSL.using(connection, dialect)
                .createSchemaIfNotExists(DSL.name(schema))
                .execute();
        connection.setSchema(schema);
    }


    public static void truncateTable(Connection connection, String name) throws InitializationException {
        DSL.using(connection)
                .truncateTable(DSL.name(name))
                .execute();
    }
}
