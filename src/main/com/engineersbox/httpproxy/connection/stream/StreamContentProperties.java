package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.formatting.compression.CompressionFormat;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Utility class for storing indicators required for reading an {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}
 * from a {@link java.io.InputStream}
 */
public class StreamContentProperties {

    public boolean pastHeaders;
    public final boolean hasContentLengthHeader;
    public boolean hasTransferEncodingHeader;
    public boolean isCompressed;
    public boolean isRaw;
    public Charset charset;
    public CompressionFormat compressionFormat;
    private final Class<? extends HTTPStartLine> classOfT;

    public StreamContentProperties(final Class<? extends HTTPStartLine> classOfT) {
        this.pastHeaders = false;
        this.hasContentLengthHeader = false;
        this.hasTransferEncodingHeader = false;
        this.compressionFormat = null;
        this.isRaw = false;
        this.isCompressed = false;
        this.charset = StandardCharsets.UTF_8;
        this.classOfT = classOfT;
    }

    /**
     * Check if a read should terminate based on whether is is a request and all headers have been read (indicated by
     * a double <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a> compliant line
     * terminator ({@code \r\n\r\n}).
     *
     * <br/><br/>
     *
     * @return {@code true} is a request and past headers, {@code false} otherwise
     */
    private boolean validateRequestTermination() {
        return !this.classOfT.isAssignableFrom(HTTPRequestStartLine.class) || !this.pastHeaders;
    }

    /**
     * Check if the previously read line contained a {@link HTTPSymbols#HEADER_KEY_VALUE_DELIMITER} and is a request.
     *
     * <br/><br/>
     *
     * @param line Previously read line
     * @return {@code true} if is a request and has a {@link HTTPSymbols#HEADER_KEY_VALUE_DELIMITER}, {@code false} otherwise
     */
    public boolean validateLineRead(final String line) {
        if (this.classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            return !line.equals(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
        }
        return line != null;
    }

    /**
     * Check if the body is chunked ({@code Content-Encoding: chunked}), and if the current line contains the chunked
     * body terminator ({@link HTTPSymbols#TRANSFER_ENCODING_TERMINATION})
     *
     * <br/><br/>
     *
     * @param line Currently read line
     * @return {@code false} if not chunked, null or not past chunked terminator, {@code true} otherwise
     */
    private boolean validateChunkedEnd(final String line) {
        return !(this.pastHeaders
                && line != null
                && this.hasTransferEncodingHeader
                && HTTPSymbols.TRANSFER_ENCODING_SIZE_REGEX.matcher(line).matches()
                && line.equals(HTTPSymbols.TRANSFER_ENCODING_TERMINATION));
    }

    /**
     * Check whether the currently read line is valid
     *
     * @param line Currently read line
     * @return {@code true} if valid, {@code false} otherwise
     */
    public boolean validateNextRead(final String line) {
        return validateChunkedEnd(line)
            && validateRequestTermination();
    }
}
