package com.daltao.oj.old.submit.poj;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.math.BigDecimal;

/**
 * Created by dalt on 2017/12/8.
 */
public class Pearls {
    static AcmInputReader input = new AcmInputReader(System.in);

    public static void main(String[] args) throws IOException {
        int testCase = input.nextInteger();
        while (testCase-- > 0) {
            Pearls pearls = new Pearls();
            pearls.init();
            System.out.println(pearls.solve());
        }
    }

    private static final int INF = 200000000;
    int levelNum;
    int[] prices;
    int[] requirements;

    public void init() throws IOException {
        levelNum = input.nextInteger();
        prices = new int[levelNum];
        requirements = new int[levelNum];
        for (int i = 0; i < levelNum; i++) {
            requirements[i] = input.nextInteger();
            prices[i] = input.nextInteger();
        }
    }

    public int cost(int num, int level) {
        return num == 0 ? 0 : (num + 10) * prices[level];
    }

    public int solve() {
        int[][] dp = new int[levelNum][levelNum];
        int[] suffixSum = new int[levelNum];
        suffixSum[levelNum - 1] = requirements[levelNum - 1];
        dp[levelNum - 1][levelNum - 1] = cost(suffixSum[levelNum - 1], levelNum - 1);
        for (int i = levelNum - 2, levelNumMinusOne = levelNum - 1; i >= 0; i--) {
            suffixSum[i] = suffixSum[i + 1] + requirements[i];
            dp[levelNumMinusOne][i] = cost(suffixSum[i], levelNumMinusOne);
        }


        for (int i = levelNum - 2; i >= 0; i--) {
            for (int j = levelNum - 1; j >= 0; j--) {
                dp[i][j] = dp[i + 1][j];
                if (j <= i) {
                    dp[i][j] = Math.min(dp[i][j], dp[i + 1][i + 1] + cost(suffixSum[j] - suffixSum[i + 1], i));
                }
            }
        }

        return dp[0][0];
    }

    /**
     * @author dalt
     * @since java1.7
     */
    static class AcmInputReader implements Cloneable {
        private PushbackInputStream in;


        public void close() throws IOException {
            in.close();
        }

        public static final byte BLANK_MARK = 1;
        public static final byte SIGN_MARK = 1 << 1;
        public static final byte NUMERAL_MARK = 1 << 2;
        public static final byte UPPERCASE_LETTER_MARK = 1 << 3;
        public static final byte LOWERCASE_LETTER_MARK = 1 << 4;
        public static final byte LETTER_MARK = UPPERCASE_LETTER_MARK | LOWERCASE_LETTER_MARK;
        public static final byte EOF = 1 << 5;
        public static byte[] asciiMarks = new byte[256];

        static {
            for (int i = 0; i <= 32; i++) {
                asciiMarks[i] = BLANK_MARK;
            }
            asciiMarks['+'] = SIGN_MARK;
            asciiMarks['-'] = SIGN_MARK;
            for (int i = '0'; i <= '9'; i++) {
                asciiMarks[i] = NUMERAL_MARK;
            }
            for (int i = 'a'; i <= 'z'; i++) {
                asciiMarks[i] = LOWERCASE_LETTER_MARK;
            }
            for (int i = 'A'; i <= 'Z'; i++) {
                asciiMarks[i] = UPPERCASE_LETTER_MARK;
            }
            asciiMarks[0xff] = EOF;
        }

        /**
         * 创建读取器
         *
         * @param input 输入流
         */
        public AcmInputReader(InputStream input) {
            in = new PushbackInputStream(new BufferedInputStream(input));
        }

        private int nextByte() throws IOException {
            return in.read() & 0xff;
        }

        /**
         * 如果下一个字节为b，则跳过该字节
         *
         * @param b 被跳过的字节值
         * @throws IOException if 输入流读取错误
         */
        public void skipByte(int b) throws IOException {
            int c;
            if ((c = nextByte()) != b) {
                in.unread(c);
            }
        }

        /**
         * 如果后续k个字节均为b，则跳过k个字节。这里{@literal k<times}
         *
         * @param b     被跳过的字节值
         * @param times 跳过次数，-1表示无穷
         * @throws IOException if 输入流读取错误
         */
        public void skipByte(int b, int times) throws IOException {
            int c;
            while ((c = nextByte()) == b && times > 0) {
                times--;
            }
            if (c != b) {
                in.unread(c);
            }
        }

        /**
         * 类似于{@link #skipByte(int, int)}, 但是会跳过中间出现的空白字符。
         *
         * @param b     被跳过的字节值
         * @param times 跳过次数，-1表示无穷
         * @throws IOException if 输入流读取错误
         */
        public void skipBlankAndByte(int b, int times) throws IOException {
            int c;
            while (times > 0) {
                skipBlank();
                if ((c = nextByte()) == b) {
                    times--;
                } else {
                    in.unread(c);
                    break;
                }
            }
        }

