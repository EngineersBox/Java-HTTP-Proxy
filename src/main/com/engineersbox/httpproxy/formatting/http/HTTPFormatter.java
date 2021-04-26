package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.common.*;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.google.inject.Inject;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class HTTPFormatter<T extends HTTPStartLine> implements BaseHTTPFormatter<T> {

    private final Logger logger = LogManager.getLogger(HTTPFormatter.class);

    private final Config config;

    @Inject
    public HTTPFormatter(final Config config) {
        this.config = config;
    }

    @SuppressWarnings("unchecked")
    private T parseRequestStartLine(final String raw) throws InvalidStartLineFormatException, InvalidHTTPVersionException {
        final String[] segmentedRaw = raw.split(HTTPSymbols.START_LINE_DELIMITER);
        if (segmentedRaw.length != 3) {
            throw new InvalidStartLineFormatException("Expected 3 segements in start line got " + segmentedRaw.length + " for value: " + raw);
        }
        return (T) new HTTPRequestStartLine(
                HTTPMethod.valueOf(segmentedRaw[0]),
                segmentedRaw[1],
                HTTPVersion.fromRaw(segmentedRaw[2])
        );
    }

    @SuppressWarnings("unchecked")
    private T parseResponseStartLine(final String raw) throws InvalidStartLineFormatException, InvalidHTTPVersionException {
        final String[] segmentedRaw = raw.split(HTTPSymbols.START_LINE_DELIMITER);
        if (segmentedRaw.length != 3) {
            throw new InvalidStartLineFormatException("Expected 3 segements in start line got " + segmentedRaw.length + " for value: " + raw);
        }
        final int statusCode = Integer.parseInt(segmentedRaw[0]);
        if (statusCode < 100 || statusCode > 599) {
            throw new InvalidStartLineFormatException("Invalid status code " + statusCode + ", required to be in range 100-599");
        }
        return (T) new HTTPResponseStartLine(
                statusCode,
                segmentedRaw[1],
                HTTPVersion.fromRaw(segmentedRaw[2])
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
    public HTTPMessage<T> fromRaw(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException {
        final String[] splitMetadataBody = raw.split(HTTPSymbols.HTTP_NEWLINE_DELIMITER + HTTPSymbols.HTTP_NEWLINE_DELIMITER);
        if (splitMetadataBody.length > 2 || splitMetadataBody.length < 1) {
            throw new InvalidHTTPMessageFormatException("Expected two sections for HEADERS and BODY, got " + splitMetadataBody.length + " sections instead");
        }
        logger.trace("Validated message HEADERS and BODY sections exists");
        final String[] segmentedRaw = splitMetadataBody[0].split(HTTPSymbols.HTTP_NEWLINE_DELIMITER);
        if (segmentedRaw.length < 1) {
            throw new InvalidStartLineFormatException("Message segments was of count " + segmentedRaw.length + ", expected at least HTTP/0.9 compliant start line");
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
