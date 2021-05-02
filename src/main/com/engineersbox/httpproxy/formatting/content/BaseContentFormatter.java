package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.domain.policies.TextReplacement;

import java.util.List;
import java.util.regex.Pattern;

public interface BaseContentFormatter {

    void withContentString(final String contentString);

    void replaceMatchingText(final Pattern toMatch, final String replacement);

    void replaceAllMatchingText(final List<TextReplacement> toReplace);

    String getContentString();

}
