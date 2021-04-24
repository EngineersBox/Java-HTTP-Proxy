package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.List;

public class RuleSet {
    public final List<String> ip;
    public final List<String> url;

    public RuleSet(List<String> ip, List<String> url) {
        this.ip = ip;
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RuleSet ruleSet = (RuleSet) o;

        return new EqualsBuilder()
                .append(ip, ruleSet.ip)
                .append(url, ruleSet.url)
                .isEquals();
    }
}
