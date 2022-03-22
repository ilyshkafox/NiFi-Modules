package ru.ilyshkafox.nifi.controllers.cashback.vk.utils;

import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.reporting.InitializationException;
import org.flywaydb.core.Flyway;
import org.jooq.impl.DSL;
import org.jooq.tools.jdbc.SingleConnectionDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public abstract class UpdateDataBaseUtils {
    public static void migrate(final ControllerService service, final Connection connection, final String schema) throws SQLException {
        Flyway flyway = Flyway.configure()
                .dataSource(new SingleConnectionDataSource(connection))
                .locations("db/migration/nifi-vkclient-service/postgres")
                .defaultSchema(schema)
                .schemas(schema)
                .createSchemas(true)
                .placeholderPrefix(service.toString())
                .load();
        flyway.migrate();
    }


    public static void truncateTable(Connection connection, String name) throws InitializationException {
        DSL.using(connection)
                .truncateTable(DSL.name(name))
                .execute();
    }
}
