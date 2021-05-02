package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.ContentType;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Handler(HandlerType.CONTENT)
public class ResponseContentResource {

    private final Logger logger = LogManager.getLogger(ResponseContentResource.class);

    private final Config config;
    private final BaseContentFormatter contentFormatter;

    @Inject
    public ResponseContentResource(final Config config, final BaseContentFormatter contentFormatter) {
        this.config = config;
        this.contentFormatter = contentFormatter;
    }

    @SuppressWarnings("unused")
    @ContentType("text/html")
    public HTTPMessage<HTTPResponseStartLine> handleHTMLResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        final String orig = message.body;
        this.contentFormatter.withContentString(message.body);
        this.contentFormatter.replaceAllMatchingText(this.config.policies.textReplacements);
        message.setBody(this.contentFormatter.getContentString());
        System.out.println(orig.equals(message.body));
        logger.info("Replaced all text values");
        return message;
    }

    @SuppressWarnings("unused")
    @ContentType("text/javascript")
    public HTTPMessage<HTTPResponseStartLine> handleJavascriptResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        return message;
    }

}
