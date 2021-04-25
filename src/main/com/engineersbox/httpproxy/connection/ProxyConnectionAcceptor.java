package com.engineersbox.httpproxy.connection;

import com.engineersbox.httpproxy.connection.handler.BackwardTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.ForwardTrafficHandler;
import com.engineersbox.httpproxy.exceptions.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.socket.SingletonSocketFactory;
import com.engineersbox.httpproxy.threading.ThreadManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ProxyConnectionAcceptor extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(ProxyConnectionAcceptor.class);

    private final Socket localSocket;
    private final String host;
    private final int port;
    private final ThreadManager poolManager;

    public ProxyConnectionAcceptor(final Socket localSocket, final String host, final int port, final ThreadManager poolManager) {
        this.localSocket = localSocket;
        this.host = host;
        this.port = port;
        this.poolManager = poolManager;
    }

    @Override
    public void task() throws Exception {
        final Socket server;
        final OutputStream outClient = localSocket.getOutputStream();
        final InputStream inClient = localSocket.getInputStream();
        try {
            server = new SingletonSocketFactory().createSocket(host, port);
            logger.info("Retrieved socket from factory");
        } catch (IOException e) {
            PrintWriter out = new PrintWriter(new OutputStreamWriter(outClient));
            out.flush();
            throw new FailedToCreateServerSocketException(
                    "Could not create socket for [Host: " + this.host + "] [Port: " + this.port + "]",
                    e
            );
        }
        final OutputStream outServer = server.getOutputStream();
        final InputStream inServer = server.getInputStream();
        this.poolManager.submitHandler(new BackwardTrafficHandler(
                outServer,
                inClient,
                host
        ));
        logger.info("Submitted BackwardTrafficHandler to PoolManager");
        this.poolManager.submitHandler(new ForwardTrafficHandler(
                inServer,
                outClient,
                server,
                localSocket
        ));
        logger.info("Submitted ForwardTrafficHandler to PoolManager");
    }

    @Override
    public void after() {}
}
