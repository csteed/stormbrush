import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class BestTrackData {
	private TreeMap stormYears;
	
	BestTrackData() {
		stormYears = new TreeMap();
	}
	
	public int getStormYearCount() {
		return stormYears.size();
	}
	
	public void loadEmanuelData(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = in.readLine()) != null) {
			if (line.isEmpty())
				continue;
			
			int count = 0;
			
			StormYear stormYear = null;
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				
				switch (count) {
					case 0:
						short year = Short.parseShort(token);
						stormYear = (StormYear)stormYears.get(year);
						break;
					case 1:
						stormYear.AUG_OCT_SST_SCALED = Float.parseFloat(token);
						break;
					case 2:
						stormYear.AUG_OCT_SST_C = Float.parseFloat(token);
						break;
					case 3:
						stormYear.AUG_OCT_SST_F = Float.parseFloat(token);
						break;
					case 4:
//						stormYear.PDI = Float.parseFloat(token);
						break;
				}
				count++;
			}
		}
		in.close();
	}
	
	public StormYear getFirstStormYear() {
		return (StormYear)stormYears.firstEntry().getValue();
	}
	
	public StormYear getLastStormYear() {
		return (StormYear)stormYears.lastEntry().getValue();
	}

	public void loadKlotzbachTCStats(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = in.readLine()) != null) {
			if (line.isEmpty())
				continue;
			
			int count = 0; 
			StormYear stormYear = null;
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				
				switch (count++) {
					case 0:
						short year = Short.parseShort(token);
						stormYear = (StormYear)stormYears.get(year);
						break;
					case 1:
						stormYear.numNamedStorms = Short.parseShort(token);
						break;
					case 2:
						stormYear.numNamedStormDays = Float.parseFloat(token);
						break;
					case 3:
						stormYear.numHurricanes = Short.parseShort(token);
						break;
					case 4:
						stormYear.numHurricaneDays = Float.parseFloat(token);
						break;
					case 5:
						stormYear.numIntenseHurricanes = Short.parseShort(token);
						break;
					case 6:
						stormYear.numIntenseHurricaneDays = Float.parseFloat(token);
						break;
					case 7:
						stormYear.ACE = Float.parseFloat(token);
						break;
					case 8:
						stormYear.HDP = Float.parseFloat(token);
						break;
					case 9:
						stormYear.PDI = Float.parseFloat(token);
						break;
					case 10:
						stormYear.netTropicalCycloneActivity = Short.parseShort(token);
						break;
				}
			}
		}
	}
	
	public void loadAllKlotzbachPredictors(File file) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		
		while ((line = in.readLine()) != null) {
			if (line.isEmpty())
				continue;
			
			int count = 0;
			StormYear stormYear = null;
			StringTokenizer st = new StringTokenizer(line);
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				
				switch (count) {
					case 0:
						short year = Short.parseShort(token);
						stormYear = (StormYear)stormYears.get(year);
						break;
					case 1:
						stormYear.JUN_JUL_SST = Float.parseFloat(token); 
						break;
					case 2:
						stormYear.JUN_JUL_SLP = Float.parseFloat(token);
						break;
					case 3:
						stormYear.JUN_JUL_NINO3 = Float.parseFloat(token);
						break;
					case 4:
						stormYear.JUN_JUL_NSD = Float.parseFloat(token);
						break;
					case 5:
//						stormYear.numNamedStorms = Short.parseShort(token);
						break;
					case 6:
//						stormYear.numNamedStormDays = Float.parseFloat(token);
						break;
					case 7:
//						stormYear.numHurricanes = Short.parseShort(token);
						break;
					case 8:
//						stormYear.numHurricaneDays = Float.parseFloat(token);
						break;
					case 9:
//						stormYear.numIntenseHurricanes = Short.parseShort(token);
						break;
					case 10:
//						stormYear.numIntenseHurricaneDays = Float.parseFloat(token);
						break;
					case 11:
//						stormYear.netTropicalCycloneActivity = Short.parseShort(token);
						break;
					case 12:
						stormYear.FEB_MAR_MB_200_U = Float.parseFloat(token);
						break;
					case 13:
						stormYear.FEB_MAR_MB_200_V = Float.parseFloat(token);
						break;
					case 14:
						stormYear.FEB_SLP = Float.parseFloat(token);
						break;
					case 15:
						stormYear.FEB_SST = Float.parseFloat(token);
						break;
					case 16:
						stormYear.NOV_MB_500_GH = Float.parseFloat(token);
						break;
					case 17:
						stormYear.SEP_NOV_SLP = Float.parseFloat(token);
						break;
					case 19:
						stormYear.OCT_NOV_SLP = Float.parseFloat(token);
						break;
					case 20:
						stormYear.SEP_MB_500_GH = Float.parseFloat(token);
						break;
					case 21:
						stormYear.JUL_MB_50_U = Float.parseFloat(token);
						break;
//					case 22:
//						stormYear.SEP_NOV_SLP = Float.parseFloat(token);
//						break;
					case 23:
						stormYear.NOV_SLP = Float.parseFloat(token);
						break;
					case 24:
						stormYear.MAY_SST = Float.parseFloat(token);
						break;
					case 25:
						stormYear.APR_MAY_SST = Float.parseFloat(token);
						break;
					case 26:
						stormYear.MAR_APR_SLP = Float.parseFloat(token);
						break;
					case 27:
						stormYear.PREV_NOV_MB_500_GH = Float.parseFloat(token);
						break;		
				}
				count++;
			}
		}
		in.close();
	}
	
	public void loadTemperatureMonthly(File file) throws IOException {
		float temperature_arr[] = new float[200*12];
		short start_year = -1;
		
		BufferedReader in = new BufferedReader(new FileReader(file));
		String line;
		
		int count = 0;
		while ((line = in.readLine()) != null) {
			if (line.isEmpty())
				continue;
			
			String tmpStr = line.substring(0, 4);
			if (start_year == -1)
				start_year = Short.parseShort(tmpStr.trim());
			
			tmpStr = line.substring(8, 17).trim();
			temperature_arr[count++] = Float.parseFloat(tmpStr);
		}
		in.close();
		
		Iterator yearIterator = getStormYearIterator();
		while (yearIterator.hasNext()) {
			StormYear stormYear = (StormYear)yearIterator.next();
			if (stormYear.getYear() < start_year)
				continue;
			int idx = 12 * (stormYear.getYear() - start_year);
//			System.out.println("storm year = " + stormYear.getYear() + " start_year = " + start_year);
			Iterator stormIterator = stormYear.getStormIterator();
			while (stormIterator.hasNext()) {
				Storm storm = (Storm)stormIterator.next();
				Iterator dayIterator = storm.getStormDayIterator();
				while (dayIterator.hasNext()) {
					StormDay day = (StormDay)dayIterator.next();
					Iterator obsIterator = day.getObservationIterator();
					while (obsIterator.hasNext()) {
						StormObservation obs = (StormObservation)obsIterator.next();					
//						System.out.println("idx + month = " + (idx+(day.getMonth()-1)) + " idx = " + idx);
						obs.temperatureAnomaly = temperature_arr[idx+(day.getMonth()-1)];
					}
				}
			}
		}
	}
	
	public static BestTrackData loadBestTrackData(File bestTrackFile) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(bestTrackFile));
		String line;
		
		BestTrackData bestTrackData = new BestTrackData();
		
		while ((line = in.readLine()) != null) {
			if (line.isEmpty())
				continue;
	
			//System.err.println(line.substring(12, 16));
			short year = Short.parseShort(line.substring(12, 16));
			
			StormYear stormYear = (StormYear)bestTrackData.stormYears.get(year);
			
			if (stormYear == null) {
				stormYear = new StormYear(year);
				bestTrackData.stormYears.put(year, stormYear);
			}
			
			Storm storm = Storm.parseStormString(line, stormYear);
			stormYear.addStorm(storm);
			
//			System.out.println("year = " + year + " name = " + storm.getName());
			
			StormDay prevDay = null;
			
			for (int iday = 0; iday < storm.getStormDayCount(); iday++) {
//				System.out.println("reading day " + iday);
				line = in.readLine();
				StormDay stormDay = StormDay.parseStormDayString(line);
				if (prevDay != null) {
					if (stormDay.getMonth() == 1 && prevDay.getMonth() == 12) {
						stormDay.setYear((short)(++year));
					} else {
						stormDay.setYear(year);
					}
				} else {
					stormDay.setYear(year);
				}
				
				Calendar cal = Calendar.getInstance();
				cal.set(stormDay.getYear(), stormDay.getMonth(), stormDay.getDayOfMonth());
				stormDay.setDayOfYear((short)cal.get(Calendar.DAY_OF_YEAR));
				
				storm.addStormDay(stormDay);
				prevDay = stormDay;
			}
			
			line = in.readLine().trim();
			
			// read the maximum storm intensity
			storm.setMaxIntensity(line.substring(6, 8));
			
			// read the US State landfall records
			int idx = 8;
			while (idx < line.length()) {
				char tmp[] = new char[3];
				int count = 0;
				while (!Character.isDigit(line.charAt(idx))) {
					tmp[count++] = line.charAt(idx++);
					if (count == 3)
						break;
				}
				
				String state = new String(tmp);
				if (state.trim().isEmpty()) {
					break;
				}
				
				short category = Short.parseShort(line.substring(idx, idx+1));
				idx++;
				
				LandfallData data = new LandfallData(state, category);
				storm.addLandfallData(data);
			}
		}
		
		in.close();
		
