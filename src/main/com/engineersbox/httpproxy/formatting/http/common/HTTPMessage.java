package com.engineersbox.httpproxy.formatting.http.common;

import com.engineersbox.httpproxy.formatting.content.GZIPCompression;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class HTTPMessage<T extends HTTPStartLine> {

    private final Logger logger = LogManager.getLogger(HTTPMessage.class);

    public final T startLine;
    public final Map<String, String> headers;
    public String body;
    public byte[] bodyBytes;

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
        this.bodyBytes = body == null ? null : body.getBytes();
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

    private Charset getCharset() {
        if (!this.headers.containsKey(HTTPSymbols.CONTENT_TYPE_HEADER)) {
            return StandardCharsets.UTF_8;
        }
        final String contentTypeHeader = this.headers.get(HTTPSymbols.CONTENT_TYPE_HEADER);
        if (!contentTypeHeader.contains(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)) {
            return StandardCharsets.UTF_8;
        }
        final String contentTypeCharset = contentTypeHeader.split(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)[1];
        return  Charset.forName(StringUtils.removeEnd(contentTypeCharset, HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER));
    }

    private byte[] getBody() {
        final String contentTypeHeader = this.headers.get(HTTPSymbols.CONTENT_TYPE_HEADER);
        if (contentTypeHeader != null && !HTTPSymbols.CONTENT_TYPE_TEXT_TYPE_REGEX.matcher(contentTypeHeader).matches()) {
            return this.bodyBytes;
        }
        if (body == null) {
            return new byte[0];
        }
        if (!this.headers.containsKey(HTTPSymbols.CONTENT_ENCODING_HEADER)
            || !this.headers.get(HTTPSymbols.CONTENT_ENCODING_HEADER).contains(HTTPSymbols.CONTENT_ENCODING_GZIP_KEY)) {
            return body.getBytes(getCharset());
        }
        try {
            return GZIPCompression.zip(this.body, getCharset());
        } catch (IOException e) {
            logger.error(e, e);
        }
        return body.getBytes(getCharset());
    }

    public byte[] toRaw() {
        final byte[] bb = getBody();
        this.headers.put(HTTPSymbols.CONTENT_LENGTH_HEADER, String.valueOf(bb.length));
        return concatAll(
                this.startLine.toRaw(),
                headersToString(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)
                        .getBytes(StandardCharsets.UTF_8),
                bb
        );
    }

    public void withBodyBytes(final byte[] bodyBytes) {
        this.bodyBytes = bodyBytes.clone();
    }

    public void setBody(final String newBody) {
        this.body = newBody;
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
