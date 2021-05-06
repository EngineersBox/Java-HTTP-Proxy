package com.engineersbox.httpproxy.formatting.compression;

import org.apache.commons.compress.compressors.CompressorStreamFactory;

/**
 * Compression formats supported under the HTTP/1.1 protocol according to entries within the
 * <a href="https://www.iana.org/assignments/http-parameters/http-parameters.xhtml#content-coding">IANA HTTP Content Coding Registry</a>
 */
public enum CompressionFormat {
    GZIP(CompressorStreamFactory.GZIP),
    DEFLATE(CompressorStreamFactory.DEFLATE),
    LZW(CompressorStreamFactory.Z), // AKA UNIX compress
    ZSTD(CompressorStreamFactory.ZSTANDARD),
    BR(CompressorStreamFactory.BROTLI);

    final String compressionFactoryName;

    CompressionFormat(final String compressionFactoryName) {
        this.compressionFactoryName = compressionFactoryName;
    }
}
