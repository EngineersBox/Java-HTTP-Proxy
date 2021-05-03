package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.http.*;
import com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError;
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

/**
 * Socket stream reader used to construct HTTP messages received via a bound {@link java.net.Socket}'s {@link java.io.InputStream},
 * on a per-request basis. Messages are read and constructed according to <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a>.
 *
 * <br/><br/>
 *
 * Note that this reader supports automatic decompression of the following formats, done after having identified the {@code Content-Type} header
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
 * @param <T> An implementation of abstract class {@link HTTPStartLine}
 */
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

    /**
     * See {@link ContentCollector#withStream(InputStream)}
     *
     * <br/><br/>
     *
     * @param stream Stream bound to an open {@link java.net.Socket}
     */
    @Override
    public void withStream(final InputStream stream) {
        this.stream = stream;
    }

    /**
     * See {@link ContentCollector#withStartLine(Class)}
     *
     * <br/><br/>
     *
     * @param classOfT The class of implementation of {@link HTTPStartLine}
     */
    @Override
    public void withStartLine(final Class<T> classOfT) {
        this.classOfT = classOfT;
    }

    /**
     * See {@link ContentCollector#withSocket(Socket)}
     *
     * <br/><br/>
     *
     * @param socket A bound and open {@link java.net.Socket} instance
     */
    @Override
    public void withSocket(final Socket socket) {
        this.socket = socket;
    }

    /**
     * Split a the given {@code header} at an instance of {@link HTTPSymbols#HEADER_KEY_VALUE_DELIMITER}, returning the
     * second element of the split array.
     *
     * <br/><br/>
     *
     * Note that the result is also trimmed, to remove excessive non-data characters. See {@link String#trim()}
     *
     * <br/><br/>
     *
     * @param header {@link String} value in the <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a> compliant format: <br/>
     * {@code <HEADER KEY>[: <HEADER VALUE>[;<HEADER VALUE>]]}
     * @return Key value of the {@code header} containing one or more values separated by {@link HTTPSymbols#HEADER_VALUE_LIST_DELIMITER}
     */
    private String splitHeader(final String header) {
        return header.split(HTTPSymbols.HEADER_KEY_VALUE_DELIMITER)[1].trim();
    }

    /**
     * Verifies whether a given line contains a given header key followed by the {@link HTTPSymbols#HEADER_KEY_VALUE_DELIMITER}
     *
     * <br/><br/>
     *
     * @param line Potential header
     * @param header Key value to check for
     * @return {@code true} if {@code line} contains "{@code header + }{@link HTTPSymbols#HEADER_KEY_VALUE_DELIMITER}",
     * {@code false} otherwise
     */
    private boolean hasHeader(final String line, final String header) {
        return line.contains(header + HTTPSymbols.HEADER_KEY_VALUE_DELIMITER);
    }

    /**
     * Logs a message to the internal log4j instance to indicate a given header has been found. The message in the format: <br/>
     * {@code "Found " + header + " header with value " + value}
     *
     * <br/><br/>
     *
     * @param header Header key
     * @param value Header value
     */
    private void logFoundHeader(final String header, final String value) {
        logger.debug("Found " + header + " header with value " + value.trim());
    }

    /**
     * Read from a configured {@link java.io.InputStream} line-by-line (terminated by a combination of {@code CR (\r)} and {@code LF (\n)}),
     * constructing a string literal version of an {@link HTTPMessage}.
     *
     * <br/><br/>
     *
     * This will additionally provide the raw bytes for data that is not in text format or is compressed. In the instance
     * that the body contains compressed data in a valid <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a>
     * format it will be decompressed and appending to the string literal and supplied as raw bytes. A valid compression
     * type is one of the following:
     *
     * <ul>
     *     <li>{@code GZIP}: Lempel-Ziv coding (LZ77) with a 32 bit CRC (<a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a>).</li>
     *     <li>{@code compress}: Adaptive Lempel-Ziv-Welch coding (LZW)</li>
     *     <li>
     *         {@code deflate}: zlib format (<a href="https://www.w3.org/Protocols/rfc1950/rfc1950.html" target="_top">RFC 1950</a>)
     *          with deflate compression mechanism (<a href="https://www.w3.org/Protocols/rfc1951/rfc1951.html" target="_top">RFC 1951</a>).
     *     </li>
     * </ul>
     *
     * @param sb Instance of {@link StringBuilder} to construct the {@link HTTPMessage} string literal
     * @param retentiveLineReader Instance of {@link CRLFRetentiveLineReader} to read {@link InputStream} line-by-line
     *                            and keep lin terminators
     * @return A {@code byte[]} containing body data if body not in text format or is compressed in a valid <a href="https://www.w3.org/Protocols/rfc2616/rfc2616.html" target="_top">RFC 2616</a> format
     * @throws IOException if the {@link InputStream} or bound {@link Socket} was closed early or other read interruptions occured
     */
    private byte[] sensitizedStreamRead(final StringBuilder sb, final CRLFRetentiveLineReader retentiveLineReader) throws IOException {
        int read = 0;
        String line = null;
        Pair<String, List<Byte>> lineBytes;
        List<Byte> bytes = new ArrayList<>();
        final StreamContentProperties scp = new StreamContentProperties(this.classOfT);
        while (scp.validateNextRead(line) && scp.validateLineRead((lineBytes = retentiveLineReader.readLineBytes()).getLeft())) {
            line = lineBytes.getLeft();
            if (!scp.pastHeaders) {
                if (!scp.isCompressed && hasHeader(line, HTTPSymbols.CONTENT_ENCODING_HEADER)) {
                    final String contentEncodingHeader = splitHeader(line);
                    scp.isCompressed = !contentEncodingHeader.contains(HTTPSymbols.CONTENT_ENCODING_IDENTITY);
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
           if (!scp.pastHeaders || (!scp.isCompressed && !scp.isRaw)) {
               sb.append(line);
               read += line.getBytes().length;
           }
           if (!scp.pastHeaders && hasHeader(line, HTTPSymbols.CONTENT_TYPE_HEADER)) {
                final String contentTypeHeader = splitHeader(line);
                if (line.contains(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)) {
                    final String contentTypeCharset = contentTypeHeader.split(HTTPSymbols.CONTENT_TYPE_CHARSET_KEY)[1];
                    scp.charset = Charset.forName(StringUtils.removeEnd(contentTypeCharset, HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER));
                }
                scp.isRaw = HTTPSymbols.CONTENT_TYPE_IMAGE_REGEX.matcher(contentTypeHeader).find();
                logFoundHeader(
                        HTTPSymbols.CONTENT_TYPE_HEADER,
                        splitHeader(line)
                );
            }
            if (!scp.pastHeaders) {
                if (line.equals(HTTPSymbols.HTTP_HEADER_NEWLINE_DELIMITER)) {
                    scp.pastHeaders = true;
                }
                continue;
            }
            if (scp.isCompressed || scp.isRaw) {
                bytes.addAll(lineBytes.getRight());
            }
        }
        if (scp.isCompressed) {
            sb.append(GZIPCompression.decompress(Bytes.toArray(bytes), scp.charset));
            logger.debug("Unzipped compressed body");
        }
        logger.debug("Read " + (read + bytes.size()) + " bytes from "
                + (this.classOfT.isAssignableFrom(HTTPRequestStartLine.class) ? "client" : "server")
                + " input stream");
        return Bytes.toArray(bytes);
    }

    /**
     * Removes a null byte ({@code \0}) from the start of a given {@link StringBuilder} instance.
     *
     * <br/><br/>
     *
     * @param sb {@link StringBuilder} with a potential leading null byte
     * @return The unaltered {@code sb} instance as a {@link String} if no null byte leads the content or a substring
     * of the {@code sb} instance starting from index {@code 1}.
     */
    private String handlePaddedPrefix(final StringBuilder sb) {
        return sb.charAt(0) == HTTPSymbols.NULL_BYTE ? sb.substring(1) : sb.toString();
    }

    /**
     * See {@link ContentCollector#synchronousReadAll()}
     *
     * <br/><br/>
     *
     * @return Instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage} constructed from stream
     *         result of reading from stream
     * @throws SocketStreamReadError Any exceptions encountered whilst reading from the {@link java.io.InputStream}
     * @throws HTTPMessageException Any formatting or initialisation exceptions encountered whilst constructing an instance
     *         of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}
     */
    @Override
    public HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError, HTTPMessageException {
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
            throw new SocketStreamReadError(e);
        }
        return this.httpFormatter.fromRawString(handlePaddedPrefix(sb), bodyBytes, this.classOfT);
    }

    /**
     * See {@link ContentCollector#futureReadAll()}
     *
     * <br/><br/>
     *
     * @return A {@link java.util.concurrent.CompletableFuture} containing an instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}
     */
    @Override
    public CompletableFuture<HTTPMessage<T>> futureReadAll() {
        return CompletableFuture.supplyAsync(this::synchronousReadAll)
                .exceptionally((_ignored) -> null);
    }

}
