package com.engineersbox.httpproxy.formatting.http.request;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMethod;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.nio.charset.StandardCharsets;

/**
 * An <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1" target="_top">RFC 2616 Section 5.1</a> compliant
 * implementation of request line.
 */
public class HTTPRequestStartLine extends HTTPStartLine {

    public final HTTPMethod method;
    public final String target;

    public HTTPRequestStartLine(){
        this(HTTPMethod.GET, "/", HTTPVersion.HTTP11);
    }

    public HTTPRequestStartLine(final HTTPMethod method, final String target, final HTTPVersion version) {
        super(version);
        this.method = method;
        this.target = target;
    }

    /**
     * See {@link HTTPStartLine#toRaw()}
     *
     * @return {@code byte[]} format of request line
     */
    @Override
    public byte[] toRaw() {
        return String.join(
            HTTPSymbols.START_LINE_DELIMITER,
            this.method.toString(),
            this.target,
            this.version.version
        ).concat(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)
        .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format(
            "{ method: %s, target: %s, version: %s }",
            this.method,
            this.target,
            this.version.version
        );
    }

    @Override
    public String toDisplayableString() {
        return String.format(
            "%s %s %s",
            this.method,
            this.target,
            this.version.version
        );
    }
}
