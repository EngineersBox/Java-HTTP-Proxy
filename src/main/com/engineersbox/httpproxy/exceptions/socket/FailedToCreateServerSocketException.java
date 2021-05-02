package com.engineersbox.httpproxy.exceptions.socket;

import com.engineersbox.httpproxy.exceptions.socket.SocketException;

public class FailedToCreateServerSocketException extends SocketException {

    public FailedToCreateServerSocketException(final String message, final Throwable cause) {
        super(message, cause);
    }

}
