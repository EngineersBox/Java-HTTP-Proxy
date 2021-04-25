package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Threading {
    public final int acceptorPoolSize;
    public final int handlerPoolSize;
    public final SchedulingPolicy schedulingPolicy;

    public Threading(final int acceptorPoolSize, final int handlerPoolSize, final SchedulingPolicy schedulingPolicy) {
        this.acceptorPoolSize = acceptorPoolSize;
        this.handlerPoolSize = handlerPoolSize;
        this.schedulingPolicy = schedulingPolicy;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Threading threading = (Threading) o;

        return new EqualsBuilder()
                .append(acceptorPoolSize, threading.acceptorPoolSize)
                .append(handlerPoolSize, threading.handlerPoolSize)
                .append(schedulingPolicy, threading.schedulingPolicy)
                .isEquals();
    }
}
