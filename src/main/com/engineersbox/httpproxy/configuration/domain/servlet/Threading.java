package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Threading {
    public final int poolSize;
    public final SchedulingPolicy schedulingPolicy;

    public Threading(final int poolSize, final SchedulingPolicy schedulingPolicy) {
        this.poolSize = poolSize;
        this.schedulingPolicy = schedulingPolicy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Threading threading = (Threading) o;

        return new EqualsBuilder()
                .append(poolSize, threading.poolSize)
                .append(schedulingPolicy, threading.schedulingPolicy)
                .isEquals();
    }
}
