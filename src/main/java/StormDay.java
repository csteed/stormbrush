import java.util.ArrayList;
import java.util.Iterator;

public class StormDay {
	private short year;
	private short month;
	private short dayOfMonth;
	private short dayOfYear;
	private ArrayList observations;
	
	StormDay(short month, short dayOfMonth) {
		this.month = month;
		this.dayOfMonth = dayOfMonth;
		observations = new ArrayList();
	}
	
	public boolean isHurricane() {
		Iterator iter = observations.iterator();
		
		while (iter.hasNext()) {
			StormObservation obs = (StormObservation)iter.next();
			if (obs.wind >= 74.f) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isIntenseHurricane() {
		Iterator iter = observations.iterator();
		while (iter.hasNext()) {
			StormObservation obs = (StormObservation)iter.next();
			if (obs.wind >= 111.f) {
				return true;
			}
		}
		return false;
	}
	
	public void setDayOfYear(short dayOfYear) {
		this.dayOfYear = dayOfYear;
	}
	
	public short getDayOfYear() {
		return dayOfYear;
	}
	
	public void setYear(short year) {
		this.year = year;
	}
	
	public short getYear() {
		return year;
	}
	
	public short getMonth() {
		return month;
	}
	
	public short getDayOfMonth() {
		return dayOfMonth;
	}
	
	public void setMonth(short month) {
		this.month = month;
	}
	
	public void setDayOfMonth(short dayOfMonth) {
		this.dayOfMonth = dayOfMonth;
	}
	
	public void addObservation(StormObservation observation) {
		observations.add(observation);
	}
	
	public StormObservation getObservation(int observationIndex) {
		return (StormObservation)observations.get(observationIndex);
	}
	
	public int getObservationCount() {
		return observations.size();
	}
	
	public Iterator getObservationIterator() {
		return observations.iterator();
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("[StormDay: year="+year+" month="+month+" dayOfMonth="+dayOfMonth+"\n");
		Iterator iter = observations.iterator();
		while (iter.hasNext()) {
			buf.append(iter.next().toString() + "\n");
		}
		return buf.toString();
	}
	
	public static StormDay parseStormDayString(String str) {
		short month = Short.parseShort(str.substring(6, 8).trim());
		short dayOfMonth = Short.parseShort(str.substring(9, 11).trim());
		
		StormDay stormDay = new StormDay(month, dayOfMonth);
		int lineStrIdx = 11;
		for (int itime = 0; itime < 4; itime++) {
			int idx = lineStrIdx + (itime*17);
			/*if (str.length() < lineStrIdx+17)
				break;*/
			String timeStr = str.substring(idx, idx+17).trim();
			/*if (timeStr.length() != 17)
				break;*/
			StormObservation obs = StormObservation.parseStormObservationString(timeStr);
			if (obs != null) {
				obs.setHour((short)(itime*6));
				stormDay.observations.add(obs);
			}
		}
		
		return stormDay;
	}
}
