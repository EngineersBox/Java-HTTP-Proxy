package com.engineersbox.httpproxy.connection.socket;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.engineersbox.httpproxy.configuration.domain.servlet.Connections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingletonServerSocketFactory extends ServerSocketFactory {

    private static final Logger logger = LogManager.getLogger(SingletonSocketFactory.class);
    private Connections connectionsConfig = new Connections(
            10,
            10,
            0,
            false,
            1024
    );

    private static final int MAX_ACCEPTOR_QUEUE_SIZE = 10;

    public SingletonServerSocketFactory withSocketConfigs(final Connections connectionsConfig) {
        this.connectionsConfig = connectionsConfig;
        return this;
    }

    private int validateQueueSize(int queueSize) {
        if (queueSize > MAX_ACCEPTOR_QUEUE_SIZE) {
            logger.warn("Queue size exceeds maximum: " + queueSize + " > " + MAX_ACCEPTOR_QUEUE_SIZE + ", defaulting to: " + MAX_ACCEPTOR_QUEUE_SIZE);
            queueSize = MAX_ACCEPTOR_QUEUE_SIZE;
        }
        return queueSize;
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(this.connectionsConfig.dropAfter);
        serverSocket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        return serverSocket;
    }

    @Override
    public ServerSocket createServerSocket(final int port, int queueSize) throws IOException {
        queueSize = validateQueueSize(queueSize);
        logger.debug("Creating server socket for [Port: " + port + "] [Queue Size: " + queueSize + "]");
        final ServerSocket serverSocket = new ServerSocket(port, queueSize);
        serverSocket.setSoTimeout(this.connectionsConfig.dropAfter);
        serverSocket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        return serverSocket;
    }

    @Override
    public ServerSocket createServerSocket(final int port, int queueSize, final InetAddress interfaceAddress) throws IOException {
        queueSize = validateQueueSize(queueSize);
        logger.debug("Creating server socket for [Port: " + port + "] [Queue Size: " + queueSize + "] [Interface: " + interfaceAddress.getHostAddress() + "]");
        final ServerSocket serverSocket = new ServerSocket(port, queueSize, interfaceAddress);
        serverSocket.setSoTimeout(this.connectionsConfig.dropAfter);
        serverSocket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        return serverSocket;
    }
}
