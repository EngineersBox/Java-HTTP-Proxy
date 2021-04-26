package com.engineersbox.httpproxy.formatting.http.common;

public abstract class HTTPStartLine {

    public HTTPVersion version;

    public HTTPStartLine(){
        this(HTTPVersion.HTTP11);
    }

    public HTTPStartLine(final HTTPVersion version) {
        this.version = version;
    }

    public abstract byte[] toRaw();

}
