package com.engineersbox.httpproxy.exceptions;

public class SocketException extends RuntimeException {

    public SocketException(final String message) {
        super(message);
    }

    public SocketException(final Throwable cause) {
        super(cause);
    }

    public SocketException(String message, Throwable cause) {
        super(message, cause);
    }
}
