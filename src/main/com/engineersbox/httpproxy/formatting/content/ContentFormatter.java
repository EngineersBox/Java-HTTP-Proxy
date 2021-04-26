package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.Config;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

    @Override
    public void replaceMatchingText(final String toMatch, final String replacement) {
        Elements els = this.document.body().getAllElements();
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                final String current = textNode.text();
                textNode.text(current.replace(toMatch, replacement));
            }
        }
    }

    @Override
    public void replaceAllMatchingText(final List<Pair<String, String>> toReplace) {
        toReplace.forEach(pair -> replaceMatchingText(pair.getLeft(), pair.getRight()));
    }

}
