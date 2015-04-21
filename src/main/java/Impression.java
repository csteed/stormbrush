/*
 *	impression -
 *		This is a paint program that implements an Impressionist
 *	painting technique based on image sampling.
 *
 *			    Paul Haeberli - 1995
 *			        paul@sgi.com
 *
 *	 	    This painting technique is covered by 
 *			 U.S patent Number 5,182,548
 *
 */
import java.awt.*;
import java.lang.*;
import java.net.*;
import java.awt.image.*;
import java.util.Random;
import java.io.PrintStream;

class MemoryImage implements ImageConsumer {
    boolean imgready;
    int imgxsize, imgysize;
    int imgpixels[][];

    MemoryImage(Image picture) {
	int ticks;

	picture.getSource().startProduction(this);
	ticks = 5*60*1000; /* 5 minutes */
	while(ticks>0) {
	    try {
		Thread.currentThread().sleep(100);
	    } catch (Exception e) { ; }
	    if(this.imgready)
		break;
	    ticks -= 100;
	}
    }
    public void setProperties(java.util.Hashtable dummy) {
    }
    public void setColorModel(ColorModel dummy) {
    }
    public void setHints(int dummy) {
    }
    public void imageComplete(int dummy) {
	imgready = true;
    }
    public void setDimensions(int x, int y) {
	imgxsize = x;
	imgysize = y;
	imgpixels = new int[y][x];
    }
    public void setPixels(int x1, int y1, int w, int h, 
	ColorModel model, byte pixels[], int off, int scansize) {
	int x, y, x2, y2, sx, sy;

	x2 = x1+w;
	y2 = y1+h;
	sy = off;
	for(y=y1; y<y2; y++) {
	    sx = sy;
	    for(x=x1; x<x2; x++) 
		imgpixels[y][x] = model.getRGB(pixels[sx++]);
	    sy += scansize;
	}
    }
    public void setPixels(int x1, int y1, int w, int h, 
	ColorModel model, int pixels[], int off, int scansize) {
	int x, y, x2, y2, sx, sy;

	x2 = x1+w;
	y2 = y1+h;
	sy = off;
	for(y=y1; y<y2; y++) {
	    sx = sy;
	    for(x=x1; x<x2; x++) 
		imgpixels[y][x] = model.getRGB(pixels[sx++]);
	    sy += scansize;
	}
    }
}

final public class Impression extends java.applet.Applet {
    Random r;
    Graphics canvasG, myG;
    Image canvasimage, picture, menu;
    int brushsize, paintmode;
    int bsize, bsize2;
    int jsize, jsize2;
    int lx, ly, brushtype;
    boolean lok;
    MemoryImage memimg;
    int wxsize, wysize;
    int cxsize, cysize;
    int dxsize, dysize;
    boolean dopaint;
    Font font;
    FontMetrics fm;
    Color border, canvas, text;
    int nstrokes[] = { 5, 5, 5, 5, 10 };
    int controlheight;
    int borderwidth;
    int brushsteps;

    public void init() {
	controlheight = 30;
	borderwidth = 1;
	brushsteps = 8;
	brushtype = 2;

	wxsize = size().width;
	wysize = size().height;
	cxsize = wxsize;
	cysize = wysize-controlheight;
	dxsize = cxsize-2*borderwidth;
	dysize = cysize-2*borderwidth;

	resize(wxsize,wysize);

	canvasimage = createImage(dxsize,dysize);
 	myG = getGraphics();
	canvasG = canvasimage.getGraphics();

	font = new java.awt.Font("TimesRoman", Font.ITALIC, 14);
        canvasG.setFont(font);
        fm = canvasG.getFontMetrics();
        lok = false;
	r = new Random();
	setbrushsize(5);
	paintmode = 1;
    }
    
    private int addjitter(int val) {
	return val + (Math.abs(r.nextInt())%jsize) - jsize2;
    }
    
