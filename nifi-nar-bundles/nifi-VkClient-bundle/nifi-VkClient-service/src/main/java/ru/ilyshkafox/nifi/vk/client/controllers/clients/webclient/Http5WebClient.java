package ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient;

import org.apache.commons.io.IOUtils;
import org.apache.hc.client5.http.classic.methods.HttpUriRequestBase;
import org.apache.hc.client5.http.cookie.CookieStore;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.BaseHttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.ContentType;
import ru.ilyshkafox.nifi.vk.client.controllers.clients.webclient.dto.HttpResponse;
import ru.ilyshkafox.nifi.vk.client.controllers.redirectstrategy.DisableRedirectStrategy;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Http5WebClient implements WebClient {
    private final CloseableHttpClient httpclient;


    public Http5WebClient(final CookieStore cookieStore) {
        httpclient = HttpClients.custom()
                .setDefaultCookieStore(cookieStore)
                .setRedirectStrategy(new DisableRedirectStrategy())
                .build();
    }


    @Override
    public HttpResponse send(String method, URI url, Map<String, String> headers, String body, ContentType contentType) {
        HttpUriRequestBase httpRequest = new HttpUriRequestBase(method, url);
        headers.forEach(httpRequest::addHeader);
        if (body != null) {
            httpRequest.setEntity(new StringEntity(body, getHttpContentType(contentType)));
        }

        HttpClientContext clientContext = HttpClientContext.create();

        try (final CloseableHttpResponse response1 = httpclient.execute(httpRequest, clientContext)
        ) {

            var request = clientContext.getRequest();
            var entity = response1.getEntity();

            final URI uri = request.getUri();
            final int code = response1.getCode();
            final String resultBody = IOUtils.toString(entity.getContent(), StandardCharsets.UTF_8);
            final Map<String, List<String>> headerMap = new HashMap<>();

            Arrays.stream(response1.getHeaders()).forEach(header -> headerMap.computeIfAbsent(header.getName(), s -> new ArrayList<>()).add(header.getValue()));
            EntityUtils.consume(entity);
            return new BaseHttpResponse(uri, code, resultBody, headerMap);

        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public HttpResponse send(String method, URI url, Map<String, String> headers, String body) {
        return send(method, url, headers, body, null);
    }

    @Override
    public HttpResponse send(String method, URI url, Map<String, String> headers) {
        return send(method, url, headers, null, null);
    }

    @Override
    public HttpResponse get(URI url) {
        return send("GET", url, Map.of(), null, null);
    }

    @Override
    public HttpResponse get(URI url, Map<String, String> headers) {
        return send("GET", url, headers, null, null);
    }

    @Override
    public HttpResponse post(URI url, Map<String, String> headers, String body, ContentType contentType) {
        return send("POST", url, headers, body, contentType);
    }

    @Override
    public HttpResponse post(URI url, Map<String, String> headers, String body) {
        return send("POST", url, headers, body, null);
    }

    @Override
    public HttpResponse put(URI url, Map<String, String> headers, String body, ContentType contentType) {
        return send("PUT", url, headers, body, contentType);
    }

    @Override
    public HttpResponse put(URI url, Map<String, String> headers, String body) {
        return send("PUT", url, headers, body, null);
    }

    @Override
    public HttpResponse delete(URI url, Map<String, String> headers) {
        return send("DELETE", url, headers, null, null);
    }


    @Override
    public void close() throws IOException {
        httpclient.close();
    }


    private org.apache.hc.core5.http.ContentType getHttpContentType(ContentType contentType) {
        switch (contentType) {
            case APPLICATION_ATOM_XML:
                return org.apache.hc.core5.http.ContentType.APPLICATION_ATOM_XML;
            case APPLICATION_FORM_URLENCODED:
                return org.apache.hc.core5.http.ContentType.APPLICATION_FORM_URLENCODED;
            case APPLICATION_JSON:
                return org.apache.hc.core5.http.ContentType.APPLICATION_JSON;
            case APPLICATION_SVG_XML:
                return org.apache.hc.core5.http.ContentType.APPLICATION_SVG_XML;
            case APPLICATION_XHTML_XML:
                return org.apache.hc.core5.http.ContentType.APPLICATION_XHTML_XML;
            case APPLICATION_XML:
                return org.apache.hc.core5.http.ContentType.APPLICATION_XML;
            case IMAGE_BMP:
                return org.apache.hc.core5.http.ContentType.IMAGE_BMP;
            case IMAGE_GIF:
                return org.apache.hc.core5.http.ContentType.IMAGE_GIF;
            case IMAGE_JPEG:
                return org.apache.hc.core5.http.ContentType.IMAGE_JPEG;
            case IMAGE_PNG:
                return org.apache.hc.core5.http.ContentType.IMAGE_PNG;
            case IMAGE_SVG:
                return org.apache.hc.core5.http.ContentType.IMAGE_SVG;
            case IMAGE_TIFF:
                return org.apache.hc.core5.http.ContentType.IMAGE_TIFF;
            case IMAGE_WEBP:
                return org.apache.hc.core5.http.ContentType.IMAGE_WEBP;
            case MULTIPART_FORM_DATA:
                return org.apache.hc.core5.http.ContentType.MULTIPART_FORM_DATA;
            case TEXT_HTML:
                return org.apache.hc.core5.http.ContentType.TEXT_HTML;
            case TEXT_PLAIN:
                return org.apache.hc.core5.http.ContentType.TEXT_PLAIN;
            case TEXT_XML:
                return org.apache.hc.core5.http.ContentType.TEXT_XML;
            default:
                throw new RuntimeException("Нереализованный ContentType");
        }
    }


}

