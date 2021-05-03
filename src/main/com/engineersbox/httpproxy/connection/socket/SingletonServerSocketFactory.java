package com.engineersbox.httpproxy.connection.socket;

import javax.net.ServerSocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

import com.engineersbox.httpproxy.configuration.domain.servlet.Connections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Factory to create {@link java.net.ServerSocket} based on {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections}
 * which are optionally set. The default {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections} configuration
 * used is:
 *
 * <ul>
 *     <li>Acceptor queue size: {@code 10}</li>
 *     <li>Handler queue size: {@code 10}</li>
 *     <li>Drop after: {@code 0}</li>
 *     <li>Drop on failed DNS lookup: {@code false}</li>
 *     <li>Read buffer size: {@code 1024}</li>
 * </ul>
 *
 * A custom {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections} configuration can be supplied via
 * the {@link SingletonServerSocketFactory#withSocketConfigs} method.
 */
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

    /**
     * Supply a custom {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections} to use when creating
     * a {@link java.net.ServerSocket}
     *
     * <br/><br/>
     *
     * @param connectionsConfig A custom configuration for the socket
     * @return {@link java.net.ServerSocket}
     */
    public SingletonServerSocketFactory withSocketConfigs(final Connections connectionsConfig) {
        this.connectionsConfig = connectionsConfig;
        return this;
    }

    /**
     * Validates a given queue size against the configured maximum ({@link SingletonServerSocketFactory#MAX_ACCEPTOR_QUEUE_SIZE}).
     * In the case that it exceeds this value or is below {@code 0}, the maximum is returned. Otherwise the supplied
     * queue size is returned.
     *
     * <br/><br/>
     *
     * @param queueSize A socket backlog queue size
     * @return {@link SingletonServerSocketFactory#MAX_ACCEPTOR_QUEUE_SIZE} if the {@code queueSize} is larger than
     * {@link SingletonServerSocketFactory#MAX_ACCEPTOR_QUEUE_SIZE} or below {@code 0}. Otherwise returns {@code queueSize}
     */
    private int validateQueueSize(int queueSize) {
        if (queueSize < 0) {
            logger.warn("Queue size is negative: " + queueSize + " < 0, defaulting to: " + MAX_ACCEPTOR_QUEUE_SIZE);
            return MAX_ACCEPTOR_QUEUE_SIZE;
        }

        if (queueSize > MAX_ACCEPTOR_QUEUE_SIZE) {
            logger.warn("Queue size exceeds maximum: " + queueSize + " > " + MAX_ACCEPTOR_QUEUE_SIZE + ", defaulting to: " + MAX_ACCEPTOR_QUEUE_SIZE);
            queueSize = MAX_ACCEPTOR_QUEUE_SIZE;
        }
        return queueSize;
    }

    /**
     * Creates a {@link java.net.ServerSocket} with a binding to the supplied {@code port}.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonServerSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     * @param port the port number, or 0 to use a port number that is automatically allocated
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.ServerSocket#ServerSocket(int)
     */
    @Override
    public ServerSocket createServerSocket(final int port) throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port);
        serverSocket.setSoTimeout(this.connectionsConfig.dropAfter);
        serverSocket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        return serverSocket;
    }

    /**
     * Creates a {@link java.net.ServerSocket} with a binding to the supplied {@code port} and a backlog from the {@code queueSize}.
     * Note that the {@code queueSize} is validated via the {@link SingletonServerSocketFactory#validateQueueSize} method.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonServerSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     * @param port the port number, or 0 to use a port number that is automatically allocated
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.ServerSocket#ServerSocket(int, int)
     */
    @Override
    public ServerSocket createServerSocket(final int port, int queueSize) throws IOException {
        queueSize = validateQueueSize(queueSize);
        logger.debug("Creating server socket for [Port: " + port + "] [Queue Size: " + queueSize + "]");
        final ServerSocket serverSocket = new ServerSocket(port, queueSize);
        serverSocket.setSoTimeout(this.connectionsConfig.dropAfter);
        serverSocket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        return serverSocket;
    }

    /**
     * Creates a {@link java.net.ServerSocket} with a binding to the supplied {@code port}, backlog from the {@code queueSize}
     * and an address binding to {@code interfaceAddress} for a given {@link java.net.InetAddress}.
     * Note that the {@code queueSize} is validated via the {@link SingletonServerSocketFactory#validateQueueSize} method.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonServerSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     * @param port the port number, or 0 to use a port number that is automatically allocated
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.ServerSocket#ServerSocket(int, int, InetAddress)
     */
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
