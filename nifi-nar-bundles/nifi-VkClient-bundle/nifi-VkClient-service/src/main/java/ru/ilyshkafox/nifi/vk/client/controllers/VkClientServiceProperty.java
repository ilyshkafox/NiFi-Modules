package ru.ilyshkafox.nifi.vk.client.controllers;

import lombok.RequiredArgsConstructor;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.expression.ExpressionLanguageScope;
import org.apache.nifi.processor.util.StandardValidators;

@RequiredArgsConstructor
public class VkClientServiceProperty {


    public static final PropertyDescriptor VK_LOGIN = new PropertyDescriptor.Builder()
            .name("vk-login")
            .displayName("VK Login")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();
    public static final PropertyDescriptor VK_PASSWORD = new PropertyDescriptor.Builder()
            .name("vk-password")
            .displayName("VK Password")
            .required(true)
            .sensitive(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor RUCAPTCHA_TOKEN = new PropertyDescriptor.Builder()
            .name("rucaptcha-token")
            .displayName("Rucaptcha token")
            .description("Токен с сайта rucaptcha.com ")
            .required(true)
            .sensitive(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();

    public static final PropertyDescriptor BROWSER_TYPE = new PropertyDescriptor.Builder()
            .name("browser-driver")
            .displayName("Тип браузера")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.NONE)
            .addValidator(StandardValidators.NON_EMPTY_VALIDATOR)
            .allowableValues(DriveType.values())
            .build();


    public static final PropertyDescriptor BROWSER_URL = new PropertyDescriptor.Builder()
            .name("browser-url")
            .displayName("Ссылка на контейнер с браузером")
            .required(true)
            .expressionLanguageSupported(ExpressionLanguageScope.VARIABLE_REGISTRY)
            .addValidator(StandardValidators.NON_BLANK_VALIDATOR)
            .build();
    //==============================================================================================

    private final ConfigurationContext context;


    public String getVkLogin() {
        return context.getProperty(VK_LOGIN).getValue();
    }

    public String getVkPassword() {
        return context.getProperty(VK_PASSWORD).getValue();
    }

    public String getRucaptchaToken() {
        return context.getProperty(RUCAPTCHA_TOKEN).getValue();
    }


    public DriveType getDriverType() {
        return DriveType.valueOf(context.getProperty(BROWSER_TYPE).getValue());
    }

    public String getBrowserUrl() {
        return context.getProperty(BROWSER_URL).getValue();
    }

}
