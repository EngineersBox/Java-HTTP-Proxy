package com.engineersbox.httpproxy.formatting.content;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface BaseContentFormatter {

    void withContentString(final String contentString);

    void replaceMatchingText(final String toMatch, final String replacement);

    void replaceAllMatchingText(final List<Pair<String, String>> toReplace);

}
