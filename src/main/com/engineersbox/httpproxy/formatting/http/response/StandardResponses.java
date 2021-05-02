package com.engineersbox.httpproxy.formatting.http.response;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStatusCode;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.util.HashMap;
import java.util.Map;

public class StandardResponses {

    public static HTTPMessage<HTTPResponseStartLine> _200(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._200();
        message.body = body;
        return message;
    }

    public static HTTPMessage<HTTPResponseStartLine> _200() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Server", "HTTPProxy");
        return new HTTPMessage<>(
                new HTTPResponseStartLine(
                        HTTPStatusCode._200.code,
                        HTTPStatusCode._200.message,
                        HTTPVersion.HTTP11
                ),
                headers
        );
    }

    public static HTTPMessage<HTTPResponseStartLine> _404(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._404();
        message.body = body;
        return message;
    }

    public static HTTPMessage<HTTPResponseStartLine> _404() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Server", "HTTPProxy");
        return new HTTPMessage<>(
                new HTTPResponseStartLine(
                        HTTPStatusCode._404.code,
                        HTTPStatusCode._404.message,
                        HTTPVersion.HTTP11
                ),
                headers
        );
    }

    public static HTTPMessage<HTTPResponseStartLine> _500(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._500();
        message.body = body;
        return message;
    }

    public static HTTPMessage<HTTPResponseStartLine> _500() {
        final Map<String, String> headers = new HashMap<>();
        headers.put("Connection", "close");
        headers.put("Server", "HTTPProxy");
        return new HTTPMessage<>(
                new HTTPResponseStartLine(
                        HTTPStatusCode._500.code,
                        HTTPStatusCode._500.message,
                        HTTPVersion.HTTP11
                ),
                headers
        );
    }
}
