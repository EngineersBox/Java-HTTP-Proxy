package com.engineersbox.httpproxy.connection;

import com.engineersbox.httpproxy.Proxy;
import com.engineersbox.httpproxy.connection.stream.ContentCollector;
import com.engineersbox.httpproxy.connection.stream.StreamCollector;
import com.engineersbox.httpproxy.connection.threading.ThreadManager;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;

public class ConnectionModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(new TypeLiteral<ContentCollector<HTTPRequestStartLine>>(){})
            .to(new TypeLiteral<StreamCollector<HTTPRequestStartLine>>(){});
        bind(new TypeLiteral<ContentCollector<HTTPResponseStartLine>>(){})
                .to(new TypeLiteral<StreamCollector<HTTPResponseStartLine>>(){});
        bind(ThreadManager.class)
                .toInstance(Proxy.poolManager);
    }
}
