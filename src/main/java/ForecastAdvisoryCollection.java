import java.io.Serializable;
import java.util.ArrayList;

public class ForecastAdvisoryCollection implements Serializable{
	public static final long serialVersionUID = 1L;

	public String stormName;
	public int stormYear;
	public ArrayList<ForecastAdvisory> advisoryList;
	public short maxWind;
	public short minWind;
	public short maxGust;
	public short minGust;
	public short maxEyeDiameter;
	public short minEyeDiameter;
	public short maxMovementSpeed;
	public short minMovementSpeed;
	public float maxLongitude;
	public float minLongitude;
	public float maxLatitude;
	public float minLatitude;
	
	public ForecastAdvisoryCollection (String stormName, int stormYear) {
		this.stormName = stormName;
		this.stormYear = stormYear;
		advisoryList = new ArrayList<ForecastAdvisory>();
	}
}
