import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.TreeMap;

public class StormYear {
	public short year;
	public short numNamedStorms = Short.MIN_VALUE;
	public float numNamedStormDays = Float.NaN;
	public short numHurricanes = Short.MIN_VALUE;
	public float numHurricaneDays = Float.NaN;
	public short numIntenseHurricanes = Short.MIN_VALUE;
	public float numIntenseHurricaneDays = Float.NaN;
	public short netTropicalCycloneActivity = Short.MIN_VALUE;  /*NTC*/
	public float ACE = Float.NaN;
	public float HDP = Float.NaN;
	
	/* August predictors */
	public float JUN_JUL_SST = Float.NaN;
	public float JUN_JUL_SLP = Float.NaN;
	public float JUN_JUL_NINO3 = Float.NaN;
	public float JUN_JUL_NSD = Float.NaN;
	
	/* April predictors */
	public float FEB_MAR_MB_200_U = Float.NaN;
	public float FEB_MAR_MB_200_V = Float.NaN;
	public float FEB_SLP = Float.NaN;
	public float FEB_SST = Float.NaN;
	public float NOV_MB_500_GH = Float.NaN;
	public float SEP_NOV_SLP = Float.NaN;
	
	/* predictors */
//	public float NOV_MB_500_GH = Float.NaN;
	public float OCT_NOV_SLP = Float.NaN;
	public float SEP_MB_500_GH = Float.NaN;
	public float JUL_MB_50_U = Float.NaN;
//	public float SEP_NOV_SLP = Float.NaN;
	public float NOV_SLP = Float.NaN;
	
	
	/* predictors */
	public float MAY_SST = Float.NaN;
	public float APR_MAY_SST = Float.NaN;
	public float MAR_APR_SLP = Float.NaN;
	public float PREV_NOV_MB_500_GH = Float.NaN;
	
	/* Emanuel Data */
	public float AUG_OCT_SST_SCALED = Float.NaN;
	public float AUG_OCT_SST_C = Float.NaN;
	public float AUG_OCT_SST_F = Float.NaN;
	public float PDI = Float.NaN;
	
	private TreeMap idStormMap;
	private TreeMap nameStormMap;
	
	StormYear(short year) {
		this.year = year;
		idStormMap = new TreeMap();
		nameStormMap = new TreeMap();
	}
	
	public void calculateStatistics() {
		ArrayList stormDays = new ArrayList();
		ArrayList hurricaneDays = new ArrayList();
		ArrayList intenseDays = new ArrayList();
		
		numNamedStormDays = numHurricaneDays = numIntenseHurricaneDays = 0.f;
		numHurricanes = numIntenseHurricanes = 0;
		
		Iterator stormIterator = idStormMap.values().iterator();
		while (stormIterator.hasNext()) {
			Storm storm = (Storm)stormIterator.next();
			boolean isNamedStorm = false;
			boolean isHurricane = false;
			boolean isIntenseHurricane = false;
			
			Iterator dayIterator = storm.getStormDayIterator();
			while (dayIterator.hasNext()) {
				StormDay day = (StormDay)dayIterator.next();
				
				Iterator obsIterator = day.getObservationIterator();
				
				while (obsIterator.hasNext()) {
					StormObservation obs = (StormObservation)obsIterator.next();
					
					Calendar date = Calendar.getInstance();
					date.set(year, day.getMonth(), day.getDayOfMonth(), obs.getHour(), 0, 0);
					
					if (obs.wind >= 39.f) {
						isNamedStorm = true;
						if (!stormDays.contains(date)) {
							stormDays.add(date);
							numNamedStormDays += 0.25f;
						}
					}
					
					if (obs.wind >= 74.f) {
						isHurricane = true;
						if (!hurricaneDays.contains(date)) {
							hurricaneDays.add(date);
							numHurricaneDays += 0.25f;
						}
					}
					
					if (obs.wind >= 111.f) {
						isIntenseHurricane = true;
						if (!intenseDays.contains(date)) {
							intenseDays.add(date);
							numIntenseHurricaneDays += 0.25f;
						}
					}
				}
			}
			if (isNamedStorm) {
				numNamedStorms++;
			}
			if (isHurricane) {
				numHurricanes++;
			}
			if (isIntenseHurricane) {
				numIntenseHurricanes++;
			}
		}
		
		System.out.println(year + " stats: NS="+numNamedStorms+" NSD="+numNamedStormDays+" NH="+numHurricanes+" NHD="+numHurricaneDays+" IH="+numIntenseHurricanes+" IHD="+numIntenseHurricaneDays);
	}
	
	public void addStorm(Storm storm) {
		nameStormMap.put(storm.getName().toUpperCase(), storm);
		idStormMap.put(new Short(storm.getYearStormNumber()), storm);
	}
	
	public short getYear() {
		return year;
	}
	
	public int getStormCount() {
		return idStormMap.size();
	}
	
	public Storm getStorm(String name) {
		return (Storm)nameStormMap.get(name.toUpperCase());
	}
	
	public Storm getStorm(short yearStormNumber) {
		return (Storm)idStormMap.get(new Short(yearStormNumber));
	}
	
	public Iterator getStormIterator() {
		return idStormMap.values().iterator();
	}
	
	public String toString() {
		StringBuffer buf = new StringBuffer("StormYear " + year + " - " + getStormCount() + " storms.\n");
		Iterator iter = getStormIterator();
		while (iter.hasNext()) {
			buf.append(((Storm)iter.next()).toString() + "\n");
		}
		return buf.toString();
	}
}
