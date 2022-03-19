package ru.ilyshkafox.nifi.controllers.cashback.vk;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.jooq.SQLDialect;

@RequiredArgsConstructor
public class VkClientServiceProperty {
    private final static ObjectMapper objectMapper = new ObjectMapper();

    static final PropertyDescriptor CONNECTION_POOL = new PropertyDescriptor.Builder()
            .name("database-jdbc-connection-pool")
            .displayName("JDBC Connection Pool")
            .description("Specifies the JDBC Connection Pool to use in order to convert the JSON message to a SQL statement. "
                    + "The Connection Pool is necessary in order to determine the appropriate database column types.")
            .identifiesControllerService(DBCPService.class)
            .required(true)
            .build();

    static final PropertyDescriptor SCHEMA_NAME = new PropertyDescriptor.Builder()
            .name("database-schema")
            .displayName("Schema name")
            .description("Схема в которой будут храниться файлы.")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .required(true)
            .defaultValue("vk")
            .build();

    static final PropertyDescriptor DATABASE_DIALECT = new PropertyDescriptor.Builder()
            .name("database-dialect")
            .displayName("Sql Dialect")
            .description("Диалект БД.")
            .allowableValues(SQLDialect.values())
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(true)
            .build();

    static final PropertyDescriptor REMIXLGCK = new PropertyDescriptor.Builder()
            .name("remixlgck")
            .displayName("Индефикатор доверенного браузера")
            .description("Данное действие необходимо, чтобы не вводить капчку. " +
                    "Для получения значениея: \n" +
                    "1) Откройте сраницу VK в браузере.\n" +
                    "2) Войдите в страничку VK? которую хотите привязать к сервису.\n" +
                    "3) Нажмите F12.\n" +
                    "4) Перейдите на вкладку Network.\n" +
                    "5) Обновите сраницу (F5).\n" +
                    "6) Выделете первый запрос, или любой другой, который адресуеться на страничку.\n" +
                    "7) На вкладке Header найти в параметре cookie.\n" +
                    "8) В cookie найдите значение remixlgck - Это личный индетификатор клиента (браузера)\n" +
                    "9) Желательно зайти на вкладку Application -> Storage и очистить данные. Чтобы браузер получил новый индетификатор.0"
            )
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .sensitive(true)
            .build();


    //==============================================================================================

    private final ConfigurationContext context;


    public DBCPService crateConnectionPool() throws InitializationException {
        return context.getProperty(CONNECTION_POOL).asControllerService(DBCPService.class);
    }

    public String getRemixlgck() throws InitializationException {
        return context.getProperty(REMIXLGCK).evaluateAttributeExpressions().getValue();
    }

    public String getSchemaName() throws InitializationException {
        return context.getProperty(SCHEMA_NAME).evaluateAttributeExpressions().getValue();
    }


    public SQLDialect getSqlDialect() throws InitializationException {
        return SQLDialect.valueOf(context.getProperty(DATABASE_DIALECT).evaluateAttributeExpressions().getValue());
    }
}
