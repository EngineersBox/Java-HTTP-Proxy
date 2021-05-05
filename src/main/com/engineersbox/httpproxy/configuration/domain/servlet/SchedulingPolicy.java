package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

public enum SchedulingPolicy {
    ABORT(ThreadPoolExecutor.AbortPolicy.class),
    CALLER_RUNS(ThreadPoolExecutor.CallerRunsPolicy.class),
    DISCARD_OLDEST(ThreadPoolExecutor.DiscardOldestPolicy.class),
    DISCARD(ThreadPoolExecutor.DiscardPolicy.class);

    private final Logger logger = LogManager.getLogger(SchedulingPolicy.class);

    public final Class<? extends RejectedExecutionHandler> handler;

    SchedulingPolicy(final Class<? extends RejectedExecutionHandler> handler) {
        this.handler = handler;
    }

    public RejectedExecutionHandler getInstance() {
        try {
            return this.handler.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            logger.error(String.format(
                    "Could not retrieve an instance of RejectedExecutionHandler: %s, defaulting to ThreadPoolExecutor.AbortPolicy",
                    this.handler.getName()
            ), e);
            return new ThreadPoolExecutor.AbortPolicy();
        }
    }
}
