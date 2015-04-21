import java.awt.geom.CubicCurve2D;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.Stroke;
import java.awt.geom.Rectangle2D;
import java.awt.geom.Line2D;
import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Arc2D;
import java.awt.Color;
import java.awt.BasicStroke;
import java.awt.Shape;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.UIManager;
import java.awt.Graphics2D;
import java.awt.Graphics;
 
public class SymbolStroke implements Stroke {
  protected BasicStroke _basicStroke = null;
  protected BasicStroke _symbolStroke = null;
  protected Shape _symbol = null;
  protected float _symbolSeparation = 0.0F;
  protected boolean _startWithSymbol = false;
  protected boolean _drawBasicLine = true;
  protected boolean _symbolFillOnly = false;
 
  public SymbolStroke()
  {
    _basicStroke = new BasicStroke();
    _symbolStroke = new BasicStroke();
  }
 
  public SymbolStroke(BasicStroke basicStroke)
  {
    _basicStroke = basicStroke;
  }
 
  public SymbolStroke(Shape symbol,float symbolSeparation,boolean startWithSymbol,boolean drawBasicLine,boolean symbolFillOnly)
  {
    this();
    _symbol = symbol;
    _symbolSeparation = symbolSeparation;
    if(_symbolSeparation <= 0.0F)
      throw new IllegalArgumentException("symbolSeparation must be greater than 0.0");
    _startWithSymbol = startWithSymbol;
    _drawBasicLine = drawBasicLine;
    _symbolFillOnly = symbolFillOnly;
  }
 
  public SymbolStroke(BasicStroke basicStroke,BasicStroke symbolStroke,Shape symbol,float symbolSeparation,boolean startWithSymbol,boolean drawBasicLine,boolean symbolFillOnly)
  {
    this(symbol,symbolSeparation,startWithSymbol,drawBasicLine,symbolFillOnly);
    _basicStroke = basicStroke;
    if(_basicStroke == null)
      throw new IllegalArgumentException("basicStroke cannot be null");
    _symbolStroke = symbolStroke;
    if(_symbolStroke == null)
      throw new IllegalArgumentException("symbolStroke cannot be null");
  }
 
  public Shape createStrokedShape(Shape s)
  {
    Shape basicShape = _basicStroke.createStrokedShape(s);
    if(_symbol == null)
      return(basicShape);
    GeneralPath retVal = null;
    if(_drawBasicLine)
      retVal = new GeneralPath(basicShape);
    else
      retVal = new GeneralPath();
    PathIterator pi = s.getPathIterator(null,1.0);
    float pt[] = new float[6];
    Point2D.Float prevPt = null;
    Point2D.Float currPt = null;
    float prevDist = 0.0F;
    float currDist = 0.0F;
    boolean newSegment = false;
    while(!pi.isDone()) {
      int type = pi.currentSegment(pt);
      switch(type) {
        case PathIterator.SEG_MOVETO:
          prevPt = currPt;
          currPt = new Point2D.Float(pt[0],pt[1]);
          prevDist = 0.0F;
          currDist = 0.0F;
          newSegment = true;
          break;
        case PathIterator.SEG_LINETO:
          prevPt = currPt;
          currPt = new Point2D.Float(pt[0],pt[1]);
          if(prevPt == null)
            break;
          prevDist = currDist;
          currDist += prevPt.distance(currPt);
          while(currDist >= _symbolSeparation) {
            float radius = _symbolSeparation - prevDist;
            Point2D.Float center = prevPt;
            Line2D.Float line = new Line2D.Float(prevPt,currPt);
            Point2D.Float intersect = intersectLineAndCircle(center,radius,line);
            if((newSegment == true) && (_startWithSymbol == true)) {
              Shape symbolShape = getStrokedSymbolShape(line,prevPt);
              retVal.append(symbolShape,false);
              newSegment = false;
            }
            if(intersect == null) {
              //Not sure why intersection failed, but clean up anyway
              prevDist = 0.0F;
              currDist = 0.0F;
              break;
            } else {
              Shape symbolShape = getStrokedSymbolShape(line,intersect);
              retVal.append(symbolShape,false);
            }
            prevDist = 0.0F;
            prevPt = intersect;
            currDist -= _symbolSeparation;
          }
          break;
        case PathIterator.SEG_CLOSE:
          break;
      }
      pi.next();
    }
    return(retVal);
  }
 
  protected Shape getStrokedSymbolShape(Line2D.Float line,Point2D.Float intersect)
  {
    //Find proper angle to draw symbol, relative to North, not East as usual
    float rotation = (float)Math.atan2(line.x1 - line.x2,line.y2 - line.y1) - ((float)Math.PI / 2.0F);
    //Transform shape to be anchored at intersection point
    AffineTransform rt = AffineTransform.getRotateInstance(rotation);
    Shape rotatedSymbol = rt.createTransformedShape(_symbol);
    AffineTransform tt = AffineTransform.getTranslateInstance(intersect.x,intersect.y);
    Shape transformedSymbol = tt.createTransformedShape(rotatedSymbol);
    Shape symbolShape = _symbolStroke.createStrokedShape(transformedSymbol);
    if(_symbolFillOnly == true)
      return(transformedSymbol);
    else
      return(symbolShape);
  }
 
