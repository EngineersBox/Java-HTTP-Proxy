package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StreamContentProperties {

    public boolean passedHeaders;
    public final boolean hasContentLengthHeader;
    public boolean hasTransferEncodingHeader;
    public boolean isCompressed;
    public boolean isRaw;
    public Charset charset;
    private final Class<? extends HTTPStartLine> classOfT;

    public StreamContentProperties(final Class<? extends HTTPStartLine> classOfT) {
        this.passedHeaders = false;
        this.hasContentLengthHeader = false;
        this.hasTransferEncodingHeader = false;
        this.isCompressed = false;
        this.isRaw = false;
        this.charset = StandardCharsets.UTF_8;
        this.classOfT = classOfT;
    }

    private boolean validateRequestTermination() {
        return !this.classOfT.isAssignableFrom(HTTPRequestStartLine.class) || !this.passedHeaders;
    }

    public boolean validateLineRead(final String line) {
        if (this.classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            return !line.equals(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
        }
        return line != null;
    }

    private boolean validateChunkedEnd(final String line) {
        return !(this.passedHeaders
                && line != null
                && this.hasTransferEncodingHeader
                && HTTPSymbols.TRANSFER_ENCODING_SIZE_REGEX.matcher(line).matches()
                && line.equals(HTTPSymbols.TRANSFER_ENCODING_TERMINATION));
    }

    public boolean validateNextRead(final String line) {
        return validateChunkedEnd(line)
            && validateRequestTermination();
    }
}
