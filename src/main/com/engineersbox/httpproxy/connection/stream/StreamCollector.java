package com.engineersbox.httpproxy.connection.stream;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.SocketStreamReadError;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;

public class StreamCollector implements ContentCollector {

    private InputStream stream;

    private final Config config;

    @Inject
    public StreamCollector(final Config config) {
        this.config = config;
    }

    @Override
    public void withStream(final InputStream stream) {
        this.stream = stream;
    }

    @Override
    public Pair<byte[], Integer> synchronousReadAll() throws SocketStreamReadError {
        int totalRead = 0;
        final ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[this.config.servlet.connections.readerBufferSize];
        try {
            for (int length; (length = stream.read(buffer)) != -1; ) {
                System.out.println("Read: " + length);
                result.write(buffer, 0, length);
                totalRead += length;
            }
        } catch (final IOException e) {
            // TODO: Return an HTTP 500 error when this occurs
            throw new SocketStreamReadError(e);
        }
        return ImmutablePair.of(result.toByteArray(), totalRead);
    }

    @Override
    public CompletableFuture<Pair<byte[], Integer>> futureReadAll() {
        return CompletableFuture.supplyAsync(this::synchronousReadAll)
                .exceptionally((_ignored) -> ImmutablePair.of(new byte[0], 0));
    }

}
