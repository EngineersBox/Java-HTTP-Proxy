package com.engineersbox.httpproxy.threading;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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

    @Override
    public void submitAcceptor(final BaseTrafficHandler task) {
        this.acceptorExecutorService.execute(task);
    }

    @Override
    public void submitHandler(final BaseTrafficHandler task) {
        this.handlerExecutorService.execute(task);
    }

}
