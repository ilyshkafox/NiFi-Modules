package ru.ilyshkafox.nifi.vk.client.processors;

import lombok.Getter;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import ru.ilyshkafox.nifi.vk.client.controllers.CheckBackClient;
import ru.ilyshkafox.nifi.vk.client.controllers.BaseVkClientService;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.ScanResponse;

import java.util.List;

@Tags({"ilyshka", "vk", "client", "checkback", "receipt", "custom"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@WritesAttribute(attribute = "", description = "")
@CapabilityDescription("Поулчем первую страницу с чеками CheckBack сервиса от VK и выводит в логи))")
public class VkCheckBackGetPageReceipt extends AbstractProcessor {

    static final PropertyDescriptor VK_CLIENT = new PropertyDescriptor.Builder()
            .name("client-vk")
            .displayName("Client Vk")
            .identifiesControllerService(BaseVkClientService.class)
            .required(true)
            .build();


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(VK_CLIENT);


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        BaseVkClientService vkClient = context.getProperty(VK_CLIENT).asControllerService(BaseVkClientService.class);
        CheckBackClient checkBackClient = vkClient.getCheckBackClient();
        ScanResponse scan = checkBackClient.getScan(1);
        List<ScanResponse.DataItem> items = scan.getResponse().getItems().getData();
        items.forEach(dataItem -> getLogger().info("Item: {}", dataItem));
    }
}
