package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.domain.policies.TextReplacement;

import java.util.List;

/**
 * Base interface for a formatter to handle text replacement
 */
public interface BaseContentFormatter {

    /**
     * Supplies the formatter with the string to perform actions on
     *
     * <br/><br/>
     *
     * @param contentString {@link String} format of content
     */
    void withContentString(final String contentString);

    /**
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String}
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link TextReplacement} with matcher and replacement
     */
    void replaceMatchingText(final TextReplacement toReplace);

    /**
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String} from a {@link List}
     * of {@link TextReplacement}
     *
     * <br/><br/>
     *
     * @param toReplace {@link List} of {@link TextReplacement} to use for replacement matching
     */
    void replaceAllMatchingText(final List<TextReplacement> toReplace);

    /**
     * Retrieve the content string currently stored. If any replacements have been performed, this string will
     * contain them
     *
     * @return Formatted content {@link String}
     */
    String getContentString();

}
