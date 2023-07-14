package com.wdbyte.network.icp;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.ParseException;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

/**
 * HTTP 请求工具类，使用 apache httpclient
 */
@Slf4j
public class HttpUtil {

    public static String post(String url, String data, ContentType contentType, Map<String, String> headers) {
        String result = null;
        HttpPost httpPost = new HttpPost(url);
        // 设置超时时间
        RequestConfig config = RequestConfig.custom()
                .setConnectTimeout(Timeout.ofSeconds(2))
                .setConnectionRequestTimeout(Timeout.ofSeconds(2))
                .setResponseTimeout(Timeout.ofSeconds(30))
                .build();
        httpPost.setConfig(config);
        if (data != null) {
            httpPost.setEntity(new StringEntity(data, contentType));
        }
        if (headers != null) {
            for (Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            try (CloseableHttpResponse response = httpclient.execute(httpPost)) {
                // 获取响应信息
                result = EntityUtils.toString(response.getEntity());
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        log.info("http post url:{}, response:{}", url, result);
        return result;
    }
}