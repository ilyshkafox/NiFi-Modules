package ru.ilyshkafox.nifi.vk.client.processors;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.cookie.BasicCookieStore;
import org.apache.hc.client5.http.cookie.Cookie;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.cookie.BasicClientCookie;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.nifi.dbcp.DBCPService;
import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.postgresql.ds.PGSimpleDataSource;
import ru.ilyshkafox.nifi.vk.client.controllers.BaseVkClientService;
import ru.ilyshkafox.nifi.vk.client.controllers.VkClientService;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.Mockito.when;

class TestVkCheckBackRegisterReceipt {
    private static String QR_STRING = "t=20210829T162200&s=2663.58&fn=9288000100178241&i=112602&fp=3418609577&n=1";
    private static String DBCP_SERVICE_ID = "mockDbcpService";
    private static String VK_CLIENT_ID = "vk-client-service";

    private TestRunner runner;
    private String jsonData;
    private DataSource dataSource;
    private BaseVkClientService vkClient;

    @Mock
    private DBCPService dbcpService;

    @BeforeEach
    public void init() throws InterruptedException {
        MockitoAnnotations.openMocks(this);
    }

    private TestRunner getTestRunner(
            String vkClientId
    ) {
        final TestRunner runner = TestRunners.newTestRunner(VkCheckBackRegisterReceipt.class);
        runner.enforceReadStreamsClosed(false);
        runner.setProperty(VkCheckBackRegisterReceipt.VK_CLIENT, vkClientId);
        runner.setProperty(VkCheckBackRegisterReceipt.QR_STRING, "${qr}");
        return runner;
    }


    @Test
    @Ignore("Тестирование на реальных данных")
    public void realTest() throws InitializationException, IOException {
        LogManager.getRootLogger().setLevel(Level.TRACE);

        initTestRunner();
        readJsonData();
        initDataSource();
        mockDBCPService();
        initClient();
        initProcessor();
        runner.assertValid();

        runner.enqueue("Test".getBytes(StandardCharsets.UTF_8)); // This is to coax the processor into reading the data in the reader.
        runner.run();
        List<MockFlowFile> results = runner.getFlowFilesForRelationship(VkCheckBackRegisterReceipt.REL_SUCCESS);
        assertEquals(1, results.size(), "Wrong count, received " + results.size());

        System.out.println(new String(results.get(0).getData()));
        assertNotEquals("{\"qr_string\":[\"The qr string field is required.\"],\"source\":[\"The source field is required.\"]}"
                , new String(results.get(0).getData())
                , "Нерпавлиьный ответ");
    }

    private void initTestRunner() throws IOException {
        runner = TestRunners.newTestRunner(VkCheckBackRegisterReceipt.class);
        runner.setValidateExpressionUsage(false);
    }

    private void readJsonData() throws IOException {
        InputStream is = getClass().getClassLoader().getResourceAsStream("login.vk.com_02-04-2022.json");
        jsonData = IOUtils.toString(is, StandardCharsets.UTF_8);
    }

    private void initDataSource() {
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setURL("jdbc:postgresql://host.docker.internal:5432/postgres");
        ds.setUser("outlay");
        ds.setPassword("outlay");

        dataSource = ds;
    }

    private void mockDBCPService() throws InitializationException {
        when(dbcpService.getConnection()).then(invocation -> dataSource.getConnection());
        when(dbcpService.getIdentifier()).thenReturn(DBCP_SERVICE_ID);
        runner.addControllerService(DBCP_SERVICE_ID, dbcpService);
        runner.assertValid(dbcpService);
        runner.enableControllerService(dbcpService);
    }


    private void initClient() throws InitializationException {
        vkClient = new VkClientService();
        runner.addControllerService(VK_CLIENT_ID, vkClient);
        runner.setProperty(vkClient, "database-jdbc-connection-pool", DBCP_SERVICE_ID);
        runner.setProperty(vkClient, "database-schema", "vk-test");
        runner.setProperty(vkClient, "database-dialect", "POSTGRES");
        runner.setProperty(vkClient, "j2team-cookie", jsonData);
        runner.setProperty(vkClient, "cookie-encoder", "NO_ENCODER");
        runner.assertValid(vkClient);
        runner.enableControllerService(vkClient);
    }


    private void initProcessor() {
        runner.setProperty(VkCheckBackRegisterReceipt.VK_CLIENT, VK_CLIENT_ID);
        runner.setProperty(VkCheckBackRegisterReceipt.QR_STRING, QR_STRING);
    }


//    @Test
//    void test2() throws IOException {
//        HttpClient client = HttpClients.custom()
//                .disableCookieManagement()
//                .build();
//
//        HttpPost httppost = new HttpPost("TEST");
//        CookieStore cookieStore = new BasicCookieStore();
//        BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", "test");
//        //cookie.setDomain("your domain");
//        cookie.setPath("/");
//
//        cookieStore.addCookie(cookie);
//        client.setCookieStore(cookieStore);
//        response = client.execute(httppost);
//
//
//        HttpClients.createMinimal(new BasicHttpClientConnectionManager())
//        HttpClient client = HttpClients.custom()
//                .disableCookieManagement()
//                .build();
//
//        HttpUriRequest request = RequestBuilder.get()
//                .setConfig(RequestConfig.custom().setc.build())
//                .setUri("http://vk.com/")
//                .setHeader(HttpHeaders.CONTENT_TYPE, "application/json")
//                .build();
//        HttpResponse execute = client.execute(request);
//        String s = IOUtils.toString(execute.getEntity().getContent(), StandardCharsets.UTF_8);
//        System.out.println(s);
//    }

    @Test
    void test3() throws IOException, URISyntaxException {
        final BasicCookieStore cookieStore = new BasicCookieStore();
        try (final CloseableHttpClient httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .build()) {
            final HttpGet httpget = new HttpGet("https://someportal/");
            try (final CloseableHttpResponse response1 = httpclient.execute(httpget)) {
                final HttpEntity entity = response1.getEntity();

                System.out.println("Login form get: " + response1.getCode() + " " + response1.getReasonPhrase());
                EntityUtils.consume(entity);

                System.out.println("Initial set of cookies:");
                final List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i));
                    }
                }
            }

            final ClassicHttpRequest login = ClassicRequestBuilder.post()
                    .setUri(new URI("https://someportal/"))
                    .addParameter("IDToken1", "username")
                    .addParameter("IDToken2", "password")
                    .build();
            try (final CloseableHttpResponse response2 = httpclient.execute(login)) {
                final HttpEntity entity = response2.getEntity();

                System.out.println("Login form get: " + response2.getCode() + " " + response2.getReasonPhrase());
                EntityUtils.consume(entity);

                System.out.println("Post logon cookies:");
                final List<Cookie> cookies = cookieStore.getCookies();
                if (cookies.isEmpty()) {
                    System.out.println("None");
                } else {
                    for (int i = 0; i < cookies.size(); i++) {
                        System.out.println("- " + cookies.get(i));
                    }
                }
            }
        }
    }

}