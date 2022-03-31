package ru.ilyshkafox.nifi.vk.client.processors;

import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.exception.ProcessException;
import ru.ilyshkafox.nifi.vk.client.controllers.CheckBackClient;
import ru.ilyshkafox.nifi.vk.client.controllers.VkClient;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.ScanResponse;

import java.util.List;

public class VkCheckBackGetPageReceipt extends AbstractProcessor {

    static final PropertyDescriptor VK_CLIENT = new PropertyDescriptor.Builder()
            .name("client-vk")
            .displayName("Client Vk")
            .identifiesControllerService(VkClient.class)
            .required(true)
            .build();


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        VkClient vkClient = context.getProperty(VK_CLIENT).asControllerService(VkClient.class);
        CheckBackClient checkBackClient = vkClient.getCheckBackClient();
        ScanResponse scan = checkBackClient.getScan(1);
        List<ScanResponse.DataItem> items = scan.getResponse().getItems().getData();
        items.forEach(dataItem -> getLogger().info("Item: {}", dataItem));
    }
}
