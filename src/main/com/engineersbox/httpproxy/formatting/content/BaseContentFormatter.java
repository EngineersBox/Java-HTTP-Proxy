package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.domain.policies.Replacement;

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
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String} within text nodes
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link Replacement} with matcher and replacement
     */
    void replaceMatchingText(final Replacement toReplace);

    /**
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String} from a {@link List}
     * of {@link Replacement} within text nodes
     *
     * <br/><br/>
     *
     * @param toReplace {@link List} of {@link Replacement} to use for replacement matching
     */
    void replaceAllMatchingText(final List<Replacement> toReplace);

    /**
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String}
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link Replacement} with matcher and replacement within link attributes
     */
    void replaceMatchingLink(final Replacement toReplace);

    /**
     * Replaces all instances a {@link java.util.regex.Pattern} with a {@link String} from a {@link List}
     * of {@link Replacement} within link attributes
     *
     * <br/><br/>
     *
     * @param toReplace {@link List} of {@link Replacement} to use for replacement matching
     */
    void replaceAllMatchingLinks(final List<Replacement> toReplace);

    /**
     * Retrieve the content string currently stored. If any replacements have been performed, this string will
     * contain them
     *
     * @return Formatted content {@link String}
     */
    String getContentString();

}
