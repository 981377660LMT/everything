package com.daltao.template;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlopeOptimize {
    private static class Point {
        final long x;
        final long y;
        final int id;

        private Point(long x, long y, int id) {
            this.x = x;
            this.y = y;
            this.id = id;
        }
    }
    Deque<Point> deque = new ArrayDeque();
    private double slope(Point a, Point b) {
        return (double) (b.y - a.y) / (b.x - a.x);
    }
    Point add(long y, long x, int id) {
        Point t1 = new Point(x, y, id);
        while (deque.size() >= 2) {
            Point t2 = deque.removeLast();
            Point t3 = deque.peekLast();
            if (slope(t3, t2) < slope(t2, t1)) {
                deque.addLast(t2);
                break;
            }
        }
        deque.addLast(t1);
        return t1;
    }
    int getBestChoice(long s) {
        while (deque.size() >= 2) {
            Point h1 = deque.removeFirst();
            Point h2 = deque.peekFirst();
            if (slope(h2, h1) > s) {
                deque.addFirst(h1);
                break;
            }
        }
        return deque.peekFirst().id;
    }
}