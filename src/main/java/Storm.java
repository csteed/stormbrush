import java.util.ArrayList;
import java.util.Iterator;

public class Storm {
	private String name;
	private short month;
	private short dayOfMonth;
	private short yearStormNumber;
	private short stormNumber;
	private boolean madeLandfall;
	private StormCategory landfallCategory;
	private String maxIntensity;
	private ArrayList landfallData;
	private ArrayList stormDays;
	private int numStormDays;
	private StormYear stormYear;
	
	Storm (String name, short yearStormNumber, short stormNumber, StormYear stormYear, 
		   short month, short dayOfMonth, boolean madeLandfall, 
		   StormCategory landfallCategory, int numStormDays) {
		this.name = new String(name);
		this.stormYear = stormYear;
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		this.yearStormNumber = yearStormNumber;
		this.stormNumber = stormNumber;
		this.madeLandfall = madeLandfall;
		this.landfallCategory = landfallCategory;
		stormDays = new ArrayList();
		this.numStormDays = numStormDays;
		landfallData = new ArrayList();
	}
	
	public StormYear getStormYear() {
		return stormYear;
	}
	
	public Iterator getStormDayIterator() {
		return stormDays.iterator();
	}
	
	public void setMaxIntensity(String maxIntensity) {
		this.maxIntensity = new String(maxIntensity);
	}
	
	public String getName() {
		return name;
	}
	
	public short getYearStormNumber() {
		return yearStormNumber;
	}
	
	public short getStormNumber() {
		return stormNumber;
	}
	
	public short getYear() {
		return stormYear.getYear();
	}
	
	public short getMonth() {
		return month;
	}
	
	public short getDayOfMonth() {
		return dayOfMonth;
	}
	
	public int getStormDayCount() {
		return numStormDays;
	}
	
	public boolean getMadeLandfall() {
		return madeLandfall;
	}
	
	public StormCategory getLandfallCategory() {
		return landfallCategory;
	}
	
	public String getMaxIntensity() {
		return maxIntensity;
	}
	
	public void addStormDay(StormDay day) {
		stormDays.add(day);
	}
	
	public StormDay getStormDay(int idx) {
		return (StormDay)stormDays.get(idx);
	}
	
	public void addLandfallData(LandfallData data) {
		landfallData.add(data);
	}
	
	public int getLandfallDataCount() {
		return landfallData.size();
	}
	
	public Iterator getLandfallDataIterator() {
		return landfallData.iterator();
	}
	
	public static Storm parseStormString(String str, StormYear stormYear) {
		short yearStormNumber = Short.parseShort(str.substring(22, 25).trim());
		short stormNumber = Short.parseShort(str.substring(30, 34).trim());
		short month = Short.parseShort(str.substring(6, 8).trim());
		short dayOfMonth = Short.parseShort(str.substring(9, 11).trim());
		String name = str.substring(35, 47).trim();
		int numStormDays = Integer.parseInt(str.substring(19, 21).trim());
		short year = Short.parseShort(str.substring(12, 16).trim());
		boolean madeLandfall = false;
		StormCategory landfallCategory = null;
		if (str.charAt(52) == '1') {
			madeLandfall = true;
			String landfallCategoryString = "";
			
			if (str.length() >= 59) {
				landfallCategoryString = str.substring(58, 59).trim();
			}
//			String landfallCategoryString = str.substring(58, 59).trim();
			short cat;
			if (landfallCategoryString.isEmpty()) {
				cat = 0;
			} else {
				cat = Short.parseShort(landfallCategoryString);
			}
			landfallCategory = new StormCategory(cat);
		}
		return new Storm(name, yearStormNumber, stormNumber, stormYear, month, dayOfMonth, madeLandfall, landfallCategory, numStormDays);
	}
	
	public String toString() {
		return " #" + yearStormNumber + " " + name;
	}
}
