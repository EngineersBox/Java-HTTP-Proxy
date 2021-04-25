package com.engineersbox.httpproxy.connection.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ForwardTrafficHandler extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final InputStream inFromServer;
    private final OutputStream outToClient;
    private final Socket server;
    private final Socket client;

    public ForwardTrafficHandler(final InputStream inFromServer, OutputStream outToClient, final Socket server, final Socket client) {
        this.inFromServer = inFromServer;
        this.outToClient = outToClient;
        this.server = server;
        this.client = client;
    }

    @Override
    public void task() throws Exception {
        byte[] reply = new byte[4096];
        int bytes_read;
        while ((bytes_read = inFromServer.read(reply)) != -1) {
            outToClient.write(reply, 0, bytes_read);
            outToClient.flush();
        }
        logger.info("Read from server: " + bytes_read);
    }

    @Override
    public void after() {
        try {
            server.close();
            client.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
