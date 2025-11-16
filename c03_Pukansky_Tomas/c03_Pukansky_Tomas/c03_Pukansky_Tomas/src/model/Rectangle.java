package model;

public class Rectangle extends Polygon {
    private Point baseStart;
    private Point baseEnd;
    private Point heightPoint;

    public Rectangle(Point baseStart, Point baseEnd) {
        super();
        this.baseStart = baseStart;
        this.baseEnd = baseEnd;
        updateRectangle(baseEnd); // Initial preview with zero height
    }
    public void setHeightPoint(Point heightPoint) {
        this.heightPoint = heightPoint;
        updateRectangle(heightPoint);
    }
    private void updateRectangle(Point thirdPoint) {
        clear(); // Clear existing points

        double x1 = baseStart.getX();
        double y1 = baseStart.getY();
        double x2 = baseEnd.getX();
        double y2 = baseEnd.getY();
        double x3 = thirdPoint.getX();
        double y3 = thirdPoint.getY();

        // Base vector
        double dx = x2 - x1;
        double dy = y2 - y1;
        double baseLength = Math.sqrt(dx * dx + dy * dy);

        if (baseLength == 0) {
            addPoint(baseStart);
            return;
        }


        // Unit perpendicular vector (rotated 90° from base)
        double perpX = dy / baseLength;
        double perpY = -dx / baseLength;

        double vx = x3 - x1;
        double vy = y3 - y1;
        double distance = vx * perpX + vy * perpY;

        // Height offset vector
        double hx = perpX * distance;
        double hy = perpY * distance;

        // Calculate four corners
        Point corner1 = baseStart;
        Point corner2 = baseEnd;
        Point corner3 = new Point(
                (int) Math.round(x2 + hx),
                (int) Math.round(y2 + hy)
        );
        Point corner4 = new Point(
                (int) Math.round(x1 + hx),
                (int) Math.round(y1 + hy)
        );

        // Add points to polygon
        addPoint(corner1);
        addPoint(corner2);
        addPoint(corner3);
        addPoint(corner4);
    }

    public void finalizeRectangle() {
        close();
    }
}
