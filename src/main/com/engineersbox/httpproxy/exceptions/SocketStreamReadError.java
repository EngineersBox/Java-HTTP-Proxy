package com.engineersbox.httpproxy.exceptions;

public class SocketStreamReadError extends SocketException {

    public SocketStreamReadError(final String message) {
        super(message);
    }

    public SocketStreamReadError(final Throwable cause) {
        super(cause);
    }

}
