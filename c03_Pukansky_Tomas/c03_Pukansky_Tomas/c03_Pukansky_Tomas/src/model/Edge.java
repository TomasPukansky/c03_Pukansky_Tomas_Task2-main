package model;


import rasterize.LineRasterizer;

public class Edge {
    private final int y1;
    private final int x1;
    private final int y2;
    private final int x2;
    private LineRasterizer rasterizer;

    public Edge(int y1, int x1, int y2, int x2) {
        this.y1 = y1;
        this.x1 = x1;
        this.y2 = y2;
        this.x2 = x2;
    }


    public Edge(Point p1, Point p2) {
        this.y1 = p1.getY();
        this.x1 = p1.getX();
        this.y2 = p2.getY();
        this.x2 = p2.getX();
    }

    public Boolean isHorizontal() {
        //todo: dorobit
        return y1 == y2;
    }

    public void orientate() {
        if (y1 > y2) {
            //todo: dorobit prehodit x a y

        }
    }

    public Boolean isIntersection(int y) {
        return y1 <= y2 && y2 >= y1;

    }

    public int getIntersection(int y) {
        return y1;
        //todo: spocitat priesecnik, pre y najdem x
    }



    public int getY1() {
        return y1;
    }

    public int getX1() {
        return x1;
    }

    public int getY2() {
        return y2;
    }

    public int getX2() {
        return x2;
    }

    public void setRasterizer(LineRasterizer rasterizer) {
        this.rasterizer = rasterizer;
    }
    public LineRasterizer getRasterizer() {
        return rasterizer;
    }
}
