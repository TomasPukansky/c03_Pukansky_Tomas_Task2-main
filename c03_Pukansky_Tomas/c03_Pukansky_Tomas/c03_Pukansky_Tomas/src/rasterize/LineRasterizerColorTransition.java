package rasterize;

import raster.RasterBufferedImage;

import java.awt.*;

public class LineRasterizerColorTransition extends LineRasterizer {

    public LineRasterizerColorTransition(RasterBufferedImage raster) {
        super(raster);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        // TODO: pozor na dělení nulou

        if (x1 == x2 && y1 == y2) {
            raster.setPixel(x1, y1, Color.RED.getRGB());
            return;
        }
        Color c1 = Color.RED;
        Color c2 = Color.GREEN;

        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            int totalYRange = Math.abs(y2 - y1);
            if (totalYRange == 0) totalYRange = 1;

            for (int y = startY; y <= endY; y++) {
                float t = (y - startY) / (float) totalYRange;
                int color = interpolateColor(c1, c2, t);
                raster.setPixel(x1, y, color);
            }
            return;
        }


        float k = (y2 - y1) / (float) (x2 - x1);
        float q = y1 - k * x1;

        int originalX1 = x1;
        int originalY1 = y1;
        int totalXRange = Math.abs(x2 - x1);
        int totalYRange = Math.abs(y2 - y1);
        //safety check
        if (totalXRange == 0) {
            totalXRange = 1;
        }
        if (totalYRange == 0) {
            totalYRange = 1;
        }
//        float[] colorComponentsC1 = c1.getColorComponents(null);
//        float[] colorComponentsC2 = c2.getColorComponents(null);

        // TODO: x1 může být větší než x2
        if (Math.abs(k) <= 1) {
            // Iterate over x axis
            boolean swapped = false;
            if (x1 > x2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;

                temp = y1;
                y1 = y2;
                y2 = temp;
                swapped = true;
            }


            for (int x = x1; x <= x2; x++) {
                // Calculate interpolation parameter t based on original x coordinates
                float t;
                if (swapped) {
                    t = (originalX1 - x) / (float) totalXRange;
                } else {
                    t = (x - originalX1) / (float) totalXRange;
                }

                // Interpolate color
                int color = interpolateColor(c1, c2, t);

                int y = Math.round(k * x + q);
                raster.setPixel(x, y, color);
            }
        } else {
            boolean swapped = false;
            if (y1 > y2) {
                int temp = x1;
                x1 = x2;
                x2 = temp;

                temp = y1;
                y1 = y2;
                y2 = temp;
                swapped = true;
            }


            for (int y = y1; y <= y2; y++) {
                int x = Math.round((y - q) / k);

                // Calculate interpolation parameter t based on y coordinates
                float t;
                if (swapped) {
                    t = (originalY1 - y) / (float) totalYRange;
                } else {
                    t = (y - originalY1) / (float) totalYRange;
                }

                // Interpolate color
                int color = interpolateColor(c1, c2, t);

                raster.setPixel(x, y, color);
            }

        }
    }

    private int interpolateColor(Color c1, Color c2, float t) {
        //maxnutie t na 0 az 1 aby nebol float point error
        t = Math.max(0.0f, Math.min(1.0f, t));
        // Get RGB [0.0 - 1.0]
        float[] components1 = c1.getColorComponents(null);
        float[] components2 = c2.getColorComponents(null);

        // Interpolate kazdeko componentu: newColor = c1 + t * (c2 - c1)
        float[] newColors = new float[3];
        for (int i = 0; i < 3; i++) {
            newColors[i] = components1[i] + t * (components2[i] - components1[i]);
            newColors[i] = Math.max(0.0f, Math.min(1.0f, newColors[i]));
        }


        // spät na RGB integer
        Color interpolatedColor = new Color(newColors[0], newColors[1], newColors[2]);
        return interpolatedColor.getRGB();
    }
}
