package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.exceptions.http.HTTPMessageException;
import com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

import java.io.InputStream;
import java.net.Socket;
import java.util.concurrent.CompletableFuture;

public interface ContentCollector<T extends HTTPStartLine> {

    void withStream(final InputStream stream);
    void withStartLine(final Class<T> classOfT);
    void withSocket(final Socket socket);

    HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError, HTTPMessageException;

    CompletableFuture<HTTPMessage<T>> futureReadAll();

}
