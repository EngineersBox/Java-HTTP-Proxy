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

/**
 * Implementation of {@link AbstractServlet} to serve an HTTP/1.1 proxy over TCP via {@link java.net.Socket}'s
 */
public class ProxyServlet implements AbstractServlet {

    private final Logger logger = LogManager.getLogger(ProxyServlet.class.getCanonicalName());

    @Inject
    public Config config;

    @Inject
    private ThreadManager poolManager;

    private ServerSocket serverSocket;

    /**
     * Create a new {@link ServerSocket} to handle connections to a binding specified via {@link Config}
     */
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

    /**
     * Serve the proxy via an infinite loop waiting listening for a connection to the {@link ServerSocket}. Upon observing
     * a connection a {@link ProxyConnectionAcceptor} is submitted to a {@link ThreadManager} as an acceptor.
     */
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
            this.poolManager.shutdown();
        }
    }
}
