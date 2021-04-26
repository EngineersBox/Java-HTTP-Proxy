package com.engineersbox.httpproxy.connection.stream;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public interface ContentCollector {

    void withStream(final InputStream stream);

    Pair<byte[], Integer> synchronousReadAll() throws IOException;

    CompletableFuture<Pair<byte[], Integer>> futureReadAll() throws IOException;

}
