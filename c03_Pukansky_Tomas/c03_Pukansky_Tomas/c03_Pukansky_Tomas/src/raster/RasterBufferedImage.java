package raster;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Optional;
import java.util.OptionalInt;

public class RasterBufferedImage implements Raster {

    private final BufferedImage image;


    public RasterBufferedImage(int  width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);


    }


    @Override
    public void setPixel(int x, int y, int color) {
        // TODO: podmínka na vykreslení mimo raster
        if (x >= 0 && x < image.getWidth() && y >= 0 && y < image.getHeight()) {
            image.setRGB(x, y, color);
        }
    }

    @Override
    public int getPixel(int x, int y) {
        //todo: podmienka mimi raster OptinalInt empty
        // ak je mimo raster, môžeš vrátiť 0 alebo nejakú defaultnú farbu
        if (x < 0 || x >= image.getWidth() || y < 0 || y >= image.getHeight()) {
            return 0; // napr. čierna
        }

        return image.getRGB(x, y);
    }

    @Override
    public int getWidth() {
        return image.getWidth();
    }

    @Override
    public int getHeight() {
        return image.getHeight();
    }

    @Override
    public void clear() {
        Graphics g = image.getGraphics();
        g.clearRect(0, 0, image.getWidth(), image.getHeight());
    }

    public BufferedImage getImage() {
        return image;
    }
}
