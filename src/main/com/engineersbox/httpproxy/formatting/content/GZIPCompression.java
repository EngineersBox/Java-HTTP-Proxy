package com.engineersbox.httpproxy.formatting.content;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a> compliant Lempel-Ziv coding (LZ77) with a 32 bit CRC
 * GZIP utility class for compression and decompression.
 */
public class GZIPCompression {

    /**
     * Decompress {@code byte[]} to {@link String} content using a given {@link Charset}
     *
     * <br/><br/>
     *
     * @param bytes Compressed <a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a>
     *              compliant data
     * @param charset Encoding used uncompressed content
     * @return Uncompressed content using specified {@link Charset}
     * @throws IOException If any exception occurs during reading of compressed data from {@link GZIPInputStream} or {@link ByteArrayInputStream}
     */
    public static String decompress(byte[] bytes, final Charset charset) throws IOException {
        final StringBuilder sb = new StringBuilder();
        final GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(bytes));
        byte[] buf = new byte[512];
        while((gzip.read(buf)) != -1) {
            sb.append(new String(buf, charset).trim());
            buf = new byte[512];
        }
        return sb.toString();
    }

    /**
     * Compress {@link String} content into {@code byte[]} using a given {@link Charset}
     *
     * <br/><br/>
     *
     * @param str Content encoded with supplied {@link Charset}
     * @param charset Encoding used for content
     * @return Compressed content using specified {@link Charset}
     * @throws IOException If any exception occurs during reading of compressed data from {@link GZIPOutputStream} or {@link ByteArrayOutputStream}
     */
    public static byte[] compress(final String str, final Charset charset) throws IOException {
        final ByteArrayOutputStream obj = new ByteArrayOutputStream();
        final GZIPOutputStream gzip = new GZIPOutputStream(obj);
        gzip.write(str.getBytes(charset));
        gzip.close();
        return obj.toByteArray();
    }
}
