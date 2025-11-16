package rasterize;

import raster.RasterBufferedImage;
import model.Point;
import model.Line;


public abstract class LineRasterizer {
   protected RasterBufferedImage raster;
    public LineRasterizer(RasterBufferedImage raster) {
        this.raster = raster;
        
    }
    public void rasterize(int x1, int y1, int x2, int y2) {

        
    }

    public void rasterize(Point p1, Point p2){

    }

    public void rasterize(Line line) {
        rasterize(line.getX1(),line.getY1(), line.getX2(),line.getY2());
    }

}
