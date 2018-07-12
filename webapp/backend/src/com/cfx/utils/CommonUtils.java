package com.cfx.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.Header;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {

    private static final Logger logger = LoggerFactory.getLogger(CommonUtils.class);

    private static List<NameValuePair> toNameValuePair(Map<String, String> nameValuePairs) {
        final List<NameValuePair> l = new ArrayList<NameValuePair>();
        if (nameValuePairs != null) {
            final Set<String> keys = nameValuePairs.keySet();
            for (String key : keys) {
                l.add(new BasicNameValuePair(key, nameValuePairs.get(key)));
            }
        }
        return l;
    }

    public static String executeGet(final String url, final Map<String, String> requestHeaders)
            throws CustomHttpClientException, IOException {

        CustomHttpClient customHttpClient = CustomHttpClient.prepareCustomHttpClient();
        customHttpClient.buildHttpClient();
        final List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        if (requestHeaders != null) {
            for (final Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        return customHttpClient.executeGet(url, headers);
    }

    public static String executePut(final String url, final String json, final Map<String, String> requestHeaders,
            final Map<String, String> parameters) throws CustomHttpClientException, IOException {

        CustomHttpClient customHttpClient = CustomHttpClient.prepareCustomHttpClient();
        customHttpClient.buildHttpClient();
        final List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        if (requestHeaders != null) {
            for (final Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        return customHttpClient.executePut(url, headers, toNameValuePair(parameters), json);
    }

    public static String executeDelete(final String url, final String json, final Map<String, String> requestHeaders,
            final Map<String, String> parameters) throws CustomHttpClientException, IOException {

        CustomHttpClient customHttpClient = CustomHttpClient.prepareCustomHttpClient();
        customHttpClient.buildHttpClient();
        final List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        if (requestHeaders != null) {
            for (final Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        return customHttpClient.executeDelete(url, headers, toNameValuePair(parameters), json);
    }

    public static String executePost(final String url, final String json, final Map<String, String> requestHeaders,
            final Map<String, String> parameters) throws CustomHttpClientException, IOException {

        CustomHttpClient customHttpClient = CustomHttpClient.prepareCustomHttpClient();
        customHttpClient.buildHttpClient();
        final List<Header> headers = new ArrayList<>();
        headers.add(new BasicHeader("Content-Type", "application/json"));
        if (requestHeaders != null) {
            for (final Map.Entry<String, String> entry : requestHeaders.entrySet()) {
                headers.add(new BasicHeader(entry.getKey(), entry.getValue()));
            }
        }
        return customHttpClient.executePost(url, headers, toNameValuePair(parameters), json);
    }

}