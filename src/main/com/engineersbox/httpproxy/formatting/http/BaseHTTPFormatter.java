package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.configuration.Config;
import com.engineersbox.httpproxy.exceptions.http.*;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

import java.nio.charset.Charset;

/**
 * Base interface for a converter to take a raw <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message in the format of {@link String} or {@code byte[]}
 * and convert it to a {@link HTTPMessage}
 *
 * @param <T> Instance of {@link HTTPStartLine} indicating whether the message is a request or response
 */
public interface BaseHTTPFormatter<T extends HTTPStartLine> {

    /**
     * Convert a {@code byte[]} into a {@link HTTPMessage}. This will encode the body into a string and store the raw
     * bytes separately. The encoded string will use the supplied {@link Charset} as its basis.
     *
     * @param raw {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param charset {@link Charset} to use as the encoding for the string representation of the body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body exceeds the max size relative to configuration supplies in a {@link Config}
     */
    HTTPMessage<T> fromRaw(final byte[] raw, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

    /**
     * Convert a {@code byte[]} into a {@link HTTPMessage}. This will encode the body into a string and store the raw
     * bytes separately. The encoded string will use the supplied {@link Charset} as its basis.
     *
     * @param raw {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param bodyBytes {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a> compliant message body
     * @param charset {@link Charset} to use as the encoding for the string representation of the body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body exceeds the max size relative to configuration supplies in a {@link Config}
     */
    HTTPMessage<T> fromRaw(final byte[] raw, final byte[] bodyBytes, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

    /**
     * Convert a {@link String} into a {@link HTTPMessage}. This will encode the body into a string and store the raw
     * bytes separately. Note that the raw bytes will retrieved from the supplied string using {@link String#getBytes()}
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body exceeds the max size relative to configuration supplies in a {@link Config}
     */
    HTTPMessage<T> fromRawString(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

    /**
     * Convert a {@link String} into a {@link HTTPMessage}. This will encode the body into a string and store the raw
     * bytes separately.
     *
     * @param raw {@link String} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html" target="_top">RFC 2616 Section 4</a> compliant message
     * @param bodyBytes {@code byte[]} representation of a <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.3" target="_top">RFC 2616 Section 4.3</a> compliant message body
     * @param classOfT Instance of {@link HTTPStartLine} to indicate whether this is a request or response
     * @return A {@link HTTPMessage}
     * @throws InvalidHTTPMessageFormatException Division of headers and body is invalid
     * @throws InvalidHTTPStartLineFormatException Start line for the request or response is invalid
     * @throws InvalidHTTPVersionException Version is unsupported or invalid
     * @throws InvalidHTTPHeaderException Headers are invalid or of the wrong format
     * @throws InvalidHTTPBodyException Body exceeds the max size relative to configuration supplies in a {@link Config}
     */
    HTTPMessage<T> fromRawString(final String raw, final byte[] bodyBytes, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

}
