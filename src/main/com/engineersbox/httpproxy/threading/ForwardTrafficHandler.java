package com.engineersbox.httpproxy.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ForwardTrafficHandler implements Runnable {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);

    private final InputStream inFromServer;
    private final OutputStream outToClient;
    private final Socket server;

    public ForwardTrafficHandler(final InputStream inFromServer, OutputStream outToClient, final Socket server) {
        this.inFromServer = inFromServer;
        this.outToClient = outToClient;
        this.server = server;
    }

    @Override
    public void run() {
        byte[] reply = new byte[4096];
        int bytes_read;
        try {
            while ((bytes_read = inFromServer.read(reply)) != -1) {
                outToClient.write(reply, 0, bytes_read);
                outToClient.flush();
                //TODO CREATE YOUR LOGIC HERE
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                server.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            outToClient.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

}
