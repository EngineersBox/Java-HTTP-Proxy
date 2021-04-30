package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Binding {
    public final String host;
    public final int port;

    public Binding(final String host, final int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Binding binding = (Binding) o;

        return new EqualsBuilder()
                .append(port, binding.port)
                .append(host, binding.host)
                .isEquals();
    }
}
