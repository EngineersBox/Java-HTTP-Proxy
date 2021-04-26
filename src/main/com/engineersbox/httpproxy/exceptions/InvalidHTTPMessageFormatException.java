package com.engineersbox.httpproxy.exceptions;

public class InvalidHTTPMessageFormatException extends Exception {

    public InvalidHTTPMessageFormatException(final String message) {
        super(message);
    }
}
