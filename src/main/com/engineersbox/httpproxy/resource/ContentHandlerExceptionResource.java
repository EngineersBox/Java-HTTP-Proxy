package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.http.HTTPMessageException;
import com.engineersbox.httpproxy.exceptions.socket.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.formatting.http.response.StandardResponses;
import com.engineersbox.httpproxy.resolver.annotation.ExceptionHandler;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.google.inject.Inject;

@Handler(HandlerType.EXCEPTION)
public class ContentHandlerExceptionResource {

    private final Config config;

    @Inject
    public ContentHandlerExceptionResource(final Config config) {
        this.config = config;
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(FailedToCreateServerSocketException.class)
    public HTTPMessage<HTTPResponseStartLine> handleSocketException(final FailedToCreateServerSocketException socketException) {
        return StandardResponses._500(String.format(
                "Could not connect to host %s:%d",
                this.config.target.host,
                this.config.target.port
        ));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(SocketStreamReadError.class)
    public HTTPMessage<HTTPResponseStartLine> handleSocketStreamReadException(final SocketStreamReadError streamReadError) {
        return StandardResponses._500(String.format(
                "Unable to read response from socket: %s",
                streamReadError.getMessage()
        ));
    }

    @SuppressWarnings("unused")
    @ExceptionHandler(HTTPMessageException.class)
    public HTTPMessage<HTTPResponseStartLine> handleHTTPException(final HTTPMessageException messageException) {
        return StandardResponses._500(messageException.getMessage());
    }

}
