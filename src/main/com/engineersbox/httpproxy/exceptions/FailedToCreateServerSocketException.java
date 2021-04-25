package com.engineersbox.httpproxy.exceptions;

public class FailedToCreateServerSocketException extends RuntimeException {

    public FailedToCreateServerSocketException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
