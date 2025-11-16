package rasterize;

import raster.RasterBufferedImage;

import java.awt.*;

public class LineRasterizerTrivial extends LineRasterizer {
    public LineRasterizerTrivial(RasterBufferedImage raster) {
        super(raster);

    }
    //Trivialny algoritmus
    //Používa rovnicu y = k*x + q (alebo x = (y-q)/k pre strmé línie)
    // Jednoduchý, ale pomalší algoritmus – vypočíta y (alebo x) pre každý pixel
    // Možnosť generovať reálne súradnice
    // Používa desatinné (float) čísla
    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        //TODO: pozor na delenie nulou



        //zvysla usecka delenie nulou
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1, y, 0xff0000);
            }
            return;
        }

        // degenerovana usecka start sa rovna koncu
        if (x1 == x2 && y1 == y2) {
            raster.setPixel(x1, y1, 0xff0000);
            return;
        }

        float k = (y2 - y1) / (float) (x2 - x1);
        float q = y1 - k * x1;



        //TODO: napisat if x1>x2
        //
        if (Math.abs(k) <= 1) {
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;

                temp = y1;
                y1 = y2;
                y2 = temp;
            }

            for (int x = x1; x <= x2; x++) {
                int y = Math.round(k * x + q);
                raster.setPixel(x, y, 0xff0000);
            }
        } else {
            // Iterate over y axis for steep lines
            if (y1 > y2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;

                temp = y1;
                y1 = y2;
                y2 = temp;
            }

            // x = (y - q) / k
            for (int y = y1; y <= y2; y++) {
                int x = Math.round((y - q) / k);
                raster.setPixel(x, y, 0xff0000);
            }
        }


    }
}
