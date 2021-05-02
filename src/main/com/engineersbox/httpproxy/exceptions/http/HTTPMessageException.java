package com.engineersbox.httpproxy.exceptions.http;

public class HTTPMessageException extends RuntimeException {

    public HTTPMessageException(final String message) {
        super(message);
    }

    public HTTPMessageException(final Throwable cause) {
        super(cause);
    }

    public HTTPMessageException(String message, Throwable cause) {
        super(message, cause);
    }

}
