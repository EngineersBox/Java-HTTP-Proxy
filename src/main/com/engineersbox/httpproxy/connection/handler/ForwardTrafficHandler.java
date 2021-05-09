package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError;
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

/**
 * Traffic handler to forward traffic from the Server socket to Client socket. Sockets
 * are configured based on configuration.
 *
 * @see com.engineersbox.httpproxy.configuration.Config
 */
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

    /**
     * Handles incoming requests from the socket bound to the server and forwards them
     * to the client. Client and server sockets are defined by the sockets created according to the config
     * file.
     *
     * <br/><br/>
     *
     * See: {@link com.engineersbox.httpproxy.configuration.Config}
     *
     * <br/><br/>
     *
     * If a {@link com.engineersbox.httpproxy.exceptions.socket.SocketStreamReadError} occurs during the reading of a
     * response, a default HTTP/1.1 500 is returned.
     *
     * <br/><br/>
     *
     * Upon having read an incoming message, it is forwarded a {@link com.engineersbox.httpproxy.resolver.ResourceResolver}
     * to be handled by any methods annotated to handle the given content type.
     *
     * @throws Exception If any issues are encountered during the processing of a response
     */
    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPResponseStartLine> message;
        try {
            message = this.resolver.matchResponse(
                    this.contentCollector.synchronousReadAll()
            );
        } catch (final SocketStreamReadError e) {
            message = StandardResponses._408(e.getMessage());
        }
        logger.info("[Client <= Server] " + message.startLine.toDisplayableString());
        final byte[] response = message.toRaw();
        this.outClient.write(response);
        logger.debug("Wrote " + response.length + " bytes to client output stream");
        this.outClient.flush();
        logger.trace("Flushed client input stream");
    }

    /**
     * To ensure the lifecycle of a socket is handled correctly, this method closes the server connection and then
     * the client connection in that order.
     *
     * <br/><br/>
     *
     * Any exceptions that occur during this process will have no further handling done to the sockets. They will however
     * be deallocated once the thread this handler is submitted to closes.
     */
    @Override
    public void after() {
        try {
            server.close();
            logger.debug("Closed server connection");
            client.close();
            logger.debug("Closed client connection");
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
