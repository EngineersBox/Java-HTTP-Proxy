package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter;
    private final BaseContentFormatter contentFormatter;
    private final ContentCollector<HTTPRequestStartLine> contentCollector;
    private final Config config;

    private OutputStream outToServer;
    private InputStream inFromClient;
    private String host;

    private int read;
    private byte[] request;

    @Inject
    public BackwardTrafficHandler(final Config config, final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter, final BaseContentFormatter contentFormatter, final ContentCollector<HTTPRequestStartLine> contentCollector) {
        this.config = config;
        this.httpFormatter = httpFormatter;
        this.contentFormatter = contentFormatter;
        this.contentCollector = contentCollector;
    }

    public BackwardTrafficHandler withStreams(final OutputStream outToServer, final InputStream inFromClient, final String host) {
        this.outToServer = outToServer;
        this.inFromClient = inFromClient;
        this.host = host;
        this.contentCollector.withStream(this.inFromClient);
        this.contentCollector.withStartLine(HTTPRequestStartLine.class);
        return this;
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPRequestStartLine> message = this.contentCollector.synchronousReadAll();
        message.headers.replace("Host", config.target.host);
        message.headers.replace("User-Agent", "HTTPProxy");
        this.request = message.toRaw();
        byte[] rawMessage = message.toRaw();
        this.outToServer.write(rawMessage);
        this.outToServer.flush();
        logger.info("Finished writing to server");
    }

    @Override
    public void after() {}
}
