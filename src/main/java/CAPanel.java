import javax.imageio.ImageIO;
import javax.swing.*;

import java.awt.image.*;
import java.awt.*;
import java.awt.font.GlyphVector;
import java.awt.geom.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import jgeodesic.JGeodesic;

public class CAPanel extends JPanel {
	private static final int MAX_TIME = 400;
	private static final int MIN_ALPHA = /*65*//*87*/85;
	private static final int MAX_ALPHA = 255;
	private BufferedImage backgroundImage;
	private BufferedImage wind64Image, wind50Image, wind34Image;
	private BufferedImage trackImage;
	private BufferedImage strokeImage;
	
	private static int SMALL_BRUSH_MODE = 0;
	private static int LONG_STROKE_MODE = 1;
	private int stroke_mode = SMALL_BRUSH_MODE;
	
	private double westBound, eastBound, northBound, southBound;
	private int lastPointX, lastPointY;
	private ForecastAdvisory lastAdvisory = null;
	private boolean firstTrackPoint = true;
	private double mapWidth, mapHeight;
	private double xFactor, yFactor;
	
	public static final int shortBrushAlpha = 100/*110*//*120*/;
	
	//private Color trackColor = new Color(214, 24, 34);
	//private Color trackColor = new Color(165, 91, 160);
	//private Color trackColor = new Color(128, 0, 255);
	private Color trackColor = Color.black;
	//private Color trackColor = Color.yellow;

	
	private Color politicalBoundaryColor = new Color(70, 70, 70);
	private Color seaColor = new Color(190, 192, 195); // political boundaries in background map
	private Color landColor = new Color(220, 222, 225);
	// Legible Cities
	//Color c34 = new Color (48, 29, 123, MAX_ALPHA);
	//Color c50 = new Color (92, 61, 96, MAX_ALPHA);
	//Color c64 = new Color (221, 18, 37, MAX_ALPHA);
	
	// Dykes
	//private Color c64 = new Color (253, 242, 188, MAX_ALPHA);
	//private Color c50 = new Color (250, 200, 109, MAX_ALPHA);
	//private Color c34 = new Color (190, 125, 73, MAX_ALPHA);
	public static final Color c64 = new Color (250, 218, 0, MAX_ALPHA);
	public static final Color c50 = new Color (235, 126, 0, MAX_ALPHA);
	public static final Color c34 = new Color (177, 72, 0, MAX_ALPHA);

	// Healey
	//Color c34 = new Color (111, 33, 23, MAX_ALPHA);
	//Color c50 = new Color (214, 24, 34, MAX_ALPHA);
	//Color c64 = new Color (165, 91, 160, MAX_ALPHA);
	
	private int spacing_factor_34knots = 12/*12*/;
	private int spacing_factor_50knots = 10/*8*/;
	private int spacing_factor_64knots = 8/*4*/;
	
	private int windMatrix[][][] = null;
	
	private ArrayList hull = new ArrayList();
	private ArrayList lastAreaPoints = null;
	
	public boolean showTrack = true;
	
	private Path2D.Float windSwath = new Path2D.Float();
	private Random r = new Random();
	
	private BufferedImage wind34Stroke, wind50Stroke, wind64Stroke;
	private int stroke_width = /*8*//*3*/4;
	private int stroke_height = /*12*//*5*/10;
	private int long_stroke_width = 8;
	private int long_stroke_height = 8;
	private int max_long_stroke_radius = 6;
	
	private int stroke_image_width = 225;
	private int stroke_image_height = 225;
	
	public CAPanel(BufferedImage backgroundImage, double west, double east, double south, double north) {
		this.backgroundImage = backgroundImage;
		resetForeground();
		
		createStrokeImage((int)(long_stroke_width), (int)(long_stroke_height));
//		foregroundImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		((Graphics2D)foregroundImage.getGraphics()).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
//		try {
//			File f = new File("images/34_icon_stroke.png");
//			wind34Stroke = ImageIO.read(f);
//			f = new File("images/50_icon_stroke.png");
//			wind50Stroke = ImageIO.read(f);
//			f = new File("images/64_icon_stroke.png");
//			wind64Stroke = ImageIO.read(f);
//
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
		
		westBound = west;
		eastBound = east;
		northBound = north;
		southBound = south;
		mapWidth = east - west;
		mapHeight = north - south;
		xFactor = mapWidth / (double)backgroundImage.getWidth();
		yFactor = mapHeight / (double)backgroundImage.getHeight();
		
		setPreferredSize(new Dimension(backgroundImage.getWidth(), backgroundImage.getHeight()));
	}
	
	public void setStrokeMode (int newStrokeMode) {
		if ((newStrokeMode != stroke_mode) &&
			((newStrokeMode == SMALL_BRUSH_MODE) || 
			 (newStrokeMode == LONG_STROKE_MODE))) {
			stroke_mode = newStrokeMode;
		}
	}
	
