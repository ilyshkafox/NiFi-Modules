package ru.ilyshkafox.nifi.controllers.cashback.vk;

//import lombok.extern.slf4j.Slf4j;
import org.apache.nifi.annotation.behavior.RequiresInstanceClassLoading;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.apache.nifi.reporting.InitializationException;

//@Slf4j
@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientServiceImpl extends AbstractControllerService implements VkClientService {
    @OnEnabled
    public void onConfigured(final ConfigurationContext context) throws InitializationException {
//        log.info("Starting properties file service");
    }

    @OnDisabled
    public void shutdown() {

    }

    @Override
    public boolean isLogin() {
        return false;
    }
}
