package clip;


import model.Point;

import java.util.ArrayList;
import java.util.List;

public class Clipper {
    public List<Point> clip(List<Point> clipperPoints, List<Point> pointsToClip) {
        if (clipperPoints.size() < 3 || pointsToClip.isEmpty()) {
            return new ArrayList<>();
        }

        List<Point> output = new ArrayList<>(pointsToClip);

        // Prejdeme každou hranou ořezávacího polygonu
        for (int i = 0; i < clipperPoints.size(); i++) {
            if (output.isEmpty()) {
                break;
            }

            Point edgeStart = clipperPoints.get(i);
            Point edgeEnd = clipperPoints.get((i + 1) % clipperPoints.size());

            List<Point> input = output;
            output = new ArrayList<>();

            // Spočítame tečný vektor hrany (dx, dy)
            double dx = edgeEnd.getX() - edgeStart.getX();
            double dy = edgeEnd.getY() - edgeStart.getY();

            // normala – rotacia 90° doleva
            double nx = -dy;
            double ny = dx;
            // normalizacia
            double len = Math.sqrt(nx * nx + ny * ny);
            if (len == 0) {
                continue;
            }
            nx /= len;
            ny /= len;

            // orezavanie
            for (int j = 0; j < input.size(); j++) {
                Point current = input.get(j);
                Point next = input.get((j + 1) % input.size());

                // Vektor od zaciatku hrany k aktualnemu bodu
                double vx1 = current.getX() - edgeStart.getX();
                double vy1 = current.getY() - edgeStart.getY();

                // Vektor od zaciatku hrany k dalsiemu bodu
                double vx2 = next.getX() - edgeStart.getX();
                double vy2 = next.getY() - edgeStart.getY();

                // Skalární součin s normálou
                double dot1 = vx1 * nx + vy1 * ny;
                double dot2 = vx2 * nx + vy2 * ny;

                boolean currentInside = dot1 >= 0; // kladné = in
                boolean nextInside = dot2 >= 0;

                if (currentInside && nextInside) {
                    // Oba body uvnitř – přidáme next
                    output.add(next);
                } else if (currentInside && !nextInside) {
                    // current uvnitř, next venku – pridame priesecnik
                    Point intersection = getIntersection(
                            current, next,
                            edgeStart, edgeEnd
                    );
                    if (intersection != null) {
                        output.add(intersection);
                    }
                } else if (!currentInside && nextInside) {
                    // current venku, next uvnitř – přidáme preisecnika next
                    Point intersection = getIntersection(
                            current, next,
                            edgeStart, edgeEnd
                    );
                    if (intersection != null) {
                        output.add(intersection);
                    }
                    output.add(next);
                }
                // ak su oba body vonku, nic nepridavame
            }
        }

        return output;
    }

    private Point getIntersection(Point p1, Point p2, Point q1, Point q2) {
        double x1 = p1.getX(), y1 = p1.getY();
        double x2 = p2.getX(), y2 = p2.getY();
        double x3 = q1.getX(), y3 = q1.getY();
        double x4 = q2.getX(), y4 = q2.getY();

        double denom = (x1 - x2) * (y3 - y4) - (y1 - y2) * (x3 - x4);

        if (Math.abs(denom) < 1e-10) {
            return null; // Rovnoběžné nebo totožné úsečky
        }

        double t = ((x1 - x3) * (y3 - y4) - (y1 - y3) * (x3 - x4)) / denom;

        int ix = (int) Math.round(x1 + t * (x2 - x1));
        int iy = (int) Math.round(y1 + t * (y2 - y1));

        return new Point(ix, iy);
        // TODO: dodělat ořezání - slide 21

        // Poznámky:
        // - spočítat tečný vektor - slide 28
        // - spočítat normálu - slide 28
        // - spočítat, vektor k bodu, který testujeme - slide 28
        // - skalární součin (dot product) - prezentace lineaární algebra
        // - podle znaménka určit, kde je bod
        // - výsledkem skalárního součinu je úhel, podle znamenka určím, jestli je vlevo nebo vpravo
        // - pokud vyjde kladné, point je na stejné straně jako normála
    }
}
