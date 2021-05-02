package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.exceptions.SocketStreamReadError;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.formatting.http.response.StandardResponses;
import com.engineersbox.httpproxy.resolver.ResourceResolver;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ForwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(ForwardTrafficHandler.class);

    private final ContentCollector<HTTPResponseStartLine> contentCollector;
    private final ResourceResolver resolver;

    private final OutputStream outClient;
    private final Socket server;
    private final Socket client;

    @Inject
    public ForwardTrafficHandler(final ContentCollector<HTTPResponseStartLine> contentCollector,
                                 final ResourceResolver resolver,
                                 @Named("Server In") final InputStream inServer,
                                 @Named("Client Out") final OutputStream outClient,
                                 @Named("Server Socket") final Socket server,
                                 @Named("Client Socket") final Socket client) {
        this.contentCollector = contentCollector;
        this.resolver = resolver;
        this.outClient = outClient;
        this.server = server;
        this.client = client;
        this.contentCollector.withStream(inServer);
        this.contentCollector.withStartLine(HTTPResponseStartLine.class);
        this.contentCollector.withSocket(this.server);
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPResponseStartLine> message;
        try {
            message = this.resolver.match(
                    this.contentCollector.synchronousReadAll()
            );
        } catch (final SocketStreamReadError e) {
            message = StandardResponses._500();
        }
        final byte[] response = message.toRaw();
        this.outClient.write(response);
        logger.debug("Wrote " + response.length + " bytes to client output stream");
        this.outClient.flush();
        logger.trace("Flushed client input stream");
    }

    @Override
    public void after() {
        try {
            server.close();
            logger.info("Closed server connection");
            client.close();
            logger.info("Closed client connection");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
