package com.engineersbox.httpproxy.formatting.http.common;

import com.engineersbox.httpproxy.exceptions.InvalidHTTPVersionException;

import java.util.regex.Pattern;

public enum HTTPVersion {
    HTTP09("HTTP/0.9"),
    HTTP10("HTTP/1.0"),
    HTTP11("HTTP/1.1"),
    HTTP20("HTTP/2.0"),
    HTTP30("HTTP/3.0");

    private static final Pattern VERSION_REGEX = Pattern.compile("HTTP/[0-3]\\.[019]");
    public final String version;

    HTTPVersion(final String version) {
        this.version = version;
    }

    private static String toEnumVersionFormat(final String value) {
        return value
            .replace("/", "")
            .replace(".", "");
    }

    public static HTTPVersion fromRaw(final String value) throws InvalidHTTPVersionException {
        if (!VERSION_REGEX.matcher(value).matches()) {
            throw new InvalidHTTPVersionException("Provided value: [" + value + "] does not conform to any known HTTP version");
        }
        return HTTPVersion.valueOf(toEnumVersionFormat(value));
    }
}
