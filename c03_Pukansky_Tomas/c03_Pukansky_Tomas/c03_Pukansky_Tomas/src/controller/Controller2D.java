package controller;


import clip.Clipper;
import fill.*;
import model.*;
import rasterize.*;
import view.Panel;
import model.Polygon;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

//TODO: code-> reformat code
public class Controller2D {
    private final Panel panel;
    private LineRasterizer lineRasterizer;
    private Polygon polygon;
    int color = 0xff0000;
    private Point currentPoint; // v realnom case
    private Point firstPoint;
    private List<Line> lines;
    private boolean polygonMode = false;// polygon mod false= line mod, true polygon
    private List<Point> currentPolygonPoints = new ArrayList<>();// polygon v procese
    private List<Polygon> polygons;// komplet polygon
    private boolean shiftPressed = false;
    private Polygon polygonClipper;
    private Polygon currentPolygon;
    private PolygonRasterizer polygonRasterizer;

    private enum FillMode {
        NONE,
        SEED_FILL,
        SCANLINE_FILL
    }

    private FillMode fillMode = FillMode.NONE;
    private int fillColor = 0x00ff00;
    private boolean rectangleMode = false;
    private Rectangle currentRectangle;
    private int rectangleClickCount = 0;
    private Point rectBaseStart;
    private Point rectBaseEnd;
    private boolean useQueueFill = true; // true = Queue, false = Stack
    private boolean clipperMode = false;
    private Polygon clipperPolygon;
    private List<Point> clipperPoints = new ArrayList<>();

    public Controller2D(Panel panel) {
        this.panel = panel;
        //lineRasterizer = new LineRasterizerGraphics(panel.getRaster());
        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        lines = new ArrayList<>();


        currentPolygonPoints = new ArrayList<>();
        polygons = new ArrayList<>();

        polygonRasterizer = new PolygonRasterizer(lineRasterizer);

        initListeners();
    }

    private Point snapToAngle(Point start, Point target) {

        int dx = target.getX() - start.getX();
        int dy = target.getY() - start.getY();

        //star to target
        double distance = Math.sqrt(dx * dx + dy * dy);

        // uhol rad
        double angle = Math.atan2(dy, dx);

        // uhol na stupne
        double angleDegrees = Math.toDegrees(angle);

        // snap na nablizsi 45 uhol
        double snappedAngleDegrees = Math.round(angleDegrees / 45.0) * 45.0;

        // spat na rad
        double snappedAngle = Math.toRadians(snappedAngleDegrees);

        // novy endpoint s uhol
        int newX = start.getX() + (int) Math.round(distance * Math.cos(snappedAngle));
        int newY = start.getY() + (int) Math.round(distance * Math.sin(snappedAngle));

        return new Point(newX, newY);
    }


    private void initListeners() {
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

                int x = Math.max(0, Math.min(e.getX(), panel.getRaster().getWidth() - 1));
                int y = Math.max(0, Math.min(e.getY(), panel.getRaster().getHeight() - 1));

                if (clipperMode && e.getButton() == MouseEvent.BUTTON1) {
                    clipperPoints.add(new Point(x, y));
                    drawScene();
                    return;
                }

                // todo: na klik mouse button3 seedfill
                if (rectangleMode) {
                    if (rectangleClickCount == 0) {
                        // First click - base start
                        rectBaseStart = new Point(x, y);
                        rectangleClickCount = 1;
                    } else if (rectangleClickCount == 1) {
                        // Second click - base end, create rectangle preview
                        rectBaseEnd = new Point(x, y);
                        currentRectangle = new Rectangle(rectBaseStart, rectBaseEnd);
                        currentRectangle.setRasterizer(lineRasterizer);
                        rectangleClickCount = 2;
                    } else if (rectangleClickCount == 2) {
                        // Third click - finalize rectangle
                        currentRectangle.setHeightPoint(new Point(x, y));
                        currentRectangle.finalizeRectangle();
                        polygons.add(currentRectangle);

                        // Reset for next rectangle
                        currentRectangle = null;
                        rectangleClickCount = 0;
                        rectBaseStart = null;
                        rectBaseEnd = null;
                    }
                    drawScene();
                    return;
                }

                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (fillMode == FillMode.SEED_FILL) {
                        int boundaryColor = color;

                        Filler seedFiller;
                        if (useQueueFill) {
                            seedFiller = new SeedFillerQueue(panel.getRaster(), fillColor, x, y, boundaryColor);
                        } else {
                            seedFiller = new SeedFillerStack(panel.getRaster(), fillColor, x, y, boundaryColor);
                        }

                        seedFiller.fill();
                        fillMode = FillMode.NONE;
                        panel.repaint();
                        return;
                    }
                }

