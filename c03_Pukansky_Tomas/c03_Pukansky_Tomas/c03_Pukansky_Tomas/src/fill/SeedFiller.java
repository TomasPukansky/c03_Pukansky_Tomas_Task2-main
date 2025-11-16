package fill;

import raster.RasterBufferedImage;

import java.util.OptionalInt;


public class SeedFiller implements Filler {
    //private Raster raster;
    private int fillColor;
    private int backgroundColor;
    private int startX, startY;
    private int boundaryColor;
    private RasterBufferedImage raster;

  public SeedFiller(RasterBufferedImage raster, int fillColor, int startX, int startY, int boundaryColor) {

      this.raster = raster;
      this.startX = startX;
      this.startY = startY;
      this.fillColor = fillColor;
      this.backgroundColor = raster.getPixel(startX,startY);
      this.boundaryColor = boundaryColor;
  }

    @Override
    public void fill() {
      seedFill(startX, startY);
    }

    private void seedFill(int x, int y){

        if (x < 0 || x >= raster.getWidth() || y < 0 || y >= raster.getHeight()) {
            return;
        }


        //todo: nacitam farbu pixelu, na ktory som kikol(starX, startY)
        int currentColor = raster.getPixel(x, y);

        // todo: podmienka, ci mam alebo nemam ofarbit
        // ak nie koncim

        if (currentColor == boundaryColor ||
                currentColor  == fillColor ||
                currentColor != backgroundColor) {
            return;
        }

        // todo: ofarbim
        raster.setPixel(x, y, fillColor);
        // todo: zavolam seedfill pre susedov
        seedFill(x + 1, y); // doprava
        seedFill(x - 1, y); // doľava
        seedFill(x, y + 1); // dole
        seedFill(x, y - 1); //hore

    }
}