  //Based on Theorem 1.4 of http://www.sonoma.edu/users/w/wilsonst/Papers/Geometry/lines/default.html
  //Based on Theorem 3.2 of http://www.sonoma.edu/users/w/wilsonst/Papers/Geometry/circles/default.html
  //Based on Theorem 3.3 of http://www.sonoma.edu/users/w/wilsonst/Papers/Geometry/circles/default.html
  protected Point2D.Float intersectLineAndCircle(Point2D.Float c,float r,Line2D.Float l)
  {
    Point2D.Float retVal = null;
    float xPos,yPos,xNeg,yNeg;
 
    float m = (l.y2 - l.y1) / (l.x2 - l.x1);
    //Check for vertical line
    if(Float.isInfinite(m)) {
      float a = l.x1;
      xPos = a;
      xNeg = a;
      float aMinusXSquared = (a - c.x) * (a - c.x);
      float yRight = (float)Math.sqrt((r * r) - aMinusXSquared);
      if(Float.isNaN(yRight))
        return(null);
      yPos = c.y + yRight;
      yNeg = c.y - yRight;
    } else {
      float b = ((l.x2 * l.y1) - (l.x1 * l.y2)) / (l.x2 - l.x1);
      float onePlusMSquared = 1.0F + (m * m);
      float xLeft = ((m * c.y) + c.x - (m * b)) / (onePlusMSquared);
      float xRightTopTop = c.y - (m * c.x) - b;
      float xRightTopBottom = (float)Math.sqrt(onePlusMSquared);
      if(Float.isNaN(xRightTopBottom))
        return(null);
      float xRightTop = (float)Math.sqrt((r * r) - ((xRightTopTop / xRightTopBottom) * (xRightTopTop / xRightTopBottom)));
      if(Float.isNaN(xRightTop))
        return(null);
      float xRight = xRightTop / (float)Math.sqrt(onePlusMSquared);
      if((Float.isNaN(xRight)) || (Float.isInfinite(xRight)))
        return(null);
      xPos = xLeft + xRight;
      xNeg = xLeft - xRight;
      float yLeft = ((m * m * c.y) + (m * c.x) + b) / (onePlusMSquared);
      float yRight = m * xRight;
      yPos = yLeft + yRight;
      yNeg = yLeft - yRight;
    }
    if((xPos >= Math.min(l.x1,l.x2)) && (xPos <= Math.max(l.x1,l.x2)) &&
       (yPos >= Math.min(l.y1,l.y2)) && (yPos <= Math.max(l.y1,l.y2)))
      retVal = new Point2D.Float(xPos,yPos);
    else if((xNeg >= Math.min(l.x1,l.x2)) && (xNeg <= Math.max(l.x1,l.x2)) &&
            (yNeg >= Math.min(l.y1,l.y2)) && (yNeg <= Math.max(l.y1,l.y2)))
      retVal = new Point2D.Float(xNeg,yNeg);
    return(retVal);
  }
 
  public static void main(String args[])
  {
    try {
      UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
    } catch(Exception ex) {
    }
    JFrame f = new JFrame("SymbolStroke Demo");
    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JPanel mainPanel = new JPanel() {
      public void paintComponent(Graphics g)
      {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;
        Line2D.Float l = new Line2D.Float(0.0F,150.0F,300.0F,150.0F);
        Arc2D.Float a = new Arc2D.Float(10.0F,10.0F,310.0F,310.0F,0.0F,360.0F,Arc2D.OPEN);
        CubicCurve2D.Float c = new CubicCurve2D.Float(0.0F,150.0F,400.0F,-50.0F,-100.0F,350.0F,300.0F,150.0F);
        SymbolStroke stroke = new SymbolStroke(new BasicStroke(1.0F),new BasicStroke(1.5F),new Rectangle2D.Float(-4.0F,-4.0F,8.0F,8.0F),5.0F,false,false,true);
//        SymbolStroke stroke = new SymbolStroke(new BasicStroke(2.0F),new BasicStroke(2.0F),new Line2D.Float(0.0F,0.0F,0.0F,-4.0F),30.0F,false,true);
//        SymbolStroke stroke = new SymbolStroke(new BasicStroke(1.0F),new BasicStroke(2.0F),new CubicCurve2D.Float(-15.0F,0.0F,0.0F,15.0F,0.0F,-15.0F,15.0F,0.0F),30.0F,true,false);
        g2d.setStroke(stroke);
        g2d.draw(c);
      }
    };
    mainPanel.setPreferredSize(new Dimension(400,400));
    f.getContentPane().add(mainPanel);
    f.pack();
    f.setVisible(true);
  }
}
