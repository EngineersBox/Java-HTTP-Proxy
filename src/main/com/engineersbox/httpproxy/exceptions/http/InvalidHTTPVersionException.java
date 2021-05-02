package com.engineersbox.httpproxy.exceptions.http;

public class InvalidHTTPVersionException extends HTTPMessageException {

    public InvalidHTTPVersionException(final String message) {
        super(message);
    }

}
