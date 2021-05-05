package com.engineersbox.httpproxy.connection.socket;

import com.engineersbox.httpproxy.configuration.domain.servlet.Connections;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

/**
 * Factory to create {@link java.net.Socket} based on {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections}
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
 * the {@link SingletonSocketFactory#withSocketConfigs} method.
 */
public class SingletonSocketFactory extends SocketFactory {

    private static final Logger logger = LogManager.getLogger(SingletonSocketFactory.class);
    private Connections connectionsConfig = new Connections(
            10,
            10,
            0,
            1024,
            1024
    );

    /**
     * Supply a custom {@link com.engineersbox.httpproxy.configuration.domain.servlet.Connections} to use when creating
     * a {@link java.net.Socket}
     *
     * <br/><br/>
     *
     * @param connectionsConfig A custom configuration for the socket
     * @return {@link java.net.Socket}
     */
    public SingletonSocketFactory withSocketConfigs(final Connections connectionsConfig) {
        this.connectionsConfig = connectionsConfig;
        return this;
    }

    /**
     * Creates a {@link java.net.Socket} with a binding to the supplied {@code host} and {@code port}.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     * @param host the host name, or null for the loopback address.
     * @param port the port number.
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.Socket#Socket(String, int)
     */
    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
        socket.setSoTimeout(this.connectionsConfig.dropAfter);
        socket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        socket.setSendBufferSize(this.connectionsConfig.writeBufferSize);
        return socket;
    }


    /**
     * Creates a {@link java.net.Socket} with a binding to the supplied {@code host} and {@code port}, additionally supplies
     * the {@code localhost} and {@code localport} parameters to bind the socket to a local configuration.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     *
     * @param host the name of the remote host, or null for the loopback address.
     * @param port the remote port
     * @param localhost the local address the socket is bound to, or null for the anyLocal address.
     * @param localport the local port the socket is bound to, or zero for a system selected free port.
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.Socket#Socket(String, int, InetAddress, int)
     */
    @Override
    public Socket createSocket(final String host, final int port, final InetAddress localhost, final int localport) throws IOException {
        logger.debug(
            "Creating socket for [Host: " + host + "] [Port: " + port + "]"
            + " with local binding [Localhost: " + localhost.getHostAddress() + "] [Localport: " + localport + "]"
        );
        final Socket socket = new Socket(
            host,
            port,
            localhost,
            localport
        );
        socket.setSoTimeout(this.connectionsConfig.dropAfter);
        socket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        socket.setSendBufferSize(this.connectionsConfig.writeBufferSize);
        return socket;
    }

    /**
     * Creates a {@link java.net.Socket} with a binding to the supplied {@code host} and {@code port}.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     * @param host the IP address.
     * @param port the port number.
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.Socket#Socket(InetAddress, int)
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
        socket.setSoTimeout(this.connectionsConfig.dropAfter);
        socket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        socket.setSendBufferSize(this.connectionsConfig.writeBufferSize);
        return socket;
    }

    /**
     * Creates a {@link java.net.Socket} with a binding to the supplied {@code host} and {@code port}, additionally supplies
     * the {@code localhost} and {@code localport} parameters to bind the socket to a local configuration.
     *
     * <br/><br/>
     *
     * Additionally sets the {@link java.net.SocketOptions#SO_TIMEOUT} and {@link java.net.SocketOptions#SO_RCVBUF} options
     * based on the set {@link SingletonSocketFactory#connectionsConfig}.
     *
     * <br/><br/>
     *
     *
     * @param host the remote address
     * @param port the remote port
     * @param localhost the local address the socket is bound to, or null for the anyLocal address.
     * @param localport the local port the socket is bound to or zero for a system selected free port.
     * @return {@link java.net.ServerSocket}
     * @throws IOException If an I/O exception occurs whilst opening the socket
     *
     * <br/><br/>
     *
     * @see java.net.Socket#Socket(InetAddress, int, InetAddress, int)
     */
    @Override
    public Socket createSocket(final InetAddress host, final int port, final InetAddress localhost, final int localport) throws IOException {
        logger.debug(
            "Creating socket for [Host: " + host.getHostAddress() + "] [Port: " + port + "]"
            + " with local binding [Localhost: " + localhost.getHostAddress() + "] [Localport: " + localport + "]"
        );
        final Socket socket = new Socket(
            host,
            port,
            localhost,
            localport
        );
        socket.setSoTimeout(this.connectionsConfig.dropAfter);
        socket.setReceiveBufferSize(this.connectionsConfig.readerBufferSize);
        socket.setSendBufferSize(this.connectionsConfig.writeBufferSize);
        return socket;
    }
}
