import javax.swing.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.*;

public class ConvexHullTest {

	public static void main(String[] args) {
		JFrame f = new JFrame("ConvexHullTest");
		JPanel mainPanel = new JPanel() {
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
				
				ArrayList points = new ArrayList();
				points.add(new Point2D.Float(10, 20));
				points.add(new Point2D.Float(20, 20));
				points.add(new Point2D.Float(10, 30));
				points.add(new Point2D.Float(20, 30));
				points.add(new Point2D.Float(50, 50));
				points.add(new Point2D.Float(70, 50));
				points.add(new Point2D.Float(50, 100));
				points.add(new Point2D.Float(70, 100));
				
				// determine convex hull of the points
				org.bluear.cg.hull.GrahamScanHull hullAlg = new org.bluear.cg.hull.GrahamScanHull();
				ArrayList hull = (ArrayList)hullAlg.getHullPoints(points);
				//ArrayList hull = getConvexHull(points);
				
				// draw convex hull
				g2.setColor(Color.blue);
				Polygon p = new Polygon();
				Iterator iter = hull.iterator();
				while (iter.hasNext()) {
					Point2D.Float point = (Point2D.Float)iter.next();
					p.addPoint((int)point.x, (int)point.y);
//					Rectangle2D.Float rect = new Rectangle2D.Float(point.x-2, point.y-2, 4, 4);
//					g2.draw(rect);
				}
				g2.draw(p);
				
				// draw points for verification
				g2.setColor(Color.black);
				iter = points.iterator();
				while (iter.hasNext()) {
					Point2D.Float point = (Point2D.Float)iter.next();
					Rectangle2D.Float rect = new Rectangle2D.Float(point.x-2, point.y-2, 4, 4);
					g2.draw(rect);
				}
			}
		};
		
		mainPanel.setPreferredSize(new Dimension(600, 450));
		f.getContentPane().add(mainPanel);
		f.pack();
		f.setVisible(true);
	}
}
