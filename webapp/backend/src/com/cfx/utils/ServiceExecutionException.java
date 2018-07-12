package com.cfx.utils;

@SuppressWarnings("serial")
public class ServiceExecutionException extends Exception {

    public ServiceExecutionException(String message) {
        super(message);
    }

    public ServiceExecutionException(Throwable e) {
        super(e);
    }

    public ServiceExecutionException(String message, Throwable e) {
        super(message, e);
    }

}
