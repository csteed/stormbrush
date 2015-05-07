package stormstroke;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.image.BufferedImage;

/**
 * Created by csg on 5/5/15.
 */
public class VisPanel extends JPanel implements ComponentListener {
    private double west, east, south, north;
    private double mapWidth, mapHeight;
    private double xFactor, yFactor;
    private BufferedImage backgroundImage;

    public VisPanel(BufferedImage backgroundImage, double west, double east, double south, double north) {
        this.backgroundImage = backgroundImage;
        this.west = west;
        this.east = east;
        this.south = south;
        this.north = north;
        mapWidth = east - west;
        mapHeight = north - south;
        xFactor = mapWidth / (double)backgroundImage.getWidth();
        yFactor = mapHeight / (double)backgroundImage.getHeight();
    }

    public static int longitudeToX(double longitude, double west, double east, int imageWidth) {
        double width = east - west;
        double norm = (longitude - west) / width;
        return (int)(norm * (double)imageWidth);
    }

    public static int latitudeToY(double latitude, double north, double south, int imageHeight) {
        double height = north - south;
        double norm = 1. - (latitude - south) / height;
        return (int)(norm * (double)imageHeight);
    }

    public static double YToLatitude(int y, double north, double south, int imageHeight) {
        double height = north - south;
        double norm = (double)y/(double)imageHeight;
        return north - (norm * height);
    }

    public static double XToLongitude(int x, double west, double east, int imageWidth) {
        double width = east - west;
        double norm = (double)x/(double)imageWidth;
        return west + (norm*width);
    }

    public Dimension getPreferredSize() {
        return new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight());
    }

    public void componentResized(ComponentEvent e) {

    }

    public void componentMoved(ComponentEvent e) {

    }

    public void componentShown(ComponentEvent e) {

    }

    public void componentHidden(ComponentEvent e) {

    }

    public void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D)g;
        g2.setColor(getBackground());
        g2.fillRect(0, 0, getWidth(), getHeight());

        g2.drawImage(backgroundImage, 0, 0, this);
    }
}
