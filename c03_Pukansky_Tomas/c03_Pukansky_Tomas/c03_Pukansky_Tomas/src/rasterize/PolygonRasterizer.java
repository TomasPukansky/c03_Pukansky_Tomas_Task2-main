package rasterize;

import model.Polygon;
import model.Point;

public class PolygonRasterizer {


    private LineRasterizer  lineRasterizer;
    public PolygonRasterizer(LineRasterizer lineRasterizer){
        this.lineRasterizer = lineRasterizer;
    };

    public void rasterize(Polygon polygon){
       //kontrola ci mame aspon 3 pointy, ak na 2 pointy osetrit ze usecku nevykresli dvakrat

       if (polygon.getSize() < 3) {
            return; // Not enough points for a polygon
        }

        for (int i = 0; i < polygon.getSize(); i++){
            int indexA =i;
            int indexB = (i + 1) % polygon.getSize();


            // If indexB se rovná polygon.getSize
            // pokud ano, tak indexB = 0

            Point pA = polygon.getPoint(indexA);
            Point pB = polygon.getPoint(indexB);

            lineRasterizer.rasterize(
                    pA.getX(), pA.getY(),
                    pB.getX(), pB.getY()
            );


                //lineRasterizer.rasterize

                // TODO: dodělat
            }
        }





    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }
}
