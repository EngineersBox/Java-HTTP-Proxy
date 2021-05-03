package com.engineersbox.httpproxy.formatting.http.response;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStatusCode;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.util.HashMap;
import java.util.Map;

/**
 * Collection of standard HTTP responses compliant with <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1.1" target="_top">RFC 2616 Section 6.1.1</a>
 */
public class StandardResponses {

    /**
     * Create an HTTP 200 (Ok) response with a custom body
     *
     * <br/><br/>
     *
     * @param body Content to use as the body of the response
     * @return HTTP 200 formatted {@link HTTPMessage}
     */
    public static HTTPMessage<HTTPResponseStartLine> _200(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._200();
        message.body = body;
        return message;
    }

    /**
     * Create an HTTP 200 (Ok) response
     *
     * <br/><br/>
     *
     * @return HTTP 200 formatted {@link HTTPMessage}
     */
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

    /**
     * Create an HTTP 404 (Not Found) response with a custom body
     *
     * <br/><br/>
     *
     * @param body Content to use as the body of the response
     * @return HTTP 404 formatted {@link HTTPMessage}
     */
    public static HTTPMessage<HTTPResponseStartLine> _404(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._404();
        message.body = body;
        return message;
    }

    /**
     * Create an HTTP 404 (Not found) response
     *
     * <br/><br/>
     *
     * @return HTTP 404 formatted {@link HTTPMessage}
     */
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

    /**
     * Create an HTTP 500 (Internal server error) response with a custom body
     *
     * <br/><br/>
     *
     * @param body Content to use as the body of the response
     * @return HTTP 500 formatted {@link HTTPMessage}
     */
    public static HTTPMessage<HTTPResponseStartLine> _500(final String body) {
        final HTTPMessage<HTTPResponseStartLine> message = StandardResponses._500();
        message.body = body;
        return message;
    }

    /**
     * Create an HTTP 500 (Internal server error) response
     *
     * <br/><br/>
     *
     * @return HTTP 500 formatted {@link HTTPMessage}
     */
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
