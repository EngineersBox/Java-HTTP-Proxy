package com.engineersbox.httpproxy.formatting.http.common;

/**
 * An base abstract class for HTTP start lines, both request and response.
 * The overlap between the two is the member property {@link HTTPStartLine#version}
 */
public abstract class HTTPStartLine {

    public final HTTPVersion version;

    public HTTPStartLine(){
        this(HTTPVersion.HTTP11);
    }

    public HTTPStartLine(final HTTPVersion version) {
        this.version = version;
    }

    /**
     * Convert the start line to a {@code byte[]}
     *
     * @return {@code byte[]}
     */
    public abstract byte[] toRaw();

    /**
     * Convert the start object into a loggable format.
     *
     * @return Loggable format of start line
     */
    public abstract String toDisplayableString();

}
