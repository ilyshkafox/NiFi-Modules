package ru.ilyshkafox.nifi.controllers.cashback.vk;

//import lombok.extern.slf4j.Slf4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import org.apache.nifi.annotation.behavior.RequiresInstanceClassLoading;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.components.state.Scope;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.reporting.InitializationException;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.UpdateDataBaseUtils;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.Assert;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.HashHttpCookie;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.MapJ2TeamCookie;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientServiceImpl extends AbstractControllerService implements VkClientService {
    private final static String HASH_J2TEAM_COOKIE_KEY = "cookie.hash";
    private final static String MIGRATION_VERSION = "migration.version";
    private final static Scope STATE_SCOPE = Scope.CLUSTER;

    private final static ObjectMapper objectMapper = new ObjectMapper();

    static final PropertyDescriptor CONNECTION_POOL = new PropertyDescriptor.Builder()
            .name("JDBC Connection Pool")
            .description("Specifies the JDBC Connection Pool to use in order to convert the JSON message to a SQL statement. "
                    + "The Connection Pool is necessary in order to determine the appropriate database column types.")
            .identifiesControllerService(DBCPService.class)
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
                    "Если вы хотите выйти из аккаунта, то в настроках VK удалите данное устройтсво авторизации."
            )
            .addValidator(J2TeamValidator.JSON_FORMAT_VALIDATOR)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .sensitive(true)
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(CONNECTION_POOL, J2TEAM_COOKIE);

    private DBCPService connectionPool;


    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException, IOException {
        var log = getLogger();
        var stateManager = getStateManager();
        var state = stateManager.getState(STATE_SCOPE).toMap();
        log.info("Запуск VkClient сервиса.");

        connectionPool = getConnectionPool(context);

        migration(context, state);


        stateManager.setState(state, STATE_SCOPE);
    }

    @OnDisabled
    public void shutdown() {

    }

    @Override
    public boolean isLogin() {
        return false;
    }


    private J2TeamCookies readJ2teamCooke(final ConfigurationContext context) throws InitializationException {
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

    private DBCPService getConnectionPool(final ConfigurationContext context) throws InitializationException {
        return context.getProperty(CONNECTION_POOL).asControllerService(DBCPService.class);
    }


    private void migration(final ConfigurationContext context, final Map<String, String> state) throws InitializationException {
        int version = Integer.parseInt(state.getOrDefault(MIGRATION_VERSION, "0"));

        try (Connection connection = connectionPool.getConnection();) {
            int newVersion = UpdateDataBaseUtils.migrate(version, connection);
            state.put(MIGRATION_VERSION, String.valueOf(newVersion));
        } catch (SQLException e) {
            throw new InitializationException("Ошибка при выполнении миграции");
        }
    }

    private void uploadOnUpdateJ2TeamCookies(final ConfigurationContext context, final Map<String, String> state) throws InitializationException {
        var log = getLogger();

        var j2TeamCookies = readJ2teamCooke(context);
        var httpCookie = MapJ2TeamCookie.toHttpCookie(j2TeamCookies);

        var newJsonHash = HashHttpCookie.getCookieHash(httpCookie);
        long oldJsonHash = Long.parseLong(state.getOrDefault(HASH_J2TEAM_COOKIE_KEY, "0"));

        if (oldJsonHash != newJsonHash) {
            log.info("Обноружено обновление или создание новых куки.");
            truncateTables();
        }

    }

    private void truncateTables() throws InitializationException {
        try (Connection connection = getConnection();) {
            UpdateDataBaseUtils.truncateTable(connection, "vk_cookie");
        } catch (SQLException e) {
            throw new InitializationException("Ошибка при очистке таблиц");
        }
    }

    private Connection getConnection() throws InitializationException, SQLException {
        Connection connection = connectionPool.getConnection();
        connection.setSchema("cashback");
        return connection;
    }

}
