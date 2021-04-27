package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter;
    private final BaseContentFormatter contentFormatter;
    private final ContentCollector<HTTPRequestStartLine> contentCollector;

    private OutputStream outToServer;
    private InputStream inFromClient;
    private String host;

    private int read;
    private byte[] request;

    @Inject
    public BackwardTrafficHandler(final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter, final BaseContentFormatter contentFormatter, final ContentCollector<HTTPRequestStartLine> contentCollector) {
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

    private byte[] createGETRequest() {
        final String fmtReq = new String(this.request, StandardCharsets.UTF_8);
        final String[] splitFmtReq = fmtReq.split(HTTPSymbols.HTTP_NEWLINE_DELIMITER);
        return (
            splitFmtReq[0] + HTTPSymbols.HTTP_NEWLINE_DELIMITER
            + "Host: " + this.host + HTTPSymbols.HTTP_NEWLINE_DELIMITER
            + "User-Agent: HTTP Proxy" + HTTPSymbols.HTTP_NEWLINE_DELIMITER
            + "Accept: */*" + HTTPSymbols.HTTP_NEWLINE_DELIMITER
            + HTTPSymbols.HTTP_NEWLINE_DELIMITER
        ).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void task() throws Exception {
        HTTPMessage<HTTPRequestStartLine> message = this.contentCollector.synchronousReadHeaders();
        message.headers.replace("User-Agent", "HTTP Proxy");
        this.request = message.toRaw();
        byte[] rawMessage = message.toRaw();
        logger.info("Length: " + rawMessage.length + " Raw message: " + new String(rawMessage, 0, rawMessage.length, StandardCharsets.UTF_8));

        this.request = createGETRequest();
        this.outToServer.write(this.request);
        this.outToServer.flush();
        logger.info("Finished writing to server");
    }

    @Override
    public void after() {}
}
