package clip;

import model.Point;
import model.Polygon;

import java.util.ArrayList;
import java.util.List;

public class Clipper {
    private int numberOfIntersections;
    private List<Point> resultPoints;

    public List<Point> clip(List<Point> clipperPoints, List<Point> pointsToClip) {
        resultPoints = new ArrayList<>();
        this.numberOfIntersections = 0;

        System.out.println("Clipper: Starting Weiler-Atherton with clipper=" + clipperPoints.size() +
                ", toClip=" + pointsToClip.size());

        List<Point> a = clipOne(pointsToClip, clipperPoints);
        List<Point> b = clipOne(clipperPoints, pointsToClip);

        System.out.println("After clipOne: a=" + a.size() + ", b=" + b.size());

        int i = 0, j = 0;
        while(true) {
            if(i >= a.size()){
                for (int n = j; n < b.size(); n++){
                    resultPoints.add(b.get(n));
                }
                break;
            }

            if(j >= b.size()){
                for (int n = i; n < a.size(); n++){
                    resultPoints.add(a.get(n));
                }
                break;
            }

            if(a.get(i).getX() == b.get(j).getX() && a.get(i).getY() == b.get(j).getY()){
                resultPoints.add(a.get(i));
                i++;
                j++;
            }
            else{
                List<Point> temp = mergePoints(a, b, i, j);
                if(temp != null){
                    for (int n = 1; n < temp.size(); n++){
                        resultPoints.add(temp.get(n));
                    }
                    if(temp.get(0).getX() == 0){
                        i+= temp.size();
                        j++;
                    }
                    else{
                        j+= temp.size();
                        i++;
                    }
                }
            }
        }

        System.out.println("Clipper: Final result has " + resultPoints.size() + " points");
        return resultPoints;
    }

    private boolean isInside(List<Point> clipperPoints, Point p) {
        int windingNumber = 0;

        for (int i = 0; i < clipperPoints.size(); i++) {
            Point start = clipperPoints.get(i);
            Point end = clipperPoints.get((i + 1) % clipperPoints.size());

            if (start.getY() <= p.getY()) {
                if (end.getY() > p.getY()) {
                    if (isLeftOfEdge(start, end, p) > 0) {
                        windingNumber++;
                    }
                }
            } else {
                if (end.getY() <= p.getY()) {
                    if (isLeftOfEdge(start, end, p) < 0) {
                        windingNumber--;
                    }
                }
            }
        }

        return windingNumber != 0;
    }

    private double isLeftOfEdge(Point start, Point end, Point p) {
        return (end.getX() - start.getX()) * (p.getY() - start.getY()) -
                (p.getX() - start.getX()) * (end.getY() - start.getY());
    }

    private Point calculateIntersection(List<Point> clipperPoints, Point p1, Point p2) {
        for (int i = 0; i < clipperPoints.size(); i++) {
            Point cP1 = clipperPoints.get(i);
            Point cP2 = clipperPoints.get((i + 1) % clipperPoints.size());

            double x1 = cP1.getX(), y1 = cP1.getY();
            double x2 = cP2.getX(), y2 = cP2.getY();
            double x3 = p1.getX(), y3 = p1.getY();
            double x4 = p2.getX(), y4 = p2.getY();

            double numeratorX = (x1 * y2 - y1 * x2) * (x3 - x4) - (x3 * y4 - y3 * x4) * (x1 - x2);
            double numeratorY = (x1 * y2 - y1 * x2) * (y3 - y4) - (x3 * y4 - y3 * x4) * (y1 - y2);
            double denominator = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

            if (Math.abs(denominator) < 1e-10) {
                continue;
            }

            double x0 = numeratorX / denominator;
            double y0 = numeratorY / denominator;

            if (isBetween(x1, x2, x0) && isBetween(y1, y2, y0) &&
                    isBetween(x3, x4, x0) && isBetween(y3, y4, y0)) {
                this.numberOfIntersections++;
                return new Point((int) Math.round(x0), (int) Math.round(y0));
            }
        }
        return null;
    }

    private boolean isBetween(double a, double b, double c) {
        return Math.min(a, b) <= c && c <= Math.max(a, b);
    }

    private List<Point> mergePoints(List<Point> a, List<Point> b, int i, int j) {
        boolean checkA = false, checkB = false;
        List<Point> pointsA = new ArrayList<>();
        pointsA.add(new Point(0, 0));

        List<Point> pointsB = new ArrayList<>();
        pointsB.add(new Point(1, 0));

        pointsA.add(a.get(i));
        pointsB.add(b.get(j));

        MAIN: for (int n = i; n < a.size()-1; n++) {
            for (Point p : b) {
                if(a.get(n+1).getX() == p.getX() && a.get(n+1).getY() == p.getY()) {
                    checkA = true;
                    pointsA.add(new Point(a.get(n+1).getX(), a.get(n+1).getY()));
                    break MAIN;
                }
            }
            pointsA.add(new Point(a.get(n+1).getX(), a.get(n+1).getY()));
        }

        MAIN: for (int n = j; n < b.size()-1; n++) {
            for (Point p : a) {
                if(b.get(n+1).getX() == p.getX() && b.get(n+1).getY() == p.getY()) {
                    checkB = true;
                    pointsB.add(new Point(b.get(n+1).getX(), b.get(n+1).getY()));
                    break MAIN;
                }
            }
            pointsB.add(new Point(b.get(n+1).getX(), b.get(n+1).getY()));
        }

        if(pointsA.size() < pointsB.size() && checkA){
            return pointsA;
        }
        else if (checkB){
            return pointsB;
        }
        return null;
    }

    private List<Point> clipOne(List<Point> a, List<Point> b) {
        System.out.println("clipOne: processing " + a.size() + " points against " + b.size() + " edges");
        List<Point> result = new ArrayList<>();

        for (int i = 0; i < a.size(); i++) {
            Point currentPoint = a.get(i);
            Point nextPoint = a.get((i + 1) % a.size());

            boolean currentInside = isInside(b, currentPoint);
            boolean nextInside = isInside(b, nextPoint);

            if (currentInside) {
                result.add(currentPoint);
            }

            if (currentInside != nextInside) {
                Point intersection = calculateIntersection(b, currentPoint, nextPoint);
                if (intersection != null) {
                    result.add(intersection);
                }
            }
        }

        System.out.println("clipOne: returning " + result.size() + " points");
        return result;
    }
}