package com.engineersbox.httpproxy.exceptions.http;

public class InvalidHTTPStartLineFormatException extends HTTPMessageException {

    public InvalidHTTPStartLineFormatException(final String message) {
        super(message);
    }
}
