package com.engineersbox.httpproxy.formatting.http.common;

import com.engineersbox.httpproxy.exceptions.http.InvalidHTTPVersionException;

import java.util.regex.Pattern;

/**
 * HTTP versions
 */
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

    /**
     * Convert a {@link String} representation of an HTTP version enum to the accepted message format
     *
     * @param value Raw {@link String} of {@link HTTPVersion} entry
     * @return RFC compliant HTTP version format
     */
    private static String toEnumVersionFormat(final String value) {
        return value
            .replace("/", "")
            .replace(".", "");
    }

    /**
     * Convert a raw RFC compliant HTTP version to a {@link HTTPVersion} entry
     *
     * @param value {@link String} of RFC compliant HTTP version
     * @return Equivalent {@link HTTPVersion} entry
     * @throws InvalidHTTPVersionException If the given string does not match a {@link HTTPVersion} entry
     */
    public static HTTPVersion fromRaw(final String value) throws InvalidHTTPVersionException {
        if (!VERSION_REGEX.matcher(value).matches()) {
            throw new InvalidHTTPVersionException("Provided value: [" + value + "] does not conform to any known HTTP version");
        }
        return HTTPVersion.valueOf(toEnumVersionFormat(value));
    }
}
