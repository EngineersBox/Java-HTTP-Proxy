package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.domain.policies.Replacement;
import com.engineersbox.httpproxy.formatting.content.html.HTMLSymbols;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

    private final Logger logger = LogManager.getLogger(ContentFormatter.class);

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
     * @param pair Instance of {@link Replacement} to base replacement on
     * @return {@code true} if a text change was made, {@code false} otherwise
     */
    private boolean replaceTextForElement(final TextNode textNode, final Replacement pair) {
        final String current = textNode.text();
        textNode.text(pair.from.matcher(current).replaceAll(pair.to));
        final Node parent = textNode.parent();
        String currentAttrValue = null;
        if (parent.hasAttr(HTMLSymbols.TITLE_ATTRIBUTE)) {
            currentAttrValue = parent.attr(HTMLSymbols.TITLE_ATTRIBUTE);
            parent.attr(
                    HTMLSymbols.TITLE_ATTRIBUTE,
                    pair.from.matcher(currentAttrValue).replaceAll(pair.to)
            );
        }
        return !current.equals(textNode.text()) || (currentAttrValue !=  null && !currentAttrValue.equals(parent.attr(HTMLSymbols.TITLE_ATTRIBUTE)));
    }

    /**
     * See {@link BaseContentFormatter#replaceMatchingText(Replacement)}
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link Replacement} with matcher and replacement
     */
    @Override
    public void replaceMatchingText(final Replacement toReplace) {
        final Elements els = this.document.body().getAllElements();
        int changes = 0;
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                if (replaceTextForElement(textNode, toReplace)) {
                    changes++;
                }
            }
        }
        logger.debug(String.format(
                "Replaced %d matching text instances",
                changes
        ));
    }

    /**
     * See {@link BaseContentFormatter#replaceAllMatchingText(List)}
     *
     * <br/><br/>
     *
     * @param toReplace {@link List} of {@link Replacement} to use for replacement matching
     */
    @Override
    public void replaceAllMatchingText(final List<Replacement> toReplace) {
        final Elements els = this.document.body().getAllElements();
        int changes = 0;
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                changes += toReplace.stream().filter(pair -> replaceTextForElement(textNode, pair)).count();
            }
        }
        logger.debug(String.format(
                "Replaced %d matching text instances",
                changes
        ));
    }

    /**
     * Replace any occurrence of the matcher in {@code pair} for the {@link Element#attr(String)} (specifically against
     * the supplied {@code attribute}) value with the replacement defined in the {@code pair}
     *
     * <br/><br/>
     *
     * @param linkElement Current {@link Element} to perform replacement on
     * @param pair Instance of {@link Replacement} to base replacement on
     * @param attribute Attribute select to perform the replacement on
     * @return {@code true} if a link change was made, {@code false} otherwise
     */
    private boolean replaceLinkForElement(final Element linkElement, final Replacement pair, final String attribute) {
        final String currentAttrValue = linkElement.attr(attribute);
        linkElement.attr(attribute, pair.from.matcher(currentAttrValue).replaceAll(pair.to));
        return !currentAttrValue.equals(linkElement.attr(attribute));
    }

    /**
     * See {@link BaseContentFormatter#replaceMatchingLink(Replacement)}
     *
     * <br/><br/>
     *
     * @param toReplace Instance of {@link Replacement} with matcher and replacement
     */
    @Override
    public void replaceMatchingLink(final Replacement toReplace) {
        int changes = 0;
        final Elements links = this.document.select(HTMLSymbols.ANCHOR_LINK_CSS_SELECTOR);
        for (final Element link : links) {
            if (replaceLinkForElement(link, toReplace, HTMLSymbols.SOURCE_ATTRIBUTE)) {
                changes ++;
            }
        }

        final Elements media = this.document.select(HTMLSymbols.MEDIA_LINK_CSS_SELECTOR);
        for (final Element link : media) {
            if (replaceLinkForElement(link, toReplace, HTMLSymbols.HREF_ATTRIBUTE)) {
                changes++;
            }
        }

        final Elements imports = this.document.select(HTMLSymbols.IMPORT_LINK_CSS_SELECTOR);
        for (final Element link : imports) {
            if (replaceLinkForElement(link, toReplace, HTMLSymbols.HREF_ATTRIBUTE)) {
                changes++;
            }
        }
        logger.debug(String.format(
                "Replaced %d matching link instances",
                changes
        ));
    }

    /**
     * See {@link BaseContentFormatter#replaceAllMatchingLinks(List)}
     *
     * @param toReplace {@link List} of {@link Replacement} to use for replacement matching
     */
    @Override
    public void replaceAllMatchingLinks(List<Replacement> toReplace) {
        int changes = 0;
        final Elements anchors = this.document.select(HTMLSymbols.ANCHOR_LINK_CSS_SELECTOR);
        for (final Element link : anchors) {
            changes += toReplace.stream().filter(pair -> replaceLinkForElement(link, pair, HTMLSymbols.SOURCE_ATTRIBUTE)).count();
        }

        final Elements media = this.document.select(HTMLSymbols.MEDIA_LINK_CSS_SELECTOR);
        for (final Element link : media) {
            changes += toReplace.stream().filter(pair -> replaceLinkForElement(link, pair, HTMLSymbols.HREF_ATTRIBUTE)).count();
        }

        final Elements imports = this.document.select(HTMLSymbols.IMPORT_LINK_CSS_SELECTOR);
        for (final Element link : imports) {
            changes += toReplace.stream().filter(pair -> replaceLinkForElement(link, pair, HTMLSymbols.HREF_ATTRIBUTE)).count();
        }
        logger.debug(String.format(
                "Replaced %d matching link instances",
                changes
        ));
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
