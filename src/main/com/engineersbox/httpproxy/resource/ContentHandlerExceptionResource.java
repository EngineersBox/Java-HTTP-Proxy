package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.exceptions.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.exceptions.SocketException;
import com.engineersbox.httpproxy.exceptions.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.ExceptionHandler;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;

@Handler(HandlerType.EXCEPTION)
public class ContentHandlerExceptionResource {

    @ExceptionHandler({
            SocketStreamReadError.class,
            FailedToCreateServerSocketException.class
    })
    public HTTPMessage<HTTPResponseStartLine> handleSocketException(final SocketException SocketException) {
        return null;
    }

}
