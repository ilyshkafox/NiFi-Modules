package ru.ilyshkafox.nifi.vk.client.controllers;

import com.twocaptcha.captcha.Normal;
import io.github.bonigarcia.wdm.WebDriverManager;
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
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import ru.ilyshkafox.nifi.vk.client.controllers.services.RuCaptchaService;

import java.io.Closeable;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.openqa.selenium.support.ui.ExpectedConditions.*;
import static ru.ilyshkafox.nifi.vk.client.controllers.VkClientServiceProperty.*;

;

@RequiresInstanceClassLoading
@Tags({"ilyshkafox", "client", "vk", "cashback"})
@CapabilityDescription("Клиент подключения к VK.")
public class VkClientService extends AbstractControllerService implements BaseVkClientService, Closeable {
    private static final String VK_INDEX_PAGE = "https://m.vk.com/";
    private static final String VK_MENU_PAGE = "https://m.vk.com/menu";
    public static final String VK_CHECKBACK_PAGE = "https://m.vk.com/checkback?ref=catalog_recent#";

    @Getter
    private final List<PropertyDescriptor> supportedPropertyDescriptors = List.of(VK_LOGIN, VK_PASSWORD, RUCAPTCHA_TOKEN);

    private WebDriver driver;
    private WebDriverWait waitLong;
    private WebDriverWait waitOneSecond;

    private VkClientServiceProperty property;
    private RuCaptchaService solver;


    @OnEnabled
    public void init(final ConfigurationContext context) {
        property = new VkClientServiceProperty(context);
        solver = new RuCaptchaService(property.getRucaptchaToken());
        initDriver(true);
        login();
    }

    private void initDriver(boolean inDocker) {
        WebDriverManager.chromedriver().setup();
        var opt = new ChromeOptions();

        if (inDocker) {
            opt.addArguments("start-maximized");            // Откройте браузер в развернутом режиме
            opt.addArguments("disable-infobars");           // Отключение информационных панелей
            opt.addArguments("--disable-extensions");       // Отключение расширений
            opt.addArguments("--disable-gpu");              // Применимо только к ОС Windows
            opt.addArguments("--disable-dev-shm-usage");    // Преодоление проблем с ограниченными ресурсами
            opt.addArguments("--no-sandbox");               // Обход модели безопасности операционной системы
            opt.addArguments("--headless");                 // Запуск без интерфейса
        }
        opt.addArguments("--lang=en");                  // Сстандартный англиский язык

        driver = new ChromeDriver(opt);
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));

        waitLong = new WebDriverWait(driver, Duration.ofSeconds(10));
        waitOneSecond = new WebDriverWait(driver, Duration.ofSeconds(1));
    }

    @OnDisabled
    public void shutdown(final ConfigurationContext context) {
        close();
    }

    @Override
    public void close() {
        if (driver != null) {
            logout();
            driver.close();
        }
        driver = null;
        solver = null;
        property = null;
        waitLong = null;
        waitOneSecond = null;
    }


    private void login() {
        if (isLogin()) return;
        getLogger().info("VK Login...");
        driver.get(VK_INDEX_PAGE);
        loginPageStart();
        loginPageLogin();
        loginValidateAndResolveBlock();
        loginPagePassword();

        waitLong.until(urlToBe("https://m.vk.com/feed"));
        getLogger().info("VK Login Finish");
    }

    private void loginPageStart() {
        visibilityAndClickable(By.cssSelector("a[href=\"/join?vkid_auth_type=sign_in\"] ")).click();
    }

    private void loginPageLogin() {
        visibilityAndClickable(By.name("login")).sendKeys(property.getVkLogin());
        driver.findElement(By.cssSelector("button[type=\"submit\"]")).click();
    }


    private void loginValidateAndResolveBlock() {
        if (!loginIsPagePassword()) {
            if (loginIsPageCode(waitOneSecond)) {
                driver.findElement(By.cssSelector(".vkc__Bottom__switchToPassword > span")).click();
            } else if (loginIsPageCaptcha(waitOneSecond)) {
                checkCaptcha();
            }
        }
    }

    private boolean loginIsPagePassword() {
        try {
            waitLong.until(visibilityOfElementLocated(By.name("password")));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private boolean loginIsPageCode(WebDriverWait wait) {
        try {
            wait.until(visibilityOfElementLocated(By.cssSelector(".vkc__Bottom__switchToPassword > span")));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private boolean loginIsPageCaptcha(WebDriverWait wait) {
        try {
            wait.until(visibilityOfElementLocated(By.className("vkc__Captcha__container")));
            return true;
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private void loginPagePassword() {
        visibilityAndClickable(By.name("password")).sendKeys(property.getVkPassword());
        driver.findElement(By.cssSelector("button[type=\"submit\"]")).click();
    }


    public boolean isLogin() {
        driver.get(VK_INDEX_PAGE);
        try {
            return waitOneSecond.until(urlContains("/feed"));
        } catch (TimeoutException ex) {
            return false;
        }
    }

    private void logout() {
        if (!isLogin()) return;
        driver.get(VK_MENU_PAGE);
        WebElement bnt = waitLong.until(visibilityOfElementLocated(By.cssSelector("a[data-log-link='/logout']")));
        if (bnt != null) {
            bnt.click();
        }

        try {
            waitLong.until(visibilityOfElementLocated(By.name("login")));
        } catch (TimeoutException e) {
            waitOneSecond.until(visibilityOfElementLocated(By.className("screen_login")));
        }
    }

    private void checkCaptcha() {
        Normal result = null;

        while (true) {
            try {
                visibility(By.className("vkc__Captcha__container"));
            } catch (TimeoutException e) {
                report(result, true);
                return;
            }
            report(result, false);
            WebElement captchaImage = visibility(By.className("vkc__Captcha__image"));
            WebElement textFieldInput = visibilityAndClickable(By.className("vkc__TextField__input"));
            WebElement button = visibilityAndClickable(By.cssSelector(".vkc__Captcha__button > button"));
            result = solver.solve(captchaImage.getAttribute("src"));
            textFieldInput.sendKeys(result.getCode());
            button.click();
        }
    }

    private void report(Normal result, boolean correct) {
        if (result != null) {
            solver.report(result, correct);
        }
    }

    private WebElement visibilityAndClickable(By by) {
        waitLong.until(visibilityOfElementLocated(by));
        waitLong.until(elementToBeClickable(by));
        return driver.findElement(by);
    }

    private WebElement visibility(By by) {
        return waitLong.until(visibilityOfElementLocated(by));
    }

    // ==============================================================================

    private String xAuthToken;
    private OffsetDateTime lastUpdateXAuthToken = OffsetDateTime.MIN;

    @Override
    public String getCheckbackXVkSign() {
        if (OffsetDateTime.now().minusMinutes(10).isAfter(lastUpdateXAuthToken)) {
            xAuthToken = loadPageAndGetCheckbackXVkSign();
            lastUpdateXAuthToken = OffsetDateTime.now();
        }
        return xAuthToken;
    }

    public String loadPageAndGetCheckbackXVkSign() {
        login();
        driver.get(VK_CHECKBACK_PAGE);
        return "?" + visibility(By.tagName("iframe")).getAttribute("src").split("\\?", 2)[1];
    }

}
