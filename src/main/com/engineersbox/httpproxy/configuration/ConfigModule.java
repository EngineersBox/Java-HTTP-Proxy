package com.engineersbox.httpproxy.configuration;

import com.engineersbox.httpproxy.Proxy;
import com.google.inject.AbstractModule;

public class ConfigModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Config.class)
            .toInstance(Proxy.config);
    }

}
