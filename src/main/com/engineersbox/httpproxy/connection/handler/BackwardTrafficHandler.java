package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final ContentCollector<HTTPRequestStartLine> contentCollector;
    private final Config config;

    private final OutputStream outServer;

    @Inject
    public BackwardTrafficHandler(final Config config,
                                  final ContentCollector<HTTPRequestStartLine> contentCollector,
                                  @Named("Client In") final InputStream inClient,
                                  @Named("Server Out") final OutputStream outServer,
                                  @Named("Client Socket") final Socket client) {
        this.config = config;
        this.contentCollector = contentCollector;
        this.outServer = outServer;
        this.contentCollector.withStream(inClient);
        this.contentCollector.withStartLine(HTTPRequestStartLine.class);
        this.contentCollector.withSocket(client);
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPRequestStartLine> message = this.contentCollector.synchronousReadAll();
        message.headers.replace("Host", this.config.target.host);
        message.headers.replace("User-Agent", "HTTPProxy");
        message.headers.put("Connection", "close");
//        message.headers.remove("Accept-Encoding");
        final byte[] request = message.toRaw();
        this.outServer.write(request);
        logger.debug("Wrote " + request.length + " bytes to server output stream");
        this.outServer.flush();
        logger.trace("Flushed server input stream");
    }

    @Override
    public void after() {}
}
