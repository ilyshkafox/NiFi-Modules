package ru.ilyshkafox.nifi.controllers.cashback.vk.utils;

import org.apache.nifi.controller.ControllerService;
import org.apache.nifi.reporting.InitializationException;
import org.flywaydb.core.Flyway;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;


public abstract class UpdateDataBaseUtils {
    public static void migrate(final ControllerService service, final DataSource connection, final String schema, final SQLDialect dialect) throws SQLException {

        if (dialect == SQLDialect.POSTGRES) {
            Flyway flyway = Flyway.configure()
                    .dataSource(connection)
                    .locations("db/migration/nifi-vkclient-service/postgres")
                    .defaultSchema(schema)
                    .schemas(schema)
                    .createSchemas(true)
                    .placeholderPrefix(service.toString())
                    .load();
            flyway.migrate();
        } else {
            throw new RuntimeException("Yt поддерживаемы тип данныз");
        }


    }


    public static void truncateTable(Connection connection, String name) throws InitializationException {
        DSL.using(connection)
                .truncateTable(DSL.name(name))
                .execute();
    }
}
