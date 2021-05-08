package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.request.HTTPRequestStartLine;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;
import com.engineersbox.httpproxy.resolver.annotation.Path;
import com.engineersbox.httpproxy.resolver.annotation.PathParam;
import com.engineersbox.httpproxy.resolver.annotation.method.GET;
import com.google.inject.Inject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Handler(HandlerType.REQUEST_CONTENT)
public class RequestContentResource {

    private final Logger logger = LogManager.getLogger(ResponseContentResource.class);

    private final Config config;

    @Inject
    public RequestContentResource(final Config config) {
        this.config = config;
    }

    @SuppressWarnings("unused")
    @GET
    @Path("/assets-140/{kind}")
    public HTTPMessage<HTTPRequestStartLine> handleHTMLResponse(final HTTPMessage<HTTPRequestStartLine> message, @PathParam("kind") final String resourceKind) {
        message.headers.replace("Host", this.config.target.host);
        message.headers.put("Connection", "close");
        logger.debug("Resource kind: " + resourceKind);
        return message;
    }

    @SuppressWarnings("unused")
    @GET
    @Path("/")
    public HTTPMessage<HTTPRequestStartLine> handleHTMLResponse(final HTTPMessage<HTTPRequestStartLine> message) {
        message.headers.replace("Host", this.config.target.host);
        message.headers.put("Connection", "close");
        logger.debug(String.format(
                "Replaced host header value to %s and added 'Connection: close'",
                this.config.target.host
        ));
        return message;
    }

}
