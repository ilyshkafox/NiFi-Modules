package ru.ilyshkafox.nifi.vk.client.controllers;

//import lombok.extern.slf4j.Slf4j;

import lombok.Getter;
import org.apache.nifi.annotation.behavior.RequiresInstanceClassLoading;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.util.StandardValidators;
import org.apache.nifi.reporting.InitializationException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder.AesCookieEncoder;
import ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder.CookieEncoder;
import ru.ilyshkafox.nifi.vk.client.controllers.cookieencoder.NoCookieEncoder;
import ru.ilyshkafox.nifi.vk.client.controllers.cookiestore.VkCookieStore;
import ru.ilyshkafox.nifi.vk.client.controllers.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.vk.client.controllers.repo.CookieRepo;
import ru.ilyshkafox.nifi.vk.client.controllers.repo.KeyValueRepo;
import ru.ilyshkafox.nifi.vk.client.controllers.services.CheckBackClientImpl;
import ru.ilyshkafox.nifi.vk.client.controllers.services.VkWebService;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.Assert;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.HashUtils;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.J2TeamCookiesMapper;
import ru.ilyshkafox.nifi.vk.client.controllers.utils.UpdateDataBaseUtils;

import javax.sql.DataSource;
import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientServiceImpl extends AbstractControllerService implements VkClient {
    private final static String DISABLE_VALIDATE_LOGIN_PROPERTY = "disableValidateLoginInStart";
    private final static String HASH_COOKIE_KEY = "vk.cookie.hash";
    private final static String STORE_ENCODE_COOKIE_KEY = "vk.cookie.encoder.class";
    private final static String STORE_PASSWORD_COOKIE_KEY = "vk.cookie.password.hash";
    private KeyValueRepo keyValueRepo;
    private CookieEncoder cockeEncoder;
    private CookieRepo cookieRepo;
    private CookieStore cookieStore;
    private VkWebService webBrowser;
    private CookieManager cookieManager;


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(
            VkClientServiceProperty.CONNECTION_POOL, VkClientServiceProperty.DATABASE_DIALECT, VkClientServiceProperty.SCHEMA_NAME,
            VkClientServiceProperty.J2TEAM_COOKIE, VkClientServiceProperty.COOKIE_ENCODER, VkClientServiceProperty.COOKIE_ENCODE_KEY
    );

    private DBCPService connectionPool = null;
    private DataSource dataSource;
    private DSLContext dsl = null;


    @OnEnabled
    public void onMigration(final ConfigurationContext context) throws InitializationException {
        var log = getLogger();
        log.info("Запуск VkClient сервиса.");

        var property = new VkClientServiceProperty(context);

        initDataSource(property);
        initRepositoryStep1(property);
        migration(property);
        initRepositoryStep2CookieStore(property);
        initWebBrowser(property);

    }

    @OnEnabled
    public void onLogin(final ConfigurationContext context) throws InitializationException {
        boolean disableValidateLogin = Boolean.parseBoolean(context.getAllProperties().getOrDefault(DISABLE_VALIDATE_LOGIN_PROPERTY, "false"));
        if (disableValidateLogin) {
            getLogger().warn("Валидация авторизации отключена! ");
            return;
        }

        if (!webBrowser.checkLogin()) {
            throw new InitializationException("Пользователь VK не авторизирован!");
        }
        getLogger().info("Авторизация VK произошла успешно!");

    }

    @OnDisabled
    public void shutdown() {
        keyValueRepo = null;
        dataSource = null;
        cookieRepo = null;
        connectionPool = null;
        cockeEncoder = null;
        dsl = null;
        cookieStore = null;
        webBrowser = null;
        cookieManager = null;
    }

    @Override
    protected PropertyDescriptor getSupportedDynamicPropertyDescriptor(final String propertyDescriptorName) {
        if (DISABLE_VALIDATE_LOGIN_PROPERTY.equals(propertyDescriptorName)) {
            return new PropertyDescriptor.Builder()
                    .name(propertyDescriptorName)
                    .required(false)
                    .addValidator(StandardValidators.BOOLEAN_VALIDATOR)
                    .dynamic(true)
                    .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
                    .build();
        }
        return null;
    }


    private void initDataSource(final VkClientServiceProperty property) throws InitializationException {
        connectionPool = property.crateConnectionPool();
        dataSource = new DBCPServiceDataSourceBridge(connectionPool);
        dsl = DSL.using(dataSource, property.getSqlDialect());
        dsl.setSchema(property.getSchemaName()).execute();
    }

    private void initRepositoryStep1(final VkClientServiceProperty property) throws InitializationException {
        keyValueRepo = new KeyValueRepo(dsl);
        cookieRepo = new CookieRepo(dsl);
        cockeEncoder = getEncode(property);
    }


    private void migration(final VkClientServiceProperty property) throws InitializationException {
        String schemaName = property.getSchemaName();
        try {
            UpdateDataBaseUtils.migrate(this, dataSource, schemaName, property.getSqlDialect());
        } catch (SQLException e) {
            throw new InitializationException("Ошибка при выполнении миграции", e);
        }
    }

    private void initRepositoryStep2CookieStore(final VkClientServiceProperty property) throws InitializationException {
        J2TeamCookies j2TeamCookies = property.loadVkJ2teamCooke();
        URI uri = URI.create(j2TeamCookies.getUrl());
        List<HttpCookie> httpCookie = J2TeamCookiesMapper.map(j2TeamCookies);

        long curHash = keyValueRepo.get(HASH_COOKIE_KEY).map(Long::parseLong).orElse(-1L);
        long newHash = HashUtils.getCookieHash(httpCookie);

        if (curHash != newHash) {
            cookieRepo.deleteAll();
            cookieStore = new VkCookieStore(cookieRepo, cockeEncoder, getLogger());
            httpCookie.forEach(hc -> cookieStore.add(uri, hc));
            updateCookieMetadata(newHash);
            getLogger().info("Куки обновлены!");
        } else {
            validateCookieMetadata();
            cookieStore = new VkCookieStore(cookieRepo, cockeEncoder, getLogger());
        }
        cookieManager = new CookieManager(cookieStore, null);

    }

    private void initWebBrowser(final VkClientServiceProperty property) throws InitializationException {
        webBrowser = new VkWebService(getLogger(), property.getHeaders(), cookieManager);
    }

    private void updateCookieMetadata(final long newHttpCookieHash) {
        keyValueRepo.set(HASH_COOKIE_KEY, String.valueOf(newHttpCookieHash));
        keyValueRepo.set(STORE_ENCODE_COOKIE_KEY, cockeEncoder.getName());
        keyValueRepo.set(STORE_PASSWORD_COOKIE_KEY, String.valueOf(HashUtils.getPasswordHash(cockeEncoder.getPassword())));
    }

    private void validateCookieMetadata() {
        String storeName = keyValueRepo.get(STORE_ENCODE_COOKIE_KEY).orElse("");
        Long storePassword = keyValueRepo.get(STORE_PASSWORD_COOKIE_KEY).map(Long::valueOf).orElse(0L);
        long crc32Checksum = HashUtils.getPasswordHash(cockeEncoder.getPassword());

        Assert.isTrue(cockeEncoder.getName().equals(storeName), "Изменен способ шифрования Storage Cookie!");
        Assert.isTrue(crc32Checksum == storePassword, "Изменен пароль от Storage Cookie!");
    }

    private CookieEncoder getEncode(final VkClientServiceProperty property) {
        switch (property.getCookieEncoderType()) {
            case AES:
                return new AesCookieEncoder(property.getEncodeKey());
            case NO_ENCODER:
            default:
                return new NoCookieEncoder();
        }
    }


    // =====================================================================================
    // Реализация интерфейса
    // =====================================================================================

    private CheckBackClientImpl checkBackClient;

    public CheckBackClientImpl getCheckBackClient() {
        if (checkBackClient == null // Нет текущего соединения
                || checkBackClient.getLastRequestTime().plusSeconds(60).isAfter(OffsetDateTime.now()) // Простой соединения час
        ) {
            createCheckBackClient();
        }
        return checkBackClient;
    }

    private void createCheckBackClient() {
        String checkBackXAuth = webBrowser.getCheckBackXAuth();
        checkBackClient = new CheckBackClientImpl(checkBackXAuth, webBrowser);
    }
}
