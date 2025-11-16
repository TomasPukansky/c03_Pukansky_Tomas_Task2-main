package clip;

import model.Point;
import java.util.ArrayList;
import java.util.List;

public class Clipper {

    public List<Point> clip(List<Point> subjectPolygon, List<Point> clipperPolygon) {
        System.out.println("Clipper: Starting with subject=" + subjectPolygon.size() +
                ", clipper=" + clipperPolygon.size());

        List<Point> result = new ArrayList<>();

        for (int i = 0; i < subjectPolygon.size(); i++) {
            Point current = subjectPolygon.get(i);
            Point next = subjectPolygon.get((i + 1) % subjectPolygon.size());

            boolean currentOutside = !isInside(clipperPolygon, current);
            boolean nextOutside = !isInside(clipperPolygon, next);

            if (currentOutside && nextOutside) {
                // Oba body mimo → pridaj ďalší bod
                result.add(next);
            } else if (currentOutside && !nextOutside) {
                // Vstup do clippera → pridaj priesečník
                Point intersection = findIntersection(clipperPolygon, current, next);
                if (intersection != null) {
                    result.add(intersection);
                }
            } else if (!currentOutside && nextOutside) {
                // Výstup z clippera → pridaj priesečník a ďalší bod
                Point intersection = findIntersection(clipperPolygon, current, next);
                if (intersection != null) {
                    result.add(intersection);
                }
                result.add(next);
            }
            // else: oba vnútri → nepridávaj nič
        }

        System.out.println("Clipper: Result has " + result.size() + " points");
        return result;
    }

    private boolean isInside(List<Point> polygon, Point p) {
        int winding = 0;

        for (int i = 0; i < polygon.size(); i++) {
            Point p1 = polygon.get(i);
            Point p2 = polygon.get((i + 1) % polygon.size());

            if (p1.getY() <= p.getY()) {
                if (p2.getY() > p.getY()) {
                    if (isLeft(p1, p2, p) > 0) {
                        winding++;
                    }
                }
            } else {
                if (p2.getY() <= p.getY()) {
                    if (isLeft(p1, p2, p) < 0) {
                        winding--;
                    }
                }
            }
        }

        return winding != 0;
    }

    private double isLeft(Point p0, Point p1, Point p2) {
        return (p1.getX() - p0.getX()) * (p2.getY() - p0.getY()) -
                (p2.getX() - p0.getX()) * (p1.getY() - p0.getY());
    }

    private Point findIntersection(List<Point> clipper, Point p1, Point p2) {
        for (int i = 0; i < clipper.size(); i++) {
            Point c1 = clipper.get(i);
            Point c2 = clipper.get((i + 1) % clipper.size());

            double x1 = c1.getX(), y1 = c1.getY();
            double x2 = c2.getX(), y2 = c2.getY();
            double x3 = p1.getX(), y3 = p1.getY();
            double x4 = p2.getX(), y4 = p2.getY();

            double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            if (Math.abs(denom) < 1e-10) {
                continue;
            }

            double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;
            double u = -((x1 - x2) * (y1 - y3) - (y1 - y2) * (x1 - x3)) / denom;

            if (t >= 0 && t <= 1 && u >= 0 && u <= 1) {
                int x = (int) Math.round(x1 + t * (x2 - x1));
                int y = (int) Math.round(y1 + t * (y2 - y1));
                return new Point(x, y);
            }
        }
        return null;
    }
}