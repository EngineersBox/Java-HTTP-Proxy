package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;

public class Policies {
    public final Enforcement enforcement;
    public final List<RuleSet> rulesets;
    public final List<Replacement> textReplacements;
    public final List<Replacement> linkReplacements;

    public Policies(final Enforcement enforcement, final List<RuleSet> rulesets, final List<Replacement> textReplacements, final List<Replacement> linkReplacements) {
        this.enforcement = enforcement;
        this.rulesets = rulesets;
        this.textReplacements = textReplacements;
        this.linkReplacements = linkReplacements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Policies policies = (Policies) o;

        return new EqualsBuilder()
                .append(enforcement, policies.enforcement)
                .append(rulesets, policies.rulesets)
                .append(textReplacements, policies.textReplacements)
                .append(linkReplacements, policies.linkReplacements)
                .isEquals();
    }
}
