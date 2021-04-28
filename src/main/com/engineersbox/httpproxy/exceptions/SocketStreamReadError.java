package com.engineersbox.httpproxy.exceptions;

public class SocketStreamReadError extends RuntimeException{

    public SocketStreamReadError(final String message) {
        super(message);
    }

    public SocketStreamReadError(final Throwable cause) {
        super(cause);
    }

}