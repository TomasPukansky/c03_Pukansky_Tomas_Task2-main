package model;

import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;

import java.util.ArrayList;
import java.util.List;

public class Polygon {
    private final List<Point> points;
    private LineRasterizer rasterizer;
    private boolean closed = false;

    public Polygon(){
        this.points = new ArrayList<>();
    }

    public Polygon(List<Point> points) {
        this.points = new ArrayList<>(points);
    }

    public void addPoint(Point p){
        this.points.add(p);
    }

    public Point getPoint(int index){
        return this.points.get(index);
    }

    public int getSize(){
        return points.size();
    }

    public List<Point> getPoints() {
        return points;
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        if (points.size() >= 3) {
            closed = true;
        }
    }
    public void setLastPoint(Point p) {
        if (points.isEmpty()) {
            points.add(p);
        } else {
            points.set(points.size() - 1, p);
        }
    }

    // vycistenie polygonu
    public void clear() {
        points.clear();
        closed = false;
    }

    //seter pre rasterize vola sa ked je polygon dokonceny
    public void setRasterizer(LineRasterizer rasterizer) {
        this.rasterizer = rasterizer;

    }

    public LineRasterizer getRasterizer() {
        return rasterizer;
    }

    public List<Line> getEdges() {
        List<Line> edges = new ArrayList<>();

        if (points.size() < 2) {
            return edges;
        }


        for (int i = 0; i < points.size() - 1; i++) {
            Point p1 = points.get(i);
            Point p2 = points.get(i + 1);
            Line edge = new Line(p1, p2);
            edge.setRasterizer(rasterizer);
            edges.add(edge);
        }

        // napojit posledny bod
        if (points.size() >= 3) {
            Point first = points.get(0);
            Point last = points.get(points.size() - 1);
            Line closingEdge = new Line(last, first);
            closingEdge.setRasterizer(rasterizer);
            edges.add(closingEdge);
        }

        return edges;
    }

}
