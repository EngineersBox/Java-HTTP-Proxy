package com.engineersbox.httpproxy.formatting.http;

import com.engineersbox.httpproxy.exceptions.http.*;
import com.engineersbox.httpproxy.formatting.http.common.HTTPMessage;
import com.engineersbox.httpproxy.formatting.http.common.HTTPStartLine;

import java.nio.charset.Charset;

public interface BaseHTTPFormatter<T extends HTTPStartLine> {

    HTTPMessage<T> fromRaw(final byte[] raw, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;
    HTTPMessage<T> fromRaw(final byte[] raw, final byte[] bodyBytes, final Charset charset, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

    HTTPMessage<T> fromRawString(final String raw, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;
    HTTPMessage<T> fromRawString(final String raw, final byte[] bodyBytes, final Class<T> classOfT) throws InvalidHTTPMessageFormatException, InvalidHTTPStartLineFormatException, InvalidHTTPVersionException, InvalidHTTPHeaderException, InvalidHTTPBodyException;

}
