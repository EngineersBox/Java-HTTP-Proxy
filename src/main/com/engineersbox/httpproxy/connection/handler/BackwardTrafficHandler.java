package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter;
    private final BaseContentFormatter contentFormatter;
    private final ContentCollector<HTTPRequestStartLine> contentCollector;
    private final Config config;

    private final OutputStream outToServer;
    private final InputStream inFromClient;

    private final Socket client;

    private int read;
    private byte[] request;

    @Inject
    public BackwardTrafficHandler(final Config config,
                                  final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter,
                                  final BaseContentFormatter contentFormatter,
                                  final ContentCollector<HTTPRequestStartLine> contentCollector,
                                  @Named("Client In") final InputStream inClient,
                                  @Named("Server Out") final OutputStream outServer,
                                  @Named("Client Socket") final Socket client) {
        this.config = config;
        this.httpFormatter = httpFormatter;
        this.contentFormatter = contentFormatter;
        this.contentCollector = contentCollector;
        this.outToServer = outServer;
        this.inFromClient = inClient;
        this.client = client;
        this.contentCollector.withStream(this.inFromClient);
        this.contentCollector.withStartLine(HTTPRequestStartLine.class);
        this.contentCollector.withSocket(this.client);
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPRequestStartLine> message = this.contentCollector.synchronousReadAll();
        message.headers.replace("Host", this.config.target.host);
        message.headers.replace("User-Agent", "HTTPProxy");
        message.headers.put("Connection", "close");
//        message.headers.remove("Accept-Encoding");
        this.request = message.toRaw();
        this.read = this.request.length;
        byte[] rawMessage = message.toRaw();
        this.outToServer.write(rawMessage);
        logger.debug("Wrote " + this.read + " bytes to server output stream");
        this.outToServer.flush();
        logger.trace("Flushed server input stream");
    }

    @Override
    public void after() {}
}
