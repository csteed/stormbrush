import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;

public class StrokeTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame f = new JFrame("StrokeTest");
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel mainPanel = new JPanel() {
			public void paint(Graphics g) {
				Graphics2D g2 = (Graphics2D)g;
				g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

				int x1 = getWidth()-10;
				int y1 = getHeight()-10;
				int x2 = getWidth()/3;
				int y2 = 2*getHeight()/3;
				int x3 = 20;
				int y3 = 10;
				
				
				g2.setColor(Color.blue);
				
				// first segment
//				Stroke stroke = new HurricaneStroke(20.f, 2.f);
				Ellipse2D.Float ellipses[] = {new Ellipse2D.Float(0, 0, 4, 4), new Ellipse2D.Float(0, 0, 10, 10), new Ellipse2D.Float(0, 0, 20, 20), new Ellipse2D.Float(0, 0, 30, 30)};
				Stroke stroke = new ShapeStroke(ellipses, 1.f);
				stroke = new BasicStroke(6.f);
				g2.setStroke(stroke);
				g2.drawLine(x1, y1, x2, y2);
				
				// second segment
				stroke = new BasicStroke(10.f);
				g2.setStroke(stroke);
				g2.drawLine(x2, y2, x3, y3);
				
				
				
				g2.setColor(Color.black);
				
//				 first segment
				g2.setStroke(new BasicStroke(2.f));
				g2.drawLine(x1, y1, x2, y2);
				
				// second segment
//				g2.drawLine(x2, y2, x3, y3);
				
				// whatever
				Arc2D.Float a = new Arc2D.Float(10.0F,10.0F,310.0F,310.0F,0.0F,360.0F,Arc2D.OPEN);
				Line2D.Float l = new Line2D.Float(0.0F,150.0F,300.0F,150.0F);
		        CubicCurve2D.Float c = new CubicCurve2D.Float(0.0F,150.0F,400.0F,-50.0F,-100.0F,350.0F,300.0F,150.0F);
		        
//				g2.setColor(Color.blue);
//				g2.setStroke(new WobbleStroke(2.f, 5.f));
//				g2.draw(l);
			}
		};
		
		mainPanel.setPreferredSize(new Dimension(600, 450));
		f.getContentPane().add(mainPanel);
		f.pack();
		f.setVisible(true);
	}

}
