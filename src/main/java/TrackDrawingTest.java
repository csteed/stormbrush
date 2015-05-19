
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.*;
import java.awt.image.*;
import javax.imageio.ImageIO;

public class TrackDrawingTest {
	public static final Random r = new Random();
	public static Polygon p = new Polygon();
	public static BufferedImage strokeImage;
	
	public static void main(String[] args) throws Exception {
		File file = new File("images/34_icon_stroke.png");
		strokeImage = ImageIO.read(file);	
		
/*		int black_rgb = Color.black.getRGB();
		int wind34_rgb = (new Color(0, 0, 255)).getRGB();
		for (int i = 0; i < strokeImage.getHeight(); i++) {
			for (int j = 0; j < strokeImage.getWidth(); j++) {
				Color c = new Color(strokeImage.getRGB(j, i));
//				if (c.getAlpha() == 255) {
				if (c.getRGB() == black_rgb) {
//					System.out.println("setting stroke pixel old value is " + strokeImage.getRGB(j, i));
					strokeImage.setRGB(j, i, wind34_rgb);
//					System.out.println(" new pixel rgb is " + strokeImage.getRGB(j, i) + " wind rgb is " + wind34_rgb);
				}
			}
		}*/
		
		p.addPoint(128, 14);
		p.addPoint(171, 22);
		p.addPoint(208, 44);
		p.addPoint(231, 78);
		p.addPoint(239, 116);
		p.addPoint(227, 116);
		p.addPoint(219, 151);
		p.addPoint(197, 180);
		p.addPoint(165, 199);
		p.addPoint(128, 206);
		p.addPoint(128, 231);
		p.addPoint(80, 223);
		p.addPoint(39, 198);
		p.addPoint(11, 161);
		p.addPoint(0, 117);
		p.addPoint(0, 117);
		p.addPoint(9, 72);
		p.addPoint(36, 34);
		p.addPoint(78, 9);
		p.addPoint(128, 0);
		
		JFrame f = new JFrame("TrackDrawingTest");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JPanel mainPanel = new JPanel() {
			public void paint(Graphics g) {		
//				System.err.println("in paint().");
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

//				Color background = new Color(234, 234, 234);
				g2.setColor(Color.white);
				g2.fillRect(0, 0, getWidth(), getHeight());

				//int x1 = 20, y1 = 30, x2 = getWidth()-60, y2 = 30;
				int x1 = 20, y1 = 30, x2 = 500, y2 = 30;
				int area0[] = {10, 10, 20, 20};
				int area1[] = {25, 25, 5, 5};
				
				int x3 = getWidth()/2;
//				g2.setColor(Color.blue);
				//g2.setColor(new Color(250, 0, 20));
				g2.setColor(CAPanel.c34);
				g2.setStroke(new BasicStroke(1.f));
				//drawLine(g2, x3, y1, 230, 10, x3, y2, 50, 30);
				//drawLine(g2, x3, y1, 300, 10, x3, y2, 50, 50);
			
			
				int width = 150;
				int height = 180;
				double orientation = 130. * (Math.PI/180.);
				Ellipse2D.Float ellipse = new Ellipse2D.Float(getWidth()/2-(width/2), getHeight()/2-(height/2), width, height);
				
				g2.translate(getWidth()/4, getHeight()/4);
				
				g2.setColor(Color.black);
				g2.setStroke(new BasicStroke(2.f));
				//g2.draw(p);
				
				//g2.setColor(new Color(250, 0, 20));
				g2.setColor(new Color(CAPanel.c34.getRed(), CAPanel.c34.getGreen(), CAPanel.c34.getBlue(), CAPanel.shortBrushAlpha));

				g2.setStroke(new BasicStroke(1.f));
				//drawStrokes(g2, p, 13, 8, 13, 8, -0.5);
				
//				g2.draw(ellipse);
				
//				g2.setColor(Color.green);
				drawLine(g2, x1, y1, 230, 70, x2, y2, 50, 10);
				
				g2.setColor(Color.black);
				Line2D.Float line = new Line2D.Float(x1, y1, x2, y2);
//				g2.draw(line);
//				g2.drawLine(x1, y1, x2, y2);
				
			}
		};
		
		mainPanel.setPreferredSize(new Dimension(600, 450));
		f.getContentPane().add(mainPanel);
		f.pack();
		f.setVisible(true);
	}
	
