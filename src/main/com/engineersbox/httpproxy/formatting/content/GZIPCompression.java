package com.engineersbox.httpproxy.formatting.content;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompression {
    public static String unzip(byte[] bytes, final Charset charset) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
        byte[] buf = new byte[512];
        while((gzip.read(buf)) != -1) {
            sb.append(new String(buf, charset).trim());
            buf = new byte[512];
        }
        return sb.toString();
    }
    public static byte[] zip(final String str, final Charset charset) throws IOException {
        final ByteArrayOutputStream obj = new ByteArrayOutputStream();
        final GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes(charset));
        gzip.close();
        return obj.toByteArray();
    }
}
