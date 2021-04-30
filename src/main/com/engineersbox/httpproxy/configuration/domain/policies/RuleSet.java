package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class RuleSet {
    public final RuleType type;
    public final boolean isWildcard;
    public final String pattern;

    public RuleSet(final RuleType type, final boolean isWildcard, final String pattern) {
        this.type = type;
        this.isWildcard = isWildcard;
        this.pattern = pattern;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final RuleSet ruleSet = (RuleSet) o;

        return new EqualsBuilder()
                .append(isWildcard, ruleSet.isWildcard)
                .append(type, ruleSet.type)
                .append(pattern, ruleSet.pattern)
                .isEquals();
    }
}
