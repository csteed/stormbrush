import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import java.util.*;
import javax.imageio.*;

import java.awt.image.*;

public class StormArt extends JFrame implements ActionListener {

	private ArrayList collectionList;
	private BestTrackData bestTrackData;
	private BufferedImage backgroundImage;
	private BufferedImage subImage;
	private CAPanel imagePanel;
	private JButton goButton = new JButton("Render Storm Track");
	private JButton snapshotButton = new JButton("Save Screen");
	private File lastSnapshotFile = null;
	private JCheckBox trackCheckBox= new JCheckBox("Show Track");
	
	private JButton clearButton = new JButton("Clear Display");
	
	// storm bounds West, East, South, North
	private double stormWestBound, stormEastBound, stormNorthBound, stormSouthBound;
	private Storm [] storms = null;
	private StormArtThread thread;
	private JLabel timeLabel = new JLabel("Current Time Step");

	private JComboBox styleCombo;
	
	private double westImageBound, eastImageBound, southImageBound, northImageBound;
	
/*	public StormArt (ArrayList advisoryList, BestTrackData bestTrackData, BufferedImage backgroundImage, double westImageBound, double eastImageBound, double southImageBound, double northImageBound) {
		this.bestTrackData = bestTrackData;
		this.backgroundImage = backgroundImage;
		advisoryLists = new ArrayList[1];
		advisoryLists[0] = advisoryList;
		storms = new Storm[1];
		//this.advisoryList = advisoryList;
		this.westImageBound = westImageBound;
		this.eastImageBound = eastImageBound;
		this.southImageBound = southImageBound;
		this.northImageBound = northImageBound;
		setTitle("Tropical Cyclone Art v0.1 - Chad Steed");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		selectStorm((short)2005, "Katrina");
		createMainPanel();
		
		pack();
	}
*/	
	public StormArt (ArrayList collectionList, BestTrackData bestTrackData, BufferedImage backgroundImage, double westImageBound, double eastImageBound, double southImageBound, double northImageBound) {
		this.bestTrackData = bestTrackData;
		this.backgroundImage = backgroundImage;
		this.collectionList = collectionList;
		storms = new Storm[collectionList.size()];
		//this.advisoryList = advisoryList;
		this.westImageBound = westImageBound;
		this.eastImageBound = eastImageBound;
		this.southImageBound = southImageBound;
		this.northImageBound = northImageBound;
		setTitle("Tropical Cyclone Art v0.1 - Chad A. Steed");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		resetStormBounds();
		for (int i = 0; i < collectionList.size(); i++) {
			ForecastAdvisoryCollection collection = (ForecastAdvisoryCollection)collectionList.get(i);
			storms[i] = getStormInfo((short)collection.stormYear, collection.stormName);
		}
		
		subsetBackgroundImage();
//		setStormBoundsToBackgroundBounds();
		
		//selectStorm((short)2005, "Katrina");
		createMainPanel();
		
		pack();
	}
	
	public void subsetBackgroundImage() {
		System.out.println("Storm extents are: W="+stormWestBound + "E="+stormEastBound+" S="+stormSouthBound+" N="+stormNorthBound);
		System.out.println("backgroundImage height = " + backgroundImage.getHeight() + " width=" + backgroundImage.getWidth());
		
		stormWestBound -= 8.;
		stormEastBound += 5.;
		stormNorthBound += 5.;
		stormSouthBound -= 5.;
		
		// figure out the dimension of the subimage
		int left = CAPanel.longitudeToX(stormWestBound, westImageBound, eastImageBound, backgroundImage.getWidth());
		int right = CAPanel.longitudeToX(stormEastBound, westImageBound, eastImageBound, backgroundImage.getWidth());
		int top = CAPanel.latitudeToY(stormNorthBound, northImageBound, southImageBound, backgroundImage.getHeight());
		int bottom = CAPanel.latitudeToY(stormSouthBound, northImageBound, southImageBound, backgroundImage.getHeight());

		System.out.println("stormWestBound="+stormWestBound+" stormEastBound="+stormEastBound+" stormNorthBound="+stormNorthBound+" stormSouthBound="+stormSouthBound);
		System.out.println("left = " + left + " right = " + right + " bottom = " + bottom + " top = " + top);
		subImage = backgroundImage.getSubimage(left, top, right-left, bottom-top);
	
		System.out.println("subImage height = " + subImage.getHeight() + " width = " + subImage.getWidth());
	}
	
