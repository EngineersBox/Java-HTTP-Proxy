package com.engineersbox.httpproxy.servlet;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.socket.SingletonServerSocketFactory;
import com.engineersbox.httpproxy.connection.ProxyConnectionAcceptor;
import com.engineersbox.httpproxy.connection.threading.ThreadManager;
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

    private ServerSocket serverSocket;

    @Override
    public void init() {
        try {
            this.serverSocket = new SingletonServerSocketFactory()
                .createServerSocket(
                    config.servlet.binding.port,
                    config.servlet.connections.acceptorQueueSize
                );
        } catch (final IOException e) {
            logger.error(e, e);
        }
        logger.info("Initialised ProxyServlet instance");
    }

    @Override
    public void serve() {
        logger.info(
            "Started serving ProxyServlet on "
            + config.servlet.binding.host
            + ":" + config.servlet.binding.port
            + " for "
            + config.target.host
            + ":" + config.target.port
        );
        try {
            while (true) {
                poolManager.submitAcceptor(
                    new ProxyConnectionAcceptor(
                        this.serverSocket.accept(),
                        config.target.host,
                        config.target.port,
                        poolManager
                    ).withSocketConfigs(this.config.servlet.connections)
                );
            }
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
