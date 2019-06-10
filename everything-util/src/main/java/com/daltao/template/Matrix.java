package com.daltao.template;

public class Matrix implements Cloneable {
    double[][] mat;
    int n;
    int m;

    public Matrix(Matrix model) {
        n = model.n;
        m = model.m;
        mat = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                mat[i][j] = model.mat[i][j];
            }
        }
    }

    public Matrix(int n, int m) {
        this.n = n;
        this.m = m;
        mat = new double[n][m];
    }

    public void fill(int v) {
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                mat[i][j] = v;
            }
        }
    }

    public void asStandard() {
        fill(0);
        for (int i = 0; i < n && i < m; i++) {
            mat[i][i] = 1;
        }
    }

    public static Matrix mul(Matrix a, Matrix b) {
        Matrix c = new Matrix(a.n, b.m);
        for (int i = 0; i < c.n; i++) {
            for (int j = 0; j < c.m; j++) {
                for (int k = 0; k < a.m; k++) {
                    c.mat[i][j] = c.mat[i][j] + a.mat[i][k] * b.mat[k][j];
                }
            }
        }
        return c;
    }

    public static Matrix pow(Matrix x, int n) {
        if (n == 0) {
            Matrix r = new Matrix(x.n, x.m);
            r.asStandard();
            return r;
        }
        Matrix r = pow(x, n >> 1);
        r = Matrix.mul(r, r);
        if (n % 2 == 1) {
            r = Matrix.mul(r, x);
        }
        return r;
    }

    public static Matrix inverse(Matrix x) {
        if (x.n != x.m) {
            throw new RuntimeException("Matrix is not square");
        }
        int n = x.n;
        Matrix l = new Matrix(x);
        Matrix r = new Matrix(n, n);
        r.asStandard();
        for (int i = 0; i < n; i++) {
            int maxRow = i;
            for (int j = i; j < n; j++) {
                if (Math.abs(l.mat[j][i]) > Math.abs(l.mat[maxRow][i])) {
                    maxRow = j;
                }
            }

            if (l.mat[maxRow][i] == 0) {
                throw new RuntimeException("Can't inverse current matrix");
            }
            r.swapRow(i, maxRow);
            l.swapRow(i, maxRow);

            r.divideRow(i, l.mat[i][i]);
            l.divideRow(i, l.mat[i][i]);

            for (int j = 0; j < n; j++) {
                if (j == i) {
                    continue;
                }
                if (l.mat[j][i] == 0) {
                    continue;
                }
                double f = l.mat[j][i];
                r.subtractRow(j, i, f);
                l.subtractRow(j, i, f);
            }
        }
        return r;
    }

    static Matrix transposition(Matrix x) {
        int n = x.n;
        int m = x.m;
        Matrix t = new Matrix(m, n);
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                t.mat[j][i] = x.mat[i][j];
            }
        }
        return t;
    }

    void swapRow(int i, int j) {
        double[] row = mat[i];
        mat[i] = mat[j];
        mat[j] = row;
    }

    void subtractRow(int i, int j, double f) {
        for (int k = 0; k < m; k++) {
            mat[i][k] -= mat[j][k] * f;
        }
    }

    void divideRow(int i, double f) {
        for (int k = 0; k < m; k++) {
            mat[i][k] /= f;
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                builder.append(mat[i][j]).append(' ');
            }
            builder.append('\n');
        }
        return builder.toString();
    }

}