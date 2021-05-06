package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.exceptions.http.CompressionHandlerException;
import com.engineersbox.httpproxy.exceptions.http.HTTPMessageException;
import com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;


/**
 * Base interface for implementing an {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage} reader.
 *
 * <br/><br/>
 *
 * @see com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine
 * @param <T> An implementation extending the {@link com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine} abstract class
 */
public interface ContentCollector<T extends HTTPStartLine> {


    /**
     * Supply an {@link java.io.InputStream} from a bound to an open {@link java.net.Socket} to read from.
     *
     * <br/><br/>
     *
     * @param stream Stream bound to an open {@link java.net.Socket}
     */
    void withStream(final InputStream stream);

    /**
     * Supply a class for the implementation of the type parameter {@code T}.
     *
     * <br/><br/>
     *
     * @param classOfT The class of implementation of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine}
     *                 used with the {@code T} type parameter
     */
    void withStartLine(final Class<T> classOfT);

    /**
     * Open {@link java.net.Socket} to provide any additional configuration, during {@link ContentCollector#synchronousReadAll()}
     * or initialisation conditionally on implementation requirements.
     *
     * <br/><br/>
     *
     * @param socket A bound and open {@link java.net.Socket} instance
     */
    void withSocket(final Socket socket);

    /**
     * Implementation of a reader to take input from the configured {@link java.io.InputStream}. An implementation will
     * construct an instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}.
     *
     * <br/><br/>
     *
     * @return Instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage} constructed from stream
     *  result of reading from stream
     * @throws SocketStreamReadError Any exceptions encountered whilst reading from the {@link java.io.InputStream}
     * @throws HTTPMessageException Any formatting or initialisation exceptions encountered whilst constructing an instance
     * of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}
     * @throws CompressionHandlerException Issues encountered whilst attempting to decompress a compressed body
     */
    HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError, HTTPMessageException, CompressionHandlerException;


    /**
     * An implementation of an asynchronous read from the configured {@link java.io.InputStream}. An implementation will
     * construct an instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage} to be supplied as
     * a {@link java.util.concurrent.CompletableFuture}
     *
     * <br/><br/>
     *
     * @return A {@link java.util.concurrent.CompletableFuture} containing an instance of {@link com.engineersbox.httpproxy.formatting.http.common.HTTPMessage}
     */
    CompletableFuture<HTTPMessage<T>> futureReadAll();

}
