import java.net.*;
import java.io.*;
import java.util.*;
import java.awt.geom.*;
import jgeodesic.*;


public class ForecastAdvisoryReader {
	public static final int GEODESIC_FLAT_OPTION = 0;
	public static final double RAD2DEG = 180./Math.PI;
	public static final double DEG2RAD = Math.PI/180.;
	public static final double NE_AZIMUTH = 315. * DEG2RAD;
	public static final double SE_AZIMUTH = 225. * DEG2RAD;
	public static final double NW_AZIMUTH = 45. * DEG2RAD;
	public static final double SW_AZIMUTH = 135. * DEG2RAD;
	public static final double NM2METERS = 1852.;

	public static String www_prefix = "http://www.nhc.noaa.gov";

	
	public static ForecastAdvisoryCollection readAdvisories (int stormYear, String stormName) throws Exception {
		System.out.println("***READING " + stormName + " " + stormYear + " INFO***");
		
		ArrayList advisoryList = new ArrayList();
		
		String urlString = www_prefix + "/archive/" + stormYear + "/" + stormName.toUpperCase() + ".shtml?";
		
		URL url = new URL(urlString);
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			if (line.contains("/mar/")) {
				String advisory_path = line.substring(line.indexOf("=")+2, line.indexOf("?")+1);
//				System.out.println(www_prefix + advisory_path);
				
				URL advisoryURL = new URL(www_prefix + advisory_path);
			
				BufferedReader advisoryReader = new BufferedReader(new InputStreamReader(advisoryURL.openStream()));
				String line2 = null;
				boolean inPre = false;
				int line_number = 1;
				
				ForecastAdvisory advisory = new ForecastAdvisory();
				advisoryList.add(advisory);
				
				while ((line2 = advisoryReader.readLine()) != null) {
					
					if (line2.contains("<pre>")) {
						inPre = true;
						continue;
					} else if (line2.contains("</pre>")) {
						break;
					}
							
					if (inPre) {
//						System.out.println("  " + line2);

						if (line_number == 5) {
							System.out.println("Date/Time is '" + line2 + "'");
							advisory.hour = Short.parseShort(line2.substring(0,2));
							String monthStr = line2.substring(10, 13);
							if (monthStr.equals("JAN"))
								advisory.month = 1;
							else if (monthStr.equals("FEB"))
								advisory.month = 2;
							else if (monthStr.equals("MAR"))
								advisory.month = 3;
							else if (monthStr.equals("APR"))
								advisory.month = 4;
							else if (monthStr.equals("MAY"))
								advisory.month = 5;
							else if (monthStr.equals("JUN"))
								advisory.month = 6;
							else if (monthStr.equals("JUL"))
								advisory.month = 7;
							else if (monthStr.equals("AUG"))
								advisory.month = 8;
							else if (monthStr.equals("SEP"))
								advisory.month = 9;
							else if (monthStr.equals("OCT"))
								advisory.month = 10;
							else if (monthStr.equals("NOV"))
								advisory.month = 11;
							else 
								advisory.month = 12;
							advisory.day = Short.parseShort(line2.substring(14, 16));
							advisory.year = Short.parseShort(line2.substring(17,21));
						} else if (line2.contains("CENTER LOCATED NEAR") &&
								   line2.startsWith("REPEAT")) {
							int start_idx = 0;
							for (; start_idx < line2.length(); start_idx++) {
								if (Character.isDigit(line2.charAt(start_idx))) {
									break;
								}
							}
							advisory.latitude = Float.parseFloat(line2.substring(start_idx, start_idx+4).trim());
							if (line2.charAt(start_idx+4) == 'S')
								advisory.latitude *= -1.;
							advisory.longitude = Float.parseFloat(line2.substring(start_idx+6, start_idx+11).trim());
//							System.out.println("char at start_idx + 11 is '"+line2.charAt(start_idx+11) + "'");
//							System.out.println("char at start_idx + 4 is '"+line2.charAt(start_idx+4) + "'");
							if (line2.charAt(start_idx+11) == 'W')
								advisory.longitude *= -1.;
//							System.out.println("Lat = '"+latStr+"'  Lon = '"+lonStr+"'");
						} else if (line2.startsWith("EYE DIAMETER")) {
							advisory.eye_diameter = Short.parseShort(line2.substring(13, 16).trim());
						} else if (line2.startsWith("PRESENT MOVEMENT")) {
							int idx = line2.indexOf("DEGREES");
							if (idx != -1) {
								advisory.movement_direction = Short.parseShort(line2.substring(idx - 4, idx -1).trim());
								advisory.movement_speed = Short.parseShort(line2.substring(idx + 11, idx + 14).trim());
							}							
						} else if (line2.startsWith("MAX SUSTAINED WINDS")) {
							advisory.wind = Short.parseShort(line2.substring(20, 23).trim());
							advisory.gust = Short.parseShort(line2.substring(41, 44).trim());
							
//							String windStr = line2.substring(20, 23);
//							String windGustStr = line2.substring(41, 44);
//							System.out.println("Wind is '"+windStr +"' and gust is '"+windGustStr+"'");
							
							// get the wind and seas radii if listed
							// read them until WINDS AND SEAS VARY GREATLY....
							
							ArrayList windRadiiList = new ArrayList();
							while ((line2 = advisoryReader.readLine()) != null) {
								AdvisoryArea area = new AdvisoryArea();
								area.points = new ArrayList();
								
//								short array[] = new short[5];
								
								if (line2.startsWith("WINDS AND SEAS VARY")) {
									break;
								}
								
								double lat_rad = advisory.latitude * DEG2RAD;
								double lon_rad = advisory.longitude * DEG2RAD;

								area.value = new Short(line2.substring(0, 2).trim());
								area.ne = Short.parseShort(line2.substring(12, 15).trim());
//								double [] coords = JGeodesic.direct1(lat_rad, lon_rad, NE_AZIMUTH, area.ne*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.se = Short.parseShort(line2.substring(18, 21).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, SE_AZIMUTH, area.se*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.sw = Short.parseShort(line2.substring(24, 27).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, SW_AZIMUTH, area.sw*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.nw = Short.parseShort(line2.substring(30, 33).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, NW_AZIMUTH, area.nw*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
//								array[0] = Short.parseShort(line2.substring(0, 2).trim());
//								array[1] = Short.parseShort(line2.substring(12, 15).trim());
//								array[2] = Short.parseShort(line2.substring(18, 21).trim());
//								array[3] = Short.parseShort(line2.substring(24, 27).trim());
//								array[4] = Short.parseShort(line2.substring(30, 33).trim());
								
//								int ne_dist = Integer.parseInt(line2.substring(12, 15).trim());
//								int se_dist = Integer.parseInt(line2.substring(18, 21).trim());
//								int sw_dist = Integer.parseInt(line2.substring(24, 27).trim());
//								int nw_dist = Integer.parseInt(line2.substring(30, 33).trim());
								
								
								
//								System.out.println("......." + ne_dist +"NE "+se_dist+"SE "+ sw_dist+"SW "+ nw_dist+"NW.");
								if (line2.subSequence(3, 5).equals("KT")) {
//									int radii_wind = Integer.parseInt(line2.substring(0, 2).trim());
//									System.out.println(radii_wind + " KT");
//									windRadiiList.add(array);
									windRadiiList.add(area);
								} else {
									advisory.seaRadii = area;
//									advisory.sea_radii = array;
//									int radii_seas = Integer.parseInt(line2.substring(0, 2).trim());
//									System.out.println(radii_seas + " FT SEAS");
								}
							}
							advisory.windRadiiList = windRadiiList;
						}
						
						line_number++;
					}
					
				}
				System.out.println("advisory: " + advisory);
				advisoryReader.close();	
			}
		}
		reader.close();
		
