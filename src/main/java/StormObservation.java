
public class StormObservation {
	public short hour;
	public float latitude;
	public float longitude;
	public char stage;
	public float wind;
	public float pressure;
	public float temperatureAnomaly = (float)0.;
	
	StormObservation(float latitude, float longitude,
			         char stage, float wind, float pressure){
		this.latitude = latitude;
		this.longitude = longitude;
		this.stage = stage;
		this.wind = wind;
		this.pressure = pressure;
	}
	
	public short getHour() {
		return hour;
	}
	
	public float getLatitude() {
		return latitude;
	}
	
	public float getLongitude() {
		return longitude;
	}
	
	public char getStage() {
		return stage;
	}
	
	public float getWind() {
		return wind;
	}
	
	public float getPressure() {
		return pressure;
	}
	
	public float getTemperatureAnomaly() {
		return temperatureAnomaly;
	}
	
	public void setTemperatureAnomaly(float temperatureAnomaly) {
		this.temperatureAnomaly = temperatureAnomaly;
	}
	
	public void setHour(short hour) {
		this.hour = hour;
	}
	
	public String toString() {
		return "[StormObservation: hour="+hour+" latitude="+latitude+" longitude="+longitude+
		       "stage="+stage+" wind="+wind+" pressure="+pressure+" temperatureAnomaly="+temperatureAnomaly+"]";
	}
	
	public static StormObservation parseStormObservationString(String str) {
//		System.out.println("StormObs String is '" + str + "'");
		if (str.length() != 17)
			return null;
		
		char stage = str.charAt(0);
		
		String tmp = str.substring(1, 4).trim();
		if (tmp.length() == 0)
			return null;
		int ilatitude = Integer.parseInt(tmp);
//		float latitude = Float.parseFloat(tmp)/(float)10.;
		
		tmp = str.substring(4, 8).trim();
		if (tmp.length() == 0)
			return null;
		int ilongitude = Integer.parseInt(tmp);
		

		
		tmp = str.substring(8,12).trim();
		if (tmp.length() == 0)
			return null;
		int iwind = Integer.parseInt(tmp);
//		float wind = Float.parseFloat(tmp);
		
		tmp = str.substring(12,17).trim();
		if (tmp.length() == 0)
			return null;
		int ipressure = Integer.parseInt(tmp);
//		float pressure = Float.parseFloat(tmp);
		
		if (ilatitude == 0 && ilongitude == 0 && iwind == 0 && ipressure == 0)
			return null;
		
		float longitude = (float)ilongitude/(float)10.;
		if (longitude < 180. && longitude >= 0.)
			longitude *= -1.;
		else 
			longitude = (float)360. - longitude;
		
		StormObservation obs = new StormObservation((float)ilatitude/(float)10., longitude, stage, (float)iwind, (float)ipressure);
		return obs;
	}
}
