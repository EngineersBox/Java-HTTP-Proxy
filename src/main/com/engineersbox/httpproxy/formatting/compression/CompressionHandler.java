package com.engineersbox.httpproxy.formatting.compression;

import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

public class CompressionHandler {

    /**
     * Match {@code Content-Encoding} header value against <a href="https://www.iana.org/assignments/http-parameters/http-parameters.xhtml#content-coding"IANA >HTTP Content Coding Registry</a>
     * values, returning {@link CompressionFormat} if a match is found. In the case that no matching compression method
     * is found, a {@code null} value is returned.
     *
     * <br/><br/>
     *
     * @param contentEncodingHeader Value of {@code Content-Encoding} header
     * @return {@link CompressionFormat} matching the encoding if found, {@code null} otherwise
     */
    public static CompressionFormat determineCompressionFormat(final String contentEncodingHeader) {
        if (HTTPSymbols.CONTENT_ENCODING_GZIP_REGEX.matcher(contentEncodingHeader).find()) {
            return CompressionFormat.GZIP;
        }
        if (HTTPSymbols.CONTENT_ENCODING_COMPRESS_REGEX.matcher(contentEncodingHeader).find()) {
            return CompressionFormat.LZW;
        }
        if (HTTPSymbols.CONTENT_ENCODING_DEFLATE_REGEX.matcher(contentEncodingHeader).find()) {
            return CompressionFormat.DEFLATE;
        }
        if (HTTPSymbols.CONTENT_ENCODING_BROTLI_REGEX.matcher(contentEncodingHeader).find()) {
            return CompressionFormat.BR;
        }
        if (HTTPSymbols.CONTENT_ENCODING_ZSTD_REGEX.matcher(contentEncodingHeader).find()) {
            return CompressionFormat.ZSTD;
        }
        return null;
    }

    /**
     * Decompress {@code byte[]} to {@link String} content using a given {@link Charset}. The {@code format} parameter
     *      * will be used to determine the compression algorithm to use.
     *
     * <br/><br/>
     *
     * @param bytes Compressed <a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a>
     *              compliant data
     * @param charset Encoding used uncompressed content
     * @param format Which compression algorithm to use from {@link CompressionFormat}
     * @return Uncompressed content using specified {@link Charset}
     * @throws IOException If any exception occurs during reading of compressed data from {@link ByteArrayInputStream}
     * @throws CompressorException If the data could not be compressed or encounters an exception
     */
    public static String decompress(final byte[] bytes, final Charset charset, final CompressionFormat format) throws IOException, CompressorException {
        final CompressorInputStream compressorInputStream = new CompressorStreamFactory()
                .createCompressorInputStream(
                    format.compressionFactoryName,
                    new BufferedInputStream(new ByteArrayInputStream(bytes))
                );
        final StringBuilder sb = new StringBuilder();
        final byte[] buffer = new byte[512];
        int n;
        while((n = compressorInputStream.read(buffer)) != -1) {
            sb.append(new String(buffer, 0, n, charset));
        }
        compressorInputStream.close();
        return sb.toString();
    }

    /**
     * Compress {@link String} content into {@code byte[]} using a given {@link Charset}. The {@code format} parameter
     * will be used to determine the compression algorithm to use.
     *
     * <br/><br/>
     *
     * @param str Content encoded with supplied {@link Charset}
     * @param charset Encoding used for content
     * @param format Which compression algorithm to use from {@link CompressionFormat}
     * @return Compressed content using specified {@link Charset}
     * @throws IOException If any exception occurs during reading of compressed data from {@link ByteArrayOutputStream}
     * @throws CompressorException If the data could not be compressed or encounters an exception
     */
    public static byte[] compress(final String str, final Charset charset, final CompressionFormat format) throws IOException, CompressorException {
        final ByteArrayOutputStream obj = new ByteArrayOutputStream();
        final CompressorOutputStream compressorOutputStream = new CompressorStreamFactory()
                .createCompressorOutputStream(
                    format.compressionFactoryName,
                    obj
                );
        compressorOutputStream.write(str.getBytes(charset));
        compressorOutputStream.close();
        return obj.toByteArray();
    }

}
