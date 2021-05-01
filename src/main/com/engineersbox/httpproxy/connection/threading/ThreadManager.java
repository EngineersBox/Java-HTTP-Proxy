package com.engineersbox.httpproxy.connection.threading;

import com.engineersbox.httpproxy.connection.handler.BaseTrafficHandler;

public interface ThreadManager {

    void submitAcceptor(final BaseTrafficHandler task);

    void submitHandler(final BaseTrafficHandler task);

}
