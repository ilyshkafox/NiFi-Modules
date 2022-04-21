package ru.ilyshkafox.nifi.vk.client.controllers;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

import java.time.Duration;

class TestVkWebLogin {
    private static final String VK_INDEX_PAGE = "https://m.vk.com/";
    private static final String VK_MENU_PAGE = "https://m.vk.com/menu";
    public static final String VK_CHECKBACK_PAGE = "https://m.vk.com/checkback?ref=catalog_recent#";

    @Test
    void testLogin() throws Exception {
        String login = System.getenv("vk_login");
        String password = System.getenv("vk_password");
        String rucaptchaToken = System.getenv("rucaptcha_token");


//        WebDriverManager.chromedriver().setup();
//        DefaultWebDriver driver = null;
//        try {
//            driver = new DefaultWebDriver(false);
//            driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
//            driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));
//
//            driver.get(VK_INDEX_PAGE);
//            driver.findElement(By.linkText("Sign in")).click();
//            Thread.sleep(0);
//            driver.findElement(By.name("login")).sendKeys(login);
//            Thread.sleep(0);
//            driver.findElement(By.cssSelector(".vkc__Button__title")).click();
//
//            try {
//                driver.findElement(By.cssSelector(".vkc__Bottom__switchToPassword > span")).click();
//            } catch (Exception ignore) {
//            }
//            driver.findElement(By.name("password")).sendKeys(password);
//            driver.findElement(By.cssSelector(".vkc__Button__title > span")).click();
//
//        } finally {
//            if (driver != null) {
//                driver.quit();
//            }
//        }


    }
}