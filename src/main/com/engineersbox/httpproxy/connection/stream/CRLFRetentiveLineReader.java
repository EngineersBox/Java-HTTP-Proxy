package com.engineersbox.httpproxy.connection.stream;

import java.io.BufferedReader;
import java.io.IOException;

public class CRLFRetentiveLineReader {

    private final BufferedReader br;
    private int asciiLiteral;

    private static final int LINE_READ_EXCEEDED = -2;
    private static final char CR = '\r';
    private static final char LF = '\n';

    public CRLFRetentiveLineReader(final BufferedReader br) {
        this.br = br;
    }

    public String readLine(final int contentLength) throws IOException {
        if (asciiLiteral == LINE_READ_EXCEEDED) {
            asciiLiteral = br.read();
        }
        if (asciiLiteral < 0) {
            return null;
        }
        final StringBuilder sb = new StringBuilder();
        sb.append((char) asciiLiteral);
        if (asciiLiteral != CR && asciiLiteral != LF) {
            while ((asciiLiteral = br.read()) != -1 && asciiLiteral != CR && asciiLiteral != LF) {
                sb.append((char) asciiLiteral);
                if (sb.length() == contentLength) {
                    break;
                }
            }
            if (asciiLiteral < 0) {
                return sb.toString();
            }
            sb.append((char) asciiLiteral);
        }
        if (asciiLiteral == CR) {
            asciiLiteral = br.read();
            if (asciiLiteral != LF) {
                return sb.toString();
            }
            sb.append(LF);
        }
        asciiLiteral = LINE_READ_EXCEEDED;
        return sb.toString();
    }

}
