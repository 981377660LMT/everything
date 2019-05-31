package com.daltao.template;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

public class GeometryUtils {
    private static final double PREC = 1e-8;
    private static final double INF = 1e30;

    public static double valueOf(double x) {
        return x > -PREC && x < PREC ? 0 : x;
    }

    public static class Point2D {
        final double x;
        final double y;
        static final Point2D ORIGIN = new Point2D(0, 0);
        static final Comparator<Point2D> SORT_BY_X_AND_Y = new Comparator<Point2D>() {
            @Override
            public int compare(Point2D a, Point2D b) {
                return a.x == b.x ? Double.compare(a.y, b.y) : Double.compare(a.x, b.x);
            }
        };

        public Point2D(double x, double y) {
            this.x = x;
            this.y = y;
        }

        public double distance2Between(Point2D another) {
            double dx = x - another.x;
            double dy = y - another.y;
            return valueOf(dx * dx + dy * dy);
        }

        public double distanceBetween(Point2D another) {
            return valueOf(Math.sqrt(distance2Between(another)));
        }

        /**
         * 以自己为起点，判断线段a和b的叉乘
         */
        public double cross(Point2D a, Point2D b) {
            return GeometryUtils.cross(a.x - x, a.y - y, b.x - x, b.y - y);
        }

        public Point2D getApeak() {
            return new Point2D(-y, x);
        }

        public Point2D add(Point2D vector) {
            return new Point2D(x + vector.x, y + vector.y);
        }

        public Point2D add(Point2D vector, double times) {
            return new Point2D(x + vector.x * times, y + vector.y * times);
        }

        public Point2D normalize() {
            double d = distanceBetween(ORIGIN);
            return new Point2D(valueOf(x / d), valueOf(y / d));
        }

        @Override
        public String toString() {
            return String.format("(%f, %f)", x, y);
        }

        public static double cross(Point2D a, Point2D b, Point2D c, Point2D d) {
            return GeometryUtils.cross(b.x - a.x, b.y - a.y, d.x - c.x, d.y - c.y);
        }
    }

    public static class Line2D {
        final Point2D a;
        final Point2D b;
        final Point2D d;
        final double theta;
        /**
         * 按照[0,2pi)极角对线排序
         */
        static final Comparator<Line2D> SORT_BY_ANGLE = new Comparator<Line2D>() {
            @Override
            public int compare(Line2D a, Line2D b) {
                return Double.compare(a.theta, b.theta);
            }
        };


        public Line2D(Point2D a, Point2D b) {
            this.a = a;
            this.b = b;
            d = new Point2D(valueOf(b.x - a.x), valueOf(b.y - a.y));
            theta = Math.atan2(d.y, d.x);
        }

        /**
         * 判断a处于b的哪个方向，返回1，表示处于逆时针方向，返回-1，表示处于顺时针方向。0表示共线。
         */
        public int onWhichSide(Line2D b) {
            return Double.compare(cross(d.x, d.y, b.d.x, b.d.y), 0);
        }

        /**
         * 判断pt处于自己的哪个方向，返回1，表示处于逆时针方向，返回-1，表示处于顺时针方向。0表示共线。
         */
        public int whichSideIs(Point2D pt) {
            return Double.compare(a.cross(b, pt), 0);
        }

        public double getSlope() {
            return a.y / a.x;
        }

        public double getB() {
            return a.y - getSlope() * a.x;
        }

        public Point2D intersect(Line2D another) {
            double m11 = b.x - a.x;
            double m01 = another.b.x - another.a.x;
            double m10 = a.y - b.y;
            double m00 = another.a.y - another.b.y;

            double div = valueOf(m00 * m11 - m01 * m10);
            if (div == 0) {
                return null;
            }

            double v0 = (another.a.x - a.x) / div;
            double v1 = (another.a.y - a.y) / div;

            double alpha = m00 * v0 + m01 * v1;
            return getPoint(alpha);
        }

        /**
         * 获取与线段的交点，null表示无交点或有多个交点
         */
        public Point2D getPoint(double alpha) {
            return new Point2D(a.x + d.x * alpha, a.y + d.y * alpha);
        }

        public Line2D moveAlong(Point2D vector) {
            return new Line2D(a.add(vector), b.add(vector));
        }

        public Line2D moveAlong(Point2D vector, double times) {
            return new Line2D(a.add(vector, times), b.add(vector, times));
        }


        @Override
        public String toString() {
            return d.toString();
        }
    }

    public static class Segment2D extends Line2D {
        public Segment2D(Point2D a, Point2D b) {
            super(a, b);
        }

        /**
         * 判断p是否落在线段section上
         */
        public boolean contain(Point2D p) {
            return cross(p.x - a.x, p.y - a.y, d.x, d.y) == 0
                    && valueOf(p.x - Math.min(a.x, b.x)) >= 0 && valueOf(p.x - Math.min(a.x, b.x)) <= 0
                    && valueOf(p.y - Math.min(a.y, b.y)) >= 0 && valueOf(p.y - Math.min(a.y, b.y)) <= 0;
        }

