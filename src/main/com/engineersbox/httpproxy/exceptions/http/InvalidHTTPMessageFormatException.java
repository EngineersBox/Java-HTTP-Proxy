package com.engineersbox.httpproxy.exceptions.http;

public class InvalidHTTPMessageFormatException extends HTTPMessageException {

    public InvalidHTTPMessageFormatException(final String message) {
        super(message);
    }
}
