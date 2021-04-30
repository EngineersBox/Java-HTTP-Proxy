package com.engineersbox.httpproxy.formatting.content;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class GZIPCompression {
    public static String unzip(byte[] bytes) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
        byte[] buf = new byte[1024];
        while((gzip.read(buf)) != -1) {
            sb.append(new String(buf, StandardCharsets.UTF_8).trim());
            buf = new byte[1024];
        }
        return sb.toString();
    }
    public static byte[] zip(String s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(new GZIPOutputStream(bos), StandardCharsets.UTF_8);
        osw.write(s);
        osw.close();
        return bos.toByteArray();
    }
}
