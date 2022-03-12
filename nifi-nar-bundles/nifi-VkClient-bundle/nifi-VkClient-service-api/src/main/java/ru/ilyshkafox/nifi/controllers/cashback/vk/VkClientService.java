package ru.ilyshkafox.nifi.controllers.cashback.vk;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

@Tags({"client", "service", "vk", "cashback"})
@CapabilityDescription("Provides a basic connector to Accumulo services")
public interface VkClientService extends ControllerService {
    boolean isLogin();
}
