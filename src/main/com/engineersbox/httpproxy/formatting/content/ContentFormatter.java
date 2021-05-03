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

public class ContentFormatter implements BaseContentFormatter {

    private final Config config;
    private String contentString;
    private Document document;

    @Inject
    public ContentFormatter(final Config config) {
        this.config = config;
    }

    @Override
    public void withContentString(final String contentString) {
        this.contentString = contentString;
        if (this.contentString != null) {
            this.document = Jsoup.parse(contentString);
        }
    }

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

    @Override
    public String getContentString() {
        this.contentString = this.document.toString();
        return this.contentString;
    }

}
