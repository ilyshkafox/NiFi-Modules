package ru.ilyshka_fox.service.cashback.vk.services.impl;

import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.Normal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.ilyshka_fox.service.cashback.vk.services.VkWebService;

import javax.annotation.PostConstruct;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@Service
@RequiredArgsConstructor
public class VkWebServiceImpl implements VkWebService, Closeable {
    public static final String VK_INDEX_PAGE = "https://m.vk.com/";
    public static final String VK_MENU_PAGE = "https://m.vk.com/menu";

    private final TwoCaptcha solver;
    private final WebDriver driver;
    @Value("${ru.ilyshka_fox.service.vk.login}")
    private final String login;
    @Value("${ru.ilyshka_fox.service.vk.password}")
    private final String password;


    @PostConstruct
    public void init() {
        login();
        if (!isLogin()) {
            throw new RuntimeException("Пользователь VK не авторизирован!");
        }
    }
    // =======================================================

    public synchronized boolean isLogin() {
        open(VK_INDEX_PAGE);
        return !driver.getPageSource().contains("Вход ВКонтакте");
    }


    // =======================================================

    private synchronized void logout() {
        open(VK_MENU_PAGE);
        findElement(By.cssSelector("a[data-log-link='/logout']")).click();
        sleep(1000);
    }

    private synchronized void login() {
        if (isLogin()) logout();
        open(VK_INDEX_PAGE);
        findElement(By.linkText("Войти")).click();

        findElement(By.name("login")).sendKeys(login);
        findElement(By.cssSelector(".vkc__Button__title")).click();
        checkCaptcha();
        try {
            findElement(By.cssSelector(".vkc__Bottom__switchToPassword > span")).click();
        } catch (Exception ignore) {

        }
        findElement(By.name("password")).sendKeys(password);
        findElement(By.cssSelector(".vkc__Button__title > span")).click();
        sleep(1000);
    }

    private void checkCaptcha() {
        Normal result = null;
        try {
            while (true) {
                WebElement captchaContainer = findElement(By.className("vkc__Captcha__container"));
                if (result != null) {
                    report(result, false);
                }
                WebElement captchaImage = captchaContainer.findElement(By.className("vkc__Captcha__image"));
                WebElement textFieldInput = captchaContainer.findElement(By.className("vkc__TextField__input"));
                WebElement button = captchaContainer.findElement(By.cssSelector(".vkc__Captcha__button > button"));
                result = ruCaptcha(captchaImage.getAttribute("src"));
                textFieldInput.sendKeys(result.getCode());
                button.click();
                sleep(100);
            }
        } catch (NoSuchElementException ignore) {
            if (result != null) {
                report(result, true);
            }
        }
    }

    public void open(String url) {
        driver.get(url);
        sleep(100);
    }

    private Normal ruCaptcha(String src) {
        try {
            URL url = new URL(src);
            InputStream imgStream = new BufferedInputStream(url.openStream());
            byte[] bytes = IOUtils.toByteArray(imgStream);
            String encodedFile = Base64.encodeBase64String(bytes);

            Normal captcha = new Normal();
            captcha.setBase64(encodedFile);
            captcha.setCaseSensitive(true);
            captcha.setLang("en");


            solver.solve(captcha);
            return captcha;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void report(Normal captcha, Boolean correct) {
        try {
            solver.report(captcha.getId(), correct);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void close() throws IOException {
        logout();
    }


    public WebElement findElement(By by) {
        return findElement(by, 3);
    }

    private WebElement findElement(By by, int retry) {
        int i = 0;
        while (true) {
            try {
                return driver.findElement(by);
            } catch (NoSuchElementException e) {
                i++;
                if (i > retry) {
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


}
