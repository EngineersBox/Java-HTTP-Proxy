package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.exceptions.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface ContentCollector<T extends HTTPStartLine> {

    void withStream(final InputStream stream);

    void withStartLine(final Class<T> classOfT);

    HTTPMessage<T> synchronousReadAll() throws SocketStreamReadError;

    CompletableFuture<HTTPMessage<T>> futureReadAll() throws IOException;

}
