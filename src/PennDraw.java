//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D.Double;
import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.TreeSet;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public final class PennDraw implements ActionListener, MouseListener, MouseMotionListener, KeyListener {
    public static final long VERSION = 2016011311L;
    public static final Color BLACK;
    public static final Color BLUE;
    public static final Color CYAN;
    public static final Color DARK_GRAY;
    public static final Color GRAY;
    public static final Color GREEN;
    public static final Color LIGHT_GRAY;
    public static final Color MAGENTA;
    public static final Color ORANGE;
    public static final Color PINK;
    public static final Color RED;
    public static final Color WHITE;
    public static final Color YELLOW;
    public static final Color BOOK_BLUE;
    public static final Color BOOK_LIGHT_BLUE;
    public static final Color BOOK_RED;
    private static final Color DEFAULT_PEN_COLOR;
    private static final Color DEFAULT_CLEAR_COLOR;
    private static Color penColor;
    private static final int DEFAULT_SIZE = 512;
    private static int width;
    private static int height;
    private static final double DEFAULT_PEN_RADIUS = 0.002D;
    private static double penRadius;
    private static boolean defer;
    private static long nextDraw;
    private static int animationSpeed;
    private static final double BORDER = 0.0D;
    private static final double DEFAULT_XMIN = 0.0D;
    private static final double DEFAULT_XMAX = 1.0D;
    private static final double DEFAULT_YMIN = 0.0D;
    private static final double DEFAULT_YMAX = 1.0D;
    private static double xmin;
    private static double ymin;
    private static double xmax;
    private static double ymax;
    private static double xscale;
    private static double yscale;
    private static Object mouseLock;
    private static Object keyLock;
    private static final Font DEFAULT_FONT;
    private static Font font;
    private static BufferedImage offscreenImage;
    private static BufferedImage onscreenImage;
    private static Graphics2D offscreen;
    private static Graphics2D onscreen;
    private static PennDraw std;
    private static JFrame frame;
    private static boolean mousePressed;
    private static double mouseX;
    private static double mouseY;
    private static LinkedList<Character> keysTyped;
    private static TreeSet<Integer> keysDown;

    private PennDraw() {
    }

    public static void setCanvasSize() {
        setCanvasSize(512, 512);
    }

    public static void setCanvasSize(int w, int h) {
        if (w >= 1 && h >= 1) {
            width = w;
            height = h;
            init();
        } else {
            throw new IllegalArgumentException("width and height must be positive");
        }
    }

    private static void init() {
        if (frame != null) {
            frame.setVisible(false);
        }

        frame = new JFrame();
        offscreenImage = new BufferedImage(width, height, 2);
        onscreenImage = new BufferedImage(width, height, 2);
        offscreen = offscreenImage.createGraphics();
        onscreen = onscreenImage.createGraphics();
        setXscale();
        setYscale();
        offscreen.setColor(DEFAULT_CLEAR_COLOR);
        offscreen.fillRect(0, 0, width, height);
        setPenColor();
        setPenRadius();
        setFont();
        clear();
        RenderingHints hints = new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        offscreen.addRenderingHints(hints);
        ImageIcon icon = new ImageIcon(onscreenImage);
        JLabel draw = new JLabel(icon);
        draw.addMouseListener(std);
        draw.addMouseMotionListener(std);
        frame.setContentPane(draw);
        frame.addKeyListener(std);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(3);
        frame.setTitle("Standard Draw");
        frame.setJMenuBar(createMenuBar());
        frame.pack();
        frame.requestFocusInWindow();
        frame.setVisible(true);
    }

    private static JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu = new JMenu("File");
        menuBar.add(menu);
        JMenuItem menuItem1 = new JMenuItem(" Save...   ");
        menuItem1.addActionListener(std);
        menuItem1.setAccelerator(KeyStroke.getKeyStroke(83, Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()));
        menu.add(menuItem1);
        return menuBar;
    }

    public static void setXscale() {
        setXscale(0.0D, 1.0D);
    }

    public static void setYscale() {
        setYscale(0.0D, 1.0D);
    }

    public static void setXscale(double min, double max) {
        double size = max - min;
        synchronized(mouseLock) {
            xmin = min - 0.0D * size;
            xmax = max + 0.0D * size;
            setTransform();
        }
    }

    public static void setYscale(double min, double max) {
        double size = max - min;
        synchronized(mouseLock) {
            ymin = min - 0.0D * size;
            ymax = max + 0.0D * size;
            setTransform();
        }
    }

    public static void setScale(double min, double max) {
        double size = max - min;
        synchronized(mouseLock) {
            xmin = min - 0.0D * size;
            xmax = max + 0.0D * size;
            ymin = min - 0.0D * size;
            ymax = max + 0.0D * size;
            setTransform();
        }
    }

    private static void setTransform() {
        xscale = (double)width / (xmax - xmin);
        yscale = (double)height / (ymax - ymin);
    }

    private static double scaleX(double x) {
        return xscale * (x - xmin);
    }

    private static double scaleY(double y) {
        return yscale * (ymax - y);
    }

    private static double factorX(double w) {
        return w * (double)width / Math.abs(xmax - xmin);
    }

    private static double factorY(double h) {
        return h * (double)height / Math.abs(ymax - ymin);
    }

    private static double userX(double x) {
        return xmin + x / xscale;
    }

    private static double userY(double y) {
        return ymax - y / yscale;
    }

    public static void clear() {
        clear(DEFAULT_CLEAR_COLOR);
    }

    public static void clear(Color color) {
        offscreen.setColor(color);
        filledRectangle(0.5D * (xmax + xmin), 0.5D * (ymax + ymin), 0.5D * (xmax - xmin), 0.5D * (ymax - ymin));
        offscreen.setColor(penColor);
        draw();
    }

    public static void clear(int red, int green, int blue) {
        if (red >= 0 && red < 256) {
            if (green >= 0 && green < 256) {
                if (blue >= 0 && blue < 256) {
                    clear(new Color(red, green, blue));
                } else {
                    throw new IllegalArgumentException("amount of blue must be between 0 and 255");
                }
            } else {
                throw new IllegalArgumentException("amount of green must be between 0 and 255");
            }
        } else {
            throw new IllegalArgumentException("amount of red must be between 0 and 255");
        }
    }

    public static void clear(int red, int green, int blue, int alpha) {
        if (red >= 0 && red < 256) {
            if (green >= 0 && green < 256) {
                if (blue >= 0 && blue < 256) {
                    if (alpha >= 0 && alpha < 256) {
                        clear(new Color(red, green, blue, alpha));
                    } else {
                        throw new IllegalArgumentException("amount of alpha must be between 0 and 255");
                    }
                } else {
                    throw new IllegalArgumentException("amount of blue must be between 0 and 255");
                }
            } else {
                throw new IllegalArgumentException("amount of green must be between 0 and 255");
            }
        } else {
            throw new IllegalArgumentException("amount of red must be between 0 and 255");
        }
    }

    public static double getPenRadius() {
        return penRadius;
    }

    public static void setPenRadius() {
        setPenRadius(0.002D);
    }

    public static void setPenRadius(double r) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("pen radius must be nonnegative");
        } else {
            penRadius = r;
            float scaledPenRadius = (float)(r * 512.0D);
            BasicStroke stroke = new BasicStroke(scaledPenRadius, 1, 1);
            offscreen.setStroke(stroke);
        }
    }

    public static void setPenWidthInPixels(double w) {
        if (w < 0.0D) {
            throw new IllegalArgumentException("pen radius must be nonnegative");
        } else {
            setPenRadius(w / (double)(2 * width));
        }
    }

    public static void setPenWidthInPoints(double w) {
        if (w < 0.0D) {
            throw new IllegalArgumentException("pen radius must be nonnegative");
        } else {
            int dpi = frame.getToolkit().getScreenResolution();
            setPenRadius((double)dpi * w / (double)(144 * width));
        }
    }

    public static Color getPenColor() {
        return penColor;
    }

    public static void setPenColor() {
        setPenColor(DEFAULT_PEN_COLOR);
    }

    public static void setPenColor(Color color) {
        penColor = color;
        offscreen.setColor(penColor);
    }

    public static void setPenColor(int red, int green, int blue) {
        if (red >= 0 && red < 256) {
            if (green >= 0 && green < 256) {
                if (blue >= 0 && blue < 256) {
                    setPenColor(new Color(red, green, blue));
                } else {
                    throw new IllegalArgumentException("amount of blue must be between 0 and 255");
                }
            } else {
                throw new IllegalArgumentException("amount of green must be between 0 and 255");
            }
        } else {
            throw new IllegalArgumentException("amount of red must be between 0 and 255");
        }
    }

    public static void setPenColor(int red, int green, int blue, int alpha) {
        if (red >= 0 && red < 256) {
            if (green >= 0 && green < 256) {
                if (blue >= 0 && blue < 256) {
                    if (alpha >= 0 && alpha < 256) {
                        setPenColor(new Color(red, green, blue, alpha));
                    } else {
                        throw new IllegalArgumentException("amount of alpha must be between 0 and 255");
                    }
                } else {
                    throw new IllegalArgumentException("amount of blue must be between 0 and 255");
                }
            } else {
                throw new IllegalArgumentException("amount of green must be between 0 and 255");
            }
        } else {
            throw new IllegalArgumentException("amount of red must be between 0 and 255");
        }
    }

    public static Font getFont() {
        return font;
    }

    public static void listFonts() {
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
        String[] var1 = fonts;
        int var2 = fonts.length;

        for(int var3 = 0; var3 < var2; ++var3) {
            String s = var1[var3];
            System.out.println(s);
        }

    }

    public static void setFont() {
        setFont(DEFAULT_FONT);
    }

    public static void setFont(Font f) {
        font = f;
        offscreen.setFont(f);
    }

    public static void setFont(String fontName) {
        setFont(new Font(fontName, font.getStyle(), font.getSize()));
    }

    public static void setFont(String fontName, double pointSize) {
        setFont(fontName);
        setFont(font.deriveFont((float)pointSize));
    }

    public static void setFontSize(double pointSize) {
        setFont(font.deriveFont((float)pointSize));
    }

    public static void setFontSizeInPixels(double pixelHeight) {
        int dpi = frame.getToolkit().getScreenResolution();
        double pointSize = pixelHeight * (double)dpi / 72.0D;
        System.out.println(dpi);
        System.out.println(pointSize);
        setFont(font.deriveFont((float)pointSize));
    }

    public static void setFontPlain() {
        setFont(font.deriveFont(0));
    }

    public static void setFontBold() {
        setFont(font.deriveFont(1));
    }

    public static void setFontItalic() {
        setFont(font.deriveFont(2));
    }

    public static void setFontBoldItalic() {
        setFont(font.deriveFont(3));
    }

    public static void line(double x0, double y0, double x1, double y1) {
        offscreen.draw(new Double(scaleX(x0), scaleY(y0), scaleX(x1), scaleY(y1)));
        draw();
    }

    private static void pixel(double x, double y) {
        offscreen.fillRect((int)Math.round(scaleX(x)), (int)Math.round(scaleY(y)), 1, 1);
    }

    public static void point(double x, double y) {
        double r = penRadius;
        float scaledPenRadius = (float)(r * 512.0D);
        if (scaledPenRadius <= 1.0F) {
            pixel(x, y);
        } else {
            offscreen.fill(new java.awt.geom.Ellipse2D.Double(scaleX(x) - (double)(scaledPenRadius / 2.0F), scaleY(y) - (double)(scaledPenRadius / 2.0F), (double)scaledPenRadius, (double)scaledPenRadius));
        }

        draw();
    }

    public static void circle(double x, double y, double r) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("circle radius must be nonnegative");
        } else {
            ellipse(x, y, r, r);
        }
    }

    public static void filledCircle(double x, double y, double r) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("circle radius must be nonnegative");
        } else {
            filledEllipse(x, y, r, r);
        }
    }

    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        ellipse(x, y, semiMajorAxis, semiMinorAxis, false);
    }

    public static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis, double degrees) {
        AffineTransform t = offscreen.getTransform();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        ellipse(x, y, semiMajorAxis, semiMinorAxis);
        offscreen.setTransform(t);
    }

    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis) {
        ellipse(x, y, semiMajorAxis, semiMinorAxis, true);
    }

    public static void filledEllipse(double x, double y, double semiMajorAxis, double semiMinorAxis, double degrees) {
        AffineTransform t = (AffineTransform)offscreen.getTransform().clone();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        filledEllipse(x, y, semiMajorAxis, semiMinorAxis);
        offscreen.setTransform(t);
    }

    private static void ellipse(double x, double y, double semiMajorAxis, double semiMinorAxis, boolean filled) {
        if (semiMajorAxis < 0.0D) {
            throw new IllegalArgumentException("ellipse semimajor axis must be nonnegative");
        } else if (semiMinorAxis < 0.0D) {
            throw new IllegalArgumentException("ellipse semiminor axis must be nonnegative");
        } else {
            double xs = scaleX(x);
            double ys = scaleY(y);
            double ws = factorX(2.0D * semiMajorAxis);
            double hs = factorY(2.0D * semiMinorAxis);
            if (ws <= 1.0D && hs <= 1.0D) {
                pixel(x, y);
            } else if (filled) {
                offscreen.fill(new java.awt.geom.Ellipse2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs));
            } else {
                offscreen.draw(new java.awt.geom.Ellipse2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs));
            }

            draw();
        }
    }

    public static void arc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, 0, false);
    }

    public static void closedArc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, 1, false);
    }

    public static void pie(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, 2, false);
    }

    public static void filledPie(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, 2, true);
    }

    public static void filledArc(double x, double y, double r, double angle1, double angle2) {
        arc(x, y, r, angle1, angle2, 1, true);
    }

    private static void arc(double x, double y, double r, double angle1, double angle2, int pathType, boolean fill) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("arc radius must be nonnegative");
        } else {
            while(angle2 < angle1) {
                angle2 += 360.0D;
            }

            double xs = scaleX(x);
            double ys = scaleY(y);
            double ws = factorX(2.0D * r);
            double hs = factorY(2.0D * r);
            if (ws <= 1.0D && hs <= 1.0D) {
                pixel(x, y);
            } else if (fill) {
                offscreen.fill(new java.awt.geom.Arc2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs, angle1, angle2 - angle1, pathType));
            } else {
                offscreen.draw(new java.awt.geom.Arc2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs, angle1, angle2 - angle1, pathType));
            }

            draw();
        }
    }

    public static void square(double x, double y, double r) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("square side length must be nonnegative");
        } else {
            rectangle(x, y, r, r);
        }
    }

    public static void square(double x, double y, double r, double degrees) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("square side length must be nonnegative");
        } else {
            rectangle(x, y, r, r, degrees);
        }
    }

    public static void filledSquare(double x, double y, double r) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("square side length must be nonnegative");
        } else {
            filledRectangle(x, y, r, r);
        }
    }

    public static void filledSquare(double x, double y, double r, double degrees) {
        if (r < 0.0D) {
            throw new IllegalArgumentException("square side length must be nonnegative");
        } else {
            filledRectangle(x, y, r, r, degrees);
        }
    }

    public static void rectangle(double x, double y, double halfWidth, double halfHeight) {
        rectangle(x, y, halfWidth, halfHeight, false);
    }

    public static void rectangle(double x, double y, double halfWidth, double halfHeight, double degrees) {
        AffineTransform t = (AffineTransform)offscreen.getTransform().clone();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        rectangle(x, y, halfWidth, halfHeight);
        offscreen.setTransform(t);
    }

    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight) {
        rectangle(x, y, halfWidth, halfHeight, true);
    }

    public static void filledRectangle(double x, double y, double halfWidth, double halfHeight, double degrees) {
        AffineTransform t = offscreen.getTransform();
        offscreen.rotate(Math.toRadians(-degrees), scaleX(x), scaleY(y));
        filledRectangle(x, y, halfWidth, halfHeight);
        offscreen.setTransform(t);
    }

    private static void rectangle(double x, double y, double halfWidth, double halfHeight, boolean filled) {
        if (halfWidth < 0.0D) {
            throw new IllegalArgumentException("half width must be nonnegative");
        } else if (halfHeight < 0.0D) {
            throw new IllegalArgumentException("half height must be nonnegative");
        } else {
            double xs = scaleX(x);
            double ys = scaleY(y);
            double ws = factorX(2.0D * halfWidth);
            double hs = factorY(2.0D * halfHeight);
            if (ws <= 1.0D && hs <= 1.0D) {
                pixel(x, y);
            } else if (filled) {
                offscreen.fill(new java.awt.geom.Rectangle2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs));
            } else {
                offscreen.draw(new java.awt.geom.Rectangle2D.Double(xs - ws / 2.0D, ys - hs / 2.0D, ws, hs));
            }

            draw();
        }
    }

    public static void polyline(double[] x, double[] y) {
        polygon(x, y, false, false);
    }

    public static void polygon(double[] x, double[] y) {
        polygon(x, y, true, false);
    }

    public static void filledPolygon(double[] x, double[] y) {
        polygon(x, y, true, true);
    }

    private static void polygon(double[] x, double[] y, boolean close, boolean fill) {
        int N = x.length;
        if (y.length != N) {
            throw new IllegalArgumentException("x[] and y[] must have the same number of elements.  x[] has " + x.length + " elements, but y[] has " + y.length + " elements.");
        } else if ((close || fill) && N < 3) {
            throw new IllegalArgumentException("You must specify at least three for a polygon.  You have only provided " + N + " points.");
        } else {
            java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
            path.moveTo(scaleX(x[0]), scaleY(y[0]));

            for(int i = 0; i < N; ++i) {
                path.lineTo(scaleX(x[i]), scaleY(y[i]));
            }

            if (close || fill) {
                path.closePath();
            }

            if (fill) {
                offscreen.fill(path);
            } else {
                offscreen.draw(path);
            }

            draw();
        }
    }

    public static void polyline(double... coords) {
        polygon(false, false, coords);
    }

    public static void polygon(double... coords) {
        polygon(true, false, coords);
    }

    public static void filledPolygon(double... coords) {
        polygon(true, true, coords);
    }

    private static void polygon(boolean close, boolean fill, double... coords) {
        int N = coords.length;
        if (N % 2 != 0) {
            throw new IllegalArgumentException("You must specify an even number of coordinates.  You actually specified " + N + " coordinates.");
        } else if ((close || fill) && N < 6) {
            throw new IllegalArgumentException("You must specify at least six coordinates (three points).  You only specified " + N + " coordinates.");
        } else {
            java.awt.geom.Path2D.Double path = new java.awt.geom.Path2D.Double();
            path.moveTo(scaleX(coords[0]), scaleY(coords[1]));

            for(int i = 0; i < N; i += 2) {
                path.lineTo(scaleX(coords[i]), scaleY(coords[i + 1]));
            }

            if (close || fill) {
                path.closePath();
            }

            if (fill) {
                offscreen.fill(path);
            } else {
                offscreen.draw(path);
            }

            draw();
        }
    }

    private static Image getImage(String filename) {
        ImageIcon icon = new ImageIcon(filename);
        URL url;
        if (icon == null || icon.getImageLoadStatus() != 8) {
            try {
                url = new URL(filename);
                icon = new ImageIcon(url);
            } catch (Exception var3) {
            }
        }

        if (icon == null || icon.getImageLoadStatus() != 8) {
            url = PennDraw.class.getResource(filename);
            if (url == null) {
                throw new IllegalArgumentException("image " + filename + " not found");
            }

            icon = new ImageIcon(url);
        }

        return icon.getImage();
    }

    public static void picture(double x, double y, String s) {
        picture(x, y, s, 0.0D, 0.0D, 0.0D);
    }

    public static void picture(double x, double y, String s, double degrees) {
        picture(x, y, s, 0.0D, 0.0D, degrees);
    }

    public static void picture(double x, double y, String s, double w, double h) {
        picture(x, y, s, w, h, 0.0D);
    }

    public static void picture(double x, double y, String s, double w, double h, double degrees) {
        Image image = getImage(s);
        int iw = image.getWidth((ImageObserver)null);
        int ih = image.getHeight((ImageObserver)null);
        if (iw > 0 && ih > 0) {
            AffineTransform t = offscreen.getTransform();
            double xs = xscale * (x - xmin);
            double ys = (double)height - yscale * (y - ymin);
            if (degrees != 0.0D) {
                offscreen.rotate(-Math.toRadians(degrees), xs, ys);
            }

            if (w == 0.0D && h == 0.0D) {
                offscreen.drawImage(image, (int)Math.round(xs - 0.5D * (double)iw), (int)Math.round(ys - 0.5D * (double)ih), (ImageObserver)null);
            } else {
                if (w == 0.0D) {
                    w = (double)iw * h / (double)ih;
                }

                if (h == 0.0D) {
                    h = (double)ih * w / (double)iw;
                }

                offscreen.drawImage(image, (int)Math.round(xs - 0.5D * w), (int)Math.round(ys - 0.5D * h), (int)Math.round(w), (int)Math.round(h), (ImageObserver)null);
            }

            if (degrees != 0.0D) {
                offscreen.setTransform(t);
            }

            draw();
        } else {
            throw new IllegalArgumentException("image " + s + " is corrupt");
        }
    }

    public static void text(double x, double y, String s) {
        text(x, y, s, 0.0D);
    }

    public static void text(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, -0.5D);
    }

    public static void textLeft(double x, double y, String s) {
        textLeft(x, y, s, 0.0D);
    }

    public static void textLeft(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, 0.0D);
    }

    public static void textRight(double x, double y, String s) {
        textRight(x, y, s, 0.0D);
    }

    public static void textRight(double x, double y, String s, double degrees) {
        text(x, y, s, degrees, -1.0D);
    }

    private static void text(double x, double y, String s, double degrees, double dw) {
        AffineTransform t = offscreen.getTransform();
        FontMetrics metrics = offscreen.getFontMetrics();
        int w = metrics.stringWidth(s);
        int h = metrics.getDescent();
        double xs = scaleX(x);
        double ys = scaleY(y);
        if (degrees != 0.0D) {
            offscreen.rotate(-Math.toRadians(degrees), xs, ys);
        }

        offscreen.drawString(s, (float)(xs + dw * (double)w), (float)(ys + (double)h));
        if (degrees != 0.0D) {
            offscreen.setTransform(t);
        }

        draw();
    }

    public static void show(int t) {
        long millis = System.currentTimeMillis();
        if (millis < nextDraw) {
            try {
                Thread.sleep(nextDraw - millis);
            } catch (InterruptedException var4) {
                System.out.println("Error sleeping");
            }

            millis = nextDraw;
        }

        defer = false;
        draw();
        defer = true;
        nextDraw = millis + (long)t;
    }

    public static void show() {
        defer = false;
        draw();
    }

    private static void draw() {
        if (!defer) {
            onscreen.drawImage(offscreenImage, 0, 0, (ImageObserver)null);
            frame.repaint();
        }
    }

    public static void disableAnimation() {
        animationSpeed = -1;
        show();
    }

    public static void enableAnimation(double frameRate) {
        if (frameRate < 0.0D) {
            throw new IllegalArgumentException("frameRate must be >= 0");
        } else {
            animationSpeed = frameRate == 0.0D ? 0 : (int)Math.round(1000.0D / frameRate);
            show(0);
        }
    }

    public static void advance() {
        if (animationSpeed < 0) {
            throw new RuntimeException("You must call PennDraw.enableAnimation() to activate animation mode before calling PennDraw.advance()");
        } else {
            show(animationSpeed);
        }
    }

    public static void save(String filename) {
        File file = new File(filename);
        String suffix = filename.substring(filename.lastIndexOf(46) + 1);
        if (suffix.toLowerCase().equals("png")) {
            try {
                ImageIO.write(onscreenImage, suffix, file);
            } catch (IOException var10) {
                var10.printStackTrace();
            }
        } else if (suffix.toLowerCase().equals("jpg")) {
            WritableRaster raster = onscreenImage.getRaster();
            WritableRaster newRaster = raster.createWritableChild(0, 0, width, height, 0, 0, new int[]{0, 1, 2});
            DirectColorModel cm = (DirectColorModel)onscreenImage.getColorModel();
            DirectColorModel newCM = new DirectColorModel(cm.getPixelSize(), cm.getRedMask(), cm.getGreenMask(), cm.getBlueMask());
            BufferedImage rgbBuffer = new BufferedImage(newCM, newRaster, false, (Hashtable)null);

            try {
                ImageIO.write(rgbBuffer, suffix, file);
            } catch (IOException var9) {
                var9.printStackTrace();
            }
        } else {
            System.out.println("Invalid image file type: " + suffix);
        }

    }

    public void actionPerformed(ActionEvent e) {
        FileDialog chooser = new FileDialog(frame, "Use a .png or .jpg extension", 1);
        chooser.setVisible(true);
        String filename = chooser.getFile();
        if (filename != null) {
            String var10000 = chooser.getDirectory();
            save(var10000 + File.separator + chooser.getFile());
        }

    }

    public static boolean mousePressed() {
        synchronized(mouseLock) {
            return mousePressed;
        }
    }

    public static double mouseX() {
        synchronized(mouseLock) {
            return mouseX;
        }
    }

    public static double mouseY() {
        synchronized(mouseLock) {
            return mouseY;
        }
    }

    public void mouseClicked(MouseEvent e) {
    }

    public void mouseEntered(MouseEvent e) {
    }

    public void mouseExited(MouseEvent e) {
    }

    public void mousePressed(MouseEvent e) {
        synchronized(mouseLock) {
            mouseX = userX((double)e.getX());
            mouseY = userY((double)e.getY());
            mousePressed = true;
        }
    }

    public void mouseReleased(MouseEvent e) {
        synchronized(mouseLock) {
            mousePressed = false;
        }
    }

    public void mouseDragged(MouseEvent e) {
        synchronized(mouseLock) {
            mouseX = userX((double)e.getX());
            mouseY = userY((double)e.getY());
        }
    }

    public void mouseMoved(MouseEvent e) {
        synchronized(mouseLock) {
            mouseX = userX((double)e.getX());
            mouseY = userY((double)e.getY());
        }
    }

    public static boolean hasNextKeyTyped() {
        synchronized(keyLock) {
            return !keysTyped.isEmpty();
        }
    }

    public static char nextKeyTyped() {
        synchronized(keyLock) {
            return (Character)keysTyped.removeLast();
        }
    }

    public static boolean isKeyPressed(int keycode) {
        synchronized(keyLock) {
            return keysDown.contains(keycode);
        }
    }

    public void keyTyped(KeyEvent e) {
        synchronized(keyLock) {
            keysTyped.addFirst(e.getKeyChar());
        }
    }

    public void keyPressed(KeyEvent e) {
        synchronized(keyLock) {
            keysDown.add(e.getKeyCode());
        }
    }

    public void keyReleased(KeyEvent e) {
        synchronized(keyLock) {
            keysDown.remove(e.getKeyCode());
        }
    }

    public static void main(String[] args) {
        square(0.2D, 0.8D, 0.1D);
        setPenWidthInPoints(12.0D);
        rectangle(0.2D, 0.8D, 0.1D, 0.2D, 10.0D);
        setPenRadius();
        filledRectangle(0.8D, 0.8D, 0.2D, 0.1D, 10.0D);
        circle(0.8D, 0.2D, 0.2D);
        filledEllipse(0.8D, 0.2D, 0.2D, 0.1D, 10.0D);
        setPenColor(BOOK_RED);
        setPenRadius(0.02D);
        arc(0.8D, 0.2D, 0.1D, 200.0D, 45.0D);
        setPenRadius();
        setPenColor(BOOK_BLUE);
        double[] x = new double[]{0.1D, 0.2D, 0.3D, 0.2D};
        double[] y = new double[]{0.2D, 0.3D, 0.2D, 0.1D};
        polyline(x, y);
        filledPolygon(0.1D, 0.2D, 0.2D, 0.3D, 0.3D, 0.2D);
        setFontSize(12.0D);
        setPenColor(BLACK);
        text(0.2D, 0.5D, "black text");
        text(0.2D, 0.5D, "black text", 30.0D);
        setFont("Serif");
        setPenColor(WHITE);
        text(0.8D, 0.8D, "white serif text");
    }

    static {
        BLACK = Color.BLACK;
        BLUE = Color.BLUE;
        CYAN = Color.CYAN;
        DARK_GRAY = Color.DARK_GRAY;
        GRAY = Color.GRAY;
        GREEN = Color.GREEN;
        LIGHT_GRAY = Color.LIGHT_GRAY;
        MAGENTA = Color.MAGENTA;
        ORANGE = Color.ORANGE;
        PINK = Color.PINK;
        RED = Color.RED;
        WHITE = Color.WHITE;
        YELLOW = Color.YELLOW;
        BOOK_BLUE = new Color(9, 90, 166);
        BOOK_LIGHT_BLUE = new Color(103, 198, 243);
        BOOK_RED = new Color(150, 35, 31);
        DEFAULT_PEN_COLOR = BLACK;
        DEFAULT_CLEAR_COLOR = WHITE;
        width = 512;
        height = 512;
        defer = false;
        nextDraw = -1L;
        animationSpeed = -1;
        mouseLock = new Object();
        keyLock = new Object();
        DEFAULT_FONT = new Font("SansSerif", 0, 16);
        std = new PennDraw();
        mousePressed = false;
        mouseX = 0.0D;
        mouseY = 0.0D;
        keysTyped = new LinkedList();
        keysDown = new TreeSet();
        init();
    }
}
