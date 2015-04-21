import java.io.*;
import java.util.*;

public class ForecastAdvisory implements Serializable{
	public static final long serialVersionUID = 1L;
	
	public short year;
	public short month;
	public short day;
	public short hour;
	public float longitude;
	public float latitude;
	public short wind;
	public short gust;
	public short eye_diameter;
	public short movement_direction;
	public short movement_speed;
	public ArrayList windRadiiList;
	public AdvisoryArea seaRadii;
	
//	public ArrayList windRadiiList;
//	public short sea_radii[];
	
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(month+"."+day+"."+year+" "+hour+"00Z\n");
		buffer.append("  latitude="+ latitude+" longitude="+longitude + "\n");
		buffer.append("  wind="+wind+" gust="+gust +"\n");
		buffer.append("  eye_diameter="+eye_diameter+" movement_direction="+movement_direction+" movement_speed="+movement_speed+"\n");
		if (windRadiiList != null) {
			Iterator iter = windRadiiList.iterator();
			while (iter.hasNext()) {
				AdvisoryArea area = (AdvisoryArea)iter.next();
				buffer.append("    " + area.value + " KT...... " + area.ne + "NE  "+area.se +"SE  "+area.sw+"SW  "+area.nw+"NW.\n");
	//			short windRadiiArray [] = (short[])iter.next();
	//			buffer.append("    "+windRadiiArray[0] + " KT....... "+windRadiiArray[1]+"NE  "+windRadiiArray[2]+"SE  "+windRadiiArray[3]+"SW  "+windRadiiArray[4]+"NW.\n");
			}
		}
		if (seaRadii != null) {
			buffer.append("    "+seaRadii.value + " FT SEAS.. "+seaRadii.ne+"NE  "+seaRadii.se+"SE  "+seaRadii.sw+"SW  "+seaRadii.nw+"NW.\n");
		}
		return buffer.toString();
	}
}
