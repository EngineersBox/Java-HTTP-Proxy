package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Connections {
    public final int acceptorQueueSize;
    public final int handlerQueueSize;
    public final int dropAfter;
    public final int readerBufferSize;
    public final int writeBufferSize;

    public Connections(final int acceptorQueueSize, final int handlerQueueSize, final int dropAfter, final int readerBufferSize, final int writeBufferSize) {
        this.acceptorQueueSize = acceptorQueueSize;
        this.handlerQueueSize = handlerQueueSize;
        this.dropAfter = dropAfter;
        this.readerBufferSize = readerBufferSize;
        this.writeBufferSize = writeBufferSize;
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
                .append(readerBufferSize, that.readerBufferSize)
                .append(writeBufferSize, that.writeBufferSize)
                .isEquals();
    }
}
