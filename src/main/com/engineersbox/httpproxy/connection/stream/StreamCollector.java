package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
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

    private boolean validateLineRead(final String line) {
        return classOfT.isAssignableFrom(HTTPRequestStartLine.class) ? !StringUtils.isEmpty(line) : line != null;
    }

    private void sensitizedStreamRead(final StringBuilder sb, final BufferedReader br) throws IOException {
        int read = 0;
        int maxRead = Integer.MAX_VALUE;
        String line;
        boolean pastHeaders = false;
        boolean sectionDelimiter = false;
        while (read <= maxRead && validateLineRead(line = br.readLine())) {
            if (line.contains(HTTPSymbols.CONTENT_LENGTH_HEADER)) {
                final String contentLengthHeader = line.split(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER)[1];
                maxRead = Integer.parseInt(contentLengthHeader);
            }
            sb.append(line);
            if (!pastHeaders && StringUtils.isEmpty(line)) {
                pastHeaders = true;
                sectionDelimiter = true;
                sb.append(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER);
            }
            if (!pastHeaders) {
                sb.append(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER);
                continue;
            }
            if (sectionDelimiter) {
                sectionDelimiter = false;
            } else {
                sb.append(HTTPSymbols.HTTP_BODY_NEWLINE_DELIMITER);
            }
            read += line.getBytes(StandardCharsets.UTF_8).length + 1;
        }
        logger.debug("Read " + read + " bytes from stream");
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
            return this.httpFormatter.fromRawString(sb.toString(), this.classOfT);
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
