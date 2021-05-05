package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Servlet {
    public final Threading threading;
    public final Connections connections;
    public final Messages messages;
    public final Binding binding;

    public Servlet(final Threading threading, final Connections connections, final Messages messages, final Binding binding) {
        this.threading = threading;
        this.connections = connections;
        this.messages = messages;
        this.binding = binding;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Servlet servlet = (Servlet) o;

        return new EqualsBuilder()
                .append(threading, servlet.threading)
                .append(connections, servlet.connections)
                .append(messages, servlet.messages)
                .append(binding, servlet.binding)
                .isEquals();
    }
}