    private Color samplecolor(int x, int y) {
	x = (x*memimg.imgxsize)/dxsize;
	y = (y*memimg.imgysize)/dysize;
	if(x<0) x = 0;
	if(x>=memimg.imgxsize) x=memimg.imgxsize-1;
	if(y<0) y = 0;
	if(y>=memimg.imgysize) y=memimg.imgysize-1;
	return new Color(memimg.imgpixels[y][x]);
    }
    private void drawbrush(Graphics g, int lx, int ly, int cx, int cy, int nsteps, boolean demo) {
    	int i, x, y, rx, ry, px, py, del;
    	int m, nmarks;

    	for(i=0; i<nsteps; i++) {
    		px = (lx*(nsteps-i)+i*cx)/nsteps;
    		py = (ly*(nsteps-i)+i*cy)/nsteps;
    		if(demo) {
    			x = px;
    			y = py;
    		} else {
    			x = addjitter(px);
    			y = addjitter(py);
    			g.setColor(samplecolor(x,y));
    		}
    		switch(brushtype) {
    		case 0:
    			g.fillOval(x-bsize2,y-bsize2,bsize,bsize);
    			break;
    		case 1:
    			if(demo)
    				nmarks = 7;
    			else
    				nmarks = 1+(bsize*bsize)/200;
    			for(m=0; m<nmarks; m++) {
    				rx = addjitter(x);
    				ry = addjitter(y);
    				g.drawLine(rx-1,ry+1,rx+1,ry-1);
    			}
    			break;
    		case 2:
    			g.drawLine(x-bsize2,y+bsize2,x+bsize2,y-bsize2);
    			break;
    		case 3:
    			if(demo)
    				nmarks = 8;
    			else
    				nmarks = 2+(bsize)/3;
    			px = x-bsize2;		
    			py = y+bsize2;		
    			for(m=0; m<nmarks; m++) {
    				del = (Math.abs(r.nextInt())%bsize);
    				x = px + del;
    				y = py - del;
    				g.drawLine(x,y,x,y);
    			}
    			break;
    		case 4:
    			g.drawLine(x,y,x,y);
    			break;
    		}
    	}
    }
    private void showbrushsize() {
	int xorg, yorg, mpos, barwidth, barheight, markwidth;

 	xorg = 146;
 	yorg = cysize+10;
 	barwidth = 37;
 	barheight = 5;
 	markwidth = 3;
   	mpos = (int)((barwidth-markwidth)*(brushsize/(float)brushsteps));
	myG.setColor(Color.white);
	myG.fillRect(xorg,yorg,barwidth,barheight);
	myG.setColor(Color.red);
	myG.fillRect(xorg+mpos,yorg,markwidth,barheight);
    }
    private void showbrushtype() {
	int xorg, yorg, oldsize;

	xorg = 277;
	yorg = cysize+18;
	oldsize = brushsize;
	setbrushsize(3);
	myG.setColor(Color.white);
	myG.fillRect(xorg-15,yorg-15,30,30);
	myG.setColor(Color.black);
	drawbrush(myG,xorg,yorg,xorg,yorg,1,true);
	setbrushsize(oldsize);
    }
    private void nextbrush() {
	brushtype = (brushtype+1)%5;
	showbrushtype();
    }
    private void setbrushsize(int newbrushsize) {
	float diam;

	brushsize = newbrushsize;
	diam = (int)(4.0*Math.pow(1.41,newbrushsize));
	bsize2 = (int)(diam/2.0);
	bsize = 2*bsize2+1;
	jsize2 = (int)(2.0*diam/2.0);
	jsize = 2*jsize2+1;
    }
    private void biggerbrush() {
	if(brushsize<brushsteps) {
	    setbrushsize(brushsize+1);
	    showbrushsize();
	}
    }
    private void smallerbrush() {
	if(brushsize>0) {
	    setbrushsize(brushsize-1);
	    showbrushsize();
	}
    }
    private void showtext(String word) {
	canvasG.setColor(text);
	canvasG.drawString(word,(wxsize-fm.stringWidth(word))/2, wysize/3);
    }
    private void clearscreen() {
	canvasG.setColor(canvas);
	canvasG.fillRect(0,0,dxsize,dysize);
	switch(paintmode) {
	    case 1:
		showtext("Loading image . . . ");
		break;
	    case 2:
		showtext("Paint Here to Reveal. . .");
		break;
	}
    }
    public void update(Graphics g) {
	paint(g);
    }
    public boolean mouseDown(Event evt, int x, int y) {
	if(x>0 && x<cxsize && y>0 && y<cysize) {
	    if(paintmode == 2) {
		paintmode++;
		clearscreen();
		updatescreen(myG);
	    }
	    dopaint = true;
	    lok = false;
	} else if(x>0 && x<cxsize && y>cysize+1 && y<wysize) {
	    if(x>0 && x<57) {
		clearscreen();
		updatescreen(myG);
	    } else if(x>80 && x<161)
		smallerbrush();
	    else if(x>181 && x<259)
		biggerbrush();
	    else if(x>261 && x<291)
		nextbrush();
	}
	return true;
    }
    public boolean mouseUp(Event evt, int x, int y) {
	dopaint = false;
	return true;
    }
    public boolean mouseDrag(Event evt, int x, int y) {
	if(dopaint) {
	    if(lok) {
		if((lx == x) && (ly == y))
		    return true;
		drawbrush(canvasG,lx,ly,x,y,nstrokes[brushtype],false);
		updatescreen(myG);
	    } else {
		lok = true;
	    }
	    lx = x;
	    ly = y;
	}
	return true;
    }
    public void updatescreen(Graphics g) {
	g.drawImage(canvasimage,1,1,this);
    }
    private void updateall(Graphics g) {
	g.setColor(border);
	g.drawRect(0,0,cxsize-1,cysize-1);
	g.setColor(Color.white);
	g.fillRect(0,cysize,wxsize,controlheight);
	updatescreen(g);
	g.drawImage(menu,0,cysize+4,this);
    }
    private Color paramcolor(String name, Color defcolor) {
	String pval;
 	Integer i;

	if((pval=getParameter(name)) != null) {
	    i = Integer.valueOf(pval,16);
	    return new Color(i.intValue());
	} else {
	    return defcolor;
	}
    }
    public void paint(Graphics g) {	
	String sourcename;

	if(paintmode == 1) {
	    border = paramcolor("bordercolor", new Color(190,190,190));
	    canvas = paramcolor("canvascolor", new Color(231,215,199));
	    text = paramcolor("textcolor", new Color(20,20,20));
	    sourcename = getParameter("source");
	    clearscreen();
	    menu = getImage(getCodeBase(),"menu.gif");
	    updateall(g);
	    if(sourcename.startsWith("http://")) {
		try {
		    picture = getImage(new URL(sourcename));
		} catch (Exception e) { 
		    System.out.println("impression: bad URL spec");
		}
	    } else {
		picture = getImage(getDocumentBase(),sourcename);
	    }
	    memimg = new MemoryImage(picture);
	    if(memimg.imgpixels == null)
		System.out.println("impression: error on image read!");
	    paintmode = 2;
	    clearscreen();
	    updatescreen(g);
	} else 
	    updateall(g);
	showbrushsize();
	showbrushtype();
    }
}
