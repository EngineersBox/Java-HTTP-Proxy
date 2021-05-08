package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.resolver.ResourceResolver;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Traffic handler to forward traffic from the Client socket to Server socket. Sockets
 * are configured based on configuration.
 *
 * <br/><br/>
 *
 * @see com.engineersbox.httpproxy.configuration.Config
 */
public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final ContentCollector<HTTPRequestStartLine> contentCollector;
    private final Config config;
    final ResourceResolver resolver;

    private final OutputStream outServer;

    @Inject
    public BackwardTrafficHandler(final Config config,
                                  final ContentCollector<HTTPRequestStartLine> contentCollector,
                                  final ResourceResolver resolver,
                                  @Named("Client In") final InputStream inClient,
                                  @Named("Server Out") final OutputStream outServer,
                                  @Named("Client Socket") final Socket client) {
        this.config = config;
        this.contentCollector = contentCollector;
        this.resolver = resolver;
        this.outServer = outServer;
        this.contentCollector.withStream(inClient);
        this.contentCollector.withStartLine(HTTPRequestStartLine.class);
        this.contentCollector.withSocket(client);
    }

    /**
     * Handles incoming requests from the socket bound to the client and forwards them
     * to the server. Client and server sockets are defined by the sockets created according to the config
     * file.
     *
     * <br/><br/>
     *
     * See: {@link com.engineersbox.httpproxy.configuration.Config}
     *
     * <br/><br/>
     *
     * According to RFC-2616, requests MAY NOT include a {@code host} header, and as such will add the target
     * in order to ensure a 400 (Bad Request) with content {@code The requested URL "[no URL]", is invalid.} is
     * not returned
     *
     * <br/><br/>
     *
     * In order to ensure that the socket InputStream is signalled with a termination correctly, the
     * {@code Connection: Close} header is added. This results in a {@code -1} being identified by the stream read.
     *
     * <br/><br/>
     *
     * @throws Exception If any issues are encountered during the processing of a request
     */
    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPRequestStartLine> message = resolver.matchRequest(this.contentCollector.synchronousReadAll());
        logger.info("[Client => Server] " + message.startLine.toDisplayableString());
        final byte[] request = message.toRaw();
        this.outServer.write(request);
        logger.debug("Wrote " + request.length + " bytes to server output stream");
        this.outServer.flush();
        logger.trace("Flushed server input stream");
    }

    @Override
    public void after() {}
}
