package com.engineersbox.httpproxy.socket;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class SingletonSocketFactory extends SocketFactory {

    private static final Logger logger = LogManager.getLogger(SingletonSocketFactory.class);


    @Override
    public Socket createSocket(final String host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
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
        return socket;
    }

    @Override
    public Socket createSocket(final InetAddress host, final int port) throws IOException {
        final Socket socket =  new Socket(
            host,
            port
        );
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
        return socket;
    }
}