	public void saveImage(File f) {
		BufferedImage image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		paint(image.getGraphics());
						
		try {
			ImageIO.write(image, "png", f);
		} catch (IOException ex) {
			JOptionPane.showMessageDialog(null, "An error occured while saving the screen shot image.", "Screenshot Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
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
		
	public void resetForeground() {
		trackImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		wind64Image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		wind50Image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		wind34Image = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
//		tempImage = new BufferedImage(backgroundImage.getWidth(), backgroundImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
		
		
		windMatrix = new int[3][backgroundImage.getHeight()][backgroundImage.getWidth()];
		for (int i = 0; i < windMatrix.length; i++) {
			for (int row = 0; row < backgroundImage.getHeight(); row++) {
				Arrays.fill(windMatrix[i][row], 0);
			}
		}
		System.out.println("windMatrix dimensions are: ["+windMatrix.length+"]["+windMatrix[0].length+"]["+windMatrix[0][0].length+"]");
		
		firstTrackPoint = true;
		
		repaint();
	}
	
	public void drawStrokes(Graphics2D g2, BufferedImage strokeImage, Shape shape, int stroke_width, int stroke_height, int stroke_x_spacing, int stroke_y_spacing, double orientation) {
		Rectangle bounds = shape.getBounds();
		
		Color c = g2.getColor();
		
		int gap = 0;
		
		int cols = (int)(1.5f + (float)bounds.getWidth()/(float)(stroke_x_spacing));
		int rows = (int)(1.5f + (float)bounds.getHeight()/(float)(stroke_y_spacing));
		
		stroke_x_spacing = (int)(0.5f + (float)bounds.getWidth()/(float)cols);
		stroke_y_spacing = (int)(0.5f + (float)bounds.getHeight()/(float)rows);
		
		ArrayList indexList = new ArrayList(rows*cols);
		for (int i = 0; i < rows*cols; i++) {
			while(true) {
				int next = r.nextInt(rows*cols);
				if (!indexList.contains(next)) {
					indexList.add(next);
//					System.out.print(next + " ");
					break;
				}
			}
		}
//		System.out.print("\n");
		
		/* The below block comment is for drawing the stroke placement grid		
		int iminx = (int)bounds.getMinX();
		int imaxx = (int)bounds.getMaxX();
		int iminy = (int)bounds.getMinY();
		int imaxy = (int)bounds.getMaxY();
//		System.out.println("bounds.getWidth() = " + bounds.getWidth() + " bounds.getHeight() = " + bounds.getHeight());
//		System.out.println("iminx = " + iminx + " imaxx = " + imaxx + " iminy = " + iminy + " imaxy = " + imaxy);
		g2.setColor(Color.cyan);
		for (int irow = 0; irow <= rows; irow++) {
			int iy = (int)bounds.getMinY() + (irow*stroke_y_spacing);
//			System.out.println("iy = " + iy);
			g2.drawLine(iminx, iy, imaxx, iy);
		}
		
		for (int icol = 0; icol <= cols; icol++) {
			int ix = (int)bounds.getMinX() + (icol*stroke_x_spacing);
			g2.drawLine(ix, iminy, ix, imaxy);
		}
		g2.setColor(c);
		*/
		
		for (Iterator iter = indexList.iterator(); iter.hasNext();) {
			int index = ((Integer)iter.next()).intValue();
			int row = index / cols;
			int col = index - (row * cols);
			row = (int)bounds.getMinY() + (row*(stroke_y_spacing));
			col = (int)bounds.getMinX() + (col*(stroke_x_spacing));
			row += r.nextInt(stroke_y_spacing-1);
			col += r.nextInt(stroke_x_spacing-1);
//			System.out.println("row="+row +" col="+col);
			if (shape.contains(col, row)) {
				g2.rotate(orientation, col, row);
				Rectangle2D.Float stroke = new Rectangle2D.Float(col-stroke_width/2, row-stroke_height/2, stroke_width, stroke_height);
				g2.setColor(c);
				g2.fill(stroke);
//				g2.drawImage(strokeImage, (int)stroke.getMinX(), (int)stroke.getMinY(), (int)stroke.getWidth(), (int)stroke.getHeight(), this);
				g2.setColor(Color.darkGray);
				//g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue()));
//				g2.draw(stroke);
				g2.rotate(-orientation, col, row);
			}
		}
	
		g2.setColor(c);
	}
	
	private void createStrokeImage(int stroke_x_spacing, int stroke_y_spacing) {
		System.out.println("CAPanel.createStrokeImage(): Entered method.");
		//strokeImage = new BufferedImage((int)(backgroundImage.getWidth()*.6), (int)(backgroundImage.getHeight()*.6), BufferedImage.TYPE_INT_ARGB);
		strokeImage = new BufferedImage(stroke_image_width, stroke_image_height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = (Graphics2D)strokeImage.getGraphics();
//		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(Color.lightGray);
		
		int cols = (int)(1.5f + (float)strokeImage.getWidth()/(float)(stroke_x_spacing));
		int rows = (int)(1.5f + (float)strokeImage.getHeight()/(float)(stroke_y_spacing));
		
		stroke_x_spacing = (int)(0.5f + (float)strokeImage.getWidth()/(float)cols);
		stroke_y_spacing = (int)(0.5f + (float)strokeImage.getHeight()/(float)rows);
		
		ArrayList indexList = new ArrayList(rows*cols);
		for (int i = 0; i < rows*cols; i++) {
			while(true) {
				int next = r.nextInt(rows*cols);
				if (!indexList.contains(next)) {
					indexList.add(next);
//					System.out.print(next + " ");
					break;
				}
			}
		}
//		System.out.print("\n");
		
		/* The below block comment is for drawing the stroke placement grid		
		int iminx = (int)bounds.getMinX();
		int imaxx = (int)bounds.getMaxX();
		int iminy = (int)bounds.getMinY();
		int imaxy = (int)bounds.getMaxY();
//		System.out.println("bounds.getWidth() = " + bounds.getWidth() + " bounds.getHeight() = " + bounds.getHeight());
//		System.out.println("iminx = " + iminx + " imaxx = " + imaxx + " iminy = " + iminy + " imaxy = " + imaxy);
		g2.setColor(Color.cyan);
		for (int irow = 0; irow <= rows; irow++) {
			int iy = (int)bounds.getMinY() + (irow*stroke_y_spacing);
//			System.out.println("iy = " + iy);
			g2.drawLine(iminx, iy, imaxx, iy);
		}
		
		for (int icol = 0; icol <= cols; icol++) {
			int ix = (int)bounds.getMinX() + (icol*stroke_x_spacing);
			g2.drawLine(ix, iminy, ix, imaxy);
		}
		g2.setColor(c);
		*/
		
		int maxOvalSize = max_long_stroke_radius;
		int center_x = strokeImage.getWidth()/2;
		int center_y = strokeImage.getHeight()/2;
		double max_dist = Point.distance(0, 0, center_x, center_y);
//		int ovalSize = 4;
//		int ovalSizeHalf = ovalSize/2;
		
		for (Iterator iter = indexList.iterator(); iter.hasNext();) {
			int index = ((Integer)iter.next()).intValue();
			int row = index / cols;
			int col = index - (row * cols);
			row = (int)strokeImage.getMinY() + (row*(stroke_y_spacing));
			col = (int)strokeImage.getMinX() + (col*(stroke_x_spacing));
			row += r.nextInt(stroke_y_spacing-1);
			col += r.nextInt(stroke_x_spacing-1);
			
			double dist = Point.distance(center_x, center_y, col, row);
			double size_factor = 1. - (dist/max_dist);
			int ovalSize = (int)(0.5 + (size_factor * maxOvalSize));
			g2.fillOval(col - ovalSize/2, row - ovalSize/2, ovalSize, ovalSize);
			
//			System.out.println("row="+row +" col="+col);
//			if (shape.contains(col, row)) {
				
//				g2.rotate(orientation, col, row);
//				Rectangle2D.Float stroke = new Rectangle2D.Float(col-stroke_width/2, row-stroke_height/2, stroke_width, stroke_height);
//				g2.setColor(c);
//				g2.fill(stroke);
//				g2.drawImage(strokeImage, (int)stroke.getMinX(), (int)stroke.getMinY(), (int)stroke.getWidth(), (int)stroke.getHeight(), this);
//				g2.setColor(Color.black);
//				g2.draw(stroke);
//				g2.rotate(-orientation, col, row);
//			}
		}
		
		try {
			ImageIO.write(strokeImage, "png", new File("stroke_image.png"));
		} catch (IOException ex) {
//			JOptionPane.showMessageDialog(null, "An error occured while saving the stroke image.", "Screenshot Error", JOptionPane.ERROR_MESSAGE);
			ex.printStackTrace();
		}
		System.out.println("CAPanel.createStrokeImage(): Exited method.");
	}
		
	private Polygon convertAdvisoryAreaToPolygon(double center_lat, double center_lon, double ne, double se, double sw, double nw) {
		int num_slices = 4;
		double slice_angle = (90. * ForecastAdvisoryReader.DEG2RAD) / num_slices;
		Polygon p = new Polygon();
		double lat_rad = center_lat * ForecastAdvisoryReader.DEG2RAD;
		double lon_rad = center_lon * ForecastAdvisoryReader.DEG2RAD;
		double [] coords;
		int ix, iy;
		
		// calculate coordinates for nw quadrant
		double start_az = 0.;
		double s = nw * ForecastAdvisoryReader.NM2METERS;
		if (nw > 0.) {
			for (int iaz = 0; iaz <= num_slices; iaz++) {
				double az = start_az + ((double)iaz * slice_angle);
//				System.out.println("az = " + (az * ForecastAdvisoryReader.RAD2DEG));
				coords = JGeodesic.direct1(lat_rad, lon_rad, az, s, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
				ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
				iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
				p.addPoint(ix, iy);
			}
		}
		
		// calculate coordinates for the sw quadrant
		start_az = 90. * (Math.PI/180.);
		s = sw * ForecastAdvisoryReader.NM2METERS;
		if (sw > 0.) {
			for (int iaz = 0; iaz <= num_slices; iaz++) {
				double az = start_az + ((double)iaz * slice_angle);
				coords = JGeodesic.direct1(lat_rad, lon_rad, az, s, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
				ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
				iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
				p.addPoint(ix, iy);
			}
		}
		
		// calculate coordinates for the se quadrant
		start_az = 180. * (Math.PI/180.);
		s = se * ForecastAdvisoryReader.NM2METERS;
		if (se > 0.) {
			for (int iaz = 0; iaz <= num_slices; iaz++) {
				double az = start_az + ((double)iaz * slice_angle);
				coords = JGeodesic.direct1(lat_rad, lon_rad, az, s, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
				ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
				iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
				p.addPoint(ix, iy);
			}
		}
		
		// calculate coordinates for the ne quadrant
		start_az = 270. * (Math.PI/180.);
		s = ne * ForecastAdvisoryReader.NM2METERS;
		if (ne > 0.) {
			for (int iaz = 0; iaz <= num_slices; iaz++) {
				double az = start_az + ((double)iaz * slice_angle);
				coords = JGeodesic.direct1(lat_rad, lon_rad, az, s, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
				ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
				iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
				p.addPoint(ix, iy);
			}
		}
		
		/* Old swath computate that looking like a odd box
		double [] coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.NE_AZIMUTH, ne*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
		int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		int iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
		p.addPoint(ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.SE_AZIMUTH, se*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
		p.addPoint(ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.SW_AZIMUTH, sw*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
		p.addPoint(ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.NW_AZIMUTH, nw*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
		p.addPoint(ix, iy);
		*/
		return p;
	}
	
	private void drawAdvisoryArea(Graphics2D g2, double center_lat, double center_lon, AdvisoryArea area) {
		Polygon p = new Polygon();
		
		int center_ix = longitudeToX(center_lon, westBound, eastBound, backgroundImage.getWidth());
		int center_iy = latitudeToY(center_lat, northBound, southBound, backgroundImage.getHeight());
		
		double lat_rad = center_lat * ForecastAdvisoryReader.DEG2RAD;
		double lon_rad = center_lon * ForecastAdvisoryReader.DEG2RAD;

		double [] coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.NE_AZIMUTH, area.ne*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
		int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		int iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		p.addPoint(ix, iy);
//		g2.setColor(Color.blue);
//		g2.drawLine(center_ix, center_iy, ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.SE_AZIMUTH, area.se*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		p.addPoint(ix, iy);
//		g2.setColor(Color.blue);
//		g2.drawLine(center_ix, center_iy, ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.SW_AZIMUTH, area.sw*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		p.addPoint(ix, iy);
//		g2.setColor(Color.blue);
//		g2.drawLine(center_ix, center_iy, ix, iy);
		
		coords = JGeodesic.direct1(lat_rad, lon_rad, ForecastAdvisoryReader.NW_AZIMUTH, area.nw*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
		iy = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
//		area.points.add(new Point2D.Double(coords[1]*ForecastAdvisoryReader.RAD2DEG, coords[0]*ForecastAdvisoryReader.RAD2DEG));
		p.addPoint(ix, iy);
//		g2.setColor(Color.blue);
//		g2.drawLine(center_ix, center_iy, ix, iy);
		
		g2.setColor(new Color(0, 0, 200, 150));
		g2.fillPolygon(p);
	}
	
	public void drawTrack(Graphics2D g2, ForecastAdvisory advisory0, ForecastAdvisory advisory1) {
		// determine the centers for the advisories
		int x0 = longitudeToX(advisory0.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y0 = latitudeToY(advisory0.latitude, northBound, southBound, backgroundImage.getHeight());
		int x1 = longitudeToX(advisory1.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y1 = latitudeToY(advisory1.latitude, northBound, southBound, backgroundImage.getHeight());

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int tmp = x0;
			x0 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		
		int eye0 = advisory0.eye_diameter;
		int eye1 = advisory1.eye_diameter;
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
			tmp = y0;
			y0 = y1;
			y1 = tmp;
			eye1 = advisory0.eye_diameter;
			eye0 = advisory1.eye_diameter;
		}
		
		double dx = (double)(x1 - x0);
		double dy = (double)(y1 - y0)/dx;
		double deye = (double)(eye1 - eye0)/dx;
//		double dne = (double)(ne1 - ne0)/dx;
//		double dse = (double)(se1 - se0)/dx;
//		double dsw = (double)(sw1 - sw0)/dx;
//		double dnw = (double)(nw1 - nw0)/dx;
		
		double y = y0;
		double eye = eye0;
//		double ne = ne0;
//		double nw = nw0;
//		double se = se0;
//		double sw = sw0;

		if (x0 < x1) {
			for (int x = x0; x <= x1; x++) {
				int iy = (int)(y+0.5);

				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					if (eye > 0.) {	
						double coords [] = JGeodesic.direct1(lat, lon, 0., eye*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
	//					int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
						int eye_y = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
						
						int diameter = (int)iy-eye_y;  
						g2.drawOval(x-(diameter/2), iy-(diameter/2), diameter, diameter);
						g2.fillOval(x-(diameter/2), iy-(diameter/2), diameter, diameter);
					}
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					if (eye > 0.) {	
						double coords [] = JGeodesic.direct1(lat, lon, 0., eye*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
	//					int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
						int eye_y = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
						
						int diameter = (int)x-eye_y;  
						g2.drawOval(iy-(diameter/2), x-(diameter/2), diameter, diameter);
						g2.fillOval(iy-(diameter/2), x-(diameter/2), diameter, diameter);
					}
				}
				
//				Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
				
//				g2.draw(p);
//				g2.fill(p);
//				g2.drawLine(x, iy, x, iy);
				
				y = y + dy;
//				ne = ne + dne;
//				nw = nw + dnw;
//				sw = sw + dsw;
//				se = se + dse;
				eye = eye + deye;
			}
		} else {
			for (int x = x1; x >= x0; x--) {
				int iy = (int)(y+0.5);
	
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					if (eye > 0.) {	
						double coords [] = JGeodesic.direct1(lat, lon, 0., eye*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
	//					int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
						int eye_y = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
						
						int diameter = (int)iy-eye_y;  
						g2.drawOval(x-(diameter/2), iy-(diameter/2), diameter, diameter);
						g2.fillOval(x-(diameter/2), iy-(diameter/2), diameter, diameter);
					}
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					if (eye > 0.) {	
						double coords [] = JGeodesic.direct1(lat, lon, 0., eye*ForecastAdvisoryReader.NM2METERS, ForecastAdvisoryReader.GEODESIC_FLAT_OPTION);
	//					int ix = longitudeToX(coords[1]*ForecastAdvisoryReader.RAD2DEG, westBound, eastBound, backgroundImage.getWidth());
						int eye_y = latitudeToY(coords[0]*ForecastAdvisoryReader.RAD2DEG, northBound, southBound, backgroundImage.getHeight());
						
						int diameter = (int)x-eye_y;  
						g2.drawOval(iy-(diameter/2), x-(diameter/2), diameter, diameter);
						g2.fillOval(iy-(diameter/2), x-(diameter/2), diameter, diameter);
					}
				}
				
//				double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
//				double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
//				Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
				
//				g2.draw(p);
//				g2.fill(p);
//				g2.drawLine(x, iy, x, iy);
				
				y = y + dy;
//				ne = ne + dne;
//				nw = nw + dnw;
//				sw = sw + dsw;
//				se = se + dse;
			}
		}
	}
	
	
	
	public void drawWindSwaths(Graphics2D g2, BufferedImage strokeImage, ForecastAdvisory advisory0, ForecastAdvisory advisory1, AdvisoryArea area0, AdvisoryArea area1) {
		
		
		// determine the centers for the advisories
		int x0 = longitudeToX(advisory0.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y0 = latitudeToY(advisory0.latitude, northBound, southBound, backgroundImage.getHeight());
		int x1 = longitudeToX(advisory1.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y1 = latitudeToY(advisory1.latitude, northBound, southBound, backgroundImage.getHeight());

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int tmp = x0;
			x0 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		
		int ne0 = area0.ne;
		int ne1 = area1.ne;
		int se0 = area0.se;
		int se1 = area1.se;
		int sw0 = area0.sw;
		int sw1 = area1.sw;
		int nw0 = area0.nw;
		int nw1 = area1.nw;
		
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
			tmp = y0;
			y0 = y1;
			y1 = tmp;
			ne1 = area0.ne;
			ne0 = area1.ne;
			se1 = area0.se;
			se0 = area1.se;
			sw1 = area0.sw;
			sw0 = area1.sw;
			nw1 = area0.nw;
			nw0 = area1.nw;
		}
		
		double dx = (float)(x1 - x0);
		double dy = (float)(y1 - y0)/dx;
		double dne = (double)(ne1 - ne0)/dx;
		double dse = (double)(se1 - se0)/dx;
		double dsw = (double)(sw1 - sw0)/dx;
		double dnw = (double)(nw1 - nw0)/dx;
		
		double y = y0;
		double ne = ne0;
		double nw = nw0;
		double se = se0;
		double sw = sw0;

//		if (advisory0.day == 29) {
//			int test = 0;
//			return;
//		}
		
		int spacing_factor = 4;
		if (area0.value.shortValue() == (short)34) {
			spacing_factor = spacing_factor_34knots;
		} else if (area0.value.shortValue() == (short)50) {
			spacing_factor = spacing_factor_50knots;
		} else if (area0.value.shortValue() == (short)64) {
			spacing_factor = spacing_factor_64knots;
		}
		
		if (x0 < x1) {
			for (int x = x0; x <= x1; x++) {
				int iy = (int)(y+0.5);
				
//				if (advisory0.day == 29) {
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
					
//					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
//					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					
//					((Graphics2D)wind34Image.getGraphics()).draw(p);
					drawStrokes(g2, strokeImage, p, stroke_width, stroke_height, stroke_width*spacing_factor, stroke_height*spacing_factor, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
					
//					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
//					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					
//					((Graphics2D)wind34Image.getGraphics()).draw(p);
					drawStrokes(g2, strokeImage, p, stroke_width, stroke_height, stroke_width*spacing_factor, stroke_height*spacing_factor, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
				}
//				}
//				double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
//				double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
//				Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
				
/*				System.out.println(advisory0.day + " - " + advisory0.hour);
				if (advisory0.day == 29 && advisory0.hour == 9) {
					((Graphics2D)wind34Image.getGraphics()).draw(p);
				}*/
				
/*				The block comment below was used to fix a problem
 * 				if (advisory0.day == 28 && advisory0.hour == 9 && x == x0) {
					((Graphics2D)wind34Image.getGraphics()).draw(p);
					((Graphics2D)wind34Image.getGraphics()).draw(p.getBounds());
//					g2.fill(p);
					drawStrokes(g2, p, 4, 6, 4*4, 6*4, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
//					g2.drawLine(x, iy, x, iy);
					int offsetx = (int)p.getBounds().getMinX();
					int offsety = (int)p.getBounds().getMinY();
					for (int i = 0; i < p.npoints; i++) {
						System.out.println((p.xpoints[i]-offsetx) + ", " + (p.ypoints[i]-offsety));
					}
//					System.out.println("p is " + p.toString());
				}
*/				
				
//				((Graphics2D)wind34Image.getGraphics()).draw(p);
//				g2.fill(p);
//				drawStrokes(g2, p, 4, 6, 4*4, 6*4, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
//				g2.drawLine(x, iy, x, iy);
				
				y = y + dy;
				ne = ne + dne;
				nw = nw + dnw;
				sw = sw + dsw;
				se = se + dse;
			}
		} else {
			for (int x = x1; x >= x0; x--) {
				int iy = (int)(y+0.5);
				
//				if (advisory0.day == 29) {
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
					
//					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
//					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					
//					((Graphics2D)wind34Image.getGraphics()).draw(p);
					drawStrokes(g2, strokeImage, p, stroke_width, stroke_height, stroke_width*spacing_factor, stroke_height*spacing_factor, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
//					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth()) * ForecastAdvisoryReader.DEG2RAD;
//					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight()) * ForecastAdvisoryReader.DEG2RAD;
					Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					
//					((Graphics2D)wind34Image.getGraphics()).draw(p);
					drawStrokes(g2, strokeImage, p, stroke_width, stroke_height, stroke_width*spacing_factor, stroke_height*spacing_factor, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
				}
//				}
				
				/*double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
				double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
				Polygon p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
				*/
				/*System.out.println(advisory0.day + " - " + advisory0.hour);
				if (advisory0.day == 29 && advisory0.hour == 9) {
					((Graphics2D)wind34Image.getGraphics()).draw(p);
				}*/
				
/*				if (advisory0.day == 28 && advisory0.hour == 9 && x == x0) {
					((Graphics2D)wind34Image.getGraphics()).draw(p);
					((Graphics2D)wind34Image.getGraphics()).draw(p.getBounds());
//					g2.fill(p);
					drawStrokes(g2, p, 4, 6, 4*4, 6*4, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
//					g2.drawLine(x, iy, x, iy);
					int offsetx = (int)p.getBounds().getMinX();
					int offsety = (int)p.getBounds().getMinY();
					for (int i = 0; i < p.npoints; i++) {
						System.out.println((p.xpoints[i]-offsetx) + ", " + (p.ypoints[i]-offsety));
					}
//					System.out.println("p is " + p.toString());
				}*/
				
//				drawStrokes(g2, p, 4, 6, 4*4, 6*4, advisory0.movement_direction*ForecastAdvisoryReader.DEG2RAD);
//				((Graphics2D)wind34Image.getGraphics()).draw(p);
//				g2.fill(p);
//				g2.drawLine(x, iy, x, iy);
				
				y = y + dy;
				ne = ne + dne;
				nw = nw + dnw;
				sw = sw + dsw;
				se = se + dse;
			}
		}
	}
	
	private void paintShape(BufferedImage image, int matrix[][], Color c, Shape shape, int x, int y, boolean swap_xy) {
		iterateTime();
		
//		Graphics2D g2 = (Graphics2D)image.getGraphics();
		
		Rectangle shapeBounds = shape.getBounds();
		
		int offset_x = (strokeImage.getWidth() - shapeBounds.width) / 2;
		int offset_y = (strokeImage.getHeight() - shapeBounds.height) / 2;
		
//		int yellow_rgb = Color.lightGray.getRGB();
		
		int max_y = shapeBounds.y + shapeBounds.height;
		int max_x = shapeBounds.x + shapeBounds.width;
		for (int by = shapeBounds.y; by < max_y; by++) {
			for (int bx = shapeBounds.x; bx < max_x; bx++) {
				if (shape.contains(bx, by)) {
					int shape_img_x = bx - shapeBounds.x + offset_x;
					int shape_img_y = by - shapeBounds.y + offset_y;
//					if ((shape_img_x < strokeImage.getWidth()) && (shape_img_y < strokeImage.getHeight()) &&
//						 (shape_img_x >= 0) && (shape_img_y >= 0)) {
						
						int rgb;
					//int rgb = strokeImage.getRGB(bx-shapeBounds.x+offset_x, by-shapeBounds.y+offset_y);
						try {
							rgb = strokeImage.getRGB(shape_img_x, shape_img_y);
						} catch (Exception e) {
							//e.printStackTrace();
							System.out.println("x = " + shape_img_x + " y = " + shape_img_y);
							continue;
						}
						
						if (rgb != 0) {
//					if (strokeImage.getRGB(bx-shapeBounds.x+offset_x, by-shapeBounds.y+offset_y) != 0) {
						
//						if ((bx < image.getWidth() && bx >= 0) &&
//							(by < image.getHeight() && by >= 0)) {
							try {
								if (backgroundImage.getRGB(bx, by) != politicalBoundaryColor.getRGB()) {
									image.setRGB(bx, by, c.getRGB());
									matrix[by][bx] = MAX_TIME;
								}
							} catch (Exception ex) {
								ex.printStackTrace();
							}
//							if (bx >= matrix.length) {
//								System.out.println("It happened");
//								int matrix_len = matrix.length;
//								int image_width = image.getWidth();
//								int bg_image_width = backgroundImage.getWidth();
//								System.out.println("Why!");
//						}		
					}
//					}
//					image.setRGB(bx, by, c.getRGB());
//					matrix[bx][by] = MAX_TIME;
				}
			}
		}
		
		
//		g2.setColor(Color.blue);
//		if (swap_xy)
//			image.setRGB(y, x, Color.BLUE.getRGB());
//			g2.drawLine(y, x, y, x);
//		else 
//			g2.drawLine(x, y, x, y);
//			image.setRGB(x, y, Color.BLUE.getRGB());
		repaint();
	}
	
	public void drawWindSwaths3(ForecastAdvisory advisory0, ForecastAdvisory advisory1) {	
		int step = 1;
		
		// determine the centers for the advisories
		int x0 = longitudeToX(advisory0.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y0 = latitudeToY(advisory0.latitude, northBound, southBound, backgroundImage.getHeight());
		int x1 = longitudeToX(advisory1.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y1 = latitudeToY(advisory1.latitude, northBound, southBound, backgroundImage.getHeight());

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int tmp = x0;
			x0 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		
		int ne0[] = {0, 0, 0};
		int ne1[] = {0, 0, 0};
		int se0[] = {0, 0, 0};
		int se1[] = {0, 0, 0};
		int sw0[] = {0, 0, 0};
		int sw1[] = {0, 0, 0};
		int nw0[] = {0, 0, 0};
		int nw1[] = {0, 0, 0};
				
//		int ne0 = area0.ne;
//		int ne1 = area1.ne;
//		int se0 = area0.se;
//		int se1 = area1.se;
//		int sw0 = area0.sw;
//		int sw1 = area1.sw;
//		int nw0 = area0.nw;
//		int nw1 = area1.nw;
		
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
			tmp = y0;
			y0 = y1;
			y1 = tmp;
			
			for (int i = 0; i < advisory0.windRadiiList.size(); i++) {
				AdvisoryArea area = (AdvisoryArea)advisory0.windRadiiList.get(i);
				int idx = 0;
				if (area.value.intValue() == 50)
					idx = 1;
				else if (area.value.intValue() == 64)
					idx = 2;
				ne1[idx] = area.ne;
				se1[idx] = area.se;
				sw1[idx] = area.sw;
				nw1[idx] = area.nw;
			}
			
			for (int i = 0; i < advisory1.windRadiiList.size(); i++) {
				AdvisoryArea area = (AdvisoryArea)advisory1.windRadiiList.get(i);
				int idx = 0;
				if (area.value.intValue() == 50)
					idx = 1;
				else if (area.value.intValue() == 64)
					idx = 2;
				ne0[idx] = area.ne;
				se0[idx] = area.se;
				sw0[idx] = area.sw;
				nw0[idx] = area.nw;
			}
			
//			ne1 = area0.ne;
//			ne0 = area1.ne;
//			se1 = area0.se;
//			se0 = area1.se;
//			sw1 = area0.sw;
//			sw0 = area1.sw;
//			nw1 = area0.nw;
//			nw0 = area1.nw;
		} else {
			for (int i = 0; i < advisory0.windRadiiList.size(); i++) {
				AdvisoryArea area = (AdvisoryArea)advisory0.windRadiiList.get(i);
				int idx = 0;
				if (area.value.intValue() == 50)
					idx = 1;
				else if (area.value.intValue() == 64)
					idx = 2;
				ne0[idx] = area.ne;
				se0[idx] = area.se;
				sw0[idx] = area.sw;
				nw0[idx] = area.nw;
			}
			
			for (int i = 0; i < advisory1.windRadiiList.size(); i++) {
				AdvisoryArea area = (AdvisoryArea)advisory1.windRadiiList.get(i);
				int idx = 0;
				if (area.value.intValue() == 50)
					idx = 1;
				else if (area.value.intValue() == 64)
					idx = 2;
				ne1[idx] = area.ne;
				se1[idx] = area.se;
				sw1[idx] = area.sw;
				nw1[idx] = area.nw;
			}		
		}
		
		double dx = (float)(x1 - x0);
		double dy = (float)(y1 - y0)/dx;
	
		double dne[] = {(double)(ne1[0] - ne0[0])/dx, (double)(ne1[1] - ne0[1])/dx, (double)(ne1[2] - ne0[2])/dx};
		double dse[] = {(double)(se1[0] - se0[0])/dx, (double)(se1[1] - se0[1])/dx, (double)(se1[2] - se0[2])/dx};
		double dsw[] = {(double)(sw1[0] - sw0[0])/dx, (double)(sw1[1] - sw0[1])/dx, (double)(sw1[2] - sw0[2])/dx};
		double dnw[] = {(double)(nw1[0] - nw0[0])/dx, (double)(nw1[1] - nw0[1])/dx, (double)(nw1[2] - nw0[2])/dx};
//		double dne = (double)(ne1 - ne0)/dx;
//		double dse = (double)(se1 - se0)/dx;
//		double dsw = (double)(sw1 - sw0)/dx;
//		double dnw = (double)(nw1 - nw0)/dx;
		
		double y = y1;
		double ne[] = {ne1[0], ne1[1], ne1[2]};
		double se[] = {se1[0], se1[1], se1[2]};
		double sw[] = {sw1[0], sw1[1], sw1[2]};
		double nw[] = {nw1[0], nw1[1], nw1[2]};
//		double ne = ne1;
//		double nw = nw1;
//		double se = se1;
//		double sw = sw1;
		
		Polygon p [] = new Polygon[3];
		
		if (x0 < x1) {	
			for (int x = x1; x >= x0; x-=step) {
				int iy = (int)(y+0.5);
				
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
					
//					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					for (int i = 0; i < p.length; i++) {
						p[i] = convertAdvisoryAreaToPolygon(lat, lon, ne[i], se[i], sw[i], nw[i]);
					}
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
					
//					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					for (int i = 0; i < p.length; i++) {
						p[i] = convertAdvisoryAreaToPolygon(lat, lon, ne[i], se[i], sw[i], nw[i]);
					}
				}
				
				y = y - dy;
				for (int i = 0; i < p.length; i++) {
					ne[i] = ne[i] - dne[i];
					nw[i] = nw[i] - dnw[i];
					sw[i] = sw[i] - dsw[i];
					se[i] = se[i] - dse[i];
				}
//				ne = ne - dne;
//				nw = nw - dnw;
//				sw = sw - dsw;
//				se = se - dse;
				

				if (p[0].npoints > 0) {
					paintShape(wind34Image, windMatrix[0], c34, p[0], x, iy, steep);
					//paintShape(wind34Image, windMatrix[0], new Color(252, 153, 154, MAX_ALPHA), p[0], x, iy, steep);
					//paintShape(wind34Image, windMatrix[0], new Color(250, 250, 20, MAX_ALPHA), p[0], x, iy, steep);
					//paintShape(wind34Image, windMatrix[0], new Color(250, 0, 20, MAX_ALPHA), p[0], x, iy, steep);
				}	
				if (p[1].npoints > 0) {
					paintShape(wind50Image, windMatrix[1], c50, p[1], x, iy, steep);
					//paintShape(wind50Image, windMatrix[1], new Color(250, 92, 89, MAX_ALPHA), p[1], x, iy, steep);
					//paintShape(wind50Image, windMatrix[1], new Color(220, 120, 20, MAX_ALPHA), p[1], x, iy, steep);
				}
				if (p[2].npoints > 0) {
					paintShape(wind64Image, windMatrix[2], c64, p[2], x, iy, steep);
					//paintShape(wind64Image, windMatrix[2], new Color(249, 10, 18, MAX_ALPHA), p[2], x, iy, steep);
					//paintShape(wind64Image, windMatrix[2], new Color(250, 0, 20, MAX_ALPHA), p[2], x, iy, steep);
				}
//				paintShape(wind50Image, new Color(250, 153, 20), p[0], x, iy, steep);
//				paintShape(wind64Image, new Color(250, 0, 20), p[0], x, iy, steep);
				
//				try{
//					Thread.sleep(50);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
//				if (p != null){
					
//					paintShape(image, c, p, x, iy, steep);
//					try{
//						Thread.sleep(50);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				}
			}
		} else {
			for (int x = x0; x <= x1; x+=step) {
				int iy = (int)(y+0.5);
				
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
				
//					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					for (int i = 0; i < p.length; i++) {
						p[i] = convertAdvisoryAreaToPolygon(lat, lon, ne[i], se[i], sw[i], nw[i]);
					}
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
//					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					for (int i = 0; i < p.length; i++) {
						p[i] = convertAdvisoryAreaToPolygon(lat, lon, ne[i], se[i], sw[i], nw[i]);
					}
				}
				
				y = y - dy;
				for (int i = 0; i < p.length; i++) {
					ne[i] = ne[i] - dne[i];
					nw[i] = nw[i] - dnw[i];
					sw[i] = sw[i] - dsw[i];
					se[i] = se[i] - dse[i];
				}
//				ne = ne - dne;
//				nw = nw - dnw;
//				sw = sw - dsw;
//				se = se - dse;
				
				if (p[0].npoints > 0) {
					paintShape(wind34Image, windMatrix[0], c34, p[0], x, iy, steep);
					//paintShape(wind34Image, windMatrix[0], new Color(252, 153, 154, MAX_ALPHA), p[0], x, iy, steep);
					//paintShape(wind34Image, windMatrix[0], new Color(250, 250, 20, MAX_ALPHA), p[0], x, iy, steep);
//					paintShape(wind34Image, windMatrix[0], new Color(250, 0, 20, MAX_ALPHA), p[0], x, iy, steep);
				}	
				if (p[1].npoints > 0) {
					paintShape(wind50Image, windMatrix[1], c50, p[1], x, iy, steep);
					//paintShape(wind50Image, windMatrix[1], new Color(250, 92, 89, MAX_ALPHA), p[1], x, iy, steep);
					//paintShape(wind50Image, windMatrix[1], new Color(220, 120, 20, MAX_ALPHA), p[1], x, iy, steep);
				}
				if (p[2].npoints > 0) {
					paintShape(wind64Image, windMatrix[2], c64, p[2], x, iy, steep);
					//paintShape(wind64Image, windMatrix[2], new Color(249, 10, 18, MAX_ALPHA), p[2], x, iy, steep);
					//paintShape(wind64Image, windMatrix[2], new Color(250, 0, 20, MAX_ALPHA), p[2], x, iy, steep);
				}
//				paintShape(wind50Image, new Color(250, 153, 20), p[0], x, iy, steep);
//				paintShape(wind64Image, new Color(250, 0, 20), p[0], x, iy, steep);
				
//				try{
//					Thread.sleep(50);
//				} catch (Exception ex) {
//					ex.printStackTrace();
//				}
				
//				if (p != null){
//					paintShape(image, c, p, x, iy, steep);
//					try{
//						Thread.sleep(50);
//					} catch (Exception ex) {
//						ex.printStackTrace();
//					}
//				}
			}
		}	
	}
	
	
	public void drawWindSwaths2(BufferedImage image, ForecastAdvisory advisory0, ForecastAdvisory advisory1, AdvisoryArea area0, AdvisoryArea area1) {	
		// determine the centers for the advisories
		int x0 = longitudeToX(advisory0.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y0 = latitudeToY(advisory0.latitude, northBound, southBound, backgroundImage.getHeight());
		int x1 = longitudeToX(advisory1.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y1 = latitudeToY(advisory1.latitude, northBound, southBound, backgroundImage.getHeight());

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int tmp = x0;
			x0 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
			System.out.println("steep == true");
		}
		
		int ne0 = area0.ne;
		int ne1 = area1.ne;
		int se0 = area0.se;
		int se1 = area1.se;
		int sw0 = area0.sw;
		int sw1 = area1.sw;
		int nw0 = area0.nw;
		int nw1 = area1.nw;
		
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
			tmp = y0;
			y0 = y1;
			y1 = tmp;
			ne1 = area0.ne;
			ne0 = area1.ne;
			se1 = area0.se;
			se0 = area1.se;
			sw1 = area0.sw;
			sw0 = area1.sw;
			nw1 = area0.nw;
			nw0 = area1.nw;
		}
		
		double dx = (float)(x1 - x0);
		double dy = (float)(y1 - y0)/dx;
		double dne = (double)(ne1 - ne0)/dx;
		double dse = (double)(se1 - se0)/dx;
		double dsw = (double)(sw1 - sw0)/dx;
		double dnw = (double)(nw1 - nw0)/dx;
		
		double y = y1;
		double ne = ne1;
		double nw = nw1;
		double se = se1;
		double sw = sw1;

		int spacing_factor = 4;
		if (area0.value.shortValue() == (short)34) {
			spacing_factor = 12;
		} else if (area0.value.shortValue() == (short)50) {
			spacing_factor = 8;
		} else if (area0.value.shortValue() == (short)64) {
			spacing_factor = 4;
		}
	
		Color c = Color.WHITE;
//		Color c = g2.getColor();
		Polygon p = null;
		
		if (x0 < x1) {
			for (int x = x1; x >= x0; x--) {
//			for (int x = x0; x <= x1; x++) {
				int iy = (int)(y+0.5);
				
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
					
					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
					
//					g2.fill(p);
					
					
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
					
					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
//					g2.fill(p);
				}
				
				y = y - dy;
				ne = ne - dne;
				nw = nw - dnw;
				sw = sw - dsw;
				se = se - dse;
				
//				if (p != null) {
//					iterateTime();
//					Rectangle pBounds = p.getBounds();
//					g2.setColor(c);
//					int max_y = pBounds.y + pBounds.height;
//					int max_x = pBounds.x + pBounds.width;
//					for (int by = pBounds.y; by < max_y; by++) {
//						for (int bx = pBounds.x; bx < max_x; bx++) {
//							if (p.contains(bx, by)) {
//								g2.drawLine(bx, by, bx, by);
//								windMatrix[by][bx] = MAX_TIME;
//							}
//						}
//					}
//					g2.setColor(Color.blue);
//					g2.drawLine(x, iy, x, iy);
//					repaint();
//				}
				if (p != null){
					paintShape(image, null, c, p, x, iy, steep);
//					g2.setColor(c);
//					paintShape(g2, p, x, iy, !steep);
					try{
						Thread.sleep(50);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		} else {
			for (int x = x0; x <= x1; x++) {
//			for (int x = x1; x >= x0; x--) {
				int iy = (int)(y+0.5);
				
				if (!steep) {
					double lon = XToLongitude(x, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(iy, northBound, southBound, backgroundImage.getHeight());
				
					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
//					g2.fill(p);
				} else {
					double lon = XToLongitude(iy, westBound, eastBound, backgroundImage.getWidth());
					double lat = YToLatitude(x, northBound, southBound, backgroundImage.getHeight());
					p = convertAdvisoryAreaToPolygon(lat, lon, ne, se, sw, nw);
//					g2.fill(p);
				}
				
				y = y - dy;
				ne = ne - dne;
				nw = nw - dnw;
				sw = sw - dsw;
				se = se - dse;
				
//				if (p != null) {
//					iterateTime();
//					Rectangle pBounds = p.getBounds();
//					int max_y = pBounds.y + pBounds.height;
//					int max_x = pBounds.x + pBounds.width;
//					for (int by = pBounds.y; by < max_y; by++) {
//						for (int bx = pBounds.x; bx < max_x; bx++) {
//							if (p.contains(bx, by)) {
//								g2.drawLine(bx, by, bx, by);
//								windMatrix[by][bx] = MAX_TIME;
//							}
//						}
//					}
//					repaint();
//				}
				if (p != null){
//					g2.setColor(c);
					paintShape(image, null, c, p, x, iy, steep);
					try{
						Thread.sleep(50);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}
			}
		}
		
		
	}
	
	private void iterateTime() {
	//	 decrease time and saturation values on the wind matrix cells
		for (int row = 0; row < wind34Image.getHeight(); row++) {
			for (int col = 0; col < wind34Image.getWidth(); col++) {
				if (windMatrix[0][row][col] > 0) {
					windMatrix[0][row][col]--;
					Color c = new Color(wind34Image.getRGB(col, row));
					float norm_alpha = (float)windMatrix[0][row][col]/MAX_TIME;
					int new_alpha = (int)(norm_alpha*255);
					if (new_alpha < MIN_ALPHA)
						new_alpha = MIN_ALPHA;
					if (new_alpha > MAX_ALPHA)
						new_alpha = MAX_ALPHA;
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), new_alpha);
					wind34Image.setRGB(col, row, c.getRGB());
				}
				
				if (windMatrix[1][row][col] > 0) {
					windMatrix[1][row][col]--;
					Color c = new Color(wind50Image.getRGB(col, row));
					float norm_alpha = (float)windMatrix[1][row][col]/MAX_TIME;
					int new_alpha = (int)(norm_alpha*255);
					if (new_alpha < MIN_ALPHA)
						new_alpha = MIN_ALPHA;
					if (new_alpha > MAX_ALPHA)
						new_alpha = MAX_ALPHA;
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), new_alpha);
					wind50Image.setRGB(col, row, c.getRGB());
				}
				
				if (windMatrix[2][row][col] > 0) {
					windMatrix[2][row][col]--;
					Color c = new Color(wind64Image.getRGB(col, row));
					float norm_alpha = (float)windMatrix[2][row][col]/MAX_TIME;
					int new_alpha = (int)(norm_alpha*255);
					if (new_alpha < MIN_ALPHA)
						new_alpha = MIN_ALPHA;
					if (new_alpha > MAX_ALPHA)
						new_alpha = MAX_ALPHA;
					c = new Color(c.getRed(), c.getGreen(), c.getBlue(), new_alpha);
					wind64Image.setRGB(col, row, c.getRGB());
				}
			}
		}
	}
	public void nextAdvisoryLongStroke(ForecastAdvisory advisory, boolean newStorm) {
		if (newStorm) {
			lastAdvisory = null;
		}
		
		if (lastAdvisory != null) {
			drawWindSwaths3(lastAdvisory, advisory);
		}
		
		Graphics2D g2 = (Graphics2D)trackImage.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		int x = longitudeToX(advisory.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y = latitudeToY(advisory.latitude, northBound, southBound, backgroundImage.getHeight());
		
		if (lastAdvisory != null) {
			int x0 = longitudeToX(lastAdvisory.longitude, westBound, eastBound, backgroundImage.getWidth());
			int y0 = latitudeToY(lastAdvisory.latitude, northBound, southBound, backgroundImage.getHeight());
			
			g2.setColor(trackColor);
			//g2.setStroke(new BasicStroke(2.f));
			
	//		QuadCurve2D.Double curve = new QuadCurve2D.Double(x0, y0, control_x, control_y, x, y);
			g2.drawLine(x0, y0, x, y);			
		} else {
			g2.drawLine(x, y, x, y);
			firstTrackPoint = false;
		}
		repaint();
		
		lastAdvisory = advisory;
	}
	
	public void nextAdvisory2(ForecastAdvisory advisory, boolean newStorm) {
		
		if (newStorm) {
			lastAdvisory = null;
		}
		
		if (advisory.windRadiiList != null) {
			for (Iterator areaIterator = advisory.windRadiiList.iterator(); areaIterator.hasNext();) {
				AdvisoryArea lastArea = null;
				AdvisoryArea currentArea = (AdvisoryArea)areaIterator.next();
				
				if (currentArea.value.intValue() != 34)
					continue;
				
				if (lastAdvisory != null) {
					for (Iterator lastAreaIterator = lastAdvisory.windRadiiList.iterator(); lastAreaIterator.hasNext();) {
						AdvisoryArea area = (AdvisoryArea)lastAreaIterator.next();
						if (area.value.shortValue() == currentArea.value.shortValue()) {
							lastArea = area;
							break;
						}
					}
				}
				
				Graphics2D tmpG2 = (Graphics2D)this.wind34Image.getGraphics();
//				tmpG2.setColor(Color.black);
//				tmpG2.fillRect(0, 0, tempImage.getWidth(), tempImage.getHeight());
				tmpG2.setColor(Color.white);
				if (lastArea != null) {
					drawWindSwaths2(wind34Image, lastAdvisory, advisory, lastArea, currentArea);
				}
				
//				Polygon p = convertAdvisoryAreaToPolygon(advisory.latitude, advisory.longitude, currentArea.ne, currentArea.se, currentArea.sw, currentArea.nw);
//				Graphics2D g2 = (Graphics2D)this.wind50Image.getGraphics();
//				g2.setColor(Color.yellow);
//				g2.draw(p);
				
/*				// decrease time and saturation values on the wind matrix cells
				for (int row = 0; row < tempImage.getHeight(); row++) {
					for (int col = 0; col < tempImage.getWidth(); col++) {
						if (windMatrix[row][col] > 0) {
							windMatrix[row][col]--;
							Color c = new Color(wind34Image.getRGB(col, row));
							float norm_alpha = (float)windMatrix[row][col]/MAX_TIME;
							int new_alpha = (int)(norm_alpha*255);
							c = new Color(c.getRed(), c.getBlue(), c.getGreen(), (int)(norm_alpha*255));
							wind34Image.setRGB(col, row, c.getRGB());
						}
					}
				}*/
				
				/*// set the cells in the matrix using the tempImage
				int black_rgb = Color.black.getRGB();
				for (int row = 0; row < tempImage.getHeight(); row++) {
					for (int col = 0; col < tempImage.getWidth(); col++) {
						if (black_rgb != tempImage.getRGB(col, row)) {
							wind34Image.setRGB(col, row, tempImage.getRGB(col, row));
							windMatrix[row][col] = MAX_TIME;
						}
					}
				}*/
			}
		}
		
//		Graphics2D g2 = (Graphics2D)trackImage.getGraphics();
//		int x = longitudeToX(advisory.longitude, westBound, eastBound, backgroundImage.getWidth());
//		int y = latitudeToY(advisory.latitude, northBound, southBound, backgroundImage.getHeight());
//		
//		if (lastAdvisory != null) {
//			int x0 = longitudeToX(lastAdvisory.longitude, westBound, eastBound, backgroundImage.getWidth());
//			int y0 = latitudeToY(lastAdvisory.latitude, northBound, southBound, backgroundImage.getHeight());
//			
//			g2.setColor(Color.cyan);
//			g2.setStroke(new BasicStroke(2.f));
//			g2.drawLine(x0, y0, x, y);			
//		} else {
//			g2.drawLine(x, y, x, y);
//			firstTrackPoint = false;
//		}
//		
		repaint();
		
		lastAdvisory = advisory;
	}
	
	public void nextAdvisory(ForecastAdvisory advisory, boolean newStorm) {
		if (stroke_mode == SMALL_BRUSH_MODE) {
			nextAdvisorySmallBrush(advisory, newStorm);
		} else if (stroke_mode == LONG_STROKE_MODE) {
			nextAdvisoryLongStroke(advisory, newStorm);
		}
	}
	
	public void nextAdvisorySmallBrush(ForecastAdvisory advisory, boolean newStorm) {
		if (newStorm) {
	//		lastAdvisory = null;
			firstTrackPoint = true;
		}

		int x = longitudeToX(advisory.longitude, westBound, eastBound, backgroundImage.getWidth());
		int y = latitudeToY(advisory.latitude, northBound, southBound, backgroundImage.getHeight());
		
		// begin Wind drawing stuff
		Graphics2D windG2 = null;
//		Graphics2D windG2 = (Graphics2D) windImage.getGraphics();
//		windG2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (advisory.windRadiiList != null) {
			Iterator areaIterator = advisory.windRadiiList.iterator();
			while (areaIterator.hasNext()) {
				AdvisoryArea area = (AdvisoryArea)areaIterator.next();
				
//				System.out.println("area.value.shortValue()="+area.value.shortValue());
				BufferedImage strokeImage = null;
				if (area.value.shortValue() == 34) {
					windG2 = (Graphics2D)wind34Image.getGraphics();
//					windG2.setColor(new Color(130, 130, 130));
//					windG2.setColor(new Color(99, 99, 99));
//					windG2.setColor(new Color(250, 0, 20));
//					windG2.setColor(new Color(250, 250, 20));  // yellow
//					windG2.setColor(new Color(252, 153, 154)); // red with low saturation
					windG2.setColor(new Color(c34.getRed(), c34.getGreen(), c34.getBlue(), shortBrushAlpha));
					strokeImage = wind34Stroke;
//					Polygon p = convertAdvisoryAreaToPolygon(advisory.latitude, advisory.longitude, area.ne, area.se, area.sw, area.nw);
//					windG2.draw(p);
				} else if (area.value.shortValue() == 50) {
					windG2 = (Graphics2D)wind50Image.getGraphics();
//					windG2.setColor(new Color(180, 180, 180));
//					windG2.setColor(new Color(60, 160, 20));
//					windG2.setColor(new Color(200, 30, 100));
//					windG2.setColor(new Color(250, 153, 20)); // orange
//					windG2.setColor(new Color(250, 92, 89)); // red with mid saturation
//					windG2.setColor(c50);
					windG2.setColor(new Color(c50.getRed(), c50.getGreen(), c50.getBlue(), shortBrushAlpha));

					strokeImage = wind50Stroke;
//					continue;
				} else if (area.value.shortValue() == 64) {
					windG2 = (Graphics2D)wind64Image.getGraphics();
//					windG2.setColor(new Color(220, 220, 220));
//					windG2.setColor(new Color(250, 250, 20));
//					windG2.setColor(new Color(255, 0, 156));
//					windG2.setColor(new Color(250, 0, 20)); // red
//					windG2.setColor(new Color(249, 10, 18)); // red with high saturation
//					windG2.setColor(c64);
					windG2.setColor(new Color(c64.getRed(), c64.getGreen(), c64.getBlue(), shortBrushAlpha));

					strokeImage = wind64Stroke;
//					continue;
				}
				
				if (!firstTrackPoint) {
					// find the wind area from last advisory
					AdvisoryArea lastArea = null;
					for (Iterator lastAreaIterator = lastAdvisory.windRadiiList.iterator(); lastAreaIterator.hasNext();) {
						AdvisoryArea currentArea = (AdvisoryArea)lastAreaIterator.next();
						if (currentArea.value.shortValue() == area.value.shortValue()) {
							lastArea = currentArea;
							break;
						}
					}
					
					if (lastArea != null) {
						drawWindSwaths(windG2, strokeImage, lastAdvisory, advisory, lastArea, area);
					} else {
						
						int spacing_factor = 4;
						if (area.value.shortValue() == (short)34) {
							spacing_factor = spacing_factor_34knots/*12*/;
						} else if (area.value.shortValue() == (short)50) {
							spacing_factor = spacing_factor_50knots/*8*/;
						} else if (area.value.shortValue() == (short)64) {
							spacing_factor = spacing_factor_64knots/*4*/;
						}
						
						Polygon p = convertAdvisoryAreaToPolygon(advisory.latitude, advisory.longitude, area.ne, area.se, area.sw, area.nw);
						drawStrokes(windG2, strokeImage, p, stroke_width, stroke_height, stroke_width*spacing_factor, stroke_height*spacing_factor, advisory.movement_direction*ForecastAdvisoryReader.DEG2RAD);
//						windG2.draw(p);
//						windG2.fill(p);
					}
				} else {
//						drawTrack(windG2, x, y, x, y);
				}
			}
		}
		// end Wind drawing stuff
		
		// begin Track drawing stuff
		Graphics2D g2 = (Graphics2D) trackImage.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (!firstTrackPoint) {			
			g2.setColor(this.trackColor);
			g2.setStroke(new BasicStroke(2.f));
//			g2.setStroke(new SloppyStroke(2.0f, 3.0f));
//			drawTrack(g2, lastAdvisory, advisory);
			g2.drawLine(lastPointX, lastPointY, x, y);
			
		} else {
			firstTrackPoint = false;
		}
		
		// end Track drawing stuff.
		
		
		repaint();
		
		lastPointX = x;
		lastPointY = y;
		lastAdvisory = advisory;
	}
	
	public void drawAdvisories(ArrayList advisories) {
		int lastX = 0, lastY = 0;
		Path2D.Float track = new Path2D.Float();
		boolean firstPoint = true;

		Ellipse2D.Float ellipse_array[] = {new Ellipse2D.Float(0, 0, 4, 4), new Ellipse2D.Float(0, 0, 10, 10), new Ellipse2D.Float(0, 0, 20, 20), new Ellipse2D.Float(0, 0, 30, 30)};
		
		Graphics2D g2 = (Graphics2D) trackImage.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setColor(new Color(0, 0, 200, 100));
		
		Iterator advisoryIter = advisories.iterator();
		while (advisoryIter.hasNext()) {
			ForecastAdvisory advisory = (ForecastAdvisory)advisoryIter.next();
			
			int x = longitudeToX(advisory.longitude, westBound, eastBound, backgroundImage.getWidth());
			int y = latitudeToY(advisory.latitude, northBound, southBound, backgroundImage.getHeight());
			
			if (firstPoint) {
//				track.moveTo(x, y);
				firstPoint = false;
			} else {
				g2.setStroke(new ShapeStroke(ellipse_array, 10.f));
				g2.drawLine(lastX, lastY, x, y);
//				track.lineTo(x, y);
			}
			lastX = x;
			lastY = y;
		}
		
//		Ellipse2D.Float ellipse = new Ellipse2D.Float(0, 0, 4, 4);
//		g2.setStroke(new ShapeStroke(ellipse, 5.f));
//		g2.setStroke(new WobbleStroke(20.f, 2.f));
//		g2.draw(track);
		repaint();
	}
	
	public void paint(Graphics g) {
		Graphics2D g2 = (Graphics2D)g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
//		g2.setColor(Color.white);
//		g2.fillRect(0, 0, getWidth(), getHeight());
		g2.setColor(this.getBackground());
		g2.fillRect(0, 0, getWidth(), getHeight());
		
		if (backgroundImage != null) {
			g2.drawImage(backgroundImage, 0, 0, backgroundImage.getWidth(), backgroundImage.getHeight(), null);

			g2.drawImage(wind34Image, 0, 0, wind34Image.getWidth(), wind34Image.getHeight(), null);
			g2.drawImage(wind50Image, 0, 0, wind50Image.getWidth(), wind50Image.getHeight(), null);
			g2.drawImage(wind64Image, 0, 0, wind64Image.getWidth(), wind64Image.getHeight(), null);
			
			if (showTrack) {
				g2.drawImage(trackImage, 0, 0, trackImage.getWidth(), trackImage.getHeight(), null);
			}
//			g2.drawImage(strokeImage, 0, 0, strokeImage.getWidth(), strokeImage.getHeight(), null);
		}
	}


	
class SloppyStroke implements Stroke {
	  BasicStroke stroke;

	  float sloppiness;

	  public SloppyStroke(float width, float sloppiness) {
	    this.stroke = new BasicStroke(width); // Used to stroke modified shape
	    this.sloppiness = sloppiness; // How sloppy should we be?
	  }

	  public Shape createStrokedShape(Shape shape) {
	    GeneralPath newshape = new GeneralPath(); // Start with an empty shape

	    // Iterate through the specified shape, perturb its coordinates, and
	    // use them to build up the new shape.
	    float[] coords = new float[6];
	    for (PathIterator i = shape.getPathIterator(null); !i.isDone(); i
	        .next()) {
	      int type = i.currentSegment(coords);
	      switch (type) {
	      case PathIterator.SEG_MOVETO:
	        perturb(coords, 2);
	        newshape.moveTo(coords[0], coords[1]);
	        break;
	      case PathIterator.SEG_LINETO:
	        perturb(coords, 2);
	        newshape.lineTo(coords[0], coords[1]);
	        break;
	      case PathIterator.SEG_QUADTO:
	        perturb(coords, 4);
	        newshape.quadTo(coords[0], coords[1], coords[2], coords[3]);
	        break;
	      case PathIterator.SEG_CUBICTO:
	        perturb(coords, 6);
	        newshape.curveTo(coords[0], coords[1], coords[2], coords[3],
	            coords[4], coords[5]);
	        break;
	      case PathIterator.SEG_CLOSE:
	        newshape.closePath();
	        break;
	      }
	    }

	    // Finally, stroke the perturbed shape and return the result
	    return stroke.createStrokedShape(newshape);
	  }

	  // Randomly modify the specified number of coordinates, by an amount
	  // specified by the sloppiness field.
	  void perturb(float[] coords, int numCoords) {
	    for (int i = 0; i < numCoords; i++)
	      coords[i] += (float) ((Math.random() * 2 - 1.0) * sloppiness);
	  }
	}
}