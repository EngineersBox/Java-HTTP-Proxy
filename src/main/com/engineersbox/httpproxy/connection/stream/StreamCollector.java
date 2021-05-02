package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.content.GZIPCompression;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.common.primitives.Bytes;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class StreamCollector<T extends HTTPStartLine> implements ContentCollector<T> {

    private final Logger logger = LogManager.getLogger(ContentCollector.class);

    private final Config config;
    private final BaseHTTPFormatter<T> httpFormatter;

    private InputStream stream;
    private Class<T> classOfT;
    private Socket socket;

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

    @Override
    public void withSocket(final Socket socket) {
        this.socket = socket;
    }

    private String splitHeader(final String header) {
        return header.split(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER)[1].trim();
    }

    private boolean hasHeader(final String line, final String header) {
        return line.contains(header + HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
    }

    private void logFoundHeader(final String header, final String value) {
        logger.debug("Found " + header + " header with value " + value.trim());
    }

    private byte[] sensitizedStreamRead(final StringBuilder sb, final CRLFRetentiveLineReader retentiveLineReader) throws IOException {
        int read = 0;
        String line = null;
        Pair<String, List<Byte>> lineBytes;
        List<Byte> bytes = new ArrayList<>();
        final StreamContentProperties scp = new StreamContentProperties(this.classOfT);
        while (scp.validateNextRead(line) && scp.validateLineRead((lineBytes = retentiveLineReader.readLineBytes()).getLeft())) {
            line = lineBytes.getLeft();
            if (!scp.passedHeaders) {
                if (!scp.isCompressed && hasHeader(line, HTTPSymbols.CONTENT_ENCODING_HEADER)) {
                    final String contentEncodingHeader = splitHeader(line);
                    scp.isCompressed = contentEncodingHeader.equals(HTTPSymbols.CONTENT_ENCODING_GZIP_KEY)
                            || contentEncodingHeader.equals(HTTPSymbols.CONTENT_ENCODING_X_GZIP_KEY);
                    logFoundHeader(
                        HTTPSymbols.CONTENT_ENCODING_HEADER,
                        splitHeader(line)
                    );
                }
                if (!scp.hasTransferEncodingHeader && hasHeader(line, HTTPSymbols.TRANSFER_ENCODING_HEADER)) {
                    final String transferEncodingHeader = splitHeader(line);
                    scp.hasTransferEncodingHeader = !transferEncodingHeader.equals(HTTPSymbols.TRANSFER_ENCODING_IDENTITY);
                    logFoundHeader(
                        HTTPSymbols.TRANSFER_ENCODING_HEADER,
                        transferEncodingHeader
                    );
                }
            }
           if (!scp.passedHeaders || !scp.isCompressed || scp.hasTextEncoding) {
               sb.append(line);
               read += line.getBytes().length;
           }
           if (!scp.passedHeaders && hasHeader(line, HTTPSymbols.CONTENT_TYPE_HEADER)) {
                final String contentTypeHeader = splitHeader(line);
                if (line.contains(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)) {
                    final String contentTypeCharset = contentTypeHeader.split(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)[1];
                    scp.charset = Charset.forName(StringUtils.removeEnd(contentTypeCharset, HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER));
                }
                if (!HTTPSymbols.CONTENT_TYPE_TEXT_TYPE_REGEX.matcher(contentTypeHeader).matches()) {
                    scp.hasTextEncoding = false;
                }
                logFoundHeader(
                        HTTPSymbols.CONTENT_TYPE_HEADER,
                        splitHeader(line)
                );
            }
            if (!scp.passedHeaders) {
                if (line.equals(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)) {
                    scp.passedHeaders = true;
                }
                continue;
            }
            if (scp.isCompressed) {
                bytes.addAll(lineBytes.getRight());
            }
        }
        if (scp.isCompressed) {
            sb.append(GZIPCompression.unzip(Bytes.toArray(bytes), scp.charset));
            logger.debug("Unzipped compressed body");
        }
        logger.debug("Read " + (read + bytes.size()) + " bytes from "
                + (this.classOfT.isAssignableFrom(HTTPRequestStartLine.class) ? "client" : "server")
                + " input stream");
        return Bytes.toArray(bytes);
    }

    private String handlePaddedPrefix(final StringBuilder sb) {
        return sb.charAt(0) == HTTPSymbols.NULL_BYTE ? sb.substring(1) : sb.toString();
    }

    @Override
    public HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError {
        try {
            this.socket.setSoTimeout(this.config.servlet.connections.dropAfter);
        } catch (final SocketException e) {
            throw new SocketStreamReadError(e);
        }
        final CRLFRetentiveLineReader retentiveLineReader = new CRLFRetentiveLineReader(stream);
        final StringBuilder sb = new StringBuilder();
        final byte[] bodyBytes;
        try {
            bodyBytes = sensitizedStreamRead(sb, retentiveLineReader);
        } catch (final IOException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
        try {
            return this.httpFormatter.fromRawString(handlePaddedPrefix(sb), bodyBytes, this.classOfT);
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
