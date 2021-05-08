package com.engineersbox.httpproxy.connection.threading;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.domain.servlet.SchedulingPolicy;
import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.*;

/**
 * Thead pool manager, using two independent {@link ThreadPoolExecutor}'s configured via {@link Config}.
 */
public class PoolManager implements ThreadManager {

    private final Logger logger = LogManager.getLogger(PoolManager.class);

    private final Config config;

    private final ThreadPoolExecutor acceptorExecutorService;
    private final ThreadPoolExecutor handlerExecutorService;

    @Inject
    public PoolManager(final Config config) {
        this.config = config;
        this.acceptorExecutorService = newRejectionHandledFixedThreadPool(config.servlet.threading.acceptorPoolSize);
        this.logger.info("Reserved acceptor fixed thread pool of size: " + config.servlet.threading.acceptorPoolSize);
        this.handlerExecutorService = newRejectionHandledFixedThreadPool(config.servlet.threading.handlerPoolSize);
        this.logger.info("Reserved handler fixed thread pool of size: " + config.servlet.threading.handlerPoolSize);
    }

    /**
     * Create a new ThreadPoolExecutor with the {@code corePoolSize} and {@code maxPoolSize} bound to the amount of
     * threads specified via the {@code threads} parameter.
     *
     * <br/><br/>
     *
     * The scheduling policy is supplied based on the configured enum value of {@code servlet.threading.schedulingPolicy}
     * in the {@link Config}.
     *
     * <br/><br/>
     *
     * This will default to no timeout for idle threads with a{@code keepAliveTime} of {@code 0L} milliseconds. Additionally,
     * a {@link LinkedBlockingQueue} will be used for the queue implementation
     *
     * @param threads Amount of threads for the {@code corePoolSize} and {@code maxPoolSize}
     * @return A {@link ThreadPoolExecutor} with provided threads and {@link RejectedExecutionHandler} from {@link Config}
     */
    private ThreadPoolExecutor newRejectionHandledFixedThreadPool(final int threads) {
        return new ThreadPoolExecutor(
                threads,
                threads,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(),
                this.config.servlet.threading.schedulingPolicy.getInstance()
        );
    }

    /**
     * Submits an acceptor task to the acceptor executor service ({@link PoolManager#acceptorExecutorService})
     *
     * @param task Acceptor as an instance of {@link BaseTrafficHandler}
     */
    @Override
    public void submitAcceptor(final BaseTrafficHandler task) {
        try {
            this.acceptorExecutorService.execute(task);
        } catch (final RejectedExecutionException e) {
            logger.error(String.format(
                    "Acceptor task %s was rejected with policy: %s",
                    task.getClass().getName(),
                    this.acceptorExecutorService.getRejectedExecutionHandler().getClass().getName()
            ), e);
        }
    }

    /**
     * Submits a handler task to the handler executor service ({@link PoolManager#handlerExecutorService})
     *
     * @param task Handler as an instance of {@link BaseTrafficHandler}
     */
    @Override
    public void submitHandler(final BaseTrafficHandler task) {
        try {
            this.handlerExecutorService.execute(task);
        } catch (final RejectedExecutionException e) {
            logger.error(String.format(
                    "Handler task %s was rejected with policy: %s",
                    task.getClass().getName(),
                    this.handlerExecutorService.getRejectedExecutionHandler().getClass().getName()
            ), e);
        }
    }

    /**
     * Schedule a shutdown operation for both the acceptor and handler thread pools
     */
    @Override
    public void shutdown() {
        this.acceptorExecutorService.shutdown();
        this.handlerExecutorService.shutdown();
    }

}