	public static void drawLine2(Graphics2D g2, int x0, int y0, int intensity0, int size0, int x1, int y1, int intensity1, int size1) {
		int xStart = x0, yStart = y0, xEnd = x1, yEnd = y1;
		int intensityStart = intensity0, intensityEnd = intensity1;
		
//		g2.drawString("p0", x0+10, y0+5);
//		g2.drawString("p1", x1+10, y1+5);
		
		boolean steep = Math.abs(yEnd - yStart) > Math.abs(xEnd - xStart);
		if (steep) {
			xStart = y0;
			yStart = x0;
			xEnd = y1;
			yEnd = x1;
		}
		
		if (xStart > xEnd) {
			int tmp = xStart;
			xStart = xEnd;
			xEnd = tmp;
			tmp = yStart;
			yStart = yEnd;
			yEnd = tmp;
			tmp = intensityStart;
			intensityStart = intensityEnd;
			intensityEnd = tmp;
		}
		
		/*
		float deltaIntensity = (float)(intensityEnd - intensityStart)/(float)Math.abs(xEnd-xStart);
		float intensity = intensityStart;
		if (intensityStart > intensityEnd)
			deltaIntensity = -deltaIntensity;
		*/
		/*
		int deltaIntensity = Math.abs(intensityEnd - intensityStart);
		int intensity = intensityStart;
		int intensityStep;
		if (intensityStart < intensityEnd) 
			intensityStep = 1;
		else
			intensityStep = -1;
		*/
		int deltax = xEnd - xStart;
		int deltay = Math.abs(yEnd - yStart);
		int error = -deltax/2;
		int ystep;
		int y = yStart;
		
		if (yStart < yEnd)
			ystep = 1;
		else 
			ystep = -1;
		for (int x = xStart; x <= xEnd; x++) {
//			g2.setColor(new Color((int)(intensity+0.5f), (int)(intensity+0.5f), (int)(intensity+0.5f)));
			if (steep) {
//				Ellipse2D.Float ellipse = new Ellipse2D.Float(y-2, x-2, 4, 4);
//				g2.fill(ellipse);
				g2.drawLine(y, x, y, x);
			} else {
//				Ellipse2D.Float ellipse = new Ellipse2D.Float(x-2, y-2, 4, 4);
//				g2.fill(ellipse);
				g2.drawLine(x, y, x, y);
			}
//			intensity = intensity + deltaIntensity;
			
			error = error + deltay;
			if (error > 0) {
				y = y + ystep;
//				intensity = intensity + intensityStep;
//				System.err.println("intensity=" + intensity + " intensityStart="+intensityStart+" intensityEnd="+intensityEnd);
//				System.err.println("y="+y+" yStart="+yStart+" yEnd="+yEnd);
				error = error - deltax;
			}
		}
	}
	
	public static void drawLine3(Graphics2D g2, int x0, int y0, int area0[], int x1, int y1, int area1[]) {
		int xstart = x0, xend = x1, ystart = y0, yend = y1;
		int areaStart[] = area0, areaEnd[] = area1;
		
		if (x0 > x1) {
			xstart = x1;
			xend = x0;
			ystart = y1;
			yend = y0;
			areaStart = area1;
			areaEnd = area0;
		}
		
		float dx = (float)(xend - xstart);
		float dy = (float)(yend - ystart)/dx;
		float darea[] = new float[areaStart.length];
		for (int i = 0; i < areaStart.length; i++) {
			darea[i] = (float)(areaEnd[i]-areaStart[i]) / dx;
		}
//		float dsz = (float)(sz1 - sz0)/(float)(xend - xstart);
//		float di = (float)(i1 - i0)/(float)(xend - xstart);
		
		float y = ystart;
		float d[] = new float[areaStart.length];
		for (int i = 0; i < areaStart.length; i++) {
			d[i] = areaStart[i];
		}
//		float i = i0;
//		float sz = sz0;
		
		
		if (x0 < x1) {
			for (int x = xstart; x <= xend; x++) {
//				int ii = (int)(i+0.5f);
				int iy = (int)(y+0.5f);
//				int isz = (int)(sz + 0.5f);
				
				g2.drawLine(x, iy, x, iy);
				
				Polygon p = new Polygon();
//				p.addPoint(arg0, arg1)
				
				y = y + dy;
//				i = i + di;
//				sz = sz + dsz;
			}
		} else {
			for (int x = xend; x >= xstart; x--) {
//				int ii = (int)(i+0.5f);
				int iy = (int)(y+0.5f);
//				int isz = (int)(sz + 0.5f);
				g2.drawLine(x, iy, x, iy);
				
				y = y + dy;
//				i = i + di;
//				sz = sz + dsz;
			}
		}
	}
	
