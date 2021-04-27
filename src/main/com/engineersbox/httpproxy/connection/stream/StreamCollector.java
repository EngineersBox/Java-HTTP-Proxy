package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.google.inject.Inject;
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

    private String readHeaders(final BufferedReader br) throws IOException {
        final StringBuilder sb = new StringBuilder();
        String line;
        while (!(line = br.readLine()).equals("")) {
            sb.append(line).append(HTTPSymbols.HTTP_NEWLINE_DELIMITER);
        }
        return sb.toString();
    }

    @Override
    public HTTPMessage<T> synchronousReadHeaders() throws SocketStreamReadError {
        final HTTPMessage<T> message;
        final BufferedReader br = new BufferedReader(new InputStreamReader(stream));
        try {
            final String headers = readHeaders(br);
            message = this.httpFormatter.fromRawString(headers, this.classOfT);
            logger.debug("Finished reading headers from stream");
        } catch (final IOException | InvalidHTTPMessageFormatException | InvalidStartLineFormatException | InvalidHTTPVersionException | InvalidHTTPHeaderException | InvalidHTTPBodyException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
        return message;
    }

    @Override
    public void synchronousReadBody(final HTTPMessage<T> message) throws SocketStreamReadError {
        int totalRead = 0;
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[this.config.servlet.connections.readerBufferSize];
        try {
            int readIn;
            final int maxRead = Integer.parseInt(message.headers.get("Content-Length"));
            while ((readIn = stream.read(buffer)) != -1 && totalRead <= maxRead) {
                logger.debug("READ VAL: " + readIn);
                result.write(buffer, 0, readIn);
                totalRead += readIn;
                buffer = new byte[this.config.servlet.connections.readerBufferSize];
                logger.debug("Read in " + readIn + " bytes from stream");
            }
            logger.debug("Finished reading from stream");
        } catch (final IOException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
        message.body = new String(result.toByteArray(), 0, totalRead, StandardCharsets.UTF_8);
    }

    @Override
    public CompletableFuture<HTTPMessage<T>> futureReadAll() {
        return CompletableFuture.supplyAsync(this::synchronousReadHeaders)
                .exceptionally((_ignored) -> null)
                .thenCompose(result -> CompletableFuture.supplyAsync(() -> {
                    synchronousReadBody(result);
                    return result;
                })).exceptionally((_ignored) -> null);
    }

}
