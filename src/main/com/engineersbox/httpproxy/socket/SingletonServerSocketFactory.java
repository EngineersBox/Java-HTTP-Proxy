package com.engineersbox.httpproxy.socket;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SingletonServerSocketFactory extends ServerSocketFactory {

    private static final Logger logger = LogManager.getLogger(SingletonSocketFactory.class);

    private static final int MAX_ACCEPTOR_QUEUE_SIZE = 10;

    private int validateQueueSize(int queueSize) {
        if (queueSize > MAX_ACCEPTOR_QUEUE_SIZE) {
            logger.warn("Queue size exceeds maximum: " + queueSize + " > " + MAX_ACCEPTOR_QUEUE_SIZE + ", defaulting to: " + MAX_ACCEPTOR_QUEUE_SIZE);
            queueSize = MAX_ACCEPTOR_QUEUE_SIZE;
        }
        return queueSize;
    }

    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        return new ServerSocket(port);
    }

    @Override
    public ServerSocket createServerSocket(final int port, int queueSize) throws IOException {
        queueSize = validateQueueSize(queueSize);
        logger.debug("Creating server socket for [Port: " + port + "] [Queue Size: " + queueSize + "]");
        return new ServerSocket(port, queueSize);
    }

    @Override
    public ServerSocket createServerSocket(final int port, int queueSize, final InetAddress interfaceAddress) throws IOException {
        queueSize = validateQueueSize(queueSize);
        logger.debug("Creating server socket for [Port: " + port + "] [Queue Size: " + queueSize + "] [Interface: " + interfaceAddress.getHostAddress() + "]");
        return new ServerSocket(port, queueSize, interfaceAddress);
    }
}
