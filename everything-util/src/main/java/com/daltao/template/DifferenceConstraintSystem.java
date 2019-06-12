package com.daltao.template;

import java.util.*;

public class DifferenceConstraintSystem {
    private static class Node {
        List<Edge> edges = new ArrayList(2);
        long dist;
        boolean inque;
        int times;
        int id;

        @Override
        public String toString() {
            return "a" + id;
        }
    }

    private static class Edge {
        final Node src;
        final Node next;
        final long len;

        private Edge(Node src, Node next, long len) {
            this.src = src;
            this.next = next;
            this.len = len;
        }

        @Override
        public String toString() {
            return String.format("%s - %s <= %d", next.toString(), src.toString(), len);
        }
    }

    Node[] nodes;
    Deque<Node> deque;
    int n;


    public DifferenceConstraintSystem(int n) {
        this.n = n;
        deque = new ArrayDeque(n);
        nodes = new Node[n];
        for (int i = 0; i < n; i++) {
            nodes[i] = new Node();
            nodes[i].id = i;
        }
    }

    public void clear(int n) {
        this.n = n;
        for (int i = 0; i < n; i++) {
            nodes[i].edges.clear();
        }
    }

    public void differenceLessThanOrEqualTo(int i, int j, long d) {
        nodes[j].edges.add(new Edge(nodes[j], nodes[i], d));
    }

    public void differenceGreaterThanOrEqualTo(int i, int j, long d) {
        differenceLessThanOrEqualTo(j, i, -d);
    }

    public void differenceEqualTo(int i, int j, long d) {
        differenceGreaterThanOrEqualTo(i, j, d);
        differenceLessThanOrEqualTo(i, j, d);
    }

    public void differenceLessThan(int i, int j, long d) {
        differenceLessThanOrEqualTo(i, j, d - 1);
    }

    public void differenceGreaterThan(int i, int j, long d) {
        differenceGreaterThanOrEqualTo(i, j, d + 1);
    }

    boolean hasSolution;

    private boolean dijkstraElog2V() {
        TreeSet<Node> heap = new TreeSet(new Comparator<Node>() {
            @Override
            public int compare(Node a, Node b) {
                return a.dist == b.dist ? a.id - b.id : a.dist < b.dist ? -1 : 1;
            }
        });
        heap.addAll(deque);
        while (!heap.isEmpty()) {
            Node head = heap.pollFirst();
            for (Edge edge : head.edges) {
                Node node = edge.next;
                if (node.dist <= head.dist + edge.len) {
                    continue;
                }
                heap.remove(node);
                node.dist = head.dist + edge.len;
                heap.add(node);
            }
        }
        return true;
    }

    private boolean spfa() {
        while (!deque.isEmpty()) {
            Node head = deque.removeFirst();
            head.inque = false;
            if (head.times >= n) {
                return false;
            }
            for (Edge edge : head.edges) {
                Node node = edge.next;
                if (node.dist <= edge.len + head.dist) {
                    continue;
                }
                node.dist = edge.len + head.dist;
                if (node.inque) {
                    continue;
                }
                node.times++;
                node.inque = true;
                deque.addLast(node);
            }
        }
        return true;
    }

    public long possibleSolutionOf(int i) {
        return nodes[i].dist;
    }

    private void prepare(long initDist) {
        deque.clear();
        for (int i = 0; i < n; i++) {
            nodes[i].dist = initDist;
            nodes[i].times = 0;
        }
    }

    public boolean hasSolution() {
        prepare(0);
        for (int i = 0; i < n; i++) {
            nodes[i].inque = true;
            deque.addLast(nodes[i]);
        }
        hasSolution = spfa();
        return hasSolution;
    }

    public static final long INF = (long) 2e18;

    /**
     * Find max(ai - aj), if INF is returned, it means no constraint between ai and aj
     */
    public long findMaxDifferenceBetween(int i, int j) {
        prepare(INF);
        deque.addLast(nodes[j]);
        nodes[j].dist = 0;
        nodes[j].inque = true;
        spfa();
        return nodes[i].dist;
    }

    /**
     * Find min(ai - aj), if INF is returned, it means no constraint between ai and aj
     */
    public long findMinDifferenceBetween(int i, int j) {
        long r = findMaxDifferenceBetween(j, i);
        if (r == INF) {
            return INF;
        }
        return -r;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (Edge edge : nodes[i].edges) {
                builder.append(edge).append('\n');
            }
        }
        builder.append("-------------\n");
        if (!hasSolution) {
            builder.append("impossible");
        } else {
            for (int i = 0; i < n; i++) {
                builder.append("a").append(i).append("=").append(nodes[i].dist).append('\n');
            }
        }
        return builder.toString();
    }
}