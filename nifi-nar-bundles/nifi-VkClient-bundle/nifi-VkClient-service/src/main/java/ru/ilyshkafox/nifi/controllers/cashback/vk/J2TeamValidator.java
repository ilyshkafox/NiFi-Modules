package ru.ilyshkafox.nifi.controllers.cashback.vk;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.nifi.components.ValidationResult;
import org.apache.nifi.components.Validator;
import ru.ilyshkafox.nifi.controllers.cashback.vk.dao.J2TeamCookies;
import ru.ilyshkafox.nifi.controllers.cashback.vk.utils.Assert;

import java.util.List;
import java.util.stream.Collectors;

public class J2TeamValidator {
    public final static List<String> ALLOW_URL = List.of("https://login.vk.com");
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static final Validator JSON_FORMAT_VALIDATOR = (subject, input, context) -> {
        final ValidationResult.Builder builder = new ValidationResult.Builder();
        builder.subject(subject).input(input);
        if (context.isExpressionLanguageSupported(subject) && context.isExpressionLanguagePresent(input)) {
            return builder.valid(true).explanation("Contains Expression Language").build();
        }

        try {
            J2TeamCookies j2TeamCookies = objectMapper.readValue(input, J2TeamCookies.class);
            Assert.isTrue(ALLOW_URL.contains(j2TeamCookies.getUrl()), "Необходимо куки от login VK. Получено: \"" + j2TeamCookies.getUrl() + "\", разрешено: " + ALLOW_URL.stream().collect(Collectors.joining("\", \"", "[\"", "\"]")) + ".");
            Assert.notEmpty(j2TeamCookies.getCookies(), "Не найдены куки для VK. Пустой Json");

            builder.valid(true);
        } catch (final JsonProcessingException e) {
            builder.valid(false).explanation(e.getMessage());
        }

        return builder.build();
    };


}
