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
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.Headers;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dto.HeadersType;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.Assert;

import java.util.Map;
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

    static final PropertyDescriptor DATABASE_DIALECT = new PropertyDescriptor.Builder()
            .name("database-dialect")
            .displayName("Sql Dialect")
            .description("Диалект БД.")
            .allowableValues(SQLDialect.POSTGRES.name())
            .defaultValue(SQLDialect.POSTGRES.name())
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .required(true)
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


    protected static final PropertyDescriptor COOKIE_ENCODER = new PropertyDescriptor.Builder()
            .name("cookie-encoder")
            .displayName("Шифрование cookie")
            .description("Обеспечивает шифрование cookie")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .allowableValues(CookieEncoderType.values())
            .defaultValue(CookieEncoderType.NO_ENCODER.name())
            .build();

    protected static final PropertyDescriptor COOKIE_ENCODE_KEY = new PropertyDescriptor.Builder()
            .name("cookie-encoder-key")
            .displayName("Encode key")
            .description("Обеспечивает шифрование cookie")
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .dependsOn(COOKIE_ENCODER, CookieEncoderType.AES.name())
            .sensitive(true)
            .build();


    //==============================================================================================

    private final ConfigurationContext context;


    public DBCPService crateConnectionPool() throws InitializationException {
        return context.getProperty(CONNECTION_POOL).asControllerService(DBCPService.class);
    }


    public String getSchemaName() throws InitializationException {
        return context.getProperty(SCHEMA_NAME).getValue();
    }


    public SQLDialect getSqlDialect() throws InitializationException {
        return SQLDialect.valueOf(context.getProperty(DATABASE_DIALECT).getValue());
    }

    public J2TeamCookies loadVkJ2teamCooke() throws InitializationException {
        String value = context.getProperty(J2TEAM_COOKIE).getValue();
        try {
            J2TeamCookies j2TeamCookies = objectMapper.readValue(value, J2TeamCookies.class);
            Assert.isTrue(J2TeamValidator.ALLOW_URL.contains(j2TeamCookies.getUrl()), "Необходимо куки от login VK. Получено: \"" + j2TeamCookies.getUrl() + "\", разрешено: " + J2TeamValidator.ALLOW_URL.stream().collect(Collectors.joining("\", \"", "[\"", "\"]")) + ".");
            Assert.notEmpty(j2TeamCookies.getCookies(), "Не найдены куки для VK. Пустой Json");
            return j2TeamCookies;
        } catch (Exception e) {
            throw new InitializationException(e.getMessage(), e);
        }
    }


    public CookieEncoderType getCookieEncoderType() {
        return CookieEncoderType.valueOf(context.getProperty(COOKIE_ENCODER).getValue());
    }

    public String getEncodeKey() {
        return context.getProperty(COOKIE_ENCODE_KEY).evaluateAttributeExpressions().getValue();
    }

    public Map<HeadersType, Headers> getHeaders() {

        Headers vkLogin = Headers.builder()
                .accept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .acceptLanguage("en-GB,en;q=0.9,en-US;q=0.8,ru;q=0.7")
                .cacheControl("max-age=0")
                .referer("https://m.vk.com/")
                .secChUa("\\\" Not;A Brand\\\";v=\\\"99\\\", \\\"Microsoft Edge\\\";v=\\\"91\\\", \\\"Chromium\\\";v=\\\"91\\\"")
                .secChUaMobile("?0")
                .secFetchDest("document")
                .secFetchMode("navigate")
                .secFetchSite("none")
                .upgradeInsecureRequests("?1")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 Edg/91.0.864.59")
                .build();


        Headers checkbackLogin = Headers.builder()
                .accept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .acceptLanguage("en-GB,en;q=0.9,en-US;q=0.8")
                .cacheControl("max-age=0")
                .referer("https://m.vk.com/services")
                .secChUa("\\\" Not;A Brand\\\";v=\\\"99\\\", \\\"Microsoft Edge\\\";v=\\\"91\\\", \\\"Chromium\\\";v=\\\"91\\\"")
                .secChUaMobile("?0")
                .secFetchDest("empty")
                .secFetchMode("same-origin")
                .secFetchSite("same-origin")
                .upgradeInsecureRequests("1")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 Edg/91.0.864.59")
                .build();

        Headers checkbackIndex = Headers.builder()
                .accept("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9")
                .acceptLanguage("en-GB,en;q=0.9,en-US;q=0.8,ru;q=0.7")
                .cacheControl("max-age=0")
                .referer("https://m.vk.com/")
                .secChUa("\\\" Not;A Brand\\\";v=\\\"99\\\", \\\"Microsoft Edge\\\";v=\\\"91\\\", \\\"Chromium\\\";v=\\\"91\\\"")
                .secChUaMobile("?0")
                .secFetchDest("iframe")
                .secFetchMode("navigate")
                .secFetchSite("cross-site")
                .upgradeInsecureRequests("1")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 Edg/91.0.864.59")
                .build();

        Headers checkbackRestHeader = Headers.builder()
                .accept("*/*")
                .acceptLanguage("en-GB,en;q=0.9,en-US;q=0.8,ru;q=0.7")
                .cacheControl("max-age=0")
                .secChUa("\\\" Not;A Brand\\\";v=\\\"99\\\", \\\"Microsoft Edge\\\";v=\\\"91\\\", \\\"Chromium\\\";v=\\\"91\\\"")
                .secChUaMobile("?0")
                .secFetchDest("empty")
                .secFetchMode("cors")
                .secFetchSite("same-origin")
                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.114 Safari/537.36 Edg/91.0.864.59")
                .build();

        return Map.of(
                HeadersType.VK_LOGIN, vkLogin,
                HeadersType.CHECKBACK_LOGIN, checkbackLogin,
                HeadersType.CHECKBACK_INDEX, checkbackIndex,
                HeadersType.CHECKBACK_REST_HEADER, checkbackRestHeader
        );

    }
}
