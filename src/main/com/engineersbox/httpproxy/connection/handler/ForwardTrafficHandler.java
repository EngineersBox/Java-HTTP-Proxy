package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.List;

public class ForwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter;
    private final BaseContentFormatter contentFormatter;
    private final ContentCollector contentCollector;

    private InputStream inFromServer;
    private OutputStream outToClient;
    private Socket server;
    private Socket client;

    private byte[] response;
    private int read;

    private List<Pair<String, String>> toReplace = ImmutableList.of(
            new ImmutablePair<>("Sydney", "New York"),
            new ImmutablePair<>("sydney", "New York")
    );

    @Inject
    public ForwardTrafficHandler(final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter, final BaseContentFormatter contentFormatter, final ContentCollector contentCollector) {
        this.httpFormatter = httpFormatter;
        this.contentFormatter = contentFormatter;
        this.contentCollector = contentCollector;
    }

    public ForwardTrafficHandler withStreams(final InputStream inFromServer, OutputStream outToClient, final Socket server, final Socket client) {
        this.inFromServer = inFromServer;
        this.outToClient = outToClient;
        this.server = server;
        this.client = client;
        this.contentCollector.withStream(inFromServer);
        return this;
    }

    @Override
    public void task() throws Exception {
        final Pair<byte[], Integer> result =  this.contentCollector.synchronousReadAll();
        this.response = result.getLeft();
        this.read = result.getRight();
        logger.debug("Read from server: " + this.read);
//        contentFormatter.withContentString(new String(this.response, 0, this.read, StandardCharsets.UTF_8));
//        contentFormatter.replaceAllMatchingText(this.toReplace);
        outToClient.write(this.response, 0, this.read);
        outToClient.flush();
    }

    @Override
    public void after() {
        try {
            server.close();
            client.close();
            outToClient.close();
            inFromServer.close();
            logger.info("Closed connections");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
