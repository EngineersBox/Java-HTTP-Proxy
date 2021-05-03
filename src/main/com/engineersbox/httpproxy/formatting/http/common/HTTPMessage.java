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

/**
 * An implementation of <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4" target="_top">RFC 2616 Section 4</a> compliant HTTP message.
 *
 * <br/><br/>
 *
 * @param <T> An instance of {@link HTTPStartLine} indicating whether the message is a request or response
 * @see com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine HTTPRequestStartLine
 * @see com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine HTTPResponseStartLine
 */
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

    /**
     * Join together a set of headers with a delimiter into a single string. This will include an extra instance of
     * the delimiter at the end of the headers. Since this is an <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>
     * compliant implementation, it is assumed that this will be {@link HTTPSymbols#HTTP_HEADER_NEWLINE_DELIMITER} as
     * the specified delimiter.
     *
     * <br/><br/>
     *
     * Note that usage outside of the RFC is not included, though still possible. As such care will need to be taken
     * with the duplicate ending delimiter.
     *
     * <br/><br/>
     *
     * @param delimiter A string value used to separate headers
     * @return {@link String} containing delimited headers with duplicate delimiter at the end
     */
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

    /**
     * Retrieves the {@link Charset} from a {@code Content-Type} header if it exists. In the case that it is not present as a
     * part of the header key (Specified by {@link HTTPSymbols#CONTENT_TYPE_CHARSET_KEY}), this will default to
     * {@link StandardCharsets#UTF_8}
     *
     * <br/><br/>
     *
     * @return {@link Charset} if it exists, otherwise {@link StandardCharsets#UTF_8}
     */
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

    /**
     * Retrieves the body of the message, compressing if the {@code Content-Encoding} header exists with anything other
     * than a value of {@code Identity}.
     *
     * <br/><br/>
     *
     * The body content will be encoded with the {@link Charset} provided by the {@code Content-Type} header if it exists,
     * otherwise will use the default, {@link StandardCharsets#UTF_8}.
     *
     * <br/><br/>
     *
     * Note that this reader supports automatic compression of the following formats, done after having identified the {@code Content-Type} header
     *
     * <ul>
     *     <li>{@code GZIP}: Lempel-Ziv coding (LZ77) with a 32 bit CRC (<a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a>).</li>
     *     <li>{@code compress}: Adaptive Lempel-Ziv-Welch coding (LZW)</li>
     *     <li>
     *         {@code deflate}: zlib format (<a href="https://www.w3.org/Protocols/rfc1950/rfc1950.html" target="_top">RFC 1950</a>)
     *           with deflate compression mechanism (<a href="https://www.w3.org/Protocols/rfc1951/rfc1951.html" target="_top">RFC 1951</a>).
     *     </li>
     * </ul>
     *
     * @return {@code byte[]} containing an encoded body of compressed or uncompressed format
     */
    private byte[] getBody() {
        final String contentTypeHeader = this.headers.get(HTTPSymbols.CONTENT_TYPE_HEADER);
        if (contentTypeHeader != null && !HTTPSymbols.CONTENT_TYPE_TEXT_TYPE_REGEX.matcher(contentTypeHeader).find()) {
            return this.bodyBytes;
        }
        if (body == null) {
            return new byte[0];
        }
        if (!this.headers.containsKey(HTTPSymbols.CONTENT_ENCODING_HEADER)
            || this.headers.get(HTTPSymbols.CONTENT_ENCODING_HEADER).contains(HTTPSymbols.CONTENT_ENCODING_IDENTITY)) {
            return body.getBytes(getCharset());
        }
        try {
            return GZIPCompression.compress(this.body, getCharset());
        } catch (IOException e) {
            logger.error(e, e);
        }
        return body.getBytes(getCharset());
    }

    /**
     * Convert the current {@link HTTPMessage} to {@code byte[]}, encoding the body and compressing as need be.
     *
     * <br/><br/>
     *
     * The structure of the bytes will be directly equivalent to converting any <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>
     * compliant message.
     *
     * <br/><br/>
     *
     * @return {@code byte[]} representation of the current {@link HTTPMessage}
     */
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

    /**
     * Set the current body raw bytes to a new value
     *
     * <br/><br/>
     *
     * @param bodyBytes {@code byte[]} representation of an <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>
     *                                compliant body
     */
    public void withBodyBytes(final byte[] bodyBytes) {
        this.bodyBytes = bodyBytes.clone();
    }

    /**
     * Set the current string representation of body to a new value
     *
     * <br/><br/>
     *
     * @param newBody {@link String} representation of an <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>
     *                compliant body
     */
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
