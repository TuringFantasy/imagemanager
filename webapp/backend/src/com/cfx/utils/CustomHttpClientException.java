package com.cfx.utils;

@SuppressWarnings("serial")
public class CustomHttpClientException extends Exception {

    public CustomHttpClientException(String msg) {
        super(msg);
    }

    public CustomHttpClientException(String msg, Throwable t) {
        super(msg, t);
    }

}
