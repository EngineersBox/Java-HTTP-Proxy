package com.engineersbox.httpproxy.connection.handler;

import com.engineersbox.httpproxy.formatting.HTTPSymbols;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class BackwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final OutputStream outToServer;
    private final InputStream inFromClient;
    private final String host;

    private int read;
    private byte[] request;

    public BackwardTrafficHandler(final OutputStream outToServer, final InputStream inFromClient, final String host) {
        this.outToServer = outToServer;
        this.inFromClient = inFromClient;
        this.host = host;
    }

    private byte[] createGETRequest() {
        final String fmtReq = new String(this.request, 0, read, StandardCharsets.UTF_8);
        final String[] splitFmtReq = fmtReq.split(HTTPSymbols.HEADER_NEWLINE_DELIMITER);
        logger.info(Arrays.toString(splitFmtReq));
        return (splitFmtReq[0] + "\r\n"
                + "Host: " + this.host + "\r\n"
                + "User-Agent: HTTP Proxy\r\n"
                + "Accept: */*\r\n"
                + "\r\n").getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public void task() throws Exception {
        this.request = new byte[1024];
        this.read = this.inFromClient.read(this.request);

        this.request = createGETRequest();
        this.outToServer.write(this.request, 0, request.length);
        this.outToServer.flush();
    }

    @Override
    public void after() {}
}
