package com.engineersbox.httpproxy.connection;

import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.connection.stream.StreamCollector;
import com.google.inject.AbstractModule;

public class ConnectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ContentCollector.class)
            .to(StreamCollector.class);
    }
}