	public static void drawStrokes(Graphics2D g2, Shape shape, int stroke_width, int stroke_height, int stroke_x_spacing, int stroke_y_spacing, double orientation) {
		Rectangle bounds = shape.getBounds();
		
		Color c = g2.getColor();
		
		int cols = (int)(0.5f + (float)bounds.getWidth()/(float)(stroke_x_spacing));
		int rows = (int)(0.5f + (float)bounds.getHeight()/(float)(stroke_y_spacing));
//		System.out.println("cols = " + cols + " rows = " + rows);
		
//		System.out.println("stroke_x_spacing = " + stroke_x_spacing + " stroke_y_spacing = " + stroke_y_spacing);
		
		stroke_x_spacing = (int)(0.5f + (float)bounds.getWidth()/(float)cols);
		stroke_y_spacing = (int)(0.5f + (float)bounds.getHeight()/(float)rows);
//		System.out.println("stroke_x_spacing = " + stroke_x_spacing + " stroke_y_spacing = " + stroke_y_spacing);
		
//		System.out.println("bounds.getWidth()/cols = " + (bounds.getWidth()/cols));
		
		
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
/*		
		int iminx = (int)bounds.getMinX();
		int imaxx = (int)bounds.getMaxX();
		int iminy = (int)bounds.getMinY();
		int imaxy = (int)bounds.getMaxY();
//		System.out.println("bounds.getWidth() = " + bounds.getWidth() + " bounds.getHeight() = " + bounds.getHeight());
//		System.out.println("iminx = " + iminx + " imaxx = " + imaxx + " iminy = " + iminy + " imaxy = " + imaxy);
		g2.setColor(Color.black);
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
				int innerStrokeWidth = stroke_width/2;
				int innerStrokeHeight = stroke_height/2;
				Rectangle2D.Float innerStroke = new Rectangle2D.Float(col-innerStrokeWidth/2, row-innerStrokeHeight/2, innerStrokeWidth, innerStrokeHeight);
				g2.setColor(c.brighter());
				g2.fill(innerStroke);
//				g2.drawImage(strokeImage, (int)stroke.getMinX(), (int)stroke.getMinY(), (int)stroke.getWidth(), (int)stroke.getHeight(), this);
//				g2.setColor(Color.darkGray);
				//g2.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue()));
//				g2.draw(stroke);
				g2.rotate(-orientation, col, row);

//				g2.rotate(orientation, col, row);
//				Rectangle2D.Float stroke = new Rectangle2D.Float(col-stroke_width/2, row-stroke_height/2, stroke_width, stroke_height);
////				g2.drawImage(strokeImage, (int)stroke.getMinX(), (int)stroke.getMinY(), (int)stroke.getWidth(), (int)stroke.getHeight(), null);
//
//				g2.setColor(c);
//				g2.fill(stroke);
//				g2.setColor(Color.black);
//				//g2.draw(stroke);
//				g2.rotate(-orientation, col, row);
			}
		}
		/*
		for (int row = (int)bounds.getMinY() + stroke_height/2+gap/2; row < bounds.getMaxY(); row+=stroke_height+gap){
			for (int col = (int)bounds.getMinX() + stroke_width/2+gap/2; col < bounds.getMaxX(); col+=stroke_width+gap){
				if (shape.contains(col, row)) {
					g2.rotate(orientation, col, row);
					Rectangle2D.Float stroke = new Rectangle2D.Float(col-stroke_width/2, row-stroke_height/2, stroke_width, stroke_height);
					g2.setColor(c);
					g2.fill(stroke);
					g2.setColor(Color.black);
					g2.draw(stroke);
					g2.rotate(-orientation, col, row);
				}
			}
		}
		*/		
	}
	
