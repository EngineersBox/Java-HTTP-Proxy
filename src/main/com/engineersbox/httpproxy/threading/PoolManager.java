package com.engineersbox.httpproxy.threading;

import com.engineersbox.httpproxy.configuration.Config;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class PoolManager {

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

    public void submitAcceptor(final Runnable task) {
        this.acceptorExecutorService.execute(task);
    }

    public void submitHandler(final Runnable task) {
        this.handlerExecutorService.execute(task);
    }

}
