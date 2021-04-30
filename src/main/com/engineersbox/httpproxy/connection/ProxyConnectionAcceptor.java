package com.engineersbox.httpproxy.connection;

import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.configuration.domain.servlet.Connections;
import com.engineersbox.httpproxy.connection.handler.BackwardTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.ForwardTrafficHandler;
import com.engineersbox.httpproxy.exceptions.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.socket.SingletonSocketFactory;
import com.engineersbox.httpproxy.threading.ThreadManager;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

public class ProxyConnectionAcceptor extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(ProxyConnectionAcceptor.class);

    private Connections connectionsConfig;

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

    public ProxyConnectionAcceptor withSocketConfigs(final Connections connectionsConfig) {
        this.connectionsConfig = connectionsConfig;
        return this;
    }

    @Override
    public void task() throws Exception {
        logger.info("Accepted client connection");
        final Socket server;
        final OutputStream outClient = localSocket.getOutputStream();
        final InputStream inClient = localSocket.getInputStream();
        try {
            server = new SingletonSocketFactory()
                    .withSocketConfigs(this.connectionsConfig)
                    .createSocket(host, port);
            logger.info("Established connection to server");
        } catch (IOException e) {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(localSocket.getOutputStream()));
            out.flush();
            throw new FailedToCreateServerSocketException(
                    "Could not create socket for [Host: " + this.host + "] [Port: " + this.port + "]",
                    e
            );
        }

        final OutputStream outServer = server.getOutputStream();
        final InputStream inServer = server.getInputStream();
        final Injector injector = Guice.createInjector(
                new ConfigModule(),
                new FormattingModule(),
                new ConnectionModule()
        );
        this.poolManager.submitHandler(
            injector.getInstance(BackwardTrafficHandler.class)
                .withStreams(
                    outServer,
                    inClient,
                    host,
                    localSocket
                )
        );
        logger.debug("Submitted BackwardTrafficHandler to PoolManager");
        this.poolManager.submitHandler(
            injector.getInstance(ForwardTrafficHandler.class)
                .withStreams(
                    inServer,
                    outClient,
                    server,
                    localSocket
                )
        );
        logger.debug("Submitted ForwardTrafficHandler to PoolManager");
    }

    @Override
    public void after() {}
}
