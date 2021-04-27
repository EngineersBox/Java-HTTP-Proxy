package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ForwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final BaseHTTPFormatter<HTTPResponseStartLine> httpFormatter;
    private final BaseContentFormatter contentFormatter;
    private final ContentCollector<HTTPResponseStartLine> contentCollector;

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
    public ForwardTrafficHandler(final BaseHTTPFormatter<HTTPResponseStartLine> httpFormatter, final BaseContentFormatter contentFormatter, final ContentCollector<HTTPResponseStartLine> contentCollector) {
        this.httpFormatter = httpFormatter;
        this.contentFormatter = contentFormatter;
        this.contentCollector = contentCollector;
    }

    public ForwardTrafficHandler withStreams(final InputStream inFromServer, OutputStream outToClient, final Socket server, final Socket client) {
        this.inFromServer = inFromServer;
        this.outToClient = outToClient;
        this.server = server;
        this.client = client;
        this.contentCollector.withStream(this.inFromServer);
        this.contentCollector.withStartLine(HTTPResponseStartLine.class);
        return this;
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPResponseStartLine> message = this.contentCollector.synchronousReadHeaders();
        this.contentCollector.synchronousReadBody(message);
        this.response = message.toRaw();
        this.read = this.response.length;
        logger.debug("Read from server: " + new String(this.response, StandardCharsets.UTF_8));
//        contentFormatter.withContentString(new String(this.response, 0, this.read, StandardCharsets.UTF_8));
//        contentFormatter.replaceAllMatchingText(this.toReplace);
        outToClient.write(this.response);
        outToClient.flush();
    }

    @Override
    public void after() {
        try {
            server.close();
            client.close();
            logger.info("Closed connections");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
