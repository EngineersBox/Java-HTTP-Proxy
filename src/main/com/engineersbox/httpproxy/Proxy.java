package com.engineersbox.httpproxy;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.socket.SingletonServerSocketFactory;
import com.engineersbox.httpproxy.threading.PoolManager;
import com.engineersbox.httpproxy.threading.ProxyConnectionAcceptor;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;

public class Proxy {

    protected static final Logger logger = LogManager.getLogger(Proxy.class);

    public static Config config;
    private static final String CONFIG_PATH_ARG_NAME = "config.path";
    private static final String DEFAULT_CONFIG_PATH = "config.json";

    private static PoolManager poolManager;

    public static void main(String[] args) {
        String configFilePath = System.getProperty(CONFIG_PATH_ARG_NAME);
        if (configFilePath == null) {
            configFilePath = DEFAULT_CONFIG_PATH;
        }
        try {
            config = Config.fromFile(configFilePath);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        final Injector injector = Guice.createInjector(new ConfigModule());
        poolManager = injector.getInstance(PoolManager.class);

        final ServerSocket serverSocket;
        try {
            serverSocket = new SingletonServerSocketFactory().createServerSocket(
                config.servlet.binding.port,
                config.servlet.connections.acceptorQueueSize
            );
            poolManager.submitAcceptor(new ProxyConnectionAcceptor(
                serverSocket.accept(),
                config.target.host,
                config.target.port,
                poolManager
            ));
        } catch (final IOException e) {
            logger.error(e.getMessage(), e);
        }

    }
}
