package com.engineersbox.httpproxy.threading;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BackwardTrafficHandler implements Runnable {

    private final Logger logger = LogManager.getLogger(BackwardTrafficHandler.class);
    private static final int REQUEST_BUFFER_SIZE = 1024;

    private final InputStream inFromClient;
    private final OutputStream outToServer;
    private final byte[] request;

    public BackwardTrafficHandler(final InputStream inFromClient, final OutputStream outToServer) {
        this.inFromClient = inFromClient;
        this.outToServer = outToServer;
        this.request = new byte[REQUEST_BUFFER_SIZE];
    }

    @Override
    public void run() {
        int bytes_read;
        try {
            while ((bytes_read = this.inFromClient.read(this.request)) != -1) {
                this.outToServer.write(this.request, 0, bytes_read);
                this.outToServer.flush();
                //TODO CREATE YOUR LOGIC HERE
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            this.outToServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
