package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.BaseHTTPFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
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
    private final ContentCollector contentCollector;

    private OutputStream outToServer;
    private InputStream inFromClient;
    private String host;

    private int read;
    private byte[] request;

    @Inject
    public BackwardTrafficHandler(final BaseHTTPFormatter<HTTPRequestStartLine> httpFormatter, final BaseContentFormatter contentFormatter, final ContentCollector contentCollector) {
        this.httpFormatter = httpFormatter;
        this.contentFormatter = contentFormatter;
        this.contentCollector = contentCollector;
    }

    public BackwardTrafficHandler withStreams(final OutputStream outToServer, final InputStream inFromClient, final String host) {
        this.outToServer = outToServer;
        this.inFromClient = inFromClient;
        this.host = host;
        this.contentCollector.withStream(inFromClient);
        return this;
    }

    private byte[] createGETRequest() {
        final String fmtReq = new String(this.request, 0, this.read, StandardCharsets.UTF_8);
        final String[] splitFmtReq = fmtReq.split(HTTPSymbols.HTTP_NEWLINE_DELIMITER);
        logger.info(Arrays.toString(splitFmtReq));
        return (
            splitFmtReq[0] + "\r\n"
            + "Host: " + this.host + "\r\n"
            + "User-Agent: HTTP Proxy\r\n"
            + "Accept: */*\r\n"
            + "\r\n"
        ).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void task() throws Exception {
        logger.info("HERE");
        final Pair<byte[], Integer> result =  this.contentCollector.synchronousReadAll();
        logger.info("HERE");
        this.request = result.getLeft();
        this.read = result.getRight();
        logger.debug("Read from client: " + this.read);

        final HTTPMessage<HTTPRequestStartLine> message =  httpFormatter.fromRaw(new String(this.request, 0, this.read, StandardCharsets.UTF_8), HTTPRequestStartLine.class);
        message.headers.replace("User-Agent", "HTTP Proxy");
        byte[] rawMessage = message.toRaw();

        logger.info("Length: " + rawMessage.length + " Raw message: " + new String(rawMessage, 0, rawMessage.length, StandardCharsets.UTF_8));
        logger.info("Message after raw: " + message);

        this.request = createGETRequest();
        this.outToServer.write(this.request, 0, request.length);
        this.outToServer.flush();
    }

    @Override
    public void after() {
        try {
            this.outToServer.close();
        } catch (IOException e) {
            logger.error(e, e);
        }
    }
}
