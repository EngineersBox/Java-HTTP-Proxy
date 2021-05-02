package com.engineersbox.httpproxy.exceptions.socket;

import com.engineersbox.httpproxy.exceptions.socket.SocketException;

public class SocketStreamReadError extends SocketException {

    public SocketStreamReadError(final String message) {
        super(message);
    }

    public SocketStreamReadError(final Throwable cause) {
        super(cause);
    }

}
