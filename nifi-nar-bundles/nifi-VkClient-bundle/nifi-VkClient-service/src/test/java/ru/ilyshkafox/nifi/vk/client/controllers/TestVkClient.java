package ru.ilyshkafox.nifi.vk.client.controllers;

import org.apache.nifi.processor.Processor;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;

import static java.lang.Thread.sleep;

class TestVkClient {

    private TestRunner runner;

    @Mock
    private Processor dummyProcessor;

    @Test
    @Disabled
     void testClient() throws Exception {
        String login = System.getenv("vk_login");
        String password = System.getenv("vk_password");
        String rucaptchaToken = System.getenv("rucaptcha_token");

        try (var ignored = MockitoAnnotations.openMocks(this);
             var vkClientService = new VkClientService()) {


            runner = TestRunners.newTestRunner(dummyProcessor);
            runner.addControllerService("vkClientService", vkClientService);
            runner.setProperty(vkClientService, VkClientServiceProperty.VK_LOGIN, login);
            runner.setProperty(vkClientService, VkClientServiceProperty.VK_PASSWORD, password);
            runner.setProperty(vkClientService, VkClientServiceProperty.RUCAPTCHA_TOKEN, rucaptchaToken);
            runner.assertValid(vkClientService);
            runner.enableControllerService(vkClientService);

            vkClientService.getCheckbackXVkSign();
            vkClientService.getCheckbackXVkSign();

        }
    }
}