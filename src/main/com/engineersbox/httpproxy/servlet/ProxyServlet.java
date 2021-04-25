package com.engineersbox.httpproxy.servlet;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.socket.SingletonServerSocketFactory;
import com.engineersbox.httpproxy.connection.ProxyConnectionAcceptor;
import com.engineersbox.httpproxy.threading.ThreadManager;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class ProxyServlet implements AbstractServlet {

    private final Logger logger = LogManager.getLogger(ProxyServlet.class.getCanonicalName());

    @Inject
    public Config config;

    @Inject
    private ThreadManager poolManager;

    @Override
    public void init() {
    }

    @Override
    public void serve() {
        final ServerSocket serverSocket;
        try {
            serverSocket = new SingletonServerSocketFactory().createServerSocket(
                    config.servlet.binding.port,
                    config.servlet.connections.acceptorQueueSize
            );
            while (true) {
                poolManager.submitAcceptor(
                    new ProxyConnectionAcceptor(
                        serverSocket.accept(),
                        config.target.host,
                        config.target.port,
                        poolManager
                    )
                );
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
