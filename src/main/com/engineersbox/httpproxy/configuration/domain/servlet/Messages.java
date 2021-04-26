package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Messages {

    public final int maxBodySize;
    public final boolean dropOnMalformed;

    public Messages(final int maxBodySize, final boolean dropOnMalformed) {
        this.maxBodySize = maxBodySize;
        this.dropOnMalformed = dropOnMalformed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Messages messages = (Messages) o;

        return new EqualsBuilder()
                .append(maxBodySize, messages.maxBodySize)
                .append(dropOnMalformed, messages.dropOnMalformed)
                .isEquals();
    }
}
