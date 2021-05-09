package com.engineersbox.httpproxy.formatting.http.common;

/**
 * Subset HTTP status codes defined in <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a>
 */
public enum HTTPStatusCode {
    _200(200, "Ok"),
    _404(404, "Not found"),
    _408(408, "Request Timeout"),
    _500(500, "Internal server error");

    public final int code;
    public final String message;

    HTTPStatusCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
}
