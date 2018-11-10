package com.daltao.oj.old.submit.poj;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Administrator on 2017/12/8.
 */
public class Palindrome {
    private static final int INF = (int) 1e8;
    private static BlockReader input;

    static {
        try {
            System.setIn(new FileInputStream("D:\\DataBase\\TESTCASE\\poj\\Palindrome.in"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    int n;
    char[] data;

    public static void main(String[] args) {
        input = new BlockReader(System.in);
        while (input.hasMore()) {
            Palindrome palindrome = new Palindrome();
            palindrome.init();
            System.out.println(palindrome.solve());
        }
    }

    public void init() {
        n = input.nextInteger();
        data = new char[n];
        input.nextBlock(data, 0);
    }

    public int solve() {
        int[] dpW2 = new int[n];
        int[] dpW1 = new int[n];
        int[] dp = new int[n];
        for (int i = 1; i < n; i++) {
            int[] tmp = dpW2;
            dpW2 = dpW1;
            dpW1 = dp;
            dp = tmp;
            for (int j = 0, bound = n - i; j < bound; j++) {
                int left = j;
                int right = i + j;
                int min = INF;
                if (data[left] == data[right]) {
                    min = left + 1 == right ? 0 : dpW2[left + 1];
                }
                min = Math.min(dpW1[left + 1] + 1, min);
                min = Math.min(dpW1[left] + 1, min);
                dp[left] = min;
            }
        }

        return dp[0];
    }

    public static class BlockReader {
        static final int EOF = -1;
        InputStream is;
        byte[] dBuf;
        int dPos, dSize, next;
        StringBuilder builder = new StringBuilder();

        public BlockReader(InputStream is) {
            this(is, 8192);
        }

        public BlockReader(InputStream is, int bufSize) {
            this.is = is;
            dBuf = new byte[bufSize];
            next = nextByte();
        }

        public void skipBlank() {
            while (Character.isWhitespace(next)) {
                next = nextByte();
            }
        }

        public String nextBlock() {
            builder.setLength(0);
            skipBlank();
            while (next != EOF && !Character.isWhitespace(next)) {
                builder.append((char) next);
                next = nextByte();
            }
            return builder.toString();
        }

        public int nextInteger() {
            skipBlank();
            int ret = 0;
            boolean rev = false;
            if (next == '+' || next == '-') {
                rev = next == '-';
                next = nextByte();
            }
            while (next >= '0' && next <= '9') {
                ret = (ret << 3) + (ret << 1) + next - '0';
                next = nextByte();
            }
            return rev ? -ret : ret;
        }

        public int nextBlock(char[] data, int offset) {
            skipBlank();
            int index = offset;
            int bound = data.length;
            while (next != EOF && index < bound && !Character.isWhitespace(next)) {
                data[index++] = (char) next;
                next = nextByte();
            }
            return index - offset;
        }

        public boolean hasMore() {
            skipBlank();
            return next != EOF;
        }

        public int nextByte() {
            while (dPos >= dSize) {
                if (dSize == -1) {
                    return EOF;
                }
                dPos = 0;
                try {
                    dSize = is.read(dBuf);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            return dBuf[dPos++];
        }
    }
}
