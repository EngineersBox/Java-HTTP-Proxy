package com.engineersbox.httpproxy.exceptions;

public class InvalidHTTPHeaderException extends Exception {
    public InvalidHTTPHeaderException(final String message) {
        super(message);
    }
}
