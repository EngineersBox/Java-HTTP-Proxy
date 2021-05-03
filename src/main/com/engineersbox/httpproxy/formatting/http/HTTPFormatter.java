package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.http.*;
import com.engineersbox.httpproxy.formatting.http.common.*;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.google.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class HTTPFormatter<T extends HTTPStartLine> implements BaseHTTPFormatter<T> {

    private final Logger logger = LogManager.getLogger(HTTPFormatter.class);

    private final Config config;

    @Inject
    public HTTPFormatter(final Config config) {
        this.config = config;
    }

    /**
     * Convert a raw {@link String} format of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.1" target="_top">RFC 2616 Section 4.1</a>
     * message start line into an instance of {@link HTTPStartLine}.
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.1" target="_top">RFC 2616 Section 4.1</a> start line
     * @return An instance {@link HTTPStartLine} of as either {@link HTTPRequestStartLine} or {@link HTTPResponseStartLine}
     *  depending on whether it is a request or response respectively
     * @throws InvalidHTTPStartLineFormatException Division of headers and body is invalid
     */
    private String[] formatSegmentedStartLine(final String raw) throws InvalidHTTPStartLineFormatException {
        String[] segmentedRaw = raw.split(HTTPSymbols.START_LINE_DELIMITER);
        if (segmentedRaw.length < 3) {
            throw new InvalidHTTPStartLineFormatException("Expected 3 segments in start line got " + segmentedRaw.length + " for value: " + raw);
        } else if (segmentedRaw.length > 3) {
            segmentedRaw = new String[]{
                    segmentedRaw[0],
                    segmentedRaw[1],
                    String.join(HTTPSymbols.START_LINE_DELIMITER, ArrayUtils.subarray(segmentedRaw, 2, segmentedRaw.length)),

            };
        }
        return segmentedRaw;
    }

    /**
     * Convert a raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1" target="_top">RFC 2616 Section 5.1</a>
     *  message start line into an instance of {@link HTTPRequestStartLine}
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec5.html#sec5.1" target="_top">RFC 2616 Section 5.1</a> start line
     * @return An parsed instance of {@link HTTPRequestStartLine}
     * @throws InvalidHTTPStartLineFormatException Division of headers and body is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     */
    @SuppressWarnings("unchecked")
    private T parseRequestStartLine(final String raw) throws InvalidHTTPStartLineFormatException, InvalidHTTPVersionException {
        final String[] segmentedRaw = formatSegmentedStartLine(raw);
        return (T) new HTTPRequestStartLine(
                HTTPMethod.valueOf(segmentedRaw[0]),
                segmentedRaw[1],
                HTTPVersion.fromRaw(segmentedRaw[2])
        );
    }

    /**
     * Convert a raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1" target="_top">RFC 2616 Section 6.1</a>
     *  message start line into an instance of {@link HTTPResponseStartLine}
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec6.html#sec6.1" target="_top">RFC 2616 Section 6.1</a> start line
     * @return An parsed instance of {@link HTTPResponseStartLine}
     * @throws InvalidHTTPStartLineFormatException Start line is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     */
    @SuppressWarnings("unchecked")
    private T parseResponseStartLine(final String raw) throws InvalidHTTPStartLineFormatException, InvalidHTTPVersionException {
        final String[] segmentedRaw = formatSegmentedStartLine(raw);
        final int statusCode = Integer.parseInt(segmentedRaw[1]);
        if (statusCode < 100 || statusCode > 599) {
            throw new InvalidHTTPStartLineFormatException("Invalid status code " + statusCode + ", required to be in range 100-599");
        }
        return (T) new HTTPResponseStartLine(
                statusCode,
                segmentedRaw[2],
                HTTPVersion.fromRaw(segmentedRaw[0])
        );
    }

    /**
     * Convert a {@code String[]} of <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a>
     * headers into a {@link Map} of {@link String} to {@link String} key, value pairs.
     *
     * <br/><br/>
     *
     * This will ensure that the headers match the {@link HTTPSymbols#HEADER_REGEX} in order to be compliant with the format
     * of <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a>
     *
     * <br/><br/>
     *
     * @param raw {@code String[]} of headers
     * @return {@link Map} of {@link String} to {@link String} mappings of <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a>
     * compliant headers
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     */
    private Map<String, String> parseHeaders(final String[] raw) throws InvalidHTTPHeaderException {
        final Map<String, String> headers = new HashMap<>();
        for (final String rawHeader : raw) {
            if (!HTTPSymbols.HEADER_REGEX.matcher(rawHeader).matches()) {
                throw new InvalidHTTPHeaderException("Header does not match expected format or valid characters: " + rawHeader);
            }
            final String[] splitHeader = rawHeader.split(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
            headers.put(splitHeader[0], splitHeader[1]);
        }
        return headers;
    }

    /**
     * See {@link BaseHTTPFormatter#fromRaw(byte[], Charset, Class)}
     *
     * <br/><br/>
     *
     * @param raw {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param charset {@link Charset} to use as the encoding for the string representation of the body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body is malformed or contains illegal characters/encodings
     */
    @Override
    public HTTPMessage<T> fromRaw(final byte[] raw, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        return fromRawString(
            new String(raw, charset),
            classOfT
        );
    }

    /**
     * See {@link BaseHTTPFormatter#fromRaw(byte[], byte[], Charset, Class)}
     *
     * <br/><br/>
     *
     * @param raw {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param bodyBytes {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a> compliant message body
     * @param charset {@link Charset} to use as the encoding for the string representation of the body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body is malformed or contains illegal characters/encodings
     */
    @Override
    public HTTPMessage<T> fromRaw(final byte[] raw, final byte[] bodyBytes, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        return fromRawString(
                new String(raw, charset),
                bodyBytes,
                classOfT
        );
    }

    /**
     * See {@link BaseHTTPFormatter#fromRawString(String, byte[], Class)}
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param bodyBytes {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a> compliant message body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body is malformed or contains illegal characters/encodings
     */
    @Override
    public HTTPMessage<T> fromRawString(final String raw, final byte[] bodyBytes, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        final HTTPMessage<T> message = fromRawString(raw, classOfT);
        message.withBodyBytes(bodyBytes);
        logger.trace("Added raw bytes to message");
        return message;
    }

    /**
     * Validate and convert a raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4" target="_top">RFC 2616 Section 4</a>
     * HTTP message.
     *
     * <br/><br/>
     *
     * This will ensure that there is at least a header section and a body optionally depending on context.
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4" target="_top">RFC 2616 Section 4</a>
     *                          HTTP message
     * @return Split message containing either a header, header and body or header and multipart body
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     */
    private String[] parseMessageSegments(final String raw) throws InvalidHTTPMessageFormatException {
        final String[] splitMetadataBody = raw.split(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER + HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER, 2);
        if (splitMetadataBody.length < 1) {
            throw new InvalidHTTPMessageFormatException("Expected two sections for HEADERS and BODY, got " + splitMetadataBody.length + " sections instead");
        }
        logger.trace("Validated message HEADERS and BODY sections exists");
        return splitMetadataBody;
    }

    /**
     * Validate and convert a {@code String[]} of headers according to <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a>.
     * The headers will be split according to the standard delimiter {@link HTTPSymbols#HTTP_HEADER_NEWLINE_DELIMITER}.
     *
     * <br/><br/>
     *
     * If the headers are not compliant with at minimum HTTP/0.9 (single like method designation)
     *
     * <br/><br/>
     *
     * @param splitMetadataBody {@code String[]} of headers
     * @return Headers split according to {@link HTTPSymbols#HTTP_HEADER_NEWLINE_DELIMITER}
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     */
    private String[] parseHeadersSegment(final String[] splitMetadataBody) throws InvalidHTTPStartLineFormatException {
        final String[] segmentedRaw = splitMetadataBody[0].split(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER);
        if (segmentedRaw.length < 1) {
            throw new InvalidHTTPStartLineFormatException("Message segments was of count " + segmentedRaw.length + ", expected at least HTTP/0.9 compliant start line");
        }
        logger.trace("Validated message HEADERS segment count");
        return segmentedRaw;
    }

    /**
     * Validate and parse the response type, based on whether the class type parameter {@code T} is an instance of
     * {@link HTTPRequestStartLine} or {@link HTTPResponseStartLine}. These will call {@link HTTPFormatter#parseRequestStartLine(String)}
     * and  {@link HTTPFormatter#parseResponseStartLine(String)} respectively.
     *
     * <br/><br/>
     *
     * In the case that the message is neither a request nor response, this will throw an {@link InvalidHTTPMessageFormatException}
     *
     * <br/><br/>
     *
     * @param segmentedRaw {@code String[]} set of <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2" target="_top">RFC 2616 Section 4.2</a> headers
     * @param classOfT Instance of {@link HTTPStartLine} designating this message as a request or response
     * @return {@link HTTPRequestStartLine} or {@link HTTPResponseStartLine} depending on the class of the class type parameter {@code T}
     * @throws InvalidHTTPMessageFormatException Message is neither a valid request or response
     */
    private T parseResponseType(final String[] segmentedRaw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException {
        final T startLine;
        if (classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            startLine = parseRequestStartLine(segmentedRaw[0]);
        } else if (classOfT.isAssignableFrom(HTTPResponseStartLine.class)) {
            startLine = parseResponseStartLine(segmentedRaw[0]);
        } else {
            throw new InvalidHTTPMessageFormatException("Message was not a request or response: " + segmentedRaw[0]);
        }
        logger.trace("Validated message request/response type");
        return startLine;
    }

    /**
     *
     * @param rawBody
     * @return
     * @throws InvalidHTTPBodyException Body is malformed or contains illegal characters/encodings
     */
    private String parseBody(final String rawBody) throws InvalidHTTPBodyException {
        if (rawBody.length() > this.config.servlet.messages.maxBodySize) {
            throw new InvalidHTTPBodyException("Body is larger than configured maximum supported size: " + rawBody.length() + " > " + this.config.servlet.messages.maxBodySize);
        }
        logger.trace("Validated message body");
        return rawBody;
    }

    /**
     * See {@link BaseHTTPFormatter#fromRawString(String, Class)}
     *
     * <br/><br/>
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body is malformed or contains illegal characters/encodings
     */
    @Override
    public HTTPMessage<T> fromRawString(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        final String[] splitMetadataBody = parseMessageSegments(raw);
        final String[] segmentedRaw = parseHeadersSegment(splitMetadataBody);
        final T startLine = parseResponseType(segmentedRaw, classOfT);
        if (startLine.version == HTTPVersion.HTTP09 || segmentedRaw.length == 1) {
            return new HTTPMessage<>(startLine);
        }

        final Map<String, String> headers = parseHeaders(ArrayUtils.subarray(segmentedRaw, 1, segmentedRaw.length - 1));
        if (splitMetadataBody.length < 2) {
            return new HTTPMessage<>(startLine, headers);
        }

        final String body = parseBody(splitMetadataBody[1]);

        logger.debug("Successfully parsed message");
        return new HTTPMessage<>(startLine, headers, body);
    }

}
