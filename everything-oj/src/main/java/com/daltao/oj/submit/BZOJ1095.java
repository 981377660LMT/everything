package com.daltao.oj.submit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BZOJ1095 {
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
            Node[] nodes = new Node[n + 1];
            for (int i = 1; i <= n; i++) {
                nodes[i] = new Node();
            }
            for (int i = 1; i < n; i++) {
                Node a = nodes[io.readInt()];
                Node b = nodes[io.readInt()];
                a.next.add(b);
                b.next.add(a);
            }

            dfs(nodes[1], null);
            int[] brackets = new int[order];
            for (int i = 1; i <= n; i++) {
                brackets[nodes[i].open] = 1;
                brackets[nodes[i].close] = -1;
            }

            Segment segment = new Segment(0, order - 1, brackets);

            int q = io.readInt();
            for (int i = 0; i < q; i++) {
                char cmd = io.readChar();
                if (cmd == 'C') {
                    int which = io.readInt();
                    nodes[which].light = !nodes[which].light;
                    segment.update(nodes[which].open, nodes[which].open, 0, order - 1, nodes[which].light);
                    //segment.update(nodes[which].close, nodes[which].close, 0, order - 1, nodes[which].light);
                } else {
                    int dp = segment.dp;
                    if (dp < 0) {
                        io.cache.append(-1);
                    } else {
                        io.cache.append(dp);
                    }
                    io.cache.append('\n');
                }
            }
        }

        int order;

        public void dfs(Node root, Node father) {
            root.open = order++;
            for (Node node : root.next) {
                if (node == father) {
                    continue;
                }
                dfs(node, root);
            }
            root.close = order++;
        }
    }

    public static class Node {
        int open;
        int close;
        boolean light;
        List<Node> next = new ArrayList(1);
    }

    public static class Segment implements Cloneable {
        public static final int INF = (int) 1e8;

        private Segment left;
        private Segment right;


        private int sum;

        private int dlr;
        private int dlSub;
        private int drSub;
        private int minDepth;
        private int dp;

        //String str;

        public void pushUp() {
            //  str = left.str + right.str;
            sum = left.sum + right.sum;

            dlr = Math.max(left.dlr, right.dlr + left.sum);
            dlSub = Math.max(right.dlSub - left.sum, left.dlSub);
            dlSub = Math.max(dlSub, left.dlr - 2 * (right.minDepth + left.sum));
            drSub = Math.max(left.drSub, right.drSub - left.sum);
            drSub = Math.max(drSub, right.dlr + left.sum - 2 * left.minDepth);
            minDepth = Math.min(left.minDepth, right.minDepth + left.sum);
            dp = Math.max(left.dp, right.dp);
            dp = Math.max(dp, left.dlr + right.drSub - left.sum);
            dp = Math.max(dp, left.dlSub + right.dlr + left.sum);
        }

        public void pushDown() {

        }

        public Segment(int l, int r, int[] vals) {
            if (l < r) {
                int m = (l + r) >> 1;
                left = new Segment(l, m, vals);
                right = new Segment(m + 1, r, vals);
                pushUp();
            } else {
                sum = vals[l];
                notLight();
            }
        }

        public void notLight() {
            if (sum == -1) {
                light();
                return;
            }
            dlr = sum;
            dlSub = -sum;
            drSub = -sum;
            minDepth = sum;
            dp = 0;

            //str = sum == 1 ? "[" : "]";
        }

        public void light() {
            dlr = -INF;
            dlSub = -INF;
            drSub = -INF;
            minDepth = sum;
            dp = -INF;

            //str = sum == 1 ? "(" : ")";
        }

        private boolean covered(int ll, int rr, int l, int r) {
            return ll <= l && rr >= r;
        }

        private boolean noIntersection(int ll, int rr, int l, int r) {
            return ll > r || rr < l;
        }

        public void update(int ll, int rr, int l, int r, boolean light) {
            if (noIntersection(ll, rr, l, r)) {
                return;
            }
            if (covered(ll, rr, l, r)) {
                if (!light) {
                    notLight();
                } else {
                    light();
                }
                return;
            }
            pushDown();
            int m = (l + r) >> 1;
            left.update(ll, rr, l, m, light);
            right.update(ll, rr, m + 1, r, light);
            pushUp();
        }

        public void query(int ll, int rr, int l, int r) {
            if (noIntersection(ll, rr, l, r)) {
                return;
            }
            if (covered(ll, rr, l, r)) {
                return;
            }
            pushDown();
            int m = (l + r) >> 1;
            left.query(ll, rr, l, m);
            right.query(ll, rr, m + 1, r);
        }
    }

    public static class FastIO {
        public final StringBuilder cache = new StringBuilder(1 << 13);
        private final InputStream is;
        private final OutputStream os;
        private final Charset charset;
        private StringBuilder defaultStringBuf = new StringBuilder(1 << 13);
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
            boolean sign = true;
            skipBlank();
            if (next == '+' || next == '-') {
                sign = next == '+';
                next = read();
            }

            long val = 0;
            while (next >= '0' && next <= '9') {
                val = val * 10 + next - '0';
                next = read();
            }
            if (next != '.') {
                return sign ? val : -val;
            }
            next = read();
            long radix = 1;
            long point = 0;
            while (next >= '0' && next <= '9') {
                point = point * 10 + next - '0';
                radix = radix * 10;
                next = read();
            }
            double result = val + (double) point / radix;
            return sign ? result : -result;
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

        public char readChar() {
            skipBlank();
            char c = (char) next;
            next = read();
            return c;
        }

        public void flush() throws IOException {
            os.write(cache.toString().getBytes(charset));
            os.flush();
            cache.setLength(0);
        }

        public boolean hasMore() {
            skipBlank();
            return next != -1;
        }
    }

    public static class Debug {
        private boolean allowDebug;

        public Debug(boolean allowDebug) {
            this.allowDebug = allowDebug;
        }

        public void assertTrue(boolean flag) {
            if (!allowDebug) {
                return;
            }
            if (!flag) {
                fail();
            }
        }

        public void fail() {
            throw new RuntimeException();
        }

        public void assertFalse(boolean flag) {
            if (!allowDebug) {
                return;
            }
            if (flag) {
                fail();
            }
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