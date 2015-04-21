
public class StormCategory {
	private short category;
	
	public StormCategory(short category) {
		this.category = category;
	}
	
	public short getCategory() {
		return category;
	}
	
	public String toString() {
		if (category == 1) {
			return "CATEGORY_1";
		} else if (category == 2) {
			return "CATEGORY_2";
		} else if (category == 3) {
			return "CATEGORY_3";
		} else if (category == 4) {
			return "CATEGORY_4";
		} else if (category == 5) {
			return "CATEGORY_5";
		}
		
		return "NON-HURRICANE";
	}
		
	public static StormCategory categoryFromWindSpeed(float windSpeed) {
		if (windSpeed < 74.) {
			return NON_HURRICANE;
		} else if (windSpeed <= 95.) {
			return CATEGORY_1;
		} else if (windSpeed <= 110.) {
			return CATEGORY_2;
		} else if (windSpeed <= 130.) {
			return CATEGORY_3;
		} else if (windSpeed <= 155.) {
			return CATEGORY_4;
		}
		return CATEGORY_5;
	}
	
	public static final StormCategory NON_HURRICANE = new StormCategory((short)0);
	public static final StormCategory CATEGORY_1 = new StormCategory((short)1);
	public static final StormCategory CATEGORY_2 = new StormCategory((short)2);
	public static final StormCategory CATEGORY_3 = new StormCategory((short)3);
	public static final StormCategory CATEGORY_4 = new StormCategory((short)4);
	public static final StormCategory CATEGORY_5 = new StormCategory((short)5);
}
