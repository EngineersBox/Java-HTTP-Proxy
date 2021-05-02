package com.engineersbox.httpproxy.configuration.domain.policies;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.util.regex.Pattern;

public class TextReplacement {

    public final Pattern from;
    public final String to;

    public TextReplacement(final Pattern from, final String to) {
        this.from = from;
        this.to = to;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        final TextReplacement that = (TextReplacement) o;

        return new EqualsBuilder().append(from, that.from).append(to, that.to).isEquals();
    }
}
