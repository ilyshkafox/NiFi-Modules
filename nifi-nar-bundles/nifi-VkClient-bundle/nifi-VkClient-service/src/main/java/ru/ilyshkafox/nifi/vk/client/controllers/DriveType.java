package ru.ilyshkafox.nifi.vk.client.controllers;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.ie.InternetExplorerOptions;
import org.openqa.selenium.safari.SafariOptions;

@Getter
@RequiredArgsConstructor
public enum DriveType {
    CROME(new ChromeOptions()),
    EDGE(new EdgeOptions()),
    FIREFOX(new FirefoxOptions()),
    SAFARI(new SafariOptions()),
    INTERNET_EXPLORER(new InternetExplorerOptions()),
    //    CHROMIUM(new ChromiumOptions()),
    //    OPERA(new OperaOptions()),
    ;


    private final Capabilities capabilities;
}
