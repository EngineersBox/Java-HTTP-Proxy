package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class Policies {
    public final Enforcement enforcement;
    public final RuleSet whitelist;
    public final RuleSet blacklist;

    public Policies(final Enforcement enforcement, final RuleSet whitelist, final RuleSet blacklist) {
        this.enforcement = enforcement;
        this.whitelist = whitelist;
        this.blacklist = blacklist;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Policies policies = (Policies) o;

        return new EqualsBuilder()
                .append(enforcement, policies.enforcement)
                .append(whitelist, policies.whitelist)
                .append(blacklist, policies.blacklist)
                .isEquals();
    }
}
