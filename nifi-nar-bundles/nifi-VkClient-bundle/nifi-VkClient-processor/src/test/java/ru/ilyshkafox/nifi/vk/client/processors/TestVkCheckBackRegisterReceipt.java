package ru.ilyshkafox.nifi.vk.client.processors;

import org.apache.nifi.reporting.InitializationException;
import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Ignore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockitoAnnotations;
import ru.ilyshkafox.nifi.vk.client.controllers.VkClientService;
import ru.ilyshkafox.nifi.vk.client.controllers.VkClientServiceProperty;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TestVkCheckBackRegisterReceipt {
    private static String VK_CLIENT_ID = "vk-client-service";

    private TestRunner runner;
    private VkClientService vkClient;

    @BeforeEach
    public void init() {
        MockitoAnnotations.openMocks(this);
    }

//    @Test
//    @Ignore("Тестирование на реальных данных")
    public void realTest() throws InitializationException {
        try {
            runner = TestRunners.newTestRunner(VkCheckBackGetXVkSign.class);
            runner.setValidateExpressionUsage(false);

            initClient();

            runner.setProperty(VkCheckBackGetXVkSign.VK_CLIENT, VK_CLIENT_ID);

            runner.assertValid();

            runner.enqueue("Test".getBytes(StandardCharsets.UTF_8)); // This is to coax the processor into reading the data in the reader.
            runner.run();
            List<MockFlowFile> results = runner.getFlowFilesForRelationship(VkCheckBackGetXVkSign.REL_SUCCESS);
            assertEquals(1, results.size(), "Wrong count, received " + results.size());
        } finally {
            runner.disableControllerService(vkClient);
        }
    }

    private void initClient() throws InitializationException {
        vkClient = new VkClientService();
        runner.addControllerService(VK_CLIENT_ID, vkClient);
        runner.setProperty(vkClient, VkClientServiceProperty.VK_LOGIN, "ilya_moslov@mail.ru");
        runner.setProperty(vkClient, VkClientServiceProperty.VK_PASSWORD, "");
        runner.setProperty(vkClient, VkClientServiceProperty.RUCAPTCHA_TOKEN, "9b65c6f3637309baff75b0d5758d0b0b");
        runner.assertValid(vkClient);
        runner.enableControllerService(vkClient);
    }
}