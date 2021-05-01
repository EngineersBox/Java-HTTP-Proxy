package com.engineersbox.httpproxy.formatting.http.common;

public enum HTTPStatusCode {
    _200(200, "Ok"),
    _404(404, "Not found"),
    _500(500, "An internal error occured");

    public final int code;
    public final String message;

    HTTPStatusCode(final int code, final String message) {
        this.code = code;
        this.message = message;
    }
}
