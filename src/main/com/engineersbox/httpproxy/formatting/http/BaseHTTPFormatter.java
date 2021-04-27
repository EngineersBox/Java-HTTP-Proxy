package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

public interface BaseHTTPFormatter<T extends HTTPStartLine> {

    HTTPMessage<T> fromRaw(final byte[] raw, final int rawLength, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

    HTTPMessage<T> fromRawString(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

}
