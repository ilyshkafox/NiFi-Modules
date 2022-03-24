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
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.repo.KeyValueRepo;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.UpdateDataBaseUtils;

import javax.sql.DataSource;
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
    private KeyValueRepo keyValueRepo;

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(
            CONNECTION_POOL, SCHEMA_NAME, J2TEAM_COOKIE
    );

    private DBCPService connectionPool = null;
    private DataSource dataSource;
    private DSLContext dsl = null;


    @OnEnabled
    public void onMigration(final ConfigurationContext context) throws InitializationException, IOException, SQLException {
        var log = getLogger();
        log.info("Запуск VkClient сервиса.");

        var stateManager = getStateManager();
        var state = stateManager.getState(STATE_SCOPE).toMap();

        var property = new VkClientServiceProperty(context);

        initDataSource(property);
        initDsl(property);
        migration(property);

        J2TeamCookies j2teamCooke = property.getJ2teamCooke();


        keyValueRepo = new KeyValueRepo(using);


        stateManager.setState(state, STATE_SCOPE);
    }

    @OnDisabled
    public void shutdown() {

    }

    private void initDataSource(final VkClientServiceProperty property) throws InitializationException {
        connectionPool = property.crateConnectionPool();
        dataSource = new DBCPServiceDataSourceBridge(connectionPool);
        dsl = DSL.using(dataSource, property.getSqlDialect());
        dsl.setSchema(property.getSchemaName()).execute();
    }


    private void migration(final VkClientServiceProperty property) throws InitializationException {
        String schemaName = property.getSchemaName();
        try {
            UpdateDataBaseUtils.migrate(this, dataSource, schemaName, property.getSqlDialect());
        } catch (SQLException e) {
            throw new InitializationException("Ошибка при выполнении миграции", e);
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
