package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Enforcement {
    public final WhitelistBehaviour whitelistBehaviour;
    public final boolean allowRedirects;

    public Enforcement(final WhitelistBehaviour whitelistBehaviour, final boolean allowRedirects) {
        this.whitelistBehaviour = whitelistBehaviour;
        this.allowRedirects = allowRedirects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Enforcement that = (Enforcement) o;

        return new EqualsBuilder()
                .append(allowRedirects, that.allowRedirects)
                .append(whitelistBehaviour, that.whitelistBehaviour)
                .isEquals();
    }
}
