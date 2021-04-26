package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.exceptions.*;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

public interface BaseHTTPFormatter<T extends HTTPStartLine> {

    HTTPMessage<T> fromRaw(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

}
