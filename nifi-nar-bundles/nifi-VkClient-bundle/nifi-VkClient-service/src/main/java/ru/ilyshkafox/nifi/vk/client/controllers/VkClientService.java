package ru.ilyshkafox.nifi.vk.client.controllers;

import com.twocaptcha.captcha.Normal;
import lombok.Getter;
import org.apache.nifi.annotation.behavior.RequiresInstanceClassLoading;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.annotation.lifecycle.OnDisabled;
import org.apache.nifi.annotation.lifecycle.OnEnabled;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.controller.AbstractControllerService;
import org.apache.nifi.controller.ConfigurationContext;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;
import ru.ilyshkafox.nifi.vk.client.controllers.services.RuCaptchaService;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;

import static ru.ilyshkafox.nifi.vk.client.controllers.VkClientServiceProperty.*;

;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientService extends AbstractControllerService implements BaseVkClientService {
    private static final String VK_INDEX_PAGE = "https://m.vk.com/";
    private static final String VK_MENU_PAGE = "https://m.vk.com/menu";
    public static final String VK_CHECKBACK_PAGE = "https://m.vk.com/checkback?ref=catalog_recent#";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(VK_LOGIN, VK_PASSWORD, RUCAPTCHA_TOKEN, BROWSER_TYPE, BROWSER_URL);

    private WebDriver driver = null;
    private VkClientServiceProperty property = null;
    private RuCaptchaService solver = null;

    @OnEnabled
    public void init(final ConfigurationContext context) throws MalformedURLException {
        driver = createDriver();
        property = new VkClientServiceProperty(context);
        solver = new RuCaptchaService(property.getRucaptchaToken());
        login();
        openNull();
    }

    @OnDisabled
    public void shutdown(final ConfigurationContext context) {
        logout();
        driver.close();

        property = null;
        solver = null;
        driver = null;
    }


    @Override
    public String getCheckbackXVkSign() {
        try {
            if (!isLogin()) login();
            driver.get(VK_CHECKBACK_PAGE);
            return "?" + findElement(By.tagName("iframe")).getAttribute("src").split("\\?", 2)[1];
        } finally {
            openNull();
        }
    }


    private WebDriver createDriver() throws MalformedURLException {
        var driver = new RemoteWebDriver(new URL(property.getBrowserUrl()), property.getDriverType().getCapabilities());
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));
        return driver;
    }


    private void login() {
        if (isLogin()) return;
        driver.get(VK_INDEX_PAGE);
        findElement(By.linkText("Войти")).click();
        findElement(By.name("login")).sendKeys(property.getVkLogin());
        findElement(By.cssSelector(".vkc__Button__title")).click();

        checkCaptcha();
        try {
            findElement(By.cssSelector(".vkc__Bottom__switchToPassword > span")).click();
        } catch (Exception ignore) {

        }
        findElement(By.name("password")).sendKeys(property.getVkPassword());
        findElement(By.cssSelector(".vkc__Button__title > span")).click();
        sleep(1000);
    }

    public boolean isLogin() {
        driver.get(VK_INDEX_PAGE);
        return !driver.getPageSource().contains("Вход ВКонтакте");
    }

    private void logout() {
        driver.get(VK_MENU_PAGE);
        findElement(By.cssSelector("a[data-log-link='/logout']")).click();
        sleep(1000);
    }

    private void checkCaptcha() {
        Normal result = null;
        try {
            while (true) {
                WebElement captchaContainer = findElement(By.className("vkc__Captcha__container"));
                if (result != null) {
                    solver.report(result, false);
                }
                WebElement captchaImage = captchaContainer.findElement(By.className("vkc__Captcha__image"));
                WebElement textFieldInput = captchaContainer.findElement(By.className("vkc__TextField__input"));
                WebElement button = captchaContainer.findElement(By.cssSelector(".vkc__Captcha__button > button"));
                result = solver.solve(captchaImage.getAttribute("src"));
                textFieldInput.sendKeys(result.getCode());
                button.click();
                sleep(100);
            }
        } catch (NoSuchElementException ignore) {
            if (result != null) {
                solver.report(result, true);
            }
        }
    }


    private WebElement findElement(final By by) {
        int i = 0;
        while (true) {
            try {
                return driver.findElement(by);
            } catch (NoSuchElementException e) {
                i++;
                if (i > 3) {
                    throw e;
                }
                sleep(100);
            }
        }
    }


    private void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignore) {
        }
    }

    private void openNull() {
        try {
            driver.get("http://localhost/");
        } catch (Exception ignore) {
        }
    }

}
