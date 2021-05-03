package com.engineersbox.httpproxy.resolver;

import com.google.inject.AbstractModule;

/**
 * Module containing a bind for {@link ResourceResolver}
 */
public class ResolverModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ResourceResolver.class)
            .to(HandlerResolver.class);
    }
}
