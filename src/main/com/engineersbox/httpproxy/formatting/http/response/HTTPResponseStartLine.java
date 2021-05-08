package com.engineersbox.httpproxy.formatting.http.response;

import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.nio.charset.StandardCharsets;

/**
 * An <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1" target="_top">RFC 2616 Section 6.1</a> compliant
 * implementation of response line.
 */
public class HTTPResponseStartLine extends HTTPStartLine {

    public final int statusCode;
    public final String statusMessage;

    public HTTPResponseStartLine() {
        this(200, "Ok", HTTPVersion.HTTP11);
    }

    public HTTPResponseStartLine(final int statusCode, final String statusMessage, final HTTPVersion version) {
        super(version);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    /**
     * See {@link HTTPStartLine#toRaw()}
     *
     * @return {@code byte[]} format of response line
     */
    @Override
    public byte[] toRaw() {
        return String.join(
            HTTPSymbols.START_LINE_DELIMITER,
            this.version.version,
            String.valueOf(this.statusCode),
            this.statusMessage
        ).concat(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)
        .getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format(
            "{ version: %s, statusCode: %d, statusMessage: %s }",
            this.version.version,
            this.statusCode,
            this.statusMessage
        );
    }

    @Override
    public String toDisplayableString() {
        return String.format(
                "%s %s %s",
                this.version.version,
                this.statusCode,
                this.statusMessage
        );
    }

}
