package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.ContentType;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

@Handler(HandlerType.CONTENT)
public class ResponseContentResource {

    private final Logger logger = LogManager.getLogger(ResponseContentResource.class);

    private final Config config;
    private final BaseContentFormatter contentFormatter;

    private List<Pair<String, String>> toReplace = ImmutableList.of(
            ImmutablePair.of("Sydney", "New York"),
            ImmutablePair.of("sydney", "New York")
    );

    @Inject
    public ResponseContentResource(final Config config, final BaseContentFormatter contentFormatter) {
        this.config = config;
        this.contentFormatter = contentFormatter;
    }

    @ContentType({"text/html"})
    public HTTPMessage<HTTPResponseStartLine> handleHTMLResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        this.contentFormatter.withContentString(message.body);
        this.contentFormatter.replaceAllMatchingText(this.toReplace);
        message.setBody(this.contentFormatter.getContentString());
        return message;
    }

}
