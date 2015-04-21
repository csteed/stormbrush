
public class LandfallData {
	private String location;
	private short intensity = -1;
	
	LandfallData (String location, short intensity) {
		this.location = new String(location);
		this.intensity = intensity;
	}
	
	public short getIntensity() {
		return intensity;
	}
	
	public String getLocation() {
		return location;
	}
	
	public void setIntensity(short intensity) {
		this.intensity = intensity;
	}
	
	public void setLocation(String location) {
		this.location = new String(location);
	}
	
	public String toString() {
		return "[LandfallData: location="+location+" intensity="+intensity+"]";
	}
}
