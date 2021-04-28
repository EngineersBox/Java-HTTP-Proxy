package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import org.apache.commons.lang3.CharSet;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

public class StreamCollector<T extends HTTPStartLine> implements ContentCollector<T> {

    private final Logger logger = LogManager.getLogger(ContentCollector.class);

    private final Config config;
    private final BaseHTTPFormatter<T> httpFormatter;

    private InputStream stream;
    private Class<T> classOfT;

    @Inject
    public StreamCollector(final Config config, final BaseHTTPFormatter<T> httpFormatter) {
        this.config = config;
        this.httpFormatter = httpFormatter;
    }

    @Override
    public void withStream(final InputStream stream) {
        this.stream = stream;
    }

    @Override
    public void withStartLine(final Class<T> classOfT) {
        this.classOfT = classOfT;
    }

    private boolean validateRequestTermination(final boolean passedHeaders) {
        if (classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            return !passedHeaders;
        }
        return true;
    }

    private boolean validateLineRead(final String line) {
        if (classOfT.isAssignableFrom(HTTPRequestStartLine.class)) {
            return !line.equals(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
        }
        return line != null;
    }

    private void sensitizedStreamRead(final StringBuilder sb, final BufferedReader br) throws IOException {
        int read = 0;
        int maxRead = Integer.MAX_VALUE;
        String line;
        boolean passedHeaders = false;
        final CRLFRetentiveLineReader retentiveLineReader = new CRLFRetentiveLineReader(br);
        while (read < maxRead
                && validateRequestTermination(passedHeaders)
                && validateLineRead(line = retentiveLineReader.readLine(maxRead))) {
            if (!passedHeaders && line.contains(HTTPSymbols.CONTENT_LENGTH_HEADER)) {
                final String contentLengthHeader = line.split(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER)[1];
                maxRead = Integer.parseInt(StringUtils.removeEnd(contentLengthHeader, HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER));
            }
            sb.append(line);
            if (!passedHeaders) {
                if (line.equals(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)) {
                    passedHeaders = true;
                }
                continue;
            }
            read += line.getBytes(StandardCharsets.UTF_8).length;
        }
        logger.debug("Read " + sb.toString().getBytes(StandardCharsets.UTF_8).length + " bytes from stream");
    }

    private String handlePaddedPrefix(final StringBuilder sb) {
        if (!CharSet.ASCII_ALPHA.contains(sb.charAt(0))) {
            return sb.substring(1);
        }
        return sb.toString();
    }

    @Override
    public HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError {
        final BufferedReader br = new BufferedReader(
                new InputStreamReader(stream),
                this.config.servlet.connections.readerBufferSize
        );
        final StringBuilder sb = new StringBuilder();
        try {
            sensitizedStreamRead(sb, br);
        } catch (final IOException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
        try {
            return this.httpFormatter.fromRawString(handlePaddedPrefix(sb), this.classOfT);
        } catch (InvalidHTTPMessageFormatException | InvalidHTTPBodyException | InvalidHTTPHeaderException | InvalidStartLineFormatException | InvalidHTTPVersionException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
    }
    @Override
    public CompletableFuture<HTTPMessage<T>> futureReadAll() {
        return CompletableFuture.supplyAsync(this::synchronousReadAll)
                .exceptionally((_ignored) -> null);
    }

}