	public void actionPerformed(ActionEvent event) {
		if (event.getSource() == goButton) {
			if (thread != null) {
				if (thread.isAlive()) {
					System.out.println("Set thread.running to false");
					thread.running = false;
				} else {
					startThread();
				}
			} else {
				startThread();
			}
//			imagePanel.drawAdvisories(advisoryList);
//			startThread();
		} else if (event.getSource() == snapshotButton) {
			// save current image to file
			
			JFileChooser chooser = new JFileChooser(lastSnapshotFile);
			chooser.setMultiSelectionEnabled(false);
			chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

			chooser.setDialogTitle("Save Snapshot as .png");
			int retVal = chooser.showSaveDialog(this);
				
			if (retVal != JFileChooser.CANCEL_OPTION) {
				try {
					String filepath = chooser.getSelectedFile().getCanonicalPath();
					if (!filepath.endsWith(".png")) {
						filepath += ".png";
					}
					File f = new File(filepath);
					imagePanel.saveImage(f);
					lastSnapshotFile = f;
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			}
		} else if (event.getSource() == styleCombo) {
			imagePanel.setStrokeMode(styleCombo.getSelectedIndex());
		} else if (event.getSource() == clearButton) {
			imagePanel.resetForeground();
			imagePanel.repaint();
		} else if (event.getSource() == trackCheckBox) {
			imagePanel.showTrack = trackCheckBox.isSelected();
			imagePanel.repaint();
		}
	}

	private void setStormBoundsToBackgroundBounds(){
		stormWestBound = westImageBound;
		stormEastBound = eastImageBound;
		stormSouthBound = southImageBound;
		stormNorthBound = northImageBound;
		subImage = backgroundImage;
	}
	
	private void resetStormBounds() {
		stormWestBound = eastImageBound;
		stormEastBound = westImageBound;
		stormSouthBound = northImageBound;
		stormNorthBound = southImageBound;
	}
	
	private void startThread() {
		thread = new StormArtThread(collectionList, storms, imagePanel, timeLabel);
		thread.init();
		thread.start();
	}
	
	private Storm getStormInfo(short year, String name) {
		StormYear stormYear = bestTrackData.getStormYear(year);
		Iterator stormIter = stormYear.getStormIterator();
		Storm storm = null;
		
		while (stormIter.hasNext()) {
			storm = (Storm)stormIter.next();
			if (storm.getName().equalsIgnoreCase(name)) {
			//	this.storm = storm;
			//	resetStormBounds();
				Iterator dayIter = storm.getStormDayIterator();
				while (dayIter.hasNext()) {
					StormDay day = (StormDay)dayIter.next();
					Iterator obsIter = day.getObservationIterator();
					while (obsIter.hasNext()) {
						StormObservation obs = (StormObservation)obsIter.next();
						
						if (obs.longitude < stormWestBound)
							stormWestBound = obs.longitude;
						if (obs.longitude > stormEastBound)
							stormEastBound = obs.longitude;
						if (obs.latitude < stormSouthBound)
							stormSouthBound = obs.latitude;
						if (obs.latitude > stormNorthBound)
							stormNorthBound = obs.latitude;
					}
				}
				return storm;
			}
		}
		return null;
	}
	
	/*private void getStorm(short year, String name) {
		StormYear stormYear = bestTrackData.getStormYear(year);
		Iterator stormIter = stormYear.getStormIterator();
		
		while (stormIter.hasNext()) {
			Storm storm = (Storm)stormIter.next();
			if (storm.getName().equalsIgnoreCase(name)) {
				this.storm = storm;
				resetStormBounds();
				Iterator dayIter = storm.getStormDayIterator();
				while (dayIter.hasNext()) {
					StormDay day = (StormDay)dayIter.next();
					Iterator obsIter = day.getObservationIterator();
					while (obsIter.hasNext()) {
						StormObservation obs = (StormObservation)obsIter.next();
						
						if (obs.longitude < stormWestBound)
							stormWestBound = obs.longitude;
						if (obs.longitude > stormEastBound)
							stormEastBound = obs.longitude;
						if (obs.latitude < stormSouthBound)
							stormSouthBound = obs.latitude;
						if (obs.latitude > stormNorthBound)
							stormNorthBound = obs.latitude;
					}
				}
				
				System.out.println("Storm extents are: W="+stormWestBound + "E="+stormEastBound+" S="+stormSouthBound+" N="+stormNorthBound);
				System.out.println("backgroundImage height = " + backgroundImage.getHeight() + " width=" + backgroundImage.getWidth());
				
				stormWestBound -= 10.;
				stormEastBound += 10.;
				stormNorthBound += 5.;
				stormSouthBound -= 5.;
				
				// figure out the dimension of the subimage
				int left = CAPanel.longitudeToX(stormWestBound, westImageBound, eastImageBound, backgroundImage.getWidth());
				int right = CAPanel.longitudeToX(stormEastBound, westImageBound, eastImageBound, backgroundImage.getWidth());
				int top = CAPanel.latitudeToY(stormNorthBound, northImageBound, southImageBound, backgroundImage.getHeight());
				int bottom = CAPanel.latitudeToY(stormSouthBound, northImageBound, southImageBound, backgroundImage.getHeight());

				int left = CAPanel.longitudeToX(stormWestBound, -180., 180., backgroundImage.getWidth());
				int right = CAPanel.longitudeToX(stormEastBound, -180., 180., backgroundImage.getWidth());
				int top = CAPanel.longitudeToX(stormNorthBound, 90., -90., backgroundImage.getHeight());
				int bottom = CAPanel.longitudeToX(stormSouthBound, 90., -90., backgroundImage.getHeight());
				
				System.out.println("stormWestBound="+stormWestBound+" stormEastBound="+stormEastBound+" stormNorthBound="+stormNorthBound+" stormSouthBound="+stormSouthBound);
				System.out.println("left = " + left + " right = " + right + " bottom = " + bottom + " top = " + top);
//				double westTest = CAPanel.XToLongitude(left, -180., 180., backgroundImage.getWidth());
//				double eastTest = CAPanel.XToLongitude(right, -180., 180., backgroundImage.getWidth());
//				int left2 = CAPanel.longitudeToX(westTest, -180., 180., backgroundImage.getWidth());
//				double westTest2 = CAPanel.XToLongitude(left2, -180., 180., backgroundImage.getWidth());
				
//				double northTest = CAPanel.YToLatitude(top, 90., -90., backgroundImage.getHeight());
//				double southTest = CAPanel.YToLatitude(bottom, 90., -90., backgroundImage.getHeight());
//				int top2 = CAPanel.longitudeToX(northTest, 90., -90., backgroundImage.getHeight());
//				int bottom2 = CAPanel.longitudeToX(southTest, 90., -90., backgroundImage.getHeight());
//				double northTest2 = CAPanel.YToLatitude(top2, 90., -90., backgroundImage.getHeight());
//				double southTest2 = CAPanel.YToLatitude(bottom2, 90., -90., backgroundImage.getHeight());
				
//				System.out.println("stormNorthBound="+stormNorthBound+" bottom="+bottom+" northTest="+northTest+" bottom2="+bottom2+" northTest2="+northTest2);
//				System.out.println("stormSouthBound="+stormSouthBound+" top="+top+" southTest="+southTest);
//				System.out.println("stormWestBound="+stormWestBound+" left="+left+" westTest="+westTest+" left2="+left2+" westTest2="+westTest2);
//				System.out.println("stormWestBound="+stormWestBound+" left="+left+" westTest="+westTest);
//				int left = (int)(((stormWestBound - (-180.)) / 360.) * backgroundImage.getWidth());
//				int right = (int)(((stormEastBound - (-180.)) / 360.) * backgroundImage.getWidth());
//				int top = (int)((1. - ((90. - stormNorthBound) / 180.)) * backgroundImage.getHeight());
//				int bottom = (int)((1. - ((90. - stormSouthBound) / 180.)) * backgroundImage.getHeight());
				
//				int left = (int)(((stormWestBound + 180.) / 360.) * backgroundImage.getWidth());
//				int right = (int)(((stormEastBound + 180.) / 360.) * backgroundImage.getWidth());
//				int top = (int)((1. - ((stormNorthBound + 90.) / 180.)) * backgroundImage.getHeight());
//				int bottom = (int)((1. - ((stormSouthBound + 90.) / 180.)) * backgroundImage.getHeight());
				
//				System.out.println("Image bounds are L="+left+" R="+right+" B="+bottom+" T="+top);
//				System.out.println("width = " + (right-left) + " height = " + (top-bottom));
				subImage = backgroundImage.getSubimage(left, top, right-left, bottom-top);
//				subImage = backgroundImage;
			
				System.out.println("subImage height = " + subImage.getHeight() + " width = " + subImage.getWidth());
				
				break;
			}
		}
	}*/
	
	private void createMainPanel() {		
//		imagePanel = new CAPanel(subImage, -180., 180., -90., 90.);
		imagePanel = new CAPanel(subImage, stormWestBound, stormEastBound, stormSouthBound, stormNorthBound);
		imagePanel.setSize(subImage.getWidth(), subImage.getHeight());
		
		JScrollPane imageScroller = new JScrollPane(imagePanel);
		imageScroller.setPreferredSize(new Dimension(800, 600));
		imageScroller.setViewportBorder(BorderFactory.createLineBorder(Color.black));
		imageScroller.getVerticalScrollBar().setUnitIncrement(50);
		imageScroller.getHorizontalScrollBar().setUnitIncrement(50);
		JToolBar statusBar = new JToolBar();
		statusBar.setFloatable(false);
		statusBar.setLayout(new BorderLayout());
		statusBar.add(timeLabel, BorderLayout.WEST);
		
		String [] styles = {"Small Brush", "Long Stroke"};
		styleCombo = new JComboBox(styles);
		styleCombo.setSelectedIndex(0);
		
		trackCheckBox.setSelected(imagePanel.showTrack);
		
		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new GridLayout(1,5));
		buttonBar.add(trackCheckBox);
		buttonBar.add(styleCombo);
		buttonBar.add(clearButton);
		buttonBar.add(snapshotButton);
		buttonBar.add(goButton);
		statusBar.add(buttonBar, BorderLayout.EAST);
		
		JPanel mainPanel = (JPanel)getContentPane();
		//mainPanel.add(imagePanel, BorderLayout.CENTER);
		mainPanel.add(imageScroller, BorderLayout.CENTER);
		mainPanel.add(statusBar, BorderLayout.SOUTH);
		//mainPanel.setPreferredSize(new Dimension(subImage.getWidth(), subImage.getHeight()+statusBar.getHeight()));
		
		goButton.addActionListener(this);
		snapshotButton.addActionListener(this);
		clearButton.addActionListener(this);
		styleCombo.addActionListener(this);
		trackCheckBox.addActionListener(this);
	}
	
