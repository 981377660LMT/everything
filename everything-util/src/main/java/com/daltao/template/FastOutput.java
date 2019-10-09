package com.daltao.template;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;

public class FastOutput {
    private StringBuilder cache = new StringBuilder(1 << 20);
    private Charset charset;
    private final OutputStream os;

    public FastOutput(OutputStream os, Charset charset) {
        this.charset = charset;
        this.os = os;
    }


    public FastOutput(OutputStream os) {
        this(os, Charset.forName("ascii"));
    }

    public FastOutput append(char c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(int c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(long c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(float c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(double c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(String c) {
        cache.append(c);
        return this;
    }

    public FastOutput append(Object c) {
        cache.append(c);
        return this;
    }

    public FastOutput printf(String format, Object... args) {
        cache.append(String.format(format, args));
        return this;
    }

    public FastOutput flush() {
        try {
            os.write(cache.toString().getBytes(charset));
            cache.setLength(0);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return this;
    }
}
