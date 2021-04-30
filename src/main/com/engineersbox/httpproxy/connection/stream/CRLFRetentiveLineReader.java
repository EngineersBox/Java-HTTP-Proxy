package com.engineersbox.httpproxy.connection.stream;

import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class CRLFRetentiveLineReader {

    private final InputStream stream;
    private int asciiLiteral;

    private static final int LINE_READ_EXCEEDED = -2;
    private static final char CR = '\r';
    private static final char LF = '\n';

    public CRLFRetentiveLineReader(final InputStream stream) {
        this.stream = stream;
    }

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
