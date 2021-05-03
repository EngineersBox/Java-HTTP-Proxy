package com.engineersbox.httpproxy.resolver;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.response.HTTPResponseStartLine;

/**
 * Base interface for an implementation of a resolver for a chosen resource delimiter (E.g. media type, encoding, etc)
 */
public interface ResourceResolver {

    /**
     * Matches the given {@link HTTPMessage} to a resource handler based on the chosen delimiter
     *
     * <br/><br/>
     *
     * @param message {@link HTTPMessage} to handle within a given resource
     * @return Handle {@link HTTPMessage}
     */
    HTTPMessage<HTTPResponseStartLine> match(final HTTPMessage<HTTPResponseStartLine> message);

}
