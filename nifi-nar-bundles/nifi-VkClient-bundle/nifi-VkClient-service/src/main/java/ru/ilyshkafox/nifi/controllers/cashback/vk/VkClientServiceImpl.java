package ru.ilyshkafox.nifi.controllers.cashback.vk;

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
import org.apache.nifi.reporting.InitializationException;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import ru.ilyshkafox.nifi.controllers.cashback.vk.cookieencoder.AesCookieEncoder;
import ru.ilyshkafox.nifi.controllers.cashback.vk.cookieencoder.NoCookieEncoder;
import ru.ilyshkafox.nifi.controllers.cashback.vk.cookiestore.VkCookieStore;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.repo.CookieRepo;
import ru.ilyshkafox.nifi.controllers.cashback.vk.repo.KeyValueRepo;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.Assert;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.HashUtils;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.J2TeamCookiesMapper;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.UpdateDataBaseUtils;

import javax.sql.DataSource;
import java.io.IOException;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.sql.SQLException;
import java.util.List;

import static ru.ilyshkafox.nifi.controllers.cashback.vk.VkClientServiceProperty.*;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientServiceImpl extends AbstractControllerService implements VkClientService {
    private final static String HASH_COOKIE_KEY = "vk.cookie.hash";
    private final static String STORE_ENCODE_COOKIE_KEY = "vk.cookie.encoder.class";
    private final static String STORE_PASSWORD_COOKIE_KEY = "vk.cookie.password.hash";
    private KeyValueRepo keyValueRepo;
    private CookieEncoder cockeEncoder;
    private CookieRepo cookieRepo;
    private CookieStore cookieStore;


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(
            CONNECTION_POOL, DATABASE_DIALECT, SCHEMA_NAME,
            J2TEAM_COOKIE, COOKIE_ENCODER, COOKIE_ENCODE_KEY
    );

    private DBCPService connectionPool = null;
    private DataSource dataSource;
    private DSLContext dsl = null;


    @OnEnabled
    public void onMigration(final ConfigurationContext context) throws InitializationException, IOException, SQLException {
        var log = getLogger();
        log.info("Запуск VkClient сервиса.");

        var property = new VkClientServiceProperty(context);

        initDataSource(property);
        initRepositoryStep1(property);
        migration(property);
        initRepositoryStep2CookieStore(property);

    }

    @OnDisabled
    public void shutdown() {
        keyValueRepo = null;
        dataSource = null;
        cookieRepo = null;
        connectionPool = null;
        cockeEncoder = null;
        dsl = null;
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


    @Override
    public boolean isLogin() {
        return false;
    }


}
