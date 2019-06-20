package com.daltao.oj.submit;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BZOJ3275 {
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
        long inf = (long) 1e18;
        Gcd gcd = new Gcd();

        public Task(FastIO io, Debug debug) {
            this.io = io;
            this.debug = debug;
        }

        @Override
        public void run() {
            solve();
        }

        int n;

        int idOfNode(int i) {
            return i + 1;
        }

        int idOfSrc() {
            return n + 1;
        }

        int idOfDst() {
            return idOfSrc() + 1;
        }

        public void solve() {
            n = io.readInt();
            ISAP isap = new ISAP(idOfDst());

            int[] data = new int[n];
            long sum = 0;
            for (int i = 0; i < n; i++) {
                data[i] = io.readInt();
                sum += data[i];
            }

            for (int i = 0; i < n; i++) {
                if (data[i] % 2 == 0) {
                    isap.getChannel(idOfSrc(), idOfNode(i)).modify(data[i], 0);
                } else {
                    isap.getChannel(idOfNode(i), idOfDst()).modify(data[i], 0);
                    continue;
                }
                for (int j = 0; j < n; j++) {
                    if (data[j] % 2 == 0) {
                        continue;
                    }
                    if (!isSquare((long) data[i] * data[i] + (long) data[j] * data[j])) {
                        continue;
                    }
                    if (gcd.gcd(data[i], data[j]) > 1) {
                        continue;
                    }
                    isap.getChannel(idOfNode(i), idOfNode(j)).modify(inf, 0);
                }
            }

            isap.setSource(idOfSrc());
            isap.setTarget(idOfDst());
            long minCut = (long) (isap.sendFlow(inf));
            io.cache.append(sum - minCut);
        }

        public boolean isSquare(long x) {
            double s = Math.sqrt(x);
            long y = (long) (s + 0.5);
            return y * y == x;
        }
    }

    public static class Gcd {
        public long gcd(long a, long b) {
            return a >= b ? gcd0(a, b) : gcd0(b, a);
        }

        private long gcd0(long a, long b) {
            return b == 0 ? a : gcd0(b, a % b);
        }

        public int gcd(int a, int b) {
            return a >= b ? gcd0(a, b) : gcd0(b, a);
        }

        private int gcd0(int a, int b) {
            return b == 0 ? a : gcd0(b, a % b);
        }
    }

    public static class ISAP {
        Node[] nodes;
        int[] distanceCnt;
        Node source;
        Node target;
        int nodeNum;
        Map<Long, DirectChannel> channelMap = new HashMap();
        Deque<Node> deque;
        boolean bfsFlag = false;

        public List<Node> getComponentS() {
            List<Node> result = new ArrayList();
            for (int i = 1; i <= nodeNum; i++) {
                nodes[i].visited = false;
            }
            deque.addLast(source);
            source.visited = true;
            while (!deque.isEmpty()) {
                Node head = deque.removeFirst();
                result.add(head);
                for (Channel channel : head.channelList) {
                    if (channel.getFlow() == channel.getCapacity()) {
                        continue;
                    }
                    Node node = channel.getDst();
                    if (node.visited) {
                        continue;
                    }
                    node.visited = true;
                    deque.addLast(node);
                }
            }
            return result;
        }

        public Collection<DirectChannel> getChannels() {
            return channelMap.values();
        }

        public DirectChannel getChannel(int src, int dst) {
            Long id = (((long) src) << 32) | dst;
            DirectChannel channel = channelMap.get(id);
            if (channel == null) {
                channel = new DirectChannel(nodes[src], nodes[dst], 0, id.hashCode());
                nodes[src].channelList.add(channel);
                nodes[dst].channelList.add(channel.getInverse());
                channelMap.put(id, channel);
            }
            return channel;
        }

        public ISAP(int nodeNum) {
            this.nodeNum = nodeNum;
            deque = new ArrayDeque(nodeNum);
            nodes = new Node[nodeNum + 1];
            distanceCnt = new int[nodeNum + 2];
            for (int i = 1; i <= nodeNum; i++) {
                Node node = new Node();
                node.id = i;
                nodes[i] = node;
            }
        }

        public long sendFlow(long flow) {
            bfs();
            long flowSnapshot = flow;
            while (flow > 0 && source.distance < nodeNum) {
                flow -= send(source, flow);
            }
            return flowSnapshot - flow;
        }

        public long send(Node node, long flowRemain) {
            if (node == target) {
                return flowRemain;
            }

            long flowSnapshot = flowRemain;
            int nextDistance = node.distance - 1;
            for (Channel channel : node.channelList) {
                long channelRemain = channel.getCapacity() - channel.getFlow();
                Node dst = channel.getDst();
                if (channelRemain == 0 || dst.distance != nextDistance) {
                    continue;
                }
                long actuallySend = send(channel.getDst(), Math.min(flowRemain, channelRemain));
                channel.sendFlow(actuallySend);
                flowRemain -= actuallySend;
                if (flowRemain == 0) {
                    break;
                }
            }

            if (flowSnapshot == flowRemain) {
                if (--distanceCnt[node.distance] == 0) {
                    distanceCnt[source.distance]--;
                    source.distance = nodeNum;
                    distanceCnt[source.distance]++;
                    if (node != source) {
                        distanceCnt[++node.distance]++;
                    }
                } else {
                    distanceCnt[++node.distance]++;
                }
            }

            return flowSnapshot - flowRemain;
        }

        public void setSource(int id) {
            source = nodes[id];
        }

        public void setTarget(int id) {
            target = nodes[id];
        }

        public void bfs() {
            if (bfsFlag) {
                return;
            }
            bfsFlag = true;
            Arrays.fill(distanceCnt, 0);
            deque.clear();

            for (int i = 1; i <= nodeNum; i++) {
                nodes[i].distance = nodeNum;
            }

            target.distance = 0;
            deque.addLast(target);

            while (!deque.isEmpty()) {
                Node head = deque.removeFirst();
                distanceCnt[head.distance]++;
                for (Channel channel : head.channelList) {
                    Channel inverse = channel.getInverse();
                    if (inverse.getCapacity() == inverse.getFlow()) {
                        continue;
                    }
                    Node dst = channel.getDst();
                    if (dst.distance != nodeNum) {
                        continue;
                    }
                    dst.distance = head.distance + 1;
                    deque.addLast(dst);
                }
            }
        }

        public static interface Channel {
            public Node getSrc();

            public Node getDst();

            public long getCapacity();

            public long getFlow();

            public void sendFlow(long volume);

            public Channel getInverse();
        }

        public static class DirectChannel implements Channel {
            final Node src;
            final Node dst;
            final int id;
            long capacity;
            long flow;
            Channel inverse;

            public DirectChannel(Node src, Node dst, int capacity, int id) {
                this.src = src;
                this.dst = dst;
                this.capacity = capacity;
                this.id = id;
                inverse = new InverseChannelWrapper(this);
            }

            public void reset(long cap, long flow) {
                this.flow = flow;
                this.capacity = cap;
            }

            public void modify(long cap, long flow) {
                this.capacity += cap;
                this.flow += flow;
            }

            @Override
            public String toString() {
                return String.format("%s--%s/%s-->%s", getSrc(), getFlow(), getCapacity(), getDst());
            }

            @Override
            public Node getSrc() {
                return src;
            }

            @Override
            public Channel getInverse() {
                return inverse;
            }


            public void setCapacity(int expand) {
                capacity = expand;
            }

            @Override
            public Node getDst() {
                return dst;
            }

            @Override
            public long getCapacity() {
                return capacity;
            }

            @Override
            public long getFlow() {
                return flow;
            }

            @Override
            public void sendFlow(long volume) {
                flow += volume;
            }


        }

        public static class InverseChannelWrapper implements Channel {
            final Channel channel;

            public InverseChannelWrapper(Channel channel) {
                this.channel = channel;
            }

            @Override
            public Channel getInverse() {
                return channel;
            }


            @Override
            public Node getSrc() {
                return channel.getDst();
            }

            @Override
            public Node getDst() {
                return channel.getSrc();
            }

            @Override
            public long getCapacity() {
                return channel.getFlow();
            }

            @Override
            public long getFlow() {
                return 0;
            }

            @Override
            public void sendFlow(long volume) {
                channel.sendFlow(-volume);
            }


            @Override
            public String toString() {
                return String.format("%s--%s/%s-->%s", getSrc(), getFlow(), getCapacity(), getDst());
            }
        }

        public static class Node {
            int id;
            int distance;
            boolean visited;
            List<Channel> channelList = new ArrayList(1);

            @Override
            public String toString() {
                return "" + id;
            }
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            for (DirectChannel channel : getChannels()) {
                if (channel.getFlow() == 0) {
                    continue;
                }
                builder.append(channel).append('\n');
            }

            for (DirectChannel channel : getChannels()) {
                if (channel.getFlow() != 0) {
                    continue;
                }
                builder.append(channel).append('\n');
            }
            return builder.toString();
        }
    }

    public static class FastIO {
        public final StringBuilder cache = new StringBuilder();
        private final InputStream is;
        private final OutputStream os;
        private final Charset charset;
        private StringBuilder defaultStringBuf = new StringBuilder(1 << 8);
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
