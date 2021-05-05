package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Messages {

    public final int maxBodySize;

    public Messages(final int maxBodySize) {
        this.maxBodySize = maxBodySize;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Messages messages = (Messages) o;

        return new EqualsBuilder()
                .append(maxBodySize, messages.maxBodySize)
                .isEquals();
    }
}
