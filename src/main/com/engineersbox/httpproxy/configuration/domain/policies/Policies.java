package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;

public class Policies {
    public final Enforcement enforcement;
    public final List<RuleSet> rulesets;
    public List<TextReplacement> textReplacements;

    public Policies(final Enforcement enforcement, final List<RuleSet> rulesets, final List<TextReplacement> textReplacements) {
        this.enforcement = enforcement;
        this.rulesets = rulesets;
        this.textReplacements = textReplacements;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final Policies policies = (Policies) o;

        return new EqualsBuilder()
                .append(enforcement, policies.enforcement)
                .append(rulesets, policies.rulesets)
                .isEquals();
    }
}
