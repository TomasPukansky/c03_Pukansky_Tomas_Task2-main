package fill;

import raster.RasterBufferedImage;
import model.Point;

import java.util.Stack;

public class SeedFillerStack implements Filler {
    private RasterBufferedImage raster;
    private int fillColor;
    private int backgroundColor;
    private int startX, startY;
    private int boundaryColor;

    public SeedFillerStack(RasterBufferedImage raster, int fillColor, int startX, int startY, int boundaryColor) {
        this.raster = raster;
        this.startX = startX;
        this.startY = startY;
        this.fillColor = fillColor;
        this.backgroundColor = raster.getPixel(startX, startY);
        this.boundaryColor = boundaryColor;
    }

    @Override
    public void fill() {
        if (startX < 0 || startX >= raster.getWidth() ||
                startY < 0 || startY >= raster.getHeight()) {
            return;
        }

        if (backgroundColor == boundaryColor || backgroundColor == fillColor) {
            return;
        }

        Stack<Point> stack = new Stack<>();
        stack.push(new Point(startX, startY));

        while (!stack.isEmpty()) {
            Point p = stack.pop();
            int x = p.getX();
            int y = p.getY();

            if (x < 0 || x >= raster.getWidth() ||
                    y < 0 || y >= raster.getHeight()) {
                continue;
            }

            int currentColor = raster.getPixel(x, y);

            if (currentColor == boundaryColor ||
                    currentColor == fillColor ||
                    currentColor != backgroundColor) {
                continue;
            }

            raster.setPixel(x, y, fillColor);

            stack.push(new Point(x + 1, y));
            stack.push(new Point(x - 1, y));
            stack.push(new Point(x, y + 1));
            stack.push(new Point(x, y - 1));
        }
    }
}