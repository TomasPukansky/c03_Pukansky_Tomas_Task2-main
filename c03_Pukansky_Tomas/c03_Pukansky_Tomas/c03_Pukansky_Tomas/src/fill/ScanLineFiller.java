package fill;

import model.Edge;
import model.Point;
import model.Polygon;
import raster.RasterBufferedImage;
import rasterize.LineRasterizer;
import rasterize.PolygonRasterizer;
import rasterize.Raster;

import java.util.ArrayList;
import java.util.Collections;

public class ScanLineFiller implements Filler {

    private Polygon polygon;
    private LineRasterizer lineRasterizer;
    private PolygonRasterizer polygonRasterizer;
    private RasterBufferedImage raster;
    private int fillColor = 0x0000ff;

    public ScanLineFiller(RasterBufferedImage raster, Polygon polygon, LineRasterizer lineRasterizer, PolygonRasterizer polygonRasterizer) {
        this.polygon = polygon;
        this.raster = raster;
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
        if (polygon == null || polygon.getSize() < 3) {
            return;
        }

        ArrayList<Edge> edges = new ArrayList<>();

        for (int i = 0; i < polygon.getSize(); i++) {
            int indexA = i;
            int indexB = (i + 1) % polygon.getSize();

            Point pA = polygon.getPoint(indexA);
            Point pB = polygon.getPoint(indexB);

            Edge edge = new Edge(pA, pB);
            if (!edge.isHorizontal()) {
                edge.orientate();
                edges.add(edge);
            }
        }

        if (edges.isEmpty()) {
            return;
        }

        //todo: najdem ymin a ymax
        int yMin = polygon.getPoint(0).getY();
        int yMax = polygon.getPoint(0).getY();
        // todo: v cyklu prejdem pointy a hladam min a max
        for (int i = 1; i < polygon.getSize(); i++) {
            int y = polygon.getPoint(i).getY();
            if (y < yMin) {
                yMin = y;
            }
            if (y > yMax) {
                yMax = y;
            }
        }

        // for cyklus od ymin do ymax
        for (int y = yMin; y <= yMax; y++) {
            ArrayList<Integer> intersections = new ArrayList<>();
            for (Edge edge : edges) {
                //todo: existuje priesecnik? ak ano spocitam
                //todo: ulozim do zoznamu hran
                if (!edge.isIntersection(y)) {
                    continue;
                }

                int x = edge.getIntersection(y);
                intersections.add(x);

            }

            // todo: zoznam priesecnikov ktore chcem spojit(musi byt parny pocet)
            if (intersections.size() < 2) {
                continue;
            }
            //todo: zoradit priesecniky od min po max
            Collections.sort(intersections);
            //todo: ofarbit pixely vzdy medzi oarnym a neparnym priesecnikom (0->1, 2->3,...)
            for (int i = 0; i < intersections.size() - 1; i += 2) {
                int xStart = intersections.get(i);
                int xEnd = intersections.get(i + 1);

                if (xStart > xEnd) {
                    int tmp = xStart;
                    xStart = xEnd;
                    xEnd = tmp;
                }

                for (int x = xStart; x <= xEnd; x++) {
                    raster.setPixel(x, y, fillColor);
                }
            }
        }
            //todo: obtiahnut hranicu polygonu - polyg rast
        if (polygonRasterizer != null) {
                polygonRasterizer.rasterize(polygon);
        }
    }
}

