package com.engineersbox.httpproxy.connection;

import com.engineersbox.httpproxy.configuration.ConfigModule;
import com.engineersbox.httpproxy.configuration.domain.servlet.Connections;
import com.engineersbox.httpproxy.connection.handler.BackwardTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.engineersbox.httpproxy.connection.handler.ForwardTrafficHandler;
import com.engineersbox.httpproxy.exceptions.socket.FailedToCreateServerSocketException;
import com.engineersbox.httpproxy.formatting.FormattingModule;
import com.engineersbox.httpproxy.connection.socket.SingletonSocketFactory;
import com.engineersbox.httpproxy.connection.threading.ThreadManager;
import com.engineersbox.httpproxy.resolver.ResolverModule;
import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.Socket;

/**
 * Handles an accepted connection to a configured {@link java.net.ServerSocket}, to a server and client {@link Socket}
 * instance.
 */
public class ProxyConnectionAcceptor extends BaseTrafficHandler {

    private final Logger logger = LogManager.getLogger(ProxyConnectionAcceptor.class);

    private Connections connectionsConfig;

    private Socket server;
    private final Socket localSocket;
    private final String host;
    private final int port;
    private final ThreadManager poolManager;

    private InputStream inClient;
    private InputStream inServer;
    private OutputStream outClient;
    private OutputStream outServer;

    public ProxyConnectionAcceptor(final Socket localSocket, final String host, final int port, final ThreadManager poolManager) {
        this.localSocket = localSocket;
        this.host = host;
        this.port = port;
        this.poolManager = poolManager;
    }

    /**
     * Supplies a configuration for {@link Connections} to use with {@link Socket} and {@link InputStream}/{@link OutputStream}
     * instantiation
     *
     * @param connectionsConfig Configuration for {@link Socket}'s and {@link InputStream}/{@link OutputStream}'s via an
     *                          instantiation of {@link Connections}
     * @return Current instance of {@link ProxyConnectionAcceptor} with applied configs
     */
    public ProxyConnectionAcceptor withSocketConfigs(final Connections connectionsConfig) {
        this.connectionsConfig = connectionsConfig;
        return this;
    }

    /**
     * Module containing all bindings for:
     *
     * <ul>
     *     <li>{@link Socket}'s</li>
     *     <li>{@link InputStream}'s</li>
     *     <li>/{@link OutputStream}'s</li>
     * </ul>
     */
    class TrafficHandlerModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(InputStream.class)
                    .annotatedWith(Names.named("Client In"))
                    .toInstance(inClient);
            bind(InputStream.class)
                    .annotatedWith(Names.named("Server In"))
                    .toInstance(inServer);
            bind(OutputStream.class)
                    .annotatedWith(Names.named("Client Out"))
                    .toInstance(outClient);
            bind(OutputStream.class)
                    .annotatedWith(Names.named("Server Out"))
                    .toInstance(outServer);
            bind(Socket.class)
                    .annotatedWith(Names.named("Client Socket"))
                    .toInstance(localSocket);
            bind(Socket.class)
                    .annotatedWith(Names.named("Server Socket"))
                    .toInstance(server);
        }
    }

    /**
     * Submits {@link BackwardTrafficHandler} to handler pool of {@link ThreadManager} instance for client-to-server
     * traffic and {@link ForwardTrafficHandler} to handler pool of {@link ThreadManager} instance for server-to-client
     * traffic.
     *
     * <br/><br/>
     *
     * Handlers are created via a {@link Injector} using the {@link Injector#getInstance(Class)} method
     *
     * @throws Exception Upon any exceptions encountered during thread lifecycle
     */
    @Override
    public void task() throws Exception {
        logger.debug("Accepted client connection");
        this.outClient = localSocket.getOutputStream();
        this.inClient = localSocket.getInputStream();
        try {
            this.server = new SingletonSocketFactory()
                    .withSocketConfigs(this.connectionsConfig)
                    .createSocket(host, port);
            logger.debug("Established connection to server");
        } catch (IOException e) {
            final PrintWriter out = new PrintWriter(new OutputStreamWriter(localSocket.getOutputStream()));
            out.flush();
            throw new FailedToCreateServerSocketException(
                    "Could not create socket for [Host: " + this.host + "] [Port: " + this.port + "]",
                    e
            );
        }

        this.outServer = server.getOutputStream();
        this.inServer = server.getInputStream();
        final Injector injector = Guice.createInjector(
                new ConfigModule(),
                new FormattingModule(),
                new ConnectionModule(),
                new TrafficHandlerModule(),
                new ResolverModule()
        );
        this.poolManager.submitHandler(
            injector.getInstance(BackwardTrafficHandler.class)
        );
        logger.debug("Submitted BackwardTrafficHandler to PoolManager");
        this.poolManager.submitHandler(
            injector.getInstance(ForwardTrafficHandler.class)
        );
        logger.debug("Submitted ForwardTrafficHandler to PoolManager");
    }

    @Override
    public void after() {}
}
