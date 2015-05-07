package stormstroke;

import java.io.Serializable;
import java.util.ArrayList;

public class ForecastAdvisoryCollection implements Serializable{
	public static final long serialVersionUID = 1L;
	public String stormName;
	public int stormYear;
	public ArrayList advisoryList;
	
	public ForecastAdvisoryCollection (String stormName, int stormYear, ArrayList advisoryList) {
		this.stormName = stormName;
		this.stormYear = stormYear;
		this.advisoryList = advisoryList;
	}
}
