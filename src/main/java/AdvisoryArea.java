import java.io.*;
import java.util.ArrayList;

public class AdvisoryArea implements Serializable {
	public static final long serialVersionUID = 1L;
	
	public Number value;
	public short ne, se, nw, sw;
	public ArrayList points;
}
