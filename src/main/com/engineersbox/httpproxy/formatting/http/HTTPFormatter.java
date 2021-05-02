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

    @SuppressWarnings("unchecked")
    private T parseRequestStartLine(final String raw) throws InvalidHTTPStartLineFormatException, InvalidHTTPVersionException {
        final String[] segmentedRaw = formatSegmentedStartLine(raw);
        return (T) new HTTPRequestStartLine(
                HTTPMethod.valueOf(segmentedRaw[0]),
                segmentedRaw[1],
                HTTPVersion.fromRaw(segmentedRaw[2])
        );
    }

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

    private void validateBody(final String rawBody) throws InvalidHTTPBodyException {
        if (rawBody.length() > this.config.servlet.messages.maxBodySize) {
            throw new InvalidHTTPBodyException("Body is larger than configured maximum supported size: " + rawBody.length() + " > " + this.config.servlet.messages.maxBodySize);
        }
    }

    @Override
    public HTTPMessage<T> fromRaw(final byte[] raw, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        return fromRawString(
            new String(raw, charset),
            classOfT
        );
    }

    @Override
    public HTTPMessage<T> fromRaw(final byte[] raw, final byte[] bodyBytes, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        return fromRawString(
                new String(raw, charset),
                bodyBytes,
                classOfT
        );
    }

    @Override
    public HTTPMessage<T> fromRawString(final String raw, final byte[] bodyBytes, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        final HTTPMessage<T> message = fromRawString(raw, classOfT);
        message.withBodyBytes(bodyBytes);
        logger.trace("Added raw bytes to message");
        return message;
    }

    @Override
    public HTTPMessage<T> fromRawString(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        final String[] splitMetadataBody = raw.split(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER + HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER, 2);
        if (splitMetadataBody.length < 1) {
            throw new InvalidHTTPMessageFormatException("Expected two sections for HEADERS and BODY, got " + splitMetadataBody.length + " sections instead");
        }
        logger.trace("Validated message HEADERS and BODY sections exists");
        final String[] segmentedRaw = splitMetadataBody[0].split(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER);
        if (segmentedRaw.length < 1) {
            throw new InvalidHTTPStartLineFormatException("Message segments was of count " + segmentedRaw.length + ", expected at least HTTP/0.9 compliant start line");
        }
        logger.trace("Validated message HEADERS segment count");
        final T startLine;
        if (classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            startLine = parseRequestStartLine(segmentedRaw[0]);
        } else if (classOfT.isAssignableFrom(HTTPResponseStartLine.class)) {
            startLine = parseResponseStartLine(segmentedRaw[0]);
        } else {
            throw new InvalidHTTPMessageFormatException("Message was not a request or response: " + segmentedRaw[0]);
        }
        logger.trace("Validated message request/response type");
        if (startLine.version == HTTPVersion.HTTP09 || segmentedRaw.length == 1) {
            return new HTTPMessage<>(startLine);
        }

        final Map<String, String> headers = parseHeaders(ArrayUtils.subarray(segmentedRaw, 1, segmentedRaw.length - 1));
        if (splitMetadataBody.length < 2) {
            return new HTTPMessage<>(startLine, headers);
        }

        final String body = splitMetadataBody[1];
        validateBody(body);

        logger.debug("Successfully parsed message");
        return new HTTPMessage<>(startLine, headers, body);
    }

}
