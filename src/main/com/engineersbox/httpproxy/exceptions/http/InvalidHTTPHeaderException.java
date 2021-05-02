package com.engineersbox.httpproxy.exceptions.http;

public class InvalidHTTPHeaderException extends HTTPMessageException {
    public InvalidHTTPHeaderException(final String message) {
        super(message);
    }
}