	public static void main(String[] args) throws Exception {
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(args[0]));
		ArrayList collectionList = (ArrayList)ois.readObject();
		//ArrayList advisoryList = (ArrayList)ois.readObject();
		ois.close();
		
		File bestTrackFile = new File("data/tracks1851to2006_atl.txt");
		BestTrackData bestTrackData = BestTrackData.loadBestTrackData(bestTrackFile);
		
//		File backgroundImageFile = new File("images/gmt_na_dark.gif");
//		File backgroundImageFile = new File("images/gmt_world_dark.gif");
//		BufferedImage backgroundImage = ImageIO.read(backgroundImageFile);
		
//		StormArt app = new StormArt(advisoryList, bestTrackData, backgroundImage, -180., 180., -90., 90.);
//		StormArt app = new StormArt(advisoryList, bestTrackData, backgroundImage, -130., 18., -10., 75.);

		
//		File backgroundImageFile = new File("images/ir_background.gif");
		File backgroundImageFile = new File("images/GRAY_LR_SR_OB.png");
		BufferedImage backgroundImage = ImageIO.read(backgroundImageFile);
		//-115/-60/10/45-105/-40/10/45
//		StormArt app = new StormArt(collectionList, bestTrackData, backgroundImage, -105., -40., 10., 45.);
		StormArt app = new StormArt(collectionList, bestTrackData, backgroundImage, -180., 180., -90., 90.);
		
		/*
		File backgroundImageFile = new File("images/gmt_na_dark.gif");
		BufferedImage backgroundImage = ImageIO.read(backgroundImageFile);
		StormArt app = new StormArt(advisoryList, bestTrackData, backgroundImage, -130., 18., -10., 75.);
		*/
		app.setVisible(true);
		
//		String availableIDs[] = TimeZone.getAvailableIDs();
//		for (int i = 0; i < availableIDs.length; i++) {
//			System.out.println(availableIDs[i]);
//		}
	}
}