		return new ForecastAdvisoryCollection(stormName, stormYear, advisoryList); 
	}
	
	public static void main (String[] args) throws Exception {
		if (args.length == 0 || ((args.length % 2) != 1)) {
			System.out.println("Usage: java ForecaseAdvisoryReader {output file name} {;storm1 year} {storm1 name} {storm2 year} {storm2 name} ... ");
			System.exit(0);
		}
		
		ArrayList stormCollections = new ArrayList();
		
		for (int i = 1; i < args.length; i += 2) {
			int year = Integer.parseInt(args[i]);
			stormCollections.add(readAdvisories(year, args[i+1]));
		}
		
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(args[0]));
		os.writeObject(stormCollections);
		os.close();	
	}
	
/*	public static void main(String[] args) throws Exception {
		ArrayList advisoryList = new ArrayList();
		
//		URL url = new URL("http://www.nhc.noaa.gov/archive/2005/mar/");
		String www_prefix = "http://www.nhc.noaa.gov";
		
		URL url = new URL("http://www.nhc.noaa.gov/archive/2005/RITA.shtml?");
		BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
		
		String line = null;
		while ((line = reader.readLine()) != null) {
			//System.out.println(line);
			if (line.contains("/mar/")) {
				String advisory_path = line.substring(line.indexOf("=")+2, line.indexOf("?")+1);
//				System.out.println(www_prefix + advisory_path);
				
				URL advisoryURL = new URL(www_prefix + advisory_path);
				BufferedReader advisoryReader = new BufferedReader(new InputStreamReader(advisoryURL.openStream()));
				String line2 = null;
				boolean inPre = false;
				int line_number = 1;
				
				ForecastAdvisory advisory = new ForecastAdvisory();
				advisoryList.add(advisory);
				
				while ((line2 = advisoryReader.readLine()) != null) {
					
					if (line2.contains("<pre>")) {
						inPre = true;
						continue;
					} else if (line2.contains("</pre>")) {
						break;
					}
							
					if (inPre) {
//						System.out.println("  " + line2);

						if (line_number == 5) {
							System.out.println("Date/Time is '" + line2 + "'");
							advisory.hour = Short.parseShort(line2.substring(0,2));
							String monthStr = line2.substring(10, 13);
							if (monthStr.equals("JAN"))
								advisory.month = 1;
							else if (monthStr.equals("FEB"))
								advisory.month = 2;
							else if (monthStr.equals("MAR"))
								advisory.month = 3;
							else if (monthStr.equals("APR"))
								advisory.month = 4;
							else if (monthStr.equals("MAY"))
								advisory.month = 5;
							else if (monthStr.equals("JUN"))
								advisory.month = 6;
							else if (monthStr.equals("JUL"))
								advisory.month = 7;
							else if (monthStr.equals("AUG"))
								advisory.month = 8;
							else if (monthStr.equals("SEP"))
								advisory.month = 9;
							else if (monthStr.equals("OCT"))
								advisory.month = 10;
							else if (monthStr.equals("NOV"))
								advisory.month = 11;
							else 
								advisory.month = 12;
							advisory.day = Short.parseShort(line2.substring(14, 16));
							advisory.year = Short.parseShort(line2.substring(17,21));
						} else if (line2.contains("CENTER LOCATED NEAR") &&
								   line2.startsWith("REPEAT")) {
							int start_idx = 0;
							for (; start_idx < line2.length(); start_idx++) {
								if (Character.isDigit(line2.charAt(start_idx))) {
									break;
								}
							}
							advisory.latitude = Float.parseFloat(line2.substring(start_idx, start_idx+4).trim());
							if (line2.charAt(start_idx+4) == 'S')
								advisory.latitude *= -1.;
							advisory.longitude = Float.parseFloat(line2.substring(start_idx+6, start_idx+11).trim());
//							System.out.println("char at start_idx + 11 is '"+line2.charAt(start_idx+11) + "'");
//							System.out.println("char at start_idx + 4 is '"+line2.charAt(start_idx+4) + "'");
							if (line2.charAt(start_idx+11) == 'W')
								advisory.longitude *= -1.;
//							System.out.println("Lat = '"+latStr+"'  Lon = '"+lonStr+"'");
						} else if (line2.startsWith("EYE DIAMETER")) {
							advisory.eye_diameter = Short.parseShort(line2.substring(13, 16).trim());
						} else if (line2.startsWith("PRESENT MOVEMENT")) {
							int idx = line2.indexOf("DEGREES");
							if (idx != -1) {
								advisory.movement_direction = Short.parseShort(line2.substring(idx - 4, idx -1).trim());
								advisory.movement_speed = Short.parseShort(line2.substring(idx + 11, idx + 14).trim());
							}							
						} else if (line2.startsWith("MAX SUSTAINED WINDS")) {
							advisory.wind = Short.parseShort(line2.substring(20, 23).trim());
							advisory.gust = Short.parseShort(line2.substring(41, 44).trim());
							
//							String windStr = line2.substring(20, 23);
//							String windGustStr = line2.substring(41, 44);
//							System.out.println("Wind is '"+windStr +"' and gust is '"+windGustStr+"'");
							
							// get the wind and seas radii if listed
							// read them until WINDS AND SEAS VARY GREATLY....
							
							ArrayList windRadiiList = new ArrayList();
							while ((line2 = advisoryReader.readLine()) != null) {
								AdvisoryArea area = new AdvisoryArea();
								area.points = new ArrayList();
								
//								short array[] = new short[5];
								
								if (line2.startsWith("WINDS AND SEAS VARY")) {
									break;
								}
								
								double lat_rad = advisory.latitude * DEG2RAD;
								double lon_rad = advisory.longitude * DEG2RAD;

								area.value = new Short(line2.substring(0, 2).trim());
								area.ne = Short.parseShort(line2.substring(12, 15).trim());
//								double [] coords = JGeodesic.direct1(lat_rad, lon_rad, NE_AZIMUTH, area.ne*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.se = Short.parseShort(line2.substring(18, 21).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, SE_AZIMUTH, area.se*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.sw = Short.parseShort(line2.substring(24, 27).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, SW_AZIMUTH, area.sw*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
								area.nw = Short.parseShort(line2.substring(30, 33).trim());
//								coords = JGeodesic.direct1(lat_rad, lon_rad, NW_AZIMUTH, area.nw*NM2METERS, GEODESIC_FLAT_OPTION);
//								area.points.add(new Point2D.Double(coords[1]*RAD2DEG, coords[0]*RAD2DEG));
								
//								array[0] = Short.parseShort(line2.substring(0, 2).trim());
//								array[1] = Short.parseShort(line2.substring(12, 15).trim());
//								array[2] = Short.parseShort(line2.substring(18, 21).trim());
//								array[3] = Short.parseShort(line2.substring(24, 27).trim());
//								array[4] = Short.parseShort(line2.substring(30, 33).trim());
								
//								int ne_dist = Integer.parseInt(line2.substring(12, 15).trim());
//								int se_dist = Integer.parseInt(line2.substring(18, 21).trim());
//								int sw_dist = Integer.parseInt(line2.substring(24, 27).trim());
//								int nw_dist = Integer.parseInt(line2.substring(30, 33).trim());
								
								
								
//								System.out.println("......." + ne_dist +"NE "+se_dist+"SE "+ sw_dist+"SW "+ nw_dist+"NW.");
								if (line2.subSequence(3, 5).equals("KT")) {
//									int radii_wind = Integer.parseInt(line2.substring(0, 2).trim());
//									System.out.println(radii_wind + " KT");
//									windRadiiList.add(array);
									windRadiiList.add(area);
								} else {
									advisory.seaRadii = area;
//									advisory.sea_radii = array;
//									int radii_seas = Integer.parseInt(line2.substring(0, 2).trim());
//									System.out.println(radii_seas + " FT SEAS");
								}
							}
							advisory.windRadiiList = windRadiiList;
						}
						
						line_number++;
					}
					
				}
				System.out.println("advisory: " + advisory);
				advisoryReader.close();	
			}
		}
		reader.close();
		
		ObjectOutputStream os = new ObjectOutputStream(new FileOutputStream(args[0]));
		os.writeObject(advisoryList);
		os.close();		
	}*/
}
