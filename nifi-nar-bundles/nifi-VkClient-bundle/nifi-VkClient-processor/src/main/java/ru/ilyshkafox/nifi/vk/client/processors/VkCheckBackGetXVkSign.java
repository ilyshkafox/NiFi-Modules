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
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import ru.ilyshkafox.nifi.vk.client.controllers.BaseVkClientService;

import java.util.List;
import java.util.Set;

@Tags({"ilyshka", "vk", "client", "checkback", "receipt", "custom"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@WritesAttribute(attribute = "X-vk-sign", description = "Токер авторизации в кешбек приложении")
@CapabilityDescription("Получаем токен авторизации))")
public class VkCheckBackGetXVkSign extends AbstractProcessor {

    static final PropertyDescriptor VK_CLIENT = new PropertyDescriptor.Builder()
            .name("client-vk")
            .displayName("Client Vk")
            .identifiesControllerService(BaseVkClientService.class)
            .required(true)
            .build();


    static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("FlowFiles that are successfully transformed will be routed to this relationship")
            .build();


    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(VK_CLIENT);

    @Getter
    public Set<Relationship> relationships = Set.of(REL_SUCCESS);


    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        var service = context.getProperty(VK_CLIENT).asControllerService(BaseVkClientService.class);
        String xVkSign = service.getCheckbackXVkSign();
        session.putAttribute(flowFile, "X-vk-sign", xVkSign);
        session.transfer(flowFile, REL_SUCCESS);
    }
}
