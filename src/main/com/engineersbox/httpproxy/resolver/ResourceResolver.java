package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;

public interface ResourceResolver {

    HTTPMessage<HTTPResponseStartLine> match(final HTTPMessage<HTTPResponseStartLine> message);

}
