package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.domain.policies.TextReplacement;
import com.engineersbox.httpproxy.formatting.content.html.HTMLSymbols;
import com.google.inject.Inject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;

/**
 * Implementation of {@link BaseContentFormatter} using {@link Jsoup} to replace matching patterns in text and title
 * attributes of HTML nodes/elements.
 */
public class ContentFormatter implements BaseContentFormatter {

    private final Config config;
    private String contentString;
    private Document document;

    @Inject
    public ContentFormatter(final Config config) {
        this.config = config;
    }

    /**
     * See {@link BaseContentFormatter#withContentString(String)}
     *
     * <br/><br/>
     *
     * @param contentString {@link String} format of content
     */
    @Override
    public void withContentString(final String contentString) {
        this.contentString = contentString;
        if (this.contentString != null) {
            this.document = Jsoup.parse(contentString);
        }
    }

    /**
     * Replace any occurrence of the matcher in {@code pair} for the {@link TextNode#text()} value with the replacement
     * defined in the {@code pair}
     *
     * <br/><br/>
     *
     * Additionally, replace any text matching the matcher for the content of the {@link HTMLSymbols#TITLE_ATTRIBUTE}
     * if it exists on the parent of the text node ({@link TextNode#parent()});
     *
     * <br/><br/>
     *
     * @param textNode Current {@link TextNode} to perform replacement on
     * @param pair Instance of {@link TextReplacement} to base replacement on
     */
    private void replaceTextForElement(final TextNode textNode, final TextReplacement pair) {
        final String current = textNode.text();
        textNode.text(pair.from.matcher(current).replaceAll(pair.to));
        final Node parent = textNode.parent();
        if (parent.hasAttr(HTMLSymbols.TITLE_ATTRIBUTE)) {
            final String currentAttrValue = parent.attr(HTMLSymbols.TITLE_ATTRIBUTE);
            parent.attr(
                    HTMLSymbols.TITLE_ATTRIBUTE,
                    pair.from.matcher(currentAttrValue).replaceAll(pair.to)
            );
        }
    }

    /**
     * See {@link BaseContentFormatter#replaceMatchingText(TextReplacement)}
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link TextReplacement} with matcher and replacement
     */
    @Override
    public void replaceMatchingText(final TextReplacement toReplace) {
        final Elements els = this.document.body().getAllElements();
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                replaceTextForElement(textNode, toReplace);
            }
        }
    }

    /**
     * See {@link BaseContentFormatter#replaceAllMatchingText(List)}
     *
     * <br/><br/>
     *
     * @param toReplace {@link List} of {@link TextReplacement} to use for replacement matching
     */
    @Override
    public void replaceAllMatchingText(final List<TextReplacement> toReplace) {
        final Elements els = this.document.body().getAllElements();
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                toReplace.forEach(pair -> replaceTextForElement(textNode, pair));
            }
        }
    }

    /**
     * See {@link BaseContentFormatter#getContentString()}
     *
     * <br/><br/>
     *
     * @return Formatted content {@link String}
     */
    @Override
    public String getContentString() {
        this.contentString = this.document.toString();
        return this.contentString;
    }

}
