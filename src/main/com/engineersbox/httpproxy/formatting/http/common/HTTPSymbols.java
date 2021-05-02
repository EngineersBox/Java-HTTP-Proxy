package com.engineersbox.httpproxy.formatting.http.common;

import java.util.regex.Pattern;

public class HTTPSymbols {
    public static final String HTTP_HEADER_NEWLINE_DELIMITER = "\r\n";
    public static final String START_LINE_DELIMITER = " ";
    public static final String HEADER_KEY_VALUE_DELIMITER = ": ";
    public static final char NULL_BYTE = '\0';
    public static final String HEADER_VALUE_LIST_DELIMITER = ";";

    public static final Pattern HEADER_REGEX = Pattern.compile("([\\w-]+)" + HEADER_KEY_VALUE_DELIMITER + "(.*)");

    public static final String CONTENT_LENGTH_HEADER = "Content-Length";
    public static final String CONTENT_TYPE_HEADER = "Content-Type";
    public static final String CONTENT_TYPE_CHARSET_KEY = "charset=";
    public static final Pattern CONTENT_TYPE_TEXT_TYPE_REGEX = Pattern.compile("text/\\w*");
    public static final Pattern CONTENT_TYPE_IMAGE_REGEX = Pattern.compile("image/\\w*");

    public static final String CONTENT_ENCODING_HEADER = "Content-Encoding";
    public static final String CONTENT_ENCODING_IDENTITY = "identity";

    public static final String TRANSFER_ENCODING_HEADER = "Transfer-Encoding";
    public static final String TRANSFER_ENCODING_IDENTITY = "identity";
    public static final Pattern TRANSFER_ENCODING_SIZE_REGEX = Pattern.compile("[0-9]+\r\n");
    public static final String TRANSFER_ENCODING_TERMINATION = "0" + HTTP_HEADER_NEWLINE_DELIMITER;
}
