package com.engineersbox.httpproxy.formatting.http.request;

import com.engineersbox.httpproxy.formatting.http.common.HTTPMethod;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;
import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.engineersbox.httpproxy.formatting.http.common.HTTPVersion;

import java.nio.charset.StandardCharsets;

public class HTTPRequestStartLine extends HTTPStartLine {

    public HTTPMethod method;
    public String target;

    public HTTPRequestStartLine(){
        this(HTTPMethod.GET, "/", HTTPVersion.HTTP11);
    }

    public HTTPRequestStartLine(final HTTPMethod method, final String target, final HTTPVersion version) {
        super(version);
        this.method = method;
        this.target = target;
    }

    @Override
    public byte[] toRaw() {
        return String.join(
            HTTPSymbols.START_LINE_DELIMITER,
            this.method.toString(),
            this.target,
            this.version.version
        ).concat(HTTPSymbols.HTTP_NEWLINE_DELIMITER).getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String toString() {
        return String.format(
            "{ method: %s, target: %s, version: %s }",
            this.method,
            this.target,
            this.version.version
        );
    }
}