/*		Iterator yearIterator = bestTrackData.getStormYearIterator();
		while(yearIterator.hasNext()) {
			((StormYear)yearIterator.next()).calculateStatistics();
		}*/
		
		return bestTrackData;
	}
	
	public Iterator getStormYearIterator() {
		return stormYears.values().iterator();
	}
	
	public StormYear getStormYear(short year) {
		return (StormYear)stormYears.get(year);
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("BestTrackData: " + stormYears.size() + " years.\n");
		Iterator iter = getStormYearIterator();
		while (iter.hasNext()) {
			buf.append(((StormYear)iter.next()).toString() + "\n");
		}
		return buf.toString();
	}
	
/*	public static TreeMap readNOAAHistoricalHurricaneASCII(File f) throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(f));
		String line;
		
		TimeZone timeZone = TimeZone.getTimeZone("GMT");
		
		TreeMap hurricaneMap = new TreeMap();
		
		while ((line = in.readLine()) != null) {
			if (!line.startsWith("DbaseRecord")) {
				continue;
			}
			
			TrackPoint point = new TrackPoint();
			StringTokenizer st = new StringTokenizer(line.trim(), ";");
			int count = 0;
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
				
				if (count == 2) {
					// year
					point.year = Short.parseShort(token);
				} else if (count == 3) {
					// month
					point.month = Short.parseShort(token);
				} else if (count == 4) {
					// day
					point.day = Short.parseShort(token);
				} else if (count == 5) {
					// time_ad
					point.time_ad = Short.parseShort(token.substring(0, token.length()-1));
				} else if (count == 6) {
					// basin id
					point.basin_id = Short.parseShort(token);
				} else if (count == 7) {
					// name
					point.name = new String(token);
				} else if (count == 8) {
					// lat
					point.latitude = Double.parseDouble(token);
				} else if (count == 9) {
					// lon
					point.longitude = Double.parseDouble(token);
				} else if (count == 10) {
					// wind_kts
					point.wind_knots = Float.parseFloat(token);
				} else if (count == 11) {
					// pressure
					point.pressure = Float.parseFloat(token);
				} else if (count == 12) {
					// category
					point.category = new String(token);
				} else if (count == 13) {
					// basin
					point.basin = new String(token);
				} else if (count == 14) {
					// in atlantic
					point.in_atlantic = Boolean.parseBoolean(token);
				} else if (count == 15) {
					// in caribiean
					point.in_carib = Boolean.parseBoolean(token);
				} else if (count == 16) {
					// in gulf
					point.in_gulf = Boolean.parseBoolean(token);
				} else if (count == 17) {
					// in east pacific
					point.in_epac = Boolean.parseBoolean(token);
				} else if (count == 18) {
					// in central pacific
					point.in_cpac = Boolean.parseBoolean(token);
				}
				
				count++;
			}
			
			point.date = Calendar.getInstance(timeZone);
			point.date.set(point.year, point.month, point.day, point.time_ad, 0);
			
//			System.out.println(" Read the point: " + point.toString());
			
			// find appropriate hurricane object and add track point to the points list
			TreeMap basinMap = (TreeMap)hurricaneMap.get(point.basin);
			if (basinMap == null) {
				basinMap = new TreeMap();
				hurricaneMap.put(point.basin, basinMap);
			}
			
			TreeMap yearMap = (TreeMap)basinMap.get(new Short(point.year));
			if (yearMap == null) {
				yearMap = new TreeMap();
				basinMap.put(new Short(point.year), yearMap);
			}
							
			Storm storm = (Storm)yearMap.get(point.basin_id + "_" + point.name);
			if (storm == null) {
				storm = new Storm();
				storm.basin = new String(point.basin);
				storm.name = new String(point.name);
				storm.year = point.year;
				storm.basin_id = point.basin_id;
				yearMap.put(point.basin_id + "_" + point.name, storm);
			}
			
			storm.pointList.add(point);
			short category = 0;
			if (point.category.equals("H1")) {
				category = 1;
			} else if (point.category.equals("H2")) {
				category = 2;
			} else if (point.category.equals("H3")) {
				category = 3;
			} else if (point.category.equals("H4")) {
				category = 4;
			} else if (point.category.equals("H5")) {
				category = 5;
			}
			if (category > storm.maximumCategory) {
				storm.maximumCategory = category;
			}
		}
		
		if (hurricaneMap.isEmpty()) 
			return null;
		
		return hurricaneMap;
	}*/
	
	public static void main (String args[]) throws Exception {
//		TreeMap hurricaneTable = DataReader.readNOAAHistoricalHurricaneASCII(new File(args[0]));
		BestTrackData bestTrackData = BestTrackData.loadBestTrackData(new File(args[0]));
		//System.out.println("Hurricane Map is:\n" + bestTrackData.toString());
		Iterator yearIterator = bestTrackData.getStormYearIterator();
		while (yearIterator.hasNext()) {
			StormYear year = (StormYear)yearIterator.next();
			System.out.println("*"+year.getYear()+"*");
			Iterator stormIterator = year.getStormIterator();
			while (stormIterator.hasNext()) {
				System.out.println(" "+((Storm)stormIterator.next()).getName());
			}
		}
	}
}