                if (e.getButton() == MouseEvent.BUTTON2) {
                    if (fillMode == FillMode.SEED_FILL) {
                        // Seed fill – obmedzenie farbou hranice aj pozadia
                        int boundaryColor = color;

                        SeedFiller seedFiller =
                                new SeedFiller(panel.getRaster(), fillColor, x, y, boundaryColor);

                        seedFiller.fill();

                        // po vyplnení mód vypnúť
                        fillMode = FillMode.NONE;
                        panel.repaint();
                        return;
                    }

                    if (fillMode == FillMode.SCANLINE_FILL) {
                        // ScanLine fill – vyplní všetky existujúce polygóny
                        for (Polygon poly : polygons) {
                            ScanLineFiller scanLineFiller =
                                    new ScanLineFiller(panel.getRaster(), poly, lineRasterizer, polygonRasterizer);
                            scanLineFiller.fill();
                        }

                        fillMode = FillMode.NONE;
                        panel.repaint();
                        return;
                    }


                }

                if (polygonMode) {
                    // POLYGON MODE
                    Point newPoint = new Point(e.getX(), e.getY());

                    // polygon snap
                    if (shiftPressed && !currentPolygonPoints.isEmpty()) {
                        Point lastPoint = currentPolygonPoints.get(currentPolygonPoints.size() - 1);
                        newPoint = snapToAngle(lastPoint, newPoint);
                    }

                    currentPolygonPoints.add(newPoint);
                    // objektový polygon – pri prvom kliku ho vytvoríme
                    if (currentPolygon == null) {
                        currentPolygon = new Polygon();
                        currentPolygon.setRasterizer(lineRasterizer);
                    }
                    currentPolygon.addPoint(newPoint);

                    drawScene();
                } else {
                    // LINE MODE
                    //prvy klik
                    if (firstPoint == null) {
                        firstPoint = new Point(e.getX(), e.getY());
                        return;
                    }
                    //druhy klik
                    Point secondPoint = new Point(x, y);
                    if (shiftPressed) {
                        secondPoint = snapToAngle(firstPoint, secondPoint);
                    }
                    Line line = new Line(firstPoint, new Point(e.getX(), e.getY()));
                    line.setRasterizer(lineRasterizer);
                    lines.add(line);
                    firstPoint = null;
                    currentPoint = null;

                    drawScene();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                // pohyb myskou RT
                if (polygonMode) {
                    // POLYGON MODE
                    if (!currentPolygonPoints.isEmpty()) {
                        currentPoint = new Point(e.getX(), e.getY());
                        // snap preview
                        if (shiftPressed) {
                            Point lastPoint = currentPolygonPoints.get(currentPolygonPoints.size() - 1);
                            currentPoint = snapToAngle(lastPoint, currentPoint);
                        }
                        drawScene();
                    }
                } else {
                    // LINE MODE
                    if (firstPoint != null) {
                        currentPoint = new Point(e.getX(), e.getY());
                        if (shiftPressed) {
                            currentPoint = snapToAngle(firstPoint, currentPoint);
                        }
                        drawScene();
                    }
                }
                if (rectangleMode && rectangleClickCount == 2 && currentRectangle != null) {
                    currentRectangle.setHeightPoint(new Point(e.getX(), e.getY()));
                    drawScene();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {

                if ((polygonMode && !currentPolygonPoints.isEmpty()) ||
                        (!polygonMode && firstPoint != null)) {
                    currentPoint = new Point(e.getX(), e.getY());
                    drawScene();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {


                if ((polygonMode && !currentPolygonPoints.isEmpty()) ||
                        (!polygonMode && firstPoint != null)) {
                    currentPoint = new Point(e.getX(), e.getY());

                    if (shiftPressed) {
                        Point lastPoint = currentPolygonPoints.get(currentPolygonPoints.size() - 1);
                        currentPoint = snapToAngle(lastPoint, currentPoint);
                    }

                    drawScene();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {

                if (polygonMode) {
                    if (!currentPolygonPoints.isEmpty()) {
                        currentPoint = new Point(e.getX(), e.getY());
                        drawScene();
                    }
                } else {
                    if (firstPoint != null) {
                        currentPoint = new Point(e.getX(), e.getY());

                        if (shiftPressed) {
                            currentPoint = snapToAngle(firstPoint, currentPoint);
                        }

                        drawScene();
                    }
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_O) {
                    clipperMode = !clipperMode;

                    if (clipperMode) {
                        System.out.println("Clipper mode: SELECT clipper polygon");
                        clipperPoints.clear();
                        clipperPolygon = null;
                        polygonMode = false;
                        currentPolygonPoints.clear();
                        currentPolygon = null;
                        currentPoint = null;
                    } else {
                        System.out.println("Clipper mode: OFF");
                    }
                    drawScene();
                }

                // U = provést ořezání
                if (e.getKeyCode() == KeyEvent.VK_U) {
                        // ← **PRIDANÉ: kontroly**
                        if (clipperPolygon == null) {
                            System.out.println("No clipper polygon defined! Use O->draw->ENTER first.");
                            return;
                        }


                        if (polygons.isEmpty()) {
                            System.out.println("No polygons to clip!");
                            return;
                        }

                        System.out.println("Starting clipping with clipper: " + clipperPolygon.getSize() + " points"); // ← **PRIDANÉ**

                        Clipper clipper = new Clipper();
                        List<Polygon> clippedPolygons = new ArrayList<>();

                        for (Polygon poly : polygons) {
                            if (poly == clipperPolygon) {
                                continue; // Neořezáváme sám sebe
                            }

                            List<Point> clipped = clipper.clip(
                                    clipperPolygon.getPoints(),
                                    poly.getPoints()
                            );

                            if (!clipped.isEmpty()) {
                                Polygon clippedPoly = new Polygon(clipped);
                                clippedPoly.setRasterizer(lineRasterizer);
                                clippedPoly.close();
                                clippedPolygons.add(clippedPoly);
                                System.out.println("Result: " + clipped.size() + " points"); // ← **PRIDANÉ**

                            }else {
                                System.out.println("Result: polygon completely clipped away"); // ← **PRIDANÉ**
                            }

                        }

                        polygons = clippedPolygons;
                        polygons.add(clipperPolygon); // Přidáme zpět clipper
                        drawScene();
                        System.out.println("Clipping done. Total polygons: " + polygons.size()); // ← **PRIDANÉ**

                }
                // M = prepínanie medzi Queue a Stack
                if (e.getKeyCode() == KeyEvent.VK_M) {
                    useQueueFill = !useQueueFill;
                    System.out.println("Seed Fill mode: " + (useQueueFill ? "QUEUE" : "STACK"));
                }

                //shift- snap mod
                if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
                    shiftPressed = true;
                    // snaped preview
                    if (currentPoint != null) {
                        drawScene();
                    }

                }
                // clear key press-C
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    lines.clear();
                    polygons.clear();
                    firstPoint = null;
                    currentPoint = null;
                    currentPolygonPoints.clear();
                    currentPolygon = null;
                    drawScene();
                }
                // zrusit momentalne kreslenu linku s ESC
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    if (polygonMode) {
                        currentPolygonPoints.clear();
                    } else {
                        firstPoint = null;
                    }
                    currentPoint = null;
                    drawScene();
                }

                if (e.getKeyCode() == KeyEvent.VK_1) {
                    lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
                    polygonRasterizer.setLineRasterizer(lineRasterizer);
                    drawScene();
                }


                if (e.getKeyCode() == KeyEvent.VK_2) {
                    lineRasterizer = new LineRasterizerColorTransition(panel.getRaster());
                    polygonRasterizer.setLineRasterizer(lineRasterizer);
                    drawScene();
                }
                // K = mód Seed Fill
                if (e.getKeyCode() == KeyEvent.VK_K) {
                    fillMode = FillMode.SEED_FILL;
                    System.out.println("Fill mode: SEED_FILL");
                }

                // L = mód ScanLine Fill
                if (e.getKeyCode() == KeyEvent.VK_L) {
                    fillMode = FillMode.SCANLINE_FILL;
                    System.out.println("Fill mode: SCANLINE_FILL");
                }

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // CLIPPER MODE
                    if (clipperMode && clipperPoints.size() >= 3) {
                        clipperPolygon = new Polygon(clipperPoints);
                        clipperPolygon.setRasterizer(lineRasterizer);
                        clipperPolygon.close();
                        polygons.add(clipperPolygon);
                        clipperMode = false;
                        clipperPoints.clear();
                        drawScene();
                        System.out.println("Clipper polygon created");
                        return;
                    }
                    // POLYGON MODE
                    if (polygonMode && currentPolygonPoints.size() >= 3) {
                        // DEBUG: Print how many points we have
                        System.out.println("Creating polygon with " + currentPolygonPoints.size() + " points");


//                        Polygon polygon = new Polygon(currentPolygonPoints);
//                        polygon.setRasterizer(lineRasterizer); // Save current rasterizer

                        if (currentPolygon == null) {
                            currentPolygon = new Polygon(currentPolygonPoints);
                            currentPolygon.setRasterizer(lineRasterizer);
                        }

                        // polygon uzavrieme
                        currentPolygon.close();
                        polygons.add(currentPolygon);

                        // DEBUG: Verify polygon was created
                        System.out.println("Polygon created with " + polygon.getSize() + " points");
                        System.out.println("Polygon has " + polygon.getEdges().size() + " edges");
                        // DEBUG: Check total polygons
                        System.out.println("Total polygons: " + polygons.size());

                        //testing
                        currentPolygonPoints = new ArrayList<>();
                        currentPoint = null;
                        currentPolygon = null;
                        drawScene();
                    } else if (polygonMode) {
                        System.out.println("Need at least 3 points. Current: " + currentPolygonPoints.size());
                    }
                }
                // P= menenie modu z poly na line a naopak
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    polygonMode = !polygonMode;

                    // vymazat vsetky aktualne body
                    firstPoint = null;
                    currentPoint = null;
                    currentPolygonPoints.clear();
                    currentPolygon = null;

                    drawScene();
                    System.out.println("Mode: " + (polygonMode ? "POLYGON" : "LINE"));
                }

                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    int centerX = panel.getRaster().getWidth() / 2;
                    int centerY = panel.getRaster().getHeight() / 2;

                    for (int x = centerX; x < panel.getRaster().getWidth(); x++) {
                        panel.getRaster().setPixel(x, centerY, color);

                        if (e.getKeyCode() == KeyEvent.VK_O) {
                            color = 0xff00ff;
                        }
                        if (e.getKeyCode() == KeyEvent.VK_P) {
                            color = 0xff0000;
                        }
                    }
                    panel.repaint();
                }
                if (e.getKeyCode() == KeyEvent.VK_R) {
                    rectangleMode = !rectangleMode;

                    // Reset rectangle state
                    currentRectangle = null;
                    rectangleClickCount = 0;
                    rectBaseStart = null;
                    rectBaseEnd = null;

                    // Exit other modes
                    polygonMode = false;
                    firstPoint = null;
                    currentPoint = null;
                    currentPolygonPoints.clear();
                    currentPolygon = null;

                    drawScene();
                    System.out.println("Mode: " + (rectangleMode ? "RECTANGLE" : "LINE"));
                }

            }
        });
    }

    private void drawScene() {
        panel.getRaster().clear();
        // Preview clipper polyg
        if (clipperMode && !clipperPoints.isEmpty()) {
            for (int i = 0; i < clipperPoints.size() - 1; i++) {
                Point p1 = clipperPoints.get(i);
                Point p2 = clipperPoints.get(i + 1);
                lineRasterizer.rasterize(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

            // Rectangle preview
            if (rectangleMode && currentRectangle != null) {
                polygonRasterizer.rasterize(currentRectangle);
            }
            // miesto pouzivania rasterizeru prevsetky lines
            // kazda linka pouziva svoj vlastny rasterizer ktory sa uklada ked sa vytvori

            for (Line line : lines) {

                if (line.getRasterizer() != null) {
                    line.getRasterizer().rasterize(line);
                } else {
                    // Fallback: ak ziaden rasterizer nebol nastaven, pouzijeme momentalny
                    lineRasterizer.rasterize(line);
                }
            }

            // Draw all completed polygons/debug
            System.out.println("Drawing " + polygons.size() + " polygons"); // DEBUG
            for (Polygon polygon : polygons) {

//            List<Line> edges = polygon.getEdges();
//
//            System.out.println("  Polygon has " + polygon.getSize() + " points and " + edges.size() + " edges"); // DEBUG
//
//            // kresli edge s pridelenim rasterizerom
//            for (Line edge : edges) {
//                if (edge.getRasterizer() != null) {
//                    edge.getRasterizer().rasterize(edge);
//                } else {
//                    lineRasterizer.rasterize(edge);
//                }
//            }
                if (polygonRasterizer != null) {
                    polygonRasterizer.rasterize(polygon);
                }
            }

            if (polygonMode) {
                // POLYGON MODE
                System.out.println("Drawing preview with " + currentPolygonPoints.size() + " points"); // DEBUG

                // linky medzi existujicimi bodmi
                for (int i = 0; i < currentPolygonPoints.size() - 1; i++) {
                    Point p1 = currentPolygonPoints.get(i);
                    Point p2 = currentPolygonPoints.get(i + 1);
                    lineRasterizer.rasterize(p1.getX(), p1.getY(), p2.getX(), p2.getY());
                }

                // last point na kurzor
                if (!currentPolygonPoints.isEmpty() && currentPoint != null) {
                    Point lastPoint = currentPolygonPoints.get(currentPolygonPoints.size() - 1);
                    lineRasterizer.rasterize(
                            lastPoint.getX(), lastPoint.getY(),
                            currentPoint.getX(), currentPoint.getY()
                    );

                    // od kurzoru k prvemu bodu
                    if (currentPolygonPoints.size() >= 2) {
                        Point firstPolygonPoint = currentPolygonPoints.get(0);
                        lineRasterizer.rasterize(
                                currentPoint.getX(), currentPoint.getY(),
                                firstPolygonPoint.getX(), firstPolygonPoint.getY()
                        );
                    }
                }
            } else {
                // LINE MODE
                if (firstPoint != null && currentPoint != null) {
                    lineRasterizer.rasterize(
                            firstPoint.getX(), firstPoint.getY(),
                            currentPoint.getX(), currentPoint.getY()
                    );
                }
            }


            panel.repaint();
        }


    }

