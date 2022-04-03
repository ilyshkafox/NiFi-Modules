package ru.ilyshkafox.nifi.vk.client.processors;

import lombok.Getter;
import org.apache.nifi.annotation.behavior.InputRequirement;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.util.StandardValidators;
import ru.ilyshkafox.nifi.vk.client.controllers.BaseVkClientService;
import ru.ilyshkafox.nifi.vk.client.controllers.dto.PostScanResponse;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@Tags({"ilyshka", "vk", "client", "checkback", "receipt", "put", "custom"})
@InputRequirement(InputRequirement.Requirement.INPUT_REQUIRED)
@WritesAttribute(attribute = "", description = "")
@CapabilityDescription("Отправляем информацию о чеке в CheckBack.")
public class VkCheckBackRegisterReceipt extends AbstractProcessor {
     static final PropertyDescriptor VK_CLIENT = new PropertyDescriptor.Builder()
            .name("client-vk")
            .displayName("Client Vk")
            .identifiesControllerService(BaseVkClientService.class)
            .required(true)
            .build();

     static final PropertyDescriptor QR_STRING = new PropertyDescriptor.Builder()
            .name("qr_string")
            .displayName("Qr String")
            .description("Срока защифрованная в QR коде!")
            .expressionLanguageSupported(ExpressionLanguageScope.FLOWFILE_ATTRIBUTES)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .required(true)
            .build();

     static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Чек отправлен, ответ получен.")
            .build();

     static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Получена Java ошибка при отправке сообщения.")
            .build();

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(VK_CLIENT, QR_STRING);

    @Getter
    public final Set<Relationship> relationships = Set.of(REL_SUCCESS, REL_FAILURE);

    @Override
    public void onTrigger(ProcessContext context, ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }
        try {
            var vkClient = context.getProperty(VK_CLIENT).asControllerService(BaseVkClientService.class);
            var qrString = context.getProperty(QR_STRING).evaluateAttributeExpressions(flowFile).getValue();

            var checkBackClient = vkClient.getCheckBackClient();
            checkBackClient.postScan(qrString);
            PostScanResponse scan = checkBackClient.postScan(qrString);

            session.write(flowFile, out -> out.write(scan.getResponseString().getBytes(StandardCharsets.UTF_8)));
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            session.putAttribute(flowFile, "error.msg", e.getMessage());
            session.putAttribute(flowFile, "error.stacktrace", Arrays.toString(e.getStackTrace()));
            session.transfer(flowFile, REL_FAILURE);
        }
    }
}
