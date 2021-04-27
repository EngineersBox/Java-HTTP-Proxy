package com.engineersbox.httpproxy.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SingletonSocketFactory extends SocketFactory {

    private static final Logger logger = LogManager.getLogger(SingletonSocketFactory.class);

    private void rfc2616CConnect(final Socket socket, final String host, final int port) throws IOException {
        socket.getOutputStream().write(String.format(
            "CONNECT %s:%d\r\n",
            host,
            port
        ).getBytes(StandardCharsets.UTF_8));
        logger.debug("Sent CONNECT request");
    }

    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
        rfc2616CConnect(socket, host, port);
        return socket;
    }

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
        rfc2616CConnect(socket, host, port);
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
        rfc2616CConnect(socket, host.getHostAddress(), port);
        return socket;
    }

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
        rfc2616CConnect(socket, host.getHostAddress(), port);
        return socket;
    }
}
