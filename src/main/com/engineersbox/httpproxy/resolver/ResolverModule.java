package com.engineersbox.httpproxy.resolver;

import com.google.inject.AbstractModule;

public class ResolverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ResourceResolver.class)
            .to(HandlerResolver.class);
    }
}
