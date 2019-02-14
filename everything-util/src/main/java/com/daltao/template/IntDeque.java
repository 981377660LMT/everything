package com.daltao.template;

public class IntDeque {
    int[] data;
    int bpos;
    int epos;
    int cap;

    public IntDeque(int cap) {
        this.cap = cap + 1;
        this.data = new int[this.cap];
    }

    public int size() {
        int s = epos - bpos;
        if (s < 0) {
            s += cap;
        }
        return s;
    }

    public boolean isEmpty() {
        return epos == bpos;
    }

    public int peekFirst() {
        return data[bpos];
    }

    private int last(int i) {
        return (i == 0 ? cap : i) - 1;
    }

    private int next(int i) {
        int n = i + 1;
        return n == cap ? 0 : n;
    }

    public int peekLast() {
        return data[last(epos)];
    }

    public int removeFirst() {
        int t = bpos;
        bpos = next(bpos);
        return data[t];
    }

    public int removeLast() {
        return data[epos = last(epos)];
    }

    public void addLast(int val) {
        data[epos] = val;
        epos = next(epos);
    }

    public void addFirst(int val) {
        data[bpos = last(bpos)] = val;
    }

    public void reset() {
        bpos = epos = 0;
    }
}
