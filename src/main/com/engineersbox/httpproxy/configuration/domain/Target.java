package com.engineersbox.httpproxy.configuration.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Target {
    public final String host;
    public final int port;

    public Target(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Target target = (Target) o;

        return new EqualsBuilder()
                .append(port, target.port)
                .append(host, target.host)
                .isEquals();
    }
}
