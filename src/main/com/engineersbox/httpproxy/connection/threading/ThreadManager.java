package com.engineersbox.httpproxy.connection.threading;

import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;

/**
 * Base interface for a thread manager, allowing submission of acceptor and handler tasks as instances of {@link BaseTrafficHandler}
 */
public interface ThreadManager {

    /**
     * Submit an acceptor task, to be handled by a thread pool
     *
     * <br/><br/>
     *
     * @param task Acceptor as an instance of {@link BaseTrafficHandler}
     */
    void submitAcceptor(final BaseTrafficHandler task);

    /**
     * Submit a handler task, to be handled by a thread pool
     *
     * <br/><br/>
     *
     * @param task Handler as an instance of {@link BaseTrafficHandler}
     */
    void submitHandler(final BaseTrafficHandler task);

    /**
     * Schedule a shutdown operation for both the acceptor and handler thread pools
     */
    void shutdown();

}
