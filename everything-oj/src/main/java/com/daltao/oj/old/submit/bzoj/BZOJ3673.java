package com.daltao.oj.old.submit.bzoj;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Comparator;

public class BZOJ3673 {
    public static void main(String[] args) throws Exception {
        boolean local = System.getProperty("ONLINE_JUDGE") == null;
        boolean async = false;

        Charset charset = Charset.forName("ascii");

        FastIO io = local ? new FastIO(new FileInputStream("D:\\DATABASE\\TESTCASE\\Code.in"), System.out, charset) : new FastIO(System.in, System.out, charset);
        Task task = new Task(io, new Debug(local));

        if (async) {
            Thread t = new Thread(null, task, "dalt", 1 << 27);
            t.setPriority(Thread.MAX_PRIORITY);
            t.start();
            t.join();
        } else {
            task.run();
        }

        if (local) {
            io.cache.append("\n\n--memory -- \n" + ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) >> 20) + "M");
        }

        io.flush();
    }

    public static class Task implements Runnable {
        final FastIO io;
        final Debug debug;
        int inf = (int) 1e8;

        public Task(FastIO io, Debug debug) {
            this.io = io;
            this.debug = debug;
        }

        @Override
        public void run() {
            solve();
        }

        public void solve() {
            int n = io.readInt();
            int m = io.readInt();
            Segment[] history = new Segment[m + 1];
            history[0] = Segment.build(1, n);
            for (int i = 1; i <= m; i++) {
                int op = io.readInt();
                if (op == 1) {
                    history[i] = merge(history[i - 1], io.readInt(), io.readInt(), 1, n);
                } else if (op == 2) {
                    history[i] = history[io.readInt()];
                } else {
                    int a = getRoot(history[i - 1], io.readInt(), 1, n);
                    int b = getRoot(history[i - 1], io.readInt(), 1, n);
                    io.cache.append(a == b ? 1 : 0).append('\n');
                    history[i] = history[i - 1];
                }
            }
        }

        public Segment merge(Segment root, int a, int b, int l, int r) {
            a = getRoot(root, a, l, r);
            b = getRoot(root, b, l, r);

            if (a == b) {
                return root;
            }

            Segment sa = Segment.query(a, a, l, r, root);
            Segment sb = Segment.query(b, b, l, r, root);
            if (sa.rank < sb.rank) {
                int tmp = a;
                a = b;
                b = tmp;
            }

            root = Segment.updatePersistently(a, a, l, r, a, sa.rank + sb.rank, root);
            root = Segment.updatePersistently(b, b, l, r, a, Math.min(sa.rank, sb.rank), root);
            return root;
        }

        public int getRoot(Segment root, int a, int l, int r) {
            Segment s = Segment.query(a, a, l, r, root);
            while (s.father != a && s.father != 0) {
                a = s.father;
                s = Segment.query(a, a, l, r, root);
            }
            return a;
        }
    }

    public static class Segment implements Cloneable {
        Segment left;
        Segment right;
        int father;
        int rank;

        public static Segment build(int l, int r) {
            Segment segment = new Segment();
            segment.left = segment.right = segment;
            return segment;
        }

        public static boolean checkOutOfRange(int ll, int rr, int l, int r) {
            return ll > r || rr < l;
        }

        public static boolean checkCoverage(int ll, int rr, int l, int r) {
            return ll <= l && rr >= r;
        }

        public static void update(int ll, int rr, int l, int r, Segment segment) {
            if (checkOutOfRange(ll, rr, l, r)) {
                return;
            }
            if (checkCoverage(ll, rr, l, r)) {
                return;
            }
            int m = (l + r) >> 1;

            segment.pushDown();
            update(ll, rr, l, m, segment.left);
            update(ll, rr, m + 1, r, segment.right);
            segment.pushUp();
        }

        public static Segment updatePersistently(int ll, int rr, int l, int r, int father, int rank, Segment segment) {
            if (checkOutOfRange(ll, rr, l, r)) {
                return segment;
            }
            segment = segment.clone();
            if (checkCoverage(ll, rr, l, r)) {
                segment.father = father;
                segment.rank = rank;
                return segment;
            }

            int m = (l + r) >> 1;

            segment.pushDown();
            segment.left = updatePersistently(ll, rr, l, m, father, rank, segment.left);
            segment.right = updatePersistently(ll, rr, m + 1, r, father, rank, segment.right);
            segment.pushUp();
            return segment;
        }

        public static Segment query(int ll, int rr, int l, int r, Segment segment) {
            if (checkOutOfRange(ll, rr, l, r)) {
                return null;
            }
            if (checkCoverage(ll, rr, l, r)) {
                return segment;
            }
            int m = (l + r) >> 1;

            segment.pushDown();
            return pickNonNull(query(ll, rr, l, m, segment.left),
                    query(ll, rr, m + 1, r, segment.right));
        }

        private static Segment pickNonNull(Segment a, Segment b) {
            return a == null ? b : a;
        }

        public void pushDown() {
        }

        public void pushUp() {
        }

        @Override
        public Segment clone() {
            try {
                return (Segment) super.clone();
            } catch (CloneNotSupportedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FastIO {
        private final InputStream is;
        private final OutputStream os;
        private final Charset charset;
        private StringBuilder defaultStringBuf = new StringBuilder(1 << 8);
        public final StringBuilder cache = new StringBuilder();

        private byte[] buf = new byte[1 << 13];
        private int bufLen;
        private int bufOffset;
        private int next;

        public FastIO(InputStream is, OutputStream os, Charset charset) {
            this.is = is;
            this.os = os;
            this.charset = charset;
        }

        public FastIO(InputStream is, OutputStream os) {
            this(is, os, Charset.forName("ascii"));
        }

        private int read() {
            while (bufLen == bufOffset) {
                bufOffset = 0;
                try {
                    bufLen = is.read(buf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                if (bufLen == -1) {
                    return -1;
                }
            }
            return buf[bufOffset++];
        }

        public void skipBlank() {
            while (next >= 0 && next <= 32) {
                next = read();
            }
        }

        public int readInt() {
            int sign = 1;

            skipBlank();
            if (next == '+' || next == '-') {
                sign = next == '+' ? 1 : -1;
                next = read();
            }

            int val = 0;
            if (sign == 1) {
                while (next >= '0' && next <= '9') {
                    val = val * 10 + next - '0';
                    next = read();
                }
            } else {
                while (next >= '0' && next <= '9') {
                    val = val * 10 - next + '0';
                    next = read();
                }
            }

            return val;
        }

        public long readLong() {
            int sign = 1;

            skipBlank();
            if (next == '+' || next == '-') {
                sign = next == '+' ? 1 : -1;
                next = read();
            }

            long val = 0;
            if (sign == 1) {
                while (next >= '0' && next <= '9') {
                    val = val * 10 + next - '0';
                    next = read();
                }
            } else {
                while (next >= '0' && next <= '9') {
                    val = val * 10 - next + '0';
                    next = read();
                }
            }

            return val;
        }

        public double readDouble() {
            long num = readLong();
            if (next != '.') {
                return num;
            }

            next = read();
            double f = readLong();
            while (f >= 100000000) {
                f /= 1000000000;
            }
            while (f >= 10000) {
                f /= 100000;
            }
            while (f >= 100) {
                f /= 1000;
            }
            while (f >= 1) {
                f /= 10;
            }
            return num > 0 ? (num + f) : (num - f);
        }

        public String readString(StringBuilder builder) {
            skipBlank();

            while (next > 32) {
                builder.append((char) next);
                next = read();
            }

            return builder.toString();
        }

        public String readString() {
            defaultStringBuf.setLength(0);
            return readString(defaultStringBuf);
        }

        public int readLine(char[] data, int offset) {
            int originalOffset = offset;
            while (next != -1 && next != '\n') {
                data[offset++] = (char) next;
                next = read();
            }
            return offset - originalOffset;
        }

        public int readString(char[] data, int offset) {
            skipBlank();

            int originalOffset = offset;
            while (next > 32) {
                data[offset++] = (char) next;
                next = read();
            }

            return offset - originalOffset;
        }

        public int readString(byte[] data, int offset) {
            skipBlank();

            int originalOffset = offset;
            while (next > 32) {
                data[offset++] = (byte) next;
                next = read();
            }

            return offset - originalOffset;
        }

        public void flush() {
            try {
                os.write(cache.toString().getBytes(charset));
                os.flush();
                cache.setLength(0);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public boolean hasMore() {
            skipBlank();
            return next != -1;
        }
    }

    public static class Memory {
        public static <T> void swap(T[] data, int i, int j) {
            T tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }

        public static void swap(char[] data, int i, int j) {
            char tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }

        public static void swap(int[] data, int i, int j) {
            int tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }

        public static void swap(long[] data, int i, int j) {
            long tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }

        public static <T> int min(T[] data, int from, int to, Comparator<T> cmp) {
            int m = from;
            for (int i = from + 1; i < to; i++) {
                if (cmp.compare(data[m], data[i]) > 0) {
                    m = i;
                }
            }
            return m;
        }

        public static <T> void move(T[] data, int from, int to, int step) {
            int len = to - from;
            step = len - (step % len + len) % len;
            Object[] buf = new Object[len];
            for (int i = 0; i < len; i++) {
                buf[i] = data[(i + step) % len + from];
            }
            System.arraycopy(buf, 0, data, from, len);
        }

        public static <T> void reverse(T[] data, int f, int t) {
            int l = f, r = t - 1;
            while (l < r) {
                swap(data, l, r);
                l++;
                r--;
            }
        }

        public static void reverse(int[] data, int f, int t) {
            int l = f, r = t - 1;
            while (l < r) {
                swap(data, l, r);
                l++;
                r--;
            }
        }

        public static void copy(Object[] src, Object[] dst, int srcf, int dstf, int len) {
            if (len < 8) {
                for (int i = 0; i < len; i++) {
                    dst[dstf + i] = src[srcf + i];
                }
            } else {
                System.arraycopy(src, srcf, dst, dstf, len);
            }
        }
    }

    public static class Debug {
        private boolean allowDebug;

        public Debug(boolean allowDebug) {
            this.allowDebug = allowDebug;
        }

        private void outputName(String name) {
            System.out.print(name + " = ");
        }

        public void debug(String name, int x) {
            if (!allowDebug) {
                return;
            }

            outputName(name);
            System.out.println("" + x);
        }

        public void debug(String name, long x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println("" + x);
        }

        public void debug(String name, double x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println("" + x);
        }

        public void debug(String name, int[] x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println(Arrays.toString(x));
        }

        public void debug(String name, long[] x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println(Arrays.toString(x));
        }

        public void debug(String name, double[] x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println(Arrays.toString(x));
        }

        public void debug(String name, Object x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println("" + x);
        }

        public void debug(String name, Object... x) {
            if (!allowDebug) {
                return;
            }
            outputName(name);
            System.out.println(Arrays.deepToString(x));
        }
    }
}
