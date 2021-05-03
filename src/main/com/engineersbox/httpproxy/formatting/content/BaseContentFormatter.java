package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.domain.policies.TextReplacement;

import java.util.List;

public interface BaseContentFormatter {

    void withContentString(final String contentString);

    void replaceMatchingText(final TextReplacement toReplace);

    void replaceAllMatchingText(final List<TextReplacement> toReplace);

    String getContentString();

}
