package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class WhitelistBehaviour {
    public final Behaviour ip;
    public final Behaviour url;

    public WhitelistBehaviour(final Behaviour ip, final Behaviour url) {
        this.ip = ip;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        WhitelistBehaviour whitelistBehaviour = (WhitelistBehaviour) o;

        return new EqualsBuilder()
                .append(ip, whitelistBehaviour.ip)
                .append(url, whitelistBehaviour.url)
                .isEquals();
    }
}
