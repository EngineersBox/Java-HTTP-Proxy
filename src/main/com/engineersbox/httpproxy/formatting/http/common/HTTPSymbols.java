package com.engineersbox.httpproxy.formatting.http.common;

import java.util.regex.Pattern;

public class HTTPSymbols {
    public static final String HTTP_NEWLINE_DELIMITER = "\r\n";
    public static final String START_LINE_DELIMITER = " ";
    public static final String HEADER_KEY_VALUE_DELIMITER = ": ";

    public static final Pattern HEADER_REGEX = Pattern.compile("([\\w-]+)" + HEADER_KEY_VALUE_DELIMITER + "(.*)");
}
