package com.daltao.template;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public class MinCostMaxFlow {
    Node[] nodes;
    Deque<Node> deque;
    Node source;
    Node sink;
    int nodeNum;
    final static int INF = (int) 1e8;

    public MinCostMaxFlow(int nodeNum) {
        this.nodeNum = nodeNum;
        nodes = new Node[nodeNum + 1];
        for (int i = 1; i <= nodeNum; i++) {
            nodes[i] = new Node(i);
        }
        deque = new ArrayDeque<>(nodeNum);
    }

    public void setSource(int id) {
        source = nodes[id];
    }

    public void setSink(int id) {
        sink = nodes[id];
    }

    public DirectFeeChannel buildChannel(int src, int dst, int cap, int fee, int id) {
        return Node.buildChannel(nodes[src], nodes[dst], cap, fee, id);
    }

    /**
     * reuslt[0] store how much flow could be sent and result[1] represents the fee
     */
    public void send(int flow, int[] result) {
        int totalFee = 0;
        int totalFlow = 0;

        while (flow > 0) {
            spfa();

            if (sink.distance == INF) {
                break;
            }


            int feeSum = sink.distance;
            int minFlow = flow;

            Node trace = sink;
            while (trace != source) {
                FeeChannel last = trace.last;
                minFlow = Math.min(minFlow, last.getCapacity() - last.getFlow());
                trace = last.getSrc();
            }

            flow -= minFlow;

            trace = sink;
            while (trace != source) {
                FeeChannel last = trace.last;
                last.sendFlow(minFlow);
                trace = last.getSrc();
            }

            totalFee += feeSum;
            totalFlow += minFlow;
        }

        result[0] = totalFlow;
        result[1] = totalFee;
    }

    private void spfa() {
        for (int i = 1; i <= nodeNum; i++) {
            nodes[i].distance = INF;
            nodes[i].inque = false;
            nodes[i].last = null;
        }

        deque.addLast(source);
        source.distance = 0;
        source.inque = true;

        while (!deque.isEmpty()) {
            Node head = deque.removeFirst();
            head.inque = false;
            for (FeeChannel channel : head.channelList) {
                if (channel.getFlow() == channel.getCapacity()) {
                    continue;
                }
                Node dst = channel.getDst();
                int newDist = head.distance + channel.getFee();
                if (dst.distance <= newDist) {
                    continue;
                }
                dst.distance = newDist;
                dst.last = channel;
                if (dst.inque) {
                    continue;
                }
                deque.addLast(dst);
                dst.inque = true;
            }
        }
    }

    public static interface FeeChannel {
        public Node getSrc();

        public Node getDst();

        public int getCapacity();

        public int getFlow();

        public void sendFlow(int volume);

        public FeeChannel getInverse();

        public int getFee();
    }

    public static class DirectFeeChannel implements FeeChannel {
        final Node src;
        final Node dst;
        final int id;
        int capacity;
        int flow;
        FeeChannel inverse;
        final int fee;

        @Override
        public int getFee() {
            return fee;
        }

        public DirectFeeChannel(Node src, Node dst, int capacity, int fee, int id) {
            this.src = src;
            this.dst = dst;
            this.capacity = capacity;
            this.id = id;
            this.fee = fee;
            inverse = new InverseFeeChannelWrapper(this);
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
        public FeeChannel getInverse() {
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
        public int getCapacity() {
            return capacity;
        }

        @Override
        public int getFlow() {
            return flow;
        }

        @Override
        public void sendFlow(int volume) {
            flow += volume;
        }


    }

    public static class InverseFeeChannelWrapper implements FeeChannel {
        final FeeChannel inner;

        public InverseFeeChannelWrapper(FeeChannel inner) {
            this.inner = inner;
        }

        @Override
        public int getFee() {
            return -inner.getFee();
        }

        @Override
        public FeeChannel getInverse() {
            return inner;
        }


        @Override
        public Node getSrc() {
            return inner.getDst();
        }

        @Override
        public Node getDst() {
            return inner.getSrc();
        }

        @Override
        public int getCapacity() {
            return inner.getFlow();
        }

        @Override
        public int getFlow() {
            return 0;
        }

        @Override
        public void sendFlow(int volume) {
            inner.sendFlow(-volume);
        }


        @Override
        public String toString() {
            return String.format("%s--%s/%s-->%s", getSrc(), getFlow(), getCapacity(), getDst());
        }
    }

    public static class Node {
        final int id;
        int distance;
        boolean inque;
        FeeChannel last;
        List<FeeChannel> channelList = new ArrayList<>(1);

        public Node(int id) {
            this.id = id;
        }

        public static DirectFeeChannel buildChannel(Node src, Node dst, int cap, int fee, int id) {
            DirectFeeChannel channel = new DirectFeeChannel(src, dst, cap, fee, id);
            src.channelList.add(channel);
            dst.channelList.add(channel.getInverse());
            return channel;
        }

        @Override
        public String toString() {
            return "" + id;
        }
    }
}
