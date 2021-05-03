package com.engineersbox.httpproxy.connection.stream;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Stream reader that can reads a line, terminating on any termination configuration of {@code CR (\r)} and/or {@code LF (\n)},
 * retaining the termination in the read line string and raw bytes
 */
public class CRLFRetentiveLineReader {

    private final InputStream stream;
    private int asciiLiteral;

    private static final int LINE_READ_EXCEEDED = -2;
    private static final char CR = '\r';
    private static final char LF = '\n';

    public CRLFRetentiveLineReader(final InputStream stream) {
        this.stream = stream;
    }

    /**
     * Reads byte-by-byte from InputStream until it finds one of 3 line termination configurations:
     *
     * <ol>
     *     <li>{@code CR (\r)}</li>
     *     <li>{@code LF (\n)}</li>
     *     <li>{@code CRLF (\r\n)}</li>
     * </ol>
     *
     * Once a line terminator has been reached it terminates the read and returns. The read line <strong>INCLUDES</strong>
     * the line terminator in the string and byte list. Each line read stores the resulting string and the raw bytes as a
     * {@code List<<Byte>>}, returned as a {@code Pair<String, List<Byte>>}.
     *
     * <br/><br/>
     *
     * This method exists to handle the issues with reading compressed data with a <code>BufferedReader</code>, in that
     * line reads have their terminators omitted from the returned string, which can malform/corrupt compressed body data.
     *
     * <br/><br/>
     *
     * Additionally, it is not possible to reconstruct these line endings as there is no way to tell which one of the
     * three possible endings was encountered
     *
     * <br/><br/>
     *
     * @return {@link org.apache.commons.lang3.tuple.Pair} with the {@link String} version of a read line and {@link List}
     * containing raw {@link Byte} of the line read.
     * @throws IOException If an I/O error occurs
     */
    public Pair<String, List<Byte>> readLineBytes() throws IOException {
        if (asciiLiteral == LINE_READ_EXCEEDED) {
            asciiLiteral = stream.read();
        }
        if (asciiLiteral < 0) {
            return ImmutablePair.of(null, new ArrayList<>());
        }
        final StringBuilder sb = new StringBuilder();
        List<Byte> bytes = new ArrayList<>();
        sb.append((char) asciiLiteral);
        bytes.add((byte) asciiLiteral);
        if (asciiLiteral != CR && asciiLiteral != LF) {
            while ((asciiLiteral = stream.read()) != -1 && asciiLiteral != CR && asciiLiteral != LF) {
                sb.append((char) asciiLiteral);
                bytes.add((byte) asciiLiteral);
            }
            if (asciiLiteral < 0) {
                return ImmutablePair.of(sb.toString(), bytes);
            }
            sb.append((char) asciiLiteral);
            bytes.add((byte) asciiLiteral);
        }
        if (asciiLiteral == CR) {
            asciiLiteral = stream.read();
            if (asciiLiteral != LF) {
                return ImmutablePair.of(sb.toString(), bytes);
            }
            sb.append(LF);
            bytes.add((byte) LF);
        }
        asciiLiteral = LINE_READ_EXCEEDED;
        return ImmutablePair.of(sb.toString(), bytes);
    }

}
