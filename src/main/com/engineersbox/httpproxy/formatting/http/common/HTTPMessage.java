package com.engineersbox.httpproxy.formatting.http.common;

import org.apache.commons.lang3.ArrayUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPMessage<T extends HTTPStartLine> {

    public T startLine;
    public Map<String, String> headers;
    public String body;

    public HTTPMessage(final T startLine) {
        this(startLine, new HashMap<>());
    }

    public HTTPMessage(final T startLine, final Map<String, String> headers) {
        this(startLine, headers, null);
    }

    public HTTPMessage(final T startLine, final Map<String, String> headers, final String body) {
        this.startLine = startLine;
        this.headers = headers;
        this.body = body;
    }

    private String headersToString(final String delimiter) {
        final StringBuilder sb = new StringBuilder();
        for (final Map.Entry<String, String> header: this.headers.entrySet()) {
            sb.append(header.getKey()).append(": ").append(header.getValue()).append(delimiter);
        }
        return sb.append(delimiter).toString();
    }

    private byte[] concatAll(final byte[] ...bytes) {
        if (bytes.length < 1) {
            return new byte[]{};
        }
        byte[] returnableBytes = new byte[]{};
        for (final byte[] bytesElem : bytes) {
            returnableBytes = ArrayUtils.addAll(
                returnableBytes,
                bytesElem
            );
        }
        return returnableBytes;
    }

    public byte[] toRaw() {
        final byte[] startLineBytes = this.startLine.toRaw();
        final byte[] headersBytes = headersToString(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)
                .getBytes(StandardCharsets.UTF_8);
        final byte[] bodyBytes = body == null ? new byte[0] : body.getBytes(StandardCharsets.UTF_8);
        return concatAll(
            startLineBytes,
            headersBytes,
            bodyBytes
        );
    }

    @Override
    public String toString() {
        return String.format(
            "{ startLine: %s, headers: { %s}, body: %s }",
            this.startLine,
            headersToString(" "),
            this.body
        );
    }

}
