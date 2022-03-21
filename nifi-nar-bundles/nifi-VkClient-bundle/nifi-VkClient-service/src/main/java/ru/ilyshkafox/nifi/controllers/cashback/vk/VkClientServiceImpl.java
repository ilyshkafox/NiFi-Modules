package ru.ilyshkafox.nifi.controllers.cashback.vk;

//import lombok.extern.slf4j.Slf4j;

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
import org.apache.nifi.reporting.InitializationException;
import org.jooq.SQLDialect;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.migrations.UpdateDataBaseUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static ru.ilyshkafox.nifi.controllers.cashback.vk.VkClientServiceProperty.*;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientServiceImpl extends AbstractControllerService implements VkClientService {
    private final static Scope STATE_SCOPE = Scope.CLUSTER;

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(
            CONNECTION_POOL, SCHEMA_NAME, DATABASE_DIALECT,
            J2TEAM_COOKIE
    );

    private DBCPService connectionPool;


    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException, IOException {
        var log = getLogger();
        log.info("Запуск VkClient сервиса.");

        var stateManager = getStateManager();
        var state = stateManager.getState(STATE_SCOPE).toMap();

        var property = new VkClientServiceProperty(context);

        initConnectionPool(property);
        migration(property);
        J2TeamCookies j2teamCooke = property.getJ2teamCooke();

        stateManager.setState(state, STATE_SCOPE);
    }

    @OnDisabled
    public void shutdown() {

    }

    private void initConnectionPool(final VkClientServiceProperty property) throws InitializationException {
        connectionPool = property.crateConnectionPool();
    }


    private void migration(final VkClientServiceProperty property) throws InitializationException {
        String schemaName = property.getSchemaName();
        SQLDialect sqlDialect = property.getSqlDialect();

        try (Connection connection = connectionPool.getConnection();) {
            UpdateDataBaseUtils.migrate(connection, schemaName, sqlDialect);
        } catch (SQLException e) {
            throw new InitializationException("Ошибка при выполнении миграции");
        }
    }


    private void checkAndUpdateCooke(final VkClientServiceProperty property) throws InitializationException {

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

    // =====================================================================================
    // Реализация интерфейса
    // =====================================================================================


    @Override
    public boolean isLogin() {
        return false;
    }


}
