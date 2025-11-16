package fill;

import model.Edge;
import model.Point;
import model.Polygon;
import raster.RasterBufferedImage;
import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;
import rasterize.Raster;

import java.util.ArrayList;

public class ScanLineFiller implements Filler {

    private Polygon polygon;
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;

    public ScanLineFiller(RasterBufferedImage raster, Polygon polygon, LineRasterizer lineRasterizer, PolygonRasterizer polygonRasterizer) {
        this.polygon = polygon;
        this.lineRasterizer = lineRasterizer;
        this.polygonRasterizer = polygonRasterizer;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void setPolygonRasterizer(PolygonRasterizer polygonRasterizer) {
        this.polygonRasterizer = polygonRasterizer;
    }

    @Override
    public void fill() {
        //todo: polygon musi mat aspon 3 pointy

        ArrayList<Edge> edges = new ArrayList<>();

        for (int i = 0; i < polygon.getSize(); i++){
            int indexA =i;
            int indexB = (i + 1) % polygon.getSize();

            Point pA = polygon.getPoint(indexA);
            Point pB = polygon.getPoint(indexB);

            Edge edge = new Edge(pA, pB);
            if (!edge.isHorizontal()){
                edge.orientate();
                edges.add(edge);
            }
        }
        //todo: najdem ymin a ymax
        int yMin = polygon.getPoint(0).getY();
        int yMax = 0;
        // todo: v cyklu prejdem pointy a hladam min a max
        // for cyklus od ymin do ymax
        for (int y = yMin; y <= yMax; y++){
            ArrayList<Integer> intersection = new ArrayList<>();
            for (Edge edge : edges) {
                //todo: existuje priesecnik? ak ano spocitam
                //todo: ulozim do zoznamu hran
                if (!edge.isIntersection(y)){
                    continue;
                }

                int x = edge.getIntersection(y);
                intersection.add(x);

            }
            // todo: zoznam priesecnikov ktore chcem spojit(musi byt parny pocet)
            //todo: zoradit priesecniky od min po max
            //todo: ofarbit pixely vzdy medzi oarnym a neparnym priesecnikom (0->1, 2->3,...)

        }
        //todo: obtiahnut hranicu polygonu - polyg rast
    }

}
