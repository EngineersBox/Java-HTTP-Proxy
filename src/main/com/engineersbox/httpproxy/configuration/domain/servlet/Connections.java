package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Connections {
    public final int dropAfter;
    public final boolean dropOnFailedDNSLookup;

    public Connections(final int dropAfter, final boolean dropOnFailedDNSLookup) {
        this.dropAfter = dropAfter;
        this.dropOnFailedDNSLookup = dropOnFailedDNSLookup;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Connections that = (Connections) o;

        return new EqualsBuilder()
                .append(dropAfter, that.dropAfter)
                .append(dropOnFailedDNSLookup, that.dropOnFailedDNSLookup)
                .isEquals();
    }
}
