package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

public class RuleSet {
    public final RuleType ruleType;
    public final boolean isWildcard;
    public final String pattern;

    public RuleSet(final RuleType ruleType, final boolean isWildcard, final String pattern) {
        this.ruleType = ruleType;
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
                .append(ruleType, ruleSet.ruleType)
                .append(pattern, ruleSet.pattern)
                .isEquals();
    }
}
