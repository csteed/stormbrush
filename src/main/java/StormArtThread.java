import java.util.*;
import java.text.*;
import java.awt.*;

import javax.swing.*;

public class StormArtThread extends Thread {
	private Storm [] storms;
	private CAPanel imagePanel;
	private JLabel timeLabel;
	
	private Calendar now;
	private Iterator dayIterator;
	private StormDay day;
	private Iterator obsIterator;
	private StormObservation obs;
	
	private StormDay lastDay;
	private StormObservation lastObs;
	private DecimalFormat df;
	private ArrayList<ForecastAdvisoryCollection> collectionList;
	
	public boolean running = false;
	
/*	StormArtThread (ArrayList advisoryList, Storm storm, CAPanel imagePanel, JLabel timeLabel) {
		super();
		advisoryLists = new ArrayList[1];
		advisoryLists[0] = advisoryList;
		//this.advisoryList = advisoryList;
		storms = new Storm[1];
		storms[0] = storm;
		//this.storm = storm;
		this.imagePanel = imagePanel;
		this.timeLabel = timeLabel;
		df = new DecimalFormat();
		df.setMaximumIntegerDigits(2);
		df.setMinimumIntegerDigits(2);
	}*/
	
	StormArtThread (ArrayList<ForecastAdvisoryCollection> collectionList, Storm [] storms, CAPanel imagePanel, JLabel timeLabel) {
		super();
		this.collectionList = collectionList;
		this.storms = storms;
		this.imagePanel = imagePanel;
		this.timeLabel = timeLabel;
		df = new DecimalFormat();
		df.setMaximumIntegerDigits(2);
		df.setMinimumIntegerDigits(2);		
	}
	
	public void init() {
//		lastDay = null;
//		lastObs = null;
		
//		now = Calendar.getInstance();
//		dayIterator = storm.getStormDayIterator();
//		day = (StormDay)dayIterator.next();
//		obsIterator = day.getObservationIterator();
//		obs = (StormObservation)obsIterator.next();
		
//		StormDay firstDay = storm.getStormDay(0);
//		StormObservation firstObs = firstDay.getObservation(0);
		
//		now.set(firstDay.getYear(), firstDay.getMonth(), firstDay.getDayOfMonth(), firstObs.getHour(), 0);
//		now.setTimeZone(TimeZone.getTimeZone("UTC"));
//		System.out.println("firstDay.getMonth() = " + firstDay.getMonth() + " now is " + now.get(Calendar.MONTH));
		running = true;
	}
	
	public void run() {
		if (collectionList.size() > 1) {
			short maxWind = Short.MIN_VALUE;
			short minWind = Short.MAX_VALUE;
			for (ForecastAdvisoryCollection advisoryCollection : collectionList) {
				if (advisoryCollection.maxWind > maxWind) {
					maxWind = advisoryCollection.maxWind;
				}
				if (advisoryCollection.minWind < minWind) {
					minWind = advisoryCollection.minWind;
				}
			}
			for (ForecastAdvisoryCollection advisoryCollection : collectionList) {
				advisoryCollection.minWind = minWind;
				advisoryCollection.maxWind = maxWind;
			}

		}

		for (int istorm = 0; istorm < collectionList.size(); istorm++) {
			if (!running) {
				return;
			}
			
			boolean newStorm = true;
			ForecastAdvisoryCollection collection = (ForecastAdvisoryCollection)collectionList.get(istorm);
			System.out.println("StormArtThread.run(): WORKING ON STORM '"+collection.stormName+"' with " + collection.advisoryList.size() + " records.");
//			ArrayList advisoryList = advisoryLists[istorm];
			Storm storm = storms[istorm];
			
			Iterator advisoryIterator = collection.advisoryList.iterator();
			while (advisoryIterator.hasNext()) {
				if (!running) {
					return;
				}
				
				ForecastAdvisory advisory = (ForecastAdvisory)advisoryIterator.next();
				//imagePanel.nextAdvisory3(advisory, newStorm);  // long stroke method
				imagePanel.nextAdvisory(collection, advisory, newStorm);  // short stroke method

				timeLabel.setText(storm.getName() + ": " + advisory.year + "." + df.format(advisory.month) + "." + df.format(advisory.day) + " " + df.format(advisory.hour) + "00Z");
				if (newStorm) {
					newStorm = false;
				}
				
			try {
				sleep(200);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
			}
		}
		/*
		ArrayList trackPointList = new ArrayList();
		Iterator dayIter = storm.getStormDayIterator();
		TrackPoint lastObsPoint = null;
		
		while (dayIter.hasNext()) {
			StormDay thisDay = (StormDay)dayIter.next();
			Iterator obsIter = thisDay.getObservationIterator();
			while(obsIter.hasNext()) {
				StormObservation thisObs = (StormObservation)obsIter.next();
				TrackPoint pt = new TrackPoint();
				pt.year = thisDay.getYear();
				pt.month = thisDay.getMonth();
				pt.day = thisDay.getDayOfMonth();
				pt.hour = (short)(thisObs.getHour());
				pt.longitude = thisObs.getLongitude();
				pt.latitude = thisObs.getLatitude();
				pt.wind = thisObs.getWind();
				trackPointList.add(pt);				
			}
		}

		
		int current_idx = 0;
		while (current_idx < trackPointList.size()) {
			TrackPoint pt = (TrackPoint)trackPointList.get(current_idx++);
			imagePanel.nextTrackPoint(pt.longitude, pt.latitude);
			timeLabel.setText(current_idx + ": " + pt.year + "." + df.format(pt.month) + "." + df.format(pt.day) + " " + df.format(pt.hour) + ":00 UTC");
			
			try {
				sleep(1000);
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
		*/
	}
/*	
	class TrackPoint {
		public short year;
		public short month;
		public short day;
		public short hour;
		public float longitude;
		public float latitude;
		public float wind;
	}*/
}
