package com.engineersbox.httpproxy.exceptions.http;

public class CompressionHandlerException extends RuntimeException {

    public CompressionHandlerException(final String message) {
        super(message);
    }

    public CompressionHandlerException(final String message, final Throwable e) {
        super(message, e);
    }

}