        /**
         * 获取与线段的交点，null表示无交点或有多个交点
         */
        public Point2D intersect(Segment2D another) {
            Point2D point = super.intersect(another);
            return point != null && contain(point) ? point : null;
        }

    }

    /**
     * 计算两个向量的叉乘
     */
    public static double cross(double x1, double y1, double x2, double y2) {
        return valueOf(x1 * y2 - y1 * x2);
    }

    public static int signOf(double x) {
        return x > 0 ? 1 : x < 0 ? -1 : 0;
    }

    public static class Area {
        public double areaOfRect(Line2D a, Line2D b) {
            return Math.abs(cross(a.d.x, a.d.y, b.d.x, b.d.y));
        }

        public double areaOfTriangle(Line2D a, Line2D b) {
            return areaOfRect(a, b) / 2;
        }

        public double areaOfTriangle(Point2D a, Point2D b, Point2D c) {
            return Math.abs(a.cross(b, c)) / 2;
        }

    }

    public static class GrahamScan {
        PointConvexHull convex;

        public GrahamScan(PointPolygon pointPolygon) {
            final Point2D[] points = pointPolygon.data.toArray(new Point2D[0]);
            int n = points.length;
            for (int i = 1; i < n; i++) {
                int cmp = points[i].y != points[0].y ? Double.compare(points[i].y, points[0].y)
                        : Double.compare(points[i].x, points[0].x);
                if (cmp >= 0) {
                    continue;
                }
                Point2D tmp = points[0];
                points[i] = points[0];
                points[0] = tmp;
            }


            Comparator<Point2D> cmp = new Comparator<Point2D>() {
                @Override
                public int compare(Point2D o1, Point2D o2) {
                    return signOf(valueOf(-points[0].cross(o1, o2)));
                }
            };
            Arrays.sort(points, 1, n, cmp);

            int shrinkSize = 2;
            for (int i = 2; i < n; i++) {
                if (cmp.compare(points[i], points[shrinkSize - 1]) == 0) {
                    if (points[i].distance2Between(points[0]) > points[shrinkSize - 1].distance2Between(points[0])) {
                        points[shrinkSize - 1] = points[i];
                    }
                } else {
                    points[shrinkSize++] = points[i];
                }
            }

            n = shrinkSize;
            Deque<Point2D> stack = new ArrayDeque(n);
            stack.addLast(points[0]);
            for (int i = 1; i < n; i++) {
                while (stack.size() >= 2) {
                    Point2D last = stack.removeLast();
                    Point2D second = stack.peekLast();
                    if (valueOf(second.cross(points[i], last)) < 0) {
                        stack.addLast(last);
                        break;
                    }
                }
                stack.addLast(points[i]);
            }

            convex = new PointConvexHull(new ArrayList(stack));
        }
    }


    public static class HalfPlaneIntersection {
        LineConvexHull convex;
        boolean hasSolution = true;

        public HalfPlaneIntersection(Polygon<Line2D> linePolygon, boolean close) {
            this(linePolygon, close, false);
        }

        public HalfPlaneIntersection(Polygon<Line2D> linePolygon, boolean close, boolean isAnticlockwise) {
            Line2D[] lines = linePolygon.data.toArray(new Line2D[linePolygon.data.size()]);
            if (!isAnticlockwise) {
                Arrays.sort(lines, Line2D.SORT_BY_ANGLE);
            }
            int n = lines.length;


            Deque<Line2D> deque = new ArrayDeque(n);
            for (int i = 0; i < n; i++) {
                Line2D line = lines[i];
                while (i + 1 < n && line.onWhichSide(lines[i + 1]) == 0) {
                    i++;
                    if (line.whichSideIs(lines[i].b) == 1) {
                        line = lines[i];
                    }
                }
                insert(deque, line, close);
            }
            insert(deque, deque.removeFirst(), close);

            //reinsert head
            if (!hasSolution) {
                return;
            }
            convex = new LineConvexHull(new ArrayList(deque));
        }

        private void insert(Deque<Line2D> deque, Line2D line, boolean close) {
            if (!hasSolution) {
                return;
            }
            while (deque.size() >= 2) {
                Line2D tail = deque.removeLast();
                Point2D pt = tail.intersect(deque.peekLast());
                if (pt == null) {
                    continue;
                }
                int side = line.whichSideIs(pt);
                if (side > 0 || (close && side == 0)) {
                    deque.add(tail);
                    break;
                }
                if (line.onWhichSide(deque.peekLast()) != tail.onWhichSide(deque.peekLast())) {
                    hasSolution = false;
                }

            }
            if (deque.size() == 1 && line.onWhichSide(deque.peekLast()) == 0) {
                int side = deque.peekLast().whichSideIs(line.b);
                if (!(side > 0 || (close && side == 0))) {
                    hasSolution = false;
                }
            }

            deque.addLast(line);
        }
    }

