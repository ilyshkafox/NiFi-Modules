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
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.Assert;

import java.util.stream.Collectors;

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


    protected static final PropertyDescriptor J2TEAM_COOKIE = new PropertyDescriptor.Builder()
            .name("j2team-cookie")
            .displayName("J2Team cookie")
            .description("Json куки от плагина J2TEAM.\n " +
                    "https://chrome.google.com/webstore/detail/j2team-cookies/okpidcojinmlaakglciglbpcpajaibco/reviews \n" +
                    "Данное поле необходимо вставлять без шифрования паролем. Чистый json.\n" +
                    "После того как вы скопируете куки, необходимо очистить их из браузера.\n" +
                    "Данное действие необходимо, чтобы получить уже авторизированный аккаунт, и не проходить проверку капчи.\n" +
                    "Если вы хотите выйти из аккаунта, то в настроках VK удалите данное устройтсво авторизации.\n" +
                    "Куки необходимо получить из поддомена: https://login.vk.com/favicon.ico"
            )
            .addValidator(J2TeamValidator.JSON_FORMAT_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .sensitive(true)
            .build();


    //==============================================================================================

    private final ConfigurationContext context;


    public DBCPService crateConnectionPool() throws InitializationException {
        return context.getProperty(CONNECTION_POOL).asControllerService(DBCPService.class);
    }


    public String getSchemaName() throws InitializationException {
        return context.getProperty(SCHEMA_NAME).evaluateAttributeExpressions().getValue();
    }



    public J2TeamCookies getJ2teamCooke() throws InitializationException {
        String value = context.getProperty(J2TEAM_COOKIE).evaluateAttributeExpressions().getValue();
        try {
            J2TeamCookies j2TeamCookies = objectMapper.readValue(value, J2TeamCookies.class);
            Assert.isTrue(J2TeamValidator.ALLOW_URL.contains(j2TeamCookies.getUrl()), "Необходимо куки от login VK. Получено: \"" + j2TeamCookies.getUrl() + "\", разрешено: " + J2TeamValidator.ALLOW_URL.stream().collect(Collectors.joining("\", \"", "[\"", "\"]")) + ".");
            Assert.notEmpty(j2TeamCookies.getCookies(), "Не найдены куки для VK. Пустой Json");
            return j2TeamCookies;
        } catch (Exception e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }
}
