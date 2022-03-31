package ru.ilyshkafox.nifi.vk.client.controllers;

import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.controller.ControllerService;

@Tags({"client", "service", "vk", "cashback"})
@CapabilityDescription("Provides a basic connector to Accumulo services")
public interface BaseVkClientService extends ControllerService {
    CheckBackClient getCheckBackClient();
}