        /**
         * 读取下一块不含空白字符的字符块
         *
         * @return 下一块不含空白字符的字符块
         * @throws IOException if 输入流读取错误
         */
        public String nextBlock() throws IOException {
            skipBlank();
            StringBuilder sb = new StringBuilder();
            int c;
            while ((asciiMarks[c = nextByte()] & (BLANK_MARK | EOF)) == 0) {
                sb.append((char) c);
            }
            in.unread(c);
            return sb.toString();
        }

        /**
         * 跳过输入流中后续空白字符
         *
         * @throws IOException if 输入流读取错误
         */
        private void skipBlank() throws IOException {
            int c;
            while ((c = nextByte()) <= 32) ;
            in.unread(c);
        }

        /**
         * 读取下一个整数（可正可负），这里没有对溢出做判断
         *
         * @return 下一个整数值
         * @throws IOException if 输入流读取错误
         */
        public int nextInteger() throws IOException {
            skipBlank();
            int value = 0;
            boolean positive = true;
            int c = nextByte();
            if (asciiMarks[c] == SIGN_MARK) {
                positive = c == '+';
            } else {
                value = '0' - c;
            }
            c = nextByte();
            while (asciiMarks[c] == NUMERAL_MARK) {
                value = (value << 3) + (value << 1) + '0' - c;
                c = nextByte();
            }

            in.unread(c);
            return positive ? -value : value;
        }

        /**
         * 判断是否到了文件结尾
         *
         * @return true如果到了文件结尾，否则false
         * @throws IOException if 输入流读取错误
         */
        public boolean isMeetEOF() throws IOException {
            int c = nextByte();
            if (asciiMarks[c] == EOF) {
                return true;
            }
            in.unread(c);
            return false;
        }

        /**
         * 判断是否在跳过空白字符后抵达文件结尾
         *
         * @return true如果到了文件结尾，否则false
         * @throws IOException if 输入流读取错误
         */
        public boolean isMeetBlankAndEOF() throws IOException {
            skipBlank();
            int c = nextByte();
            if (asciiMarks[c] == EOF) {
                return true;
            }
            in.unread(c);
            return false;
        }

        /**
         * 获取下一个用英文字母组成的单词
         *
         * @return 下一个用英文字母组成的单词
         */
        public String nextWord() throws IOException {
            StringBuilder sb = new StringBuilder(16);
            skipBlank();
            int c;
            while ((asciiMarks[(c = nextByte())] & LETTER_MARK) != 0) {
                sb.append((char) c);
            }
            in.unread(c);
            return sb.toString();
        }

        /**
         * 读取下一个长整数（可正可负），这里没有对溢出做判断
         *
         * @return 下一个长整数值
         * @throws IOException if 输入流读取错误
         */
        public long nextLong() throws IOException {
            skipBlank();
            long value = 0;
            boolean positive = true;
            int c = nextByte();
            if (asciiMarks[c] == SIGN_MARK) {
                positive = c == '+';
            } else {
                value = '0' - c;
            }
            c = nextByte();
            while (asciiMarks[c] == NUMERAL_MARK) {
                value = (value << 3) + (value << 1) + '0' - c;
                c = nextByte();
            }
            in.unread(c);
            return positive ? -value : value;
        }

        /**
         * 读取下一个浮点数（可正可负），浮点数是近似值
         *
         * @return 下一个浮点数值
         * @throws IOException if 输入流读取错误
         */
        public float nextFloat() throws IOException {
            return (float) nextDouble();
        }

        /**
         * 读取下一个浮点数（可正可负），浮点数是近似值
         *
         * @return 下一个浮点数值
         * @throws IOException if 输入流读取错误
         */
        public double nextDouble() throws IOException {
            skipBlank();
            double value = 0;
            boolean positive = true;
            int c = nextByte();
            if (asciiMarks[c] == SIGN_MARK) {
                positive = c == '+';
            } else {
                value = c - '0';
            }
            c = nextByte();
            while (asciiMarks[c] == NUMERAL_MARK) {
                value = value * 10.0 + c - '0';
                c = nextByte();
            }

            if (c == '.') {
                double littlePart = 0;
                double base = 1;
                c = nextByte();
                while (asciiMarks[c] == NUMERAL_MARK) {
                    littlePart = littlePart * 10.0 + c - '0';
                    base *= 10.0;
                    c = nextByte();
                }
                value += littlePart / base;
            }
            in.unread(c);
            return positive ? value : -value;
        }

        /**
         * 读取下一个高精度数值
         *
         * @return 下一个高精度数值
         * @throws IOException if 输入流读取错误
         */
        public BigDecimal nextDecimal() throws IOException {
            skipBlank();
            StringBuilder sb = new StringBuilder();
            sb.append((char) nextByte());
            int c = nextByte();
            while (asciiMarks[c] == NUMERAL_MARK) {
                sb.append((char) c);
                c = nextByte();
            }
            if (c == '.') {
                sb.append('.');
                c = nextByte();
                while (asciiMarks[c] == NUMERAL_MARK) {
                    sb.append((char) c);
                    c = nextByte();
                }
            }
            in.unread(c);
            return new BigDecimal(sb.toString());
        }
    }
}
