package com.daltao.oj.old.template;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

public class IOUtil {
    private static int BUF_SIZE = 1 << 13;

    private byte[] r_buf = new byte[BUF_SIZE];
    private int r_cur;
    private int r_total;
    private int r_next;
    private final InputStream in;
    private StringBuilder temporary = new StringBuilder();

    StringBuilder w_buf = new StringBuilder();
    private final OutputStream out;

    public IOUtil(InputStream in, OutputStream out) {
        this.in = in;
        this.out = out;
    }

    private void skipBlank() {
        while (r_next >= 0 && r_next <= 32) {
            r_next = read();
        }
    }

    public int readString(char[] data, int offset, int limit) {
        skipBlank();

        int originalLimit = limit;
        while (limit > 0 && r_next > 32) {
            data[offset++] = (char) r_next;
            limit--;
            r_next = read();
        }

        return originalLimit - limit;
    }

    public String readString(StringBuilder builder) {
        skipBlank();

        while (r_next > 32) {
            builder.append((char) r_next);
            r_next = read();
        }

        return builder.toString();
    }

    public String readString() {
        temporary.setLength(0);
        return readString(temporary);
    }

    public long readUnsignedLong() {
        skipBlank();

        long num = 0;
        while (r_next >= '0' && r_next <= '9') {
            num = num * 10 + r_next - '0';
            r_next = read();
        }
        return num;
    }

    public long readLong() {
        skipBlank();

        int sign = 1;
        while (r_next == '+' || r_next == '-') {
            if (r_next == '-') {
                sign *= -1;
            }
            r_next = read();
        }

        return sign == 1 ? readUnsignedLong() : readNegativeLong();
    }

    public long readNegativeLong() {
        skipBlank();

        long num = 0;
        while (r_next >= '0' && r_next <= '9') {
            num = num * 10 - r_next + '0';
            r_next = read();
        }
        return num;
    }

    public int readUnsignedInt() {
        skipBlank();

        int num = 0;
        while (r_next >= '0' && r_next <= '9') {
            num = num * 10 + r_next - '0';
            r_next = read();
        }
        return num;
    }

    public int readNegativeInt() {
        skipBlank();

        int num = 0;
        while (r_next >= '0' && r_next <= '9') {
            num = num * 10 - r_next + '0';
            r_next = read();
        }
        return num;
    }

    public int readInt() {
        skipBlank();

        int sign = 1;
        while (r_next == '+' || r_next == '-') {
            if (r_next == '-') {
                sign *= -1;
            }
            r_next = read();
        }

        return sign == 1 ? readUnsignedInt() : readNegativeInt();
    }

    public int read() {
        while (r_total <= r_cur) {
            try {
                r_total = in.read(r_buf);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            r_cur = 0;
            if (r_total == -1) {
                return -1;
            }
        }
        return r_buf[r_cur++];
    }

    public boolean hasMore() {
        skipBlank();
        return r_next != -1;
    }

    public void write(char c) {
        w_buf.append(c);
    }

    public void write(int n) {
        w_buf.append(n);
    }

    public void write(String s) {
        w_buf.append(s);
    }

    public void write(long s) {
        w_buf.append(s);
    }

    public void write(double s) {
        w_buf.append(s);
    }

    public void write(float s) {
        w_buf.append(s);
    }

    public void write(Object s) {
        w_buf.append(s);
    }

    public void write(char[] data, int offset, int cnt) {
        for (int i = offset, until = offset + cnt; i < until; i++) {
            write(data[i]);
        }
    }

    public void flush() {
        try {
            out.write(w_buf.toString().getBytes(Charset.forName("ascii")));
            w_buf.setLength(0);
            out.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public double readDouble() {
        return Double.parseDouble(readString());
    }
}