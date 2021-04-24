package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Servlet {
    public final Threading threading;
    public final Connections connections;
    public final Packets packets;
    public final int cacheSize;

    public Servlet(final Threading threading, final Connections connections, final Packets packets, final int cacheSize) {
        this.threading = threading;
        this.connections = connections;
        this.packets = packets;
        this.cacheSize = cacheSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Servlet servlet = (Servlet) o;

        return new EqualsBuilder()
                .append(cacheSize, servlet.cacheSize)
                .append(threading, servlet.threading)
                .append(connections, servlet.connections)
                .append(packets, servlet.packets)
                .isEquals();
    }
}
