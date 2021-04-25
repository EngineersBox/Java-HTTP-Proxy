package com.engineersbox.httpproxy.threading;

import com.engineersbox.httpproxy.exceptions.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.socket.SingletonSocketFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ProxyConnectionAcceptor implements Runnable {

    private final Logger logger = LogManager.getLogger(ProxyConnectionAcceptor.class);

    private final Socket localSocket;
    private final String host;
    private final int port;
    private final PoolManager poolManager;

    public ProxyConnectionAcceptor(final Socket localSocket, final String host, final int port, final PoolManager poolManager) {
        this.localSocket = localSocket;
        this.host = host;
        this.port = port;
        this.poolManager = poolManager;
    }

    @Override
    public void run() {
        try {
            logger.debug("Reserved ");
            final Socket server;
            try {
                server = new SingletonSocketFactory().createSocket(host, port);
                logger.info("Retrieved socket from factory");
            } catch (IOException e) {
                PrintWriter out = new PrintWriter(new OutputStreamWriter(localSocket.getOutputStream()));
                out.flush();
                throw new FailedToCreateServerSocketException(
                    "Could not create socket for [Host: " + this.host + "] [Port: " + this.port + "]",
                    e
                );
            }
            this.poolManager.submitHandler(new BackwardTrafficHandler(
                localSocket.getInputStream(),
                server.getOutputStream()
            ));
            logger.info("Submitted BackwardTrafficHandler to PoolManager");
            this.poolManager.submitHandler(new ForwardTrafficHandler(
                server.getInputStream(),
                localSocket.getOutputStream(),
                server
            ));
            logger.info("Submitted ForwardTrafficHandler to PoolManager");
            localSocket.close();
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
