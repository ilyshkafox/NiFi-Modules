package ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.scripts;

import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import org.jooq.impl.SQLDataType;

import java.sql.Connection;

public abstract class V001__CreateVkCookie {
    public static void migrate(Connection connection, SQLDialect dialect) {
        // Create Table If Not Exists
        var tableName = DSL.name("vk_cookie");
        var id = DSL.field(DSL.name("id"), SQLDataType.BIGINT.notNull());
        var url = DSL.field(DSL.name("url"), SQLDataType.VARCHAR.notNull());
        var name = DSL.field(DSL.name("name"), SQLDataType.VARCHAR.notNull());
        var domain = DSL.field(DSL.name("domain"), SQLDataType.VARCHAR.notNull());
        var path = DSL.field(DSL.name("path"), SQLDataType.VARCHAR.notNull());
        var value = DSL.field(DSL.name("value"), SQLDataType.VARCHAR.notNull());
        var comment = DSL.field(DSL.name("comment"), SQLDataType.VARCHAR);
        var commentURL = DSL.field(DSL.name("comment_url"), SQLDataType.VARCHAR);
        var discard = DSL.field(DSL.name("discard"), SQLDataType.VARCHAR);
        var maxAge = DSL.field(DSL.name("max_age"), SQLDataType.OFFSETDATETIME(3));
        var portlist = DSL.field(DSL.name("portlist"), SQLDataType.VARCHAR);
        var secure = DSL.field(DSL.name("secure"), SQLDataType.BOOLEAN);
        var httpOnly = DSL.field(DSL.name("http_only"), SQLDataType.BOOLEAN);
        var version = DSL.field(DSL.name("version"), SQLDataType.INTEGER);


        DSL.using(connection, dialect)
                .createTableIfNotExists(tableName)
                .columns(id
                        , url
                        , name
                        , domain
                        , path
                        , value
                        , comment
                        , commentURL
                        , discard
                        , maxAge
                        , portlist
                        , secure
                        , httpOnly
                        , version
                )
                .primaryKey(id)
                .constraint(DSL.unique(name, domain, path))
                .execute();
    }
}
