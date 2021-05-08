package com.engineersbox.httpproxy.connection.handler;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * An abstract handler for directional traffic implementing the {@link java.lang.Runnable} functional interface
 *
 * <br/><br/>
 *
 * An implementation of this class is designed to be run within a threaded context, specifically a managed thread
 * pool of some kind. Upon submitting an implementation to the thread pool, the {@link BaseTrafficHandler#run} method
 * will be invoked to begin traffic handling.
 *
 * <br/><br/>
 *
 * The {@link BaseTrafficHandler#task} method is invoked to handle the traffic, which is follows by the
 * {@link BaseTrafficHandler#after} call to run any defined clean up.
 *
 * <br/><br/>
 *
 * The {@link BaseTrafficHandler#after} method will run irrespective of any exceptions
 * thrown during the {@link BaseTrafficHandler#task} call. As such it is advised that it handles any
 * contextual exceptions.
 */
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
