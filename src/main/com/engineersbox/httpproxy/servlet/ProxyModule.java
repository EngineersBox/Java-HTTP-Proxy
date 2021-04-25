package com.engineersbox.httpproxy.servlet;

import com.engineersbox.httpproxy.Proxy;
import com.engineersbox.httpproxy.threading.ThreadManager;
import com.google.inject.AbstractModule;

public class ProxyModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ThreadManager.class)
            .toInstance(Proxy.poolManager);
    }

}
