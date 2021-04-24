package com.engineersbox.httpproxy.configuration.domain.servlet;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Packets {
    public final boolean dropOnMalformed;

    public Packets(final boolean dropOnMalformed) {
        this.dropOnMalformed = dropOnMalformed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Packets packets = (Packets) o;

        return new EqualsBuilder()
                .append(dropOnMalformed, packets.dropOnMalformed)
                .isEquals();
    }
}
