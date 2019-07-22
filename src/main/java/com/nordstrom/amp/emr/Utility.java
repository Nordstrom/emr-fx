package com.nordstrom.amp.emr;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.net.MediaType;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class Utility {
    private final static Logger log = LoggerFactory.getLogger(Utility.class);
    private final static ObjectMapper objectMapper = new ObjectMapper();

    public static <T> T readValue(HttpResponse response, Class<T> type) throws IOException {
        String responseString = EntityUtils.toString(response.getEntity(), "UTF-8");

        T item = objectMapper.readValue(responseString, type);

        return item;
    }

    public static HttpResponse httpGet(List<Header> headers, String url, List<NameValuePair> params) {
        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(2 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        if (params != null && !params.isEmpty()) {
            String paramString = URLEncodedUtils.format(params, "utf-8");
            url += "?" + paramString;
        }

        log.info("httpGet url=[{}]", url);

        HttpGet request = new HttpGet(url);
        request.setConfig(requestConfig);

        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        HttpResponse response = ExponentialBackoff.execute(5, () -> {
            HttpResponse innerResponse = httpClient.execute(request);
            return innerResponse;

        });

        int statusCode = response.getStatusLine().getStatusCode();

        log.info("httpGet url=[{}] response={}", request.getURI(), statusCode);

        return response;
    }

    public static HttpResponse httpDelete(List<Header> headers, String url, List<NameValuePair> params) throws Exception {
        RequestConfig requestConfig = RequestConfig.copy(RequestConfig.DEFAULT)
                .setConnectTimeout(2 * 1000)
                .setConnectionRequestTimeout(10 * 1000)
                .build();

        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        if (params != null && !params.isEmpty()) {
            String paramString = URLEncodedUtils.format(params, "utf-8");
            url += "?" + paramString;
        }

        log.info("httpDelete url=[{}]", url);

        HttpDelete request = new HttpDelete(url);
        request.setConfig(requestConfig);

        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        HttpResponse response = ExponentialBackoff.execute(5, () -> {
            HttpResponse innerResponse = httpClient.execute(request);
            return innerResponse;

        });

        int statusCode = response.getStatusLine().getStatusCode();

        log.info("httpDelete url=[{}] response={}", request.getURI(), statusCode);

        return response;
    }

    public static HttpResponse httpPost(List<Header> headers, String url, String payload) throws Exception {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost request = new HttpPost(url);

        if (headers != null) {
            headers.forEach(request::addHeader);
        }

        StringEntity input = new StringEntity(payload);
        input.setContentType("application/json");
        request.setEntity(input);

        HttpResponse response = httpClient.execute(request);

        int statusCode = response.getStatusLine().getStatusCode();

        HttpEntity entity = response.getEntity();
        String responseString = EntityUtils.toString(entity, "UTF-8");

        log.info("httpPost url=[{}] responseCode={} response=[{}] payload={}", request.getURI(), statusCode, responseString, payload);

        return response;
    }

    public static HttpResponse postJsonAndBlobAsMultipart(String url, String apiVersion, String jsonContent,
                                                   byte[] blobContent, MediaType mediaType) throws IOException {

        MultipartEntityBuilder builder = MultipartEntityBuilder.create();

        if (jsonContent != null) {
            builder.addTextBody("json", jsonContent, ContentType.create("application/json"));
        }

        if (blobContent != null) {
            builder.addBinaryBody("file", blobContent, ContentType.create(mediaType.toString()), "Image file");
        }

        HttpEntity entity = builder.build();

        HttpClient httpClient = HttpClientBuilder.create().build();

        HttpPost httpPost = new HttpPost(url);
        httpPost.setEntity(entity);

        HttpResponse response = httpClient.execute(httpPost);
        log.info("postJsonAndBlobAsMultipart url=[{}] response={}", httpPost.getURI(), response.getStatusLine().getStatusCode());

        return response;
    }
}