    public static class LinePolygon extends Polygon<Line2D> {
        public LinePolygon(List<Line2D> points) {
            super(points);
        }

        public PointPolygon asPoints() {
            int n = data.size();
            Point2D[] pts = new Point2D[n];
            pts[0] = data.get(0).intersect(data.get(n - 1));
            for (int i = 1; i < n; i++) {
                pts[i] = data.get(i).intersect(data.get(i - 1));
            }
            return new PointPolygon(Arrays.asList(pts));
        }
    }

    public static class LineConvexHull extends LinePolygon {
        public LineConvexHull(List<Line2D> points) {
            super(points);
        }

        public PointConvexHull asPoints() {
            int n = data.size();
            Point2D[] pts = new Point2D[n];
            pts[0] = data.get(0).intersect(data.get(n - 1));
            for (int i = 1; i < n; i++) {
                pts[i] = data.get(i).intersect(data.get(i - 1));
            }
            return new PointConvexHull(Arrays.asList(pts));
        }
    }

    public static class PointConvexHull extends PointPolygon {
        public PointConvexHull(List<Point2D> points) {
            super(points);
        }

        public LineConvexHull asLines() {
            int n = data.size();
            Line2D[] lines = new Line2D[n];
            lines[0] = new Line2D(data.get(n - 1), data.get(0));
            for (int i = 1; i < n; i++) {
                lines[i] = new Line2D(data.get(i - 1), data.get(i));
            }
            return new LineConvexHull(Arrays.asList(lines));
        }


        public double area() {
            Area areaHelper = new Area();
            double area = 0;
            int n = data.size();
            for (int i = 2; i < n; i++) {
                area += areaHelper.areaOfTriangle(data.get(0), data.get(i),
                        data.get(i - 1));
            }
            return area;
        }

        public Segment2D theFarthestPointPair() {
            //旋转卡壳
            int n = data.size();
            Point2D[] ab = new Point2D[2];
            Point2D[] cd = new Point2D[]{pointAtLoop(1), pointAtLoop(2)};

            Point2D x = cd[0];
            Point2D y = cd[1];
            double farthestDist2 = 0;
            for (int i = 0, j = 1; i < n; i++) {
                ab[0] = pointAtLoop(i);
                ab[1] = pointAtLoop(i + 1);
                while (Point2D.cross(ab[0], ab[1], cd[0], cd[1]) <= 0 && Point2D.cross(ab[0], ab[1], cd[1], pointAtLoop(j + 2)) <= 0) {
                    j++;
                    cd[0] = cd[1];
                    cd[1] = pointAtLoop(j + 1);
                }
                for (int k = 0; k < 2; k++) {
                    for (int t = 0; t < 2; t++) {
                        double dist2 = ab[k].distance2Between(cd[t]);
                        if (farthestDist2 >= dist2) {
                            continue;
                        }
                        x = ab[k];
                        y = cd[t];
                        farthestDist2 = dist2;
                    }
                }
            }

            return new Segment2D(x, y);
        }

        public Segment2D theNearestPointPair() {
            //最近点对，分而治之
            int n = data.size();
            Point2D[] points = data.toArray(new Point2D[n]);
            EnhancedPointWithRank[] enhancedPoints = new EnhancedPointWithRank[n];
            for (int i = 0; i < n; i++) {
                points[i].point = data.get(i);
            }
            Arrays.sort(points, );
        }

        private static class EnhancedPointWithRank {
            Point2D point;
            int rank;
            static final Comparator<EnhancedPointWithRank> SORT_BY_X_AND_Y = new Comparator<EnhancedPointWithRank>() {
                @Override
                public int compare(EnhancedPointWithRank a, EnhancedPointWithRank b) {
                    return a.point.x == b.point.x ? Double.compare(a.point.y, b.point.y);
                }
            }
        }

        private Point2D pointAtLoop(int i) {
            return data.get(i % data.size());
        }
    }


    public static class PointPolygon extends Polygon<Point2D> {
        public PointPolygon(List<Point2D> points) {
            super(points);
        }

        public LinePolygon asLines() {
            int n = data.size();
            Line2D[] lines = new Line2D[n];
            lines[0] = new Line2D(data.get(n - 1), data.get(0));
            for (int i = 1; i < n; i++) {
                lines[i] = new Line2D(data.get(i - 1), data.get(i));
            }
            return new LinePolygon(Arrays.asList(lines));
        }
    }

    public static class Polygon<T> {
        List<T> data;

        private Polygon(List<T> data) {
            this.data = data;
        }
    }
}
