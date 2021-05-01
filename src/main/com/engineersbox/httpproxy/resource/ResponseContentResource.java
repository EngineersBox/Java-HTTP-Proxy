package com.engineersbox.httpproxy.resource;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;
import com.engineersbox.httpproxy.resolver.annotation.ContentType;
import com.engineersbox.httpproxy.resolver.annotation.Handler;
import com.engineersbox.httpproxy.resolver.annotation.HandlerType;

@Handler(HandlerType.CONTENT)
public class ResponseContentResource {

    @ContentType({"text/html"})
    public HTTPMessage<HTTPResponseStartLine> handleHTMLResponse(final HTTPMessage<HTTPResponseStartLine> message) {
        return null;
    }

}
