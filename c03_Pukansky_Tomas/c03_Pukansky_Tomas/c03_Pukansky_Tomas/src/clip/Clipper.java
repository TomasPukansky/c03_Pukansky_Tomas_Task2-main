package clip;


import model.Point;

import java.util.ArrayList;
import java.util.List;

public class Clipper {
    public List<Point> clip(List<Point> clipperPoints, List<Point> pointsToClip) {
        List<Point> PointsToReturn = new ArrayList<Point>();

        // TODO: dodělat ořezání - slide 21

        // Poznámky:
        // - spočítat tečný vektor - slide 28
        // - spočítat normálu - slide 28
        // - spočítat, vektor k bodu, který testujeme - slide 28
        // - skalární součin (dot product) - prezentace lineaární algebra
        // - podle znaménka určit, kde je bod
        // - výsledkem skalárního součinu je úhel, podle znamenka určím, jestli je vlevo nebo vpravo
        // - pokud vyjde kladné, point je na stejné straně jako normála

        return PointsToReturn;
    }
}
