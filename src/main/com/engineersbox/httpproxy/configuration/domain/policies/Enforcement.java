package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Enforcement {
    public final WhitelistBehaviour whitelistBehaviour;
    public final boolean allow_redirects;

    public Enforcement(final WhitelistBehaviour whitelistBehaviour, final boolean allow_redirects) {
        this.whitelistBehaviour = whitelistBehaviour;
        this.allow_redirects = allow_redirects;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Enforcement that = (Enforcement) o;

        return new EqualsBuilder()
                .append(allow_redirects, that.allow_redirects)
                .append(whitelistBehaviour, that.whitelistBehaviour)
                .isEquals();
    }
}
