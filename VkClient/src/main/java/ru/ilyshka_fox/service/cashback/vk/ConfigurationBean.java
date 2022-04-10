package ru.ilyshka_fox.service.cashback.vk;

import com.twocaptcha.TwoCaptcha;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Configuration
public class ConfigurationBean {
    @Bean
    public WebDriver getWebDriver() {
        WebDriverManager.chromedriver().setup();
        var driver = new ChromeDriver();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(10));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(5));
        return driver;
    }

    @Bean
    public TwoCaptcha getTwoCaptcha(@Value("${ru.ilyshka_fox.service.rucaptcha.api-key}") String apiKey) {
        return new TwoCaptcha(apiKey);
    }


    @Bean
    public WebClient getWebClient(@Value("${ru.ilyshka_fox.service.web-client.max-in-memory-size}") int maxInMemorySize) {
        final ExchangeStrategies strategies = ExchangeStrategies.builder()
                .codecs(codecs -> codecs.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .build();
        return WebClient.builder()
                .exchangeStrategies(strategies)
                .build();
    }
}
