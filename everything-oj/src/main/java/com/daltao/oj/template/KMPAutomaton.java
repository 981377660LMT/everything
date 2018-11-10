package com.daltao.oj.template;

/**
 * Created by dalt on 2018/5/25.
 */
public class KMPAutomaton {
    char[] data;
    int[] fail;
    int buildLast;
    int matchLast = 0;

    public KMPAutomaton(int cap) {
        data = new char[cap + 2];
        fail = new int[cap + 2];
        fail[0] = -1;
        buildLast = 0;
    }

    public void beginMatch() {
        matchLast = 0;
    }

    public void match(char c) {
        matchLast = visit(c, matchLast) + 1;
    }

    public int visit(char c, int trace) {
        while (trace >= 0 && data[trace + 1] != c) {
            trace = fail[trace];
        }
        return trace;
    }

    public void build(char c) {
        buildLast++;
        fail[buildLast] = visit(c, fail[buildLast - 1]) + 1;
        data[buildLast] = c;
    }
}
