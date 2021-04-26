package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Connections {
    public final int acceptorQueueSize;
    public final int handlerQueueSize;
    public final int dropAfter;
    public final boolean dropOnFailedDNSLookup;
    public final int readerBufferSize;

    public Connections(final int acceptorQueueSize, final int handlerQueueSize, final int dropAfter, final boolean dropOnFailedDNSLookup, final int readerBufferSize) {
        this.acceptorQueueSize = acceptorQueueSize;
        this.handlerQueueSize = handlerQueueSize;
        this.dropAfter = dropAfter;
        this.dropOnFailedDNSLookup = dropOnFailedDNSLookup;
        this.readerBufferSize = readerBufferSize;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Connections that = (Connections) o;

        return new EqualsBuilder()
                .append(acceptorQueueSize, that.acceptorQueueSize)
                .append(handlerQueueSize, that.handlerQueueSize)
                .append(dropAfter, that.dropAfter)
                .append(dropOnFailedDNSLookup, that.dropOnFailedDNSLookup)
                .append(readerBufferSize, that.readerBufferSize)
                .isEquals();
    }
}
