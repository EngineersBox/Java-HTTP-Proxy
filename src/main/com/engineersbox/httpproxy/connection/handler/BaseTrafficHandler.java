package com.engineersbox.httpproxy.connection.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class BaseTrafficHandler implements Runnable {

    private final Logger logger = LogManager.getLogger(BaseTrafficHandler.class);

    public abstract void task() throws Exception;

    public abstract void after();

    @Override
    public void run() {
        try {
            task();
        } catch (final Exception e) {
            logger.error(e, e);
        } finally {
            after();
        }
    }

}
