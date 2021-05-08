package com.engineersbox.httpproxy.formatting.compression;

import com.engineersbox.httpproxy.formatting.http.common.HTTPSymbols;
import com.google.common.collect.ImmutableSet;
import org.apache.commons.compress.compressors.CompressorException;
import org.apache.commons.compress.compressors.CompressorInputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Handler for compressing and decompressing data with a given format. Supports 5 standard compression methods used
 * in <a href="https://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.5" target="_top">RFC 2616 Section 3.5</a>.
 *
 * <ul>
 *     <li>{@code gzip}: Lempel-Ziv coding (LZ77) with a 32 bit CRC (<a href="https://www.w3.org/Protocols/rfc1952/rfc1952.html" target="_top">RFC 1952</a>).</li>
 *     <li>{@code compress}: Adaptive Lempel-Ziv-Welch coding (LZW) (<a href="https://tools.ietf.org/html/rfc7230" target="_top">RFC 7230</a>)</li>
 *     <li>
 *         {@code deflate}: zlib format (<a href="https://www.w3.org/Protocols/rfc1950/rfc1950.html" target="_top">RFC 1950</a>)
 *           with deflate compression mechanism (<a href="https://www.w3.org/Protocols/rfc1951/rfc1951.html" target="_top">RFC 1951</a>).
 *     </li>
 *     <li>{@code zstd}: ZStandard compression standard (<a href="https://datatracker.ietf.org/doc/draft-kucherawy-rfc8478bis/05/" target="_top">RFC 8478</a>)</li>
 *     <li>{@code br}: Brotli compression standard (<a href="https://tools.ietf.org/html/rfc7932" target="_top">RFC 7932</a>)</li>
 * </ul>
 */
public class CompressionHandler {

    private final static Set<Pair<Pattern, CompressionFormat>> FORMAT_REGEXES = ImmutableSet.of(
            ImmutablePair.of(HTTPSymbols.CONTENT_ENCODING_GZIP_REGEX, CompressionFormat.GZIP),
            ImmutablePair.of(HTTPSymbols.CONTENT_ENCODING_COMPRESS_REGEX, CompressionFormat.LZW),
            ImmutablePair.of(HTTPSymbols.CONTENT_ENCODING_DEFLATE_REGEX, CompressionFormat.DEFLATE),
            ImmutablePair.of(HTTPSymbols.CONTENT_ENCODING_BROTLI_REGEX, CompressionFormat.BR),
            ImmutablePair.of(HTTPSymbols.CONTENT_ENCODING_ZSTD_REGEX, CompressionFormat.ZSTD)
    );

    /**
     * Match {@code Content-Encoding} header value against <a href="https://www.iana.org/assignments/http-parameters/http-parameters.xhtml#content-coding"IANA >HTTP Content Coding Registry</a>
     * values, returning {@link CompressionFormat} if a match is found. In the case that no matching compression method
     * is found, a {@code null} value is returned.
     *
     * @param contentEncodingHeader Value of {@code Content-Encoding} header
     * @return {@link CompressionFormat} matching the encoding if found, {@code null} otherwise
     */
    public static CompressionFormat determineCompressionFormat(final String contentEncodingHeader) {
        return FORMAT_REGEXES.stream()
                .filter(pair -> pair.getLeft().matcher(contentEncodingHeader).find())
                .findFirst()
                .map(Pair::getRight)
                .orElse(null);
    }

    /**
     * Decompress {@code byte[]} to {@link String} content using a given {@link Charset}. The {@code format} parameter
     * will be used to determine the compression algorithm to use.
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