	public static void drawLine(Graphics2D g2, int x0, int y0, int i0, int sz0, int x1, int y1, int i1, int sz1) {

		boolean steep = Math.abs(y1 - y0) > Math.abs(x1 - x0);
		if (steep) {
			int tmp = x0;
			x0 = y0;
			y0 = tmp;
			tmp = x1;
			x1 = y1;
			y1 = tmp;
		}
		
		if (x0 > x1) {
			int tmp = x0;
			x0 = x1;
			x1 = tmp;
			tmp = y0;
			y0 = y1;
			y1 = tmp;
			tmp = i0;
			i0 = i1;
			i1 = tmp;
			tmp = sz0;
			sz0 = sz1;
			sz1 = tmp;
		}
		
		float dy = (float)(y1 - y0)/(float)(x1 - x0);
		float dsz = (float)(sz1 - sz0)/(float)(x1 - x0);
		float di = (float)(i1 - i0)/(float)(x1 - x0);
		
		float y = y0;
		float i = i0;
		float sz = sz0;
		
		
		if (x0 < x1) {
			for (int x = x0; x <= x1; x++) {
				int ii = (int)(i+0.5f);
				int iy = (int)(y+0.5f);
				int isz = (int)(sz + 0.5f);
				
//				g2.setColor(new Color(ii, ii, ii));
//				g2.setColor(new Color(250, 0, 20));
				g2.setColor(new Color(CAPanel.c34.getRed(), CAPanel.c34.getGreen(), CAPanel.c34.getBlue(), CAPanel.shortBrushAlphaMax));
				
				Ellipse2D.Float ellipse = new Ellipse2D.Float(x-isz, iy-isz, isz*2, isz*2);
				if (steep) {
					ellipse = new Ellipse2D.Float(iy-isz, x-isz, isz*2, isz*2);
//					g2.drawLine(y, x, y, x);
				}

//				g2.setColor(Color.cyan);
//				g2.draw(ellipse);
				
				drawStrokes(g2, ellipse, 8, 12, 8*4, 12*4, 1.2);
				

//				g2.setColor(Color.cyan);
//				g2.draw(ellipse);
//				g2.fill(ellipse);
	//			g2.drawLine(x, (int)(y+0.5f), x, (int)(y+0.5f));
				
				y = y + dy;
				i = i + di;
				sz = sz + dsz;
			}
		} else {
			for (int x = x1; x >= x0; x--) {
				int ii = (int)(i+0.5f);
				int iy = (int)(y+0.5f);
				int isz = (int)(sz + 0.5f);
				
//				g2.setColor(new Color(ii, ii, ii));
//				g2.setColor(new Color(250, 0, 20));
				g2.setColor(new Color(CAPanel.c34.getRed(), CAPanel.c34.getGreen(), CAPanel.c34.getBlue(), CAPanel.shortBrushAlphaMax));
				
				
				Ellipse2D.Float ellipse = new Ellipse2D.Float(x-isz, iy-isz, isz*2, isz*2);
				if (steep) {
					ellipse = new Ellipse2D.Float(iy-isz, x-isz, isz*2, isz*2);
//					g2.drawLine(y, x, y, x);
				}
//				Ellipse2D.Float ellipse = new Ellipse2D.Float(x-isz, iy-isz, isz*2, isz*2);
				
				drawStrokes(g2, ellipse, 8, 12, 8*4, 12*4, 1.2);
				
//				g2.setColor(Color.cyan);
//				g2.draw(ellipse);
//				g2.fill(ellipse);
	//			g2.drawLine(x, (int)(y+0.5f), x, (int)(y+0.5f));
				
				y = y + dy;
				i = i + di;
				sz = sz + dsz;
			}
		}
/*		for (int x = x0; x <= xEnd; x++) {
			float t = (float)(x - x0) / (float)(xEnd - x0);
			float y = (float)y0 + t * (float)(yEnd - y0);
			g2.drawLine(x, (int)(y+0.5f), x, (int)(y+0.5f));
		}*/
	}
}

