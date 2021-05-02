package com.engineersbox.httpproxy.formatting.content;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.configuration.domain.policies.TextReplacement;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.Pair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import java.util.List;
import java.util.regex.Pattern;

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
    public void replaceMatchingText(final Pattern toMatch, final String replacement) {
        Elements els = this.document.body().getAllElements();
        for (final Element e : els) {
            final List<TextNode> textNodes = e.textNodes();
            for (final TextNode textNode : textNodes) {
                final String current = textNode.text();
                textNode.text(toMatch.matcher(current).replaceAll(replacement));
            }
        }
    }

    @Override
    public void replaceAllMatchingText(final List<TextReplacement> toReplace) {
        toReplace.forEach(pair -> replaceMatchingText(pair.from, pair.to));
    }

    @Override
    public String getContentString() {
        this.contentString = this.document.toString();
        return this.contentString;
    }

}
