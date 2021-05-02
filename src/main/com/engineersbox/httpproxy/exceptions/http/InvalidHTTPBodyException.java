package com.engineersbox.httpproxy.exceptions.http;

public class InvalidHTTPBodyException extends HTTPMessageException {
    public InvalidHTTPBodyException(final String message) {
        super(message);
    }
}
