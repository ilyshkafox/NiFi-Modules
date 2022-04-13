package ru.ilyshkafox.nifi.vk.client.controllers.services;

import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.Normal;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.URL;

public class RuCaptchaService {
    private final TwoCaptcha solver;

    public RuCaptchaService(final String apiKey) {
        this.solver = new TwoCaptcha(apiKey);
    }


    public Normal solve(String src) {
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

    public void report(Normal captcha, Boolean correct) {
        try {
            solver.report(captcha.getId(), correct);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
