package com.engineersbox.httpproxy.connection.threading;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Thead pool manager, using two independent {@link ThreadPoolExecutor}'s configured via {@link Config}.
 */
public class PoolManager implements ThreadManager {

    private final Logger logger = LogManager.getLogger(PoolManager.class);

    private final ThreadPoolExecutor acceptorExecutorService;
    private final ThreadPoolExecutor handlerExecutorService;


    @Inject
    public PoolManager(final Config config) {
        this.acceptorExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.servlet.threading.acceptorPoolSize);
        this.logger.info("Reserved acceptor fixed thread pool of size: " + config.servlet.threading.acceptorPoolSize);
        this.handlerExecutorService = (ThreadPoolExecutor) Executors.newFixedThreadPool(config.servlet.threading.handlerPoolSize);
        this.logger.info("Reserved handler fixed thread pool of size: " + config.servlet.threading.handlerPoolSize);
    }

    /**
     * Submits an acceptor task to the acceptor executor service ({@link PoolManager#acceptorExecutorService})
     *
     * <br/><br/>
     *
     * @param task Acceptor as an instance of {@link BaseTrafficHandler}
     */
    @Override
    public void submitAcceptor(final BaseTrafficHandler task) {
        this.acceptorExecutorService.execute(task);
    }

    /**
     * Submits a handler task to the handler executor service ({@link PoolManager#handlerExecutorService})
     *
     * <br/><br/>
     *
     * @param task Handler as an instance of {@link BaseTrafficHandler}
     */
    @Override
    public void submitHandler(final BaseTrafficHandler task) {
        this.handlerExecutorService.execute(task);
    }

}
