package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.formatting.content.BaseContentFormatter;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.MediaType;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Handler(HandlerType.RESPONSE_CONTENT)
public class ResponseContentResource {

    private final Logger logger = LogManager.getLogger(ResponseContentResource.class);

    private final Config config;
    private final BaseContentFormatter contentFormatter;

    @Inject
    public ResponseContentResource(final Config config, final BaseContentFormatter contentFormatter) {
        this.config = config;
        this.contentFormatter = contentFormatter;
    }

    /**
     * Handler for {@code text/html} media types. This will reformat text and {@code link} attributes in HTML element
     * of the message boyd based on replacement configs supplied in {@link Config}.
     *
     * @param message {@link HTTPMessage} to format the body of
     * @return Formatted {@link HTTPMessage} with replaced text and {@code link} attributes
     */
    @SuppressWarnings("unused")
    @MediaType("text/html")
    public HTTPMessage<HTTPResponseStartLine> handleHTMLResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        this.contentFormatter.withContentString(message.body);
        this.contentFormatter.replaceAllMatchingText(this.config.policies.textReplacements);
        this.contentFormatter.replaceAllMatchingLinks(this.config.policies.linkReplacements);
        message.setBody(this.contentFormatter.getContentString());
        logger.info("Replaced all text and link values");
        return message;
    }

}
