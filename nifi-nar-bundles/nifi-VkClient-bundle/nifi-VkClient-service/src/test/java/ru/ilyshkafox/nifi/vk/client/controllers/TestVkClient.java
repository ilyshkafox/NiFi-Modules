package ru.ilyshkafox.nifi.vk.client.controllers;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.processor.Processor;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.generated.Tables;
import org.jooq.generated.tables.Cookie;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.testcontainers.containers.PostgreSQLContainer;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.CookieEntity;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

class TestVkClient {

    public PostgreSQLContainer postgreSQLContainer;


    private TestRunner runner;

    @Mock
    private Processor dummyProcessor;
    @Mock
    private DBCPService dbcpService;
    private VkClient vkClientService;

    @BeforeEach
    public void init() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
        runner = TestRunners.newTestRunner(dummyProcessor);
        runner.setValidateExpressionUsage(false);

        vkClientService = new VkClientServiceImpl();
        when(dbcpService.getConnection()).then(invocation -> postgreSQLContainer.createConnection(""));
        when(dbcpService.getIdentifier()).thenReturn("testConnection");

        if (postgreSQLContainer != null) {
            postgreSQLContainer.close();
        }
        postgreSQLContainer = new PostgreSQLContainer("postgres:latest")
                .withDatabaseName("postgres")
                .withUsername("sa")
                .withPassword("sa");
        postgreSQLContainer.start();
    }


    @AfterEach
    public void clearAll() throws InterruptedException {
        if (postgreSQLContainer != null) {
            postgreSQLContainer.close();
        }
    }


    @Test
    public void testInit() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.NO_ENCODER.name());

        // Проверка
        runner.assertValid(vkClientService);

        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);

        Cookie COOKIE = Tables.COOKIE.as(DSL.name(Tables.COOKIE.getName()));

        List<CookieEntity> cookieEntities;
        try (Connection connection = dbcpService.getConnection()) {
            DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
            dsl.setSchema("vk").execute();
            cookieEntities = dsl.selectFrom(COOKIE).orderBy(COOKIE.NAME).fetchInto(CookieEntity.class);
        }


        assertCookie(cookieEntities.get(0), "h", "1");
        assertCookie(cookieEntities.get(1), "l", "495");
        assertCookie(cookieEntities.get(2), "p", "7a12ab37-0464-4884-812f-e5d8566c4ccf");
        assertCookie(cookieEntities.get(3), "remixcolor_scheme_mode", "auto");
        assertCookie(cookieEntities.get(4), "remixdark_color_scheme", "1");
        assertCookie(cookieEntities.get(5), "remixdt", "0");
        assertCookie(cookieEntities.get(6), "remixflash", "0.0.0");
        assertCookie(cookieEntities.get(7), "remixlang", "0");
        assertCookie(cookieEntities.get(8), "remixluas2", "099dc20e-7945-406a-a614-67f5f002b08d");
        assertCookie(cookieEntities.get(9), "remixmdevice", "1920/1080/1/!!-!!!!!");
        assertCookie(cookieEntities.get(10), "remixQUIC", "1");
        assertCookie(cookieEntities.get(11), "remixscreen_depth", "24");
        assertCookie(cookieEntities.get(12), "remixscreen_dpr", "1");
        assertCookie(cookieEntities.get(13), "remixscreen_height", "1080");
        assertCookie(cookieEntities.get(14), "remixscreen_width", "1920");
        assertCookie(cookieEntities.get(15), "remixscreen_winzoom", "1");
        assertCookie(cookieEntities.get(16), "remixseenads", "0");
        assertCookie(cookieEntities.get(17), "remixsid", "c114f20f-79ea-4570-827b-3a127a7c4db3");
        assertCookie(cookieEntities.get(18), "remixstid", "6e645c03-f7e4-4d44-a0aa-f744a44420e1");
        assertCookie(cookieEntities.get(19), "remixua", "9ac13421-2a1a-4ea3-89f5-bd01e9289c63");
        assertCookie(cookieEntities.get(20), "s", "1");

    }

    @Test
    public void testRestart() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.NO_ENCODER.name());

        // Проверка
        runner.assertValid(vkClientService);
        // Step 1 Запустить
        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);
        // Step 2 Остановить
        runner.disableControllerService(vkClientService);
        // Step 3 Запустить
        runner.enableControllerService(vkClientService);


        Cookie COOKIE = Tables.COOKIE.as(DSL.name(Tables.COOKIE.getName()));


        List<CookieEntity> cookieEntities;
        try (Connection connection = dbcpService.getConnection()) {
            DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
            dsl.setSchema("vk").execute();
            cookieEntities = dsl.selectFrom(COOKIE).orderBy(COOKIE.NAME).fetchInto(CookieEntity.class);
        }


        assertCookie(cookieEntities.get(0), "h", "1");
        assertCookie(cookieEntities.get(1), "l", "495");
        assertCookie(cookieEntities.get(2), "p", "7a12ab37-0464-4884-812f-e5d8566c4ccf");
        assertCookie(cookieEntities.get(3), "remixcolor_scheme_mode", "auto");
        assertCookie(cookieEntities.get(4), "remixdark_color_scheme", "1");
        assertCookie(cookieEntities.get(5), "remixdt", "0");
        assertCookie(cookieEntities.get(6), "remixflash", "0.0.0");
        assertCookie(cookieEntities.get(7), "remixlang", "0");
        assertCookie(cookieEntities.get(8), "remixluas2", "099dc20e-7945-406a-a614-67f5f002b08d");
        assertCookie(cookieEntities.get(9), "remixmdevice", "1920/1080/1/!!-!!!!!");
        assertCookie(cookieEntities.get(10), "remixQUIC", "1");
        assertCookie(cookieEntities.get(11), "remixscreen_depth", "24");
        assertCookie(cookieEntities.get(12), "remixscreen_dpr", "1");
        assertCookie(cookieEntities.get(13), "remixscreen_height", "1080");
        assertCookie(cookieEntities.get(14), "remixscreen_width", "1920");
        assertCookie(cookieEntities.get(15), "remixscreen_winzoom", "1");
        assertCookie(cookieEntities.get(16), "remixseenads", "0");
        assertCookie(cookieEntities.get(17), "remixsid", "c114f20f-79ea-4570-827b-3a127a7c4db3");
        assertCookie(cookieEntities.get(18), "remixstid", "6e645c03-f7e4-4d44-a0aa-f744a44420e1");
        assertCookie(cookieEntities.get(19), "remixua", "9ac13421-2a1a-4ea3-89f5-bd01e9289c63");
        assertCookie(cookieEntities.get(20), "s", "1");

    }

    @Test
    public void testAesInit() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.AES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123");

        // Проверка
        runner.assertValid(vkClientService);

        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);

        Cookie COOKIE = Tables.COOKIE.as(DSL.name(Tables.COOKIE.getName()));

        List<CookieEntity> cookieEntities;
        try (Connection connection = dbcpService.getConnection()) {
            DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
            dsl.setSchema("vk").execute();
            cookieEntities = dsl.selectFrom(COOKIE).orderBy(COOKIE.NAME).fetchInto(CookieEntity.class);
        }

        assertEquals(cookieEntities.get(0).getName(), "h");
        assertEquals(cookieEntities.get(1).getName(), "l");
        assertEquals(cookieEntities.get(2).getName(), "p");
        assertEquals(cookieEntities.get(3).getName(), "remixcolor_scheme_mode");
        assertEquals(cookieEntities.get(4).getName(), "remixdark_color_scheme");
        assertEquals(cookieEntities.get(5).getName(), "remixdt");
        assertEquals(cookieEntities.get(6).getName(), "remixflash");
        assertEquals(cookieEntities.get(7).getName(), "remixlang");
        assertEquals(cookieEntities.get(8).getName(), "remixluas2");
        assertEquals(cookieEntities.get(9).getName(), "remixmdevice");
        assertEquals(cookieEntities.get(10).getName(), "remixQUIC");
        assertEquals(cookieEntities.get(11).getName(), "remixscreen_depth");
        assertEquals(cookieEntities.get(12).getName(), "remixscreen_dpr");
        assertEquals(cookieEntities.get(13).getName(), "remixscreen_height");
        assertEquals(cookieEntities.get(14).getName(), "remixscreen_width");
        assertEquals(cookieEntities.get(15).getName(), "remixscreen_winzoom");
        assertEquals(cookieEntities.get(16).getName(), "remixseenads");
        assertEquals(cookieEntities.get(17).getName(), "remixsid");
        assertEquals(cookieEntities.get(18).getName(), "remixstid");
        assertEquals(cookieEntities.get(19).getName(), "remixua");
        assertEquals(cookieEntities.get(20).getName(), "s");

    }

    @Test
    public void testAesRestart() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.AES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123");

        // Проверка
        runner.assertValid(vkClientService);
        // Step 1 Запустить
        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);
        // Step 2 Остановить
        runner.disableControllerService(vkClientService);
        // Step 3 Запустить
        runner.enableControllerService(vkClientService);


        Cookie COOKIE = Tables.COOKIE.as(DSL.name(Tables.COOKIE.getName()));


        List<CookieEntity> cookieEntities;
        try (Connection connection = dbcpService.getConnection()) {
            DSLContext dsl = DSL.using(connection, SQLDialect.POSTGRES);
            dsl.setSchema("vk").execute();
            cookieEntities = dsl.selectFrom(COOKIE).orderBy(COOKIE.NAME).fetchInto(CookieEntity.class);
        }


        assertEquals(cookieEntities.get(0).getName(), "h");
        assertEquals(cookieEntities.get(1).getName(), "l");
        assertEquals(cookieEntities.get(2).getName(), "p");
        assertEquals(cookieEntities.get(3).getName(), "remixcolor_scheme_mode");
        assertEquals(cookieEntities.get(4).getName(), "remixdark_color_scheme");
        assertEquals(cookieEntities.get(5).getName(), "remixdt");
        assertEquals(cookieEntities.get(6).getName(), "remixflash");
        assertEquals(cookieEntities.get(7).getName(), "remixlang");
        assertEquals(cookieEntities.get(8).getName(), "remixluas2");
        assertEquals(cookieEntities.get(9).getName(), "remixmdevice");
        assertEquals(cookieEntities.get(10).getName(), "remixQUIC");
        assertEquals(cookieEntities.get(11).getName(), "remixscreen_depth");
        assertEquals(cookieEntities.get(12).getName(), "remixscreen_dpr");
        assertEquals(cookieEntities.get(13).getName(), "remixscreen_height");
        assertEquals(cookieEntities.get(14).getName(), "remixscreen_width");
        assertEquals(cookieEntities.get(15).getName(), "remixscreen_winzoom");
        assertEquals(cookieEntities.get(16).getName(), "remixseenads");
        assertEquals(cookieEntities.get(17).getName(), "remixsid");
        assertEquals(cookieEntities.get(18).getName(), "remixstid");
        assertEquals(cookieEntities.get(19).getName(), "remixua");
        assertEquals(cookieEntities.get(20).getName(), "s");

    }


    @Test
    public void testUpdateEncoder() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.NO_ENCODER.name());

        // Проверка
        runner.assertValid(vkClientService);
        // Step 1 Запустить
        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);
        // Step 2 Остановить
        runner.disableControllerService(vkClientService);
        // Step 3 Обновить
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.AES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123");
        // Step 4 Запустить
        assertThrows(org.opentest4j.AssertionFailedError.class, () -> runner.enableControllerService(vkClientService));
    }

    @Test
    public void testUpdatePass() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));

        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.AES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123");
        // Проверка
        runner.assertValid(vkClientService);
        // Step 1 Запустить
        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);
        // Step 2 Остановить
        runner.disableControllerService(vkClientService);
        // Step 3 Обновить
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123Update");
        // Step 4 Запустить
        assertThrows(org.opentest4j.AssertionFailedError.class, () -> runner.enableControllerService(vkClientService));
    }


    @Test
    public void testUpdateJson() throws InitializationException, IOException, SQLException {

        InputStream is = getClass().getClassLoader().getResourceAsStream("test.json");
        InputStream is2 = getClass().getClassLoader().getResourceAsStream("test2.json");

        String jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
        String jsonData2 = IOUtils.toString(is2, StandardCharsets.UTF_8);


        jsonData = jsonData.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));
        jsonData2 = jsonData2.replaceAll("<expirationDate>", String.valueOf((System.currentTimeMillis() / 1000L) + 1000));


        // Подготовка
        runner.addControllerService(dbcpService.getIdentifier(), dbcpService);
        runner.addControllerService("vk-client-service", vkClientService);

        runner.setProperty(vkClientService, VkClientServiceProperty.CONNECTION_POOL, dbcpService.getIdentifier());
        runner.setProperty(vkClientService, VkClientServiceProperty.SCHEMA_NAME, "vk");
        runner.setProperty(vkClientService, VkClientServiceProperty.DATABASE_DIALECT, SQLDialect.POSTGRES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData);
        runner.setProperty(vkClientService, "disableValidateLoginInStart", "true");
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.NO_ENCODER.name());
        // Проверка
        runner.assertValid(vkClientService);
        // Step 1 Запустить
        runner.enableControllerService(dbcpService);
        runner.enableControllerService(vkClientService);
        // Step 2 Остановить
        runner.disableControllerService(vkClientService);
        // Step 3 Обновить
        runner.setProperty(vkClientService, VkClientServiceProperty.J2TEAM_COOKIE, jsonData2);
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODER, CookieEncoderType.AES.name());
        runner.setProperty(vkClientService, VkClientServiceProperty.COOKIE_ENCODE_KEY, "123");
        // Step 4 Запустить
        runner.enableControllerService(vkClientService);
    }


    private void assertCookie(final CookieEntity cookie, String name, String value) {
        assertEquals(name, cookie.getName());
        assertEquals(value, cookie.getValue());
    }

}