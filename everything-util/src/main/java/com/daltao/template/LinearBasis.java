package com.daltao.template;

import java.util.Arrays;

public class LinearBasis {
    private long[] map = new long[64];
    private int size;

    public int size() {
        return size;
    }

    public void clear() {
        size = 0;
        Arrays.fill(map, 0);
    }

    private void afterAddBit(int bit) {
        for (int i = 63; i >= 0; i--) {
            if (i == bit || map[i] == 0) {
                continue;
            }
            if (bitAt(map[i], bit) == 1) {
                map[i] ^= map[bit];
            }
        }
    }

    public boolean add(long val) {
        for (int i = 63; i >= 0 && val != 0; i--) {
            if (bitAt(val, i) == 0) {
                continue;
            }
            val ^= map[i];
        }
        if (val != 0) {
            int log = 63 - Long.numberOfLeadingZeros(val);
            map[log] = val;
            size++;
            afterAddBit(log);
            return true;
        }
        return false;
    }

    private long bitAt(long val, int i) {
        return (val >>> i) & 1;
    }

    /**
     * Find the k-th smallest possible generated number, and we consider 0 is the 0-th smallest.
     */
    public long theKthSmallestNumber(long k) {
        int id = 0;
        long num = 0;
        for (int i = 0; i < 64; i++) {
            if (map[i] == 0) {
                continue;
            }
            if (bitAt(k, id) == 1) {
                num ^= map[i];
            }
            id++;
        }
        return num;
    }

    /**
     * The rank of n in all generated numbers, 0's rank is 0
     */
    long theRankOfNumber(long n)
    {
        int index = size - 1;
        long rank = 0;
        for(int i = 63; i >= 0; i--)
        {
            if(map[i] == 0)
            {
                continue;
            }
            if(bitAt(n, i) == 1)
            {
                rank |= 1L << index;
                n ^= map[i];
            }
            index--;
        }
        return rank;
    }

    /**
     * Find the maximun value x ^ v where v is generated
     */
    long theMaximumNumberXor(long x)
    {
        for (int i = 0; i < 64; i++)
        {
            if (map[i] == 0)
            {
                continue;
            }
            if (bitAt(x, i) == 0)
            {
                x ^= map[i];
            }
        }
        return x;
    }
}