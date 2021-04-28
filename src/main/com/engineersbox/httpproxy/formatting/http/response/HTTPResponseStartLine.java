package com.engineersbox.httpproxy.formatting.http.response;

import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.nio.charset.StandardCharsets;

public class HTTPResponseStartLine extends HTTPStartLine {

    public int statusCode;
    public String statusMessage;

    public HTTPResponseStartLine() {
        this(200, "Ok", HTTPVersion.HTTP11);
    }

    public HTTPResponseStartLine(final int statusCode, final String statusMessage, final HTTPVersion version) {
        super(version);
        this.statusCode = statusCode;
        this.statusMessage = statusMessage;
    }

    @Override
    public byte[] toRaw() {
        return String.join(
            HTTPSymbols.START_LINE_DELIMITER,
            this.version.version,
            String.valueOf(this.statusCode),
            this.statusMessage
        ).concat(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER).getBytes(StandardCharsets.UTF_8);
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

}
