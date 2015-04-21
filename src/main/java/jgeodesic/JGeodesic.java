package jgeodesic;

import java.io.*;
import java.util.*;

public class JGeodesic {

	/* 
	 * flat = 1 is great circle path
	 * flat = 0 is geodesic path
	 * s is to be in meters
	 * faz is in radians from north direction
	 * lat and lon are in radians positive north and east
	 */
	public static double[] direct1(double slat, double slon, double faz, double s, int flat) {
	    double baz = 0.0;
		double elat, elon;

		double pi = 3.141592654, eps = 0.5e-13;
		double a = 6378137.0, f = 1.0 / 298.25722210088;
		double tu, cu, su, sf, cf, x, sy = 0., cy = 0., y, sa, c2a, cz = 0.;
		double e = 0., c, d, r, test;

		if (flat == 1)
			f = 0.0;

		r = 1.0 - f;
		tu = r * Math.sin(slat) / Math.cos(slat);
		sf = Math.sin(faz);
		cf = Math.cos(faz);
		if (cf != 0.0)
			baz = Math.atan2(tu, cf) * 2.0;
		cu = 1.0 / Math.sqrt(tu * tu + 1.0);
		su = tu * cu;
		sa = cu * sf;
		c2a = -sa * sa + 1.0;
		x = Math.sqrt((1.0 / r / r - 1.0) * c2a + 1.0) + 1.0;
		x = (x - 2.0) / x;
		c = 1.0 - x;
		c = (x * x / 4.0 + 1.0) / c;
		d = (0.375 * x * x - 1.0) * x;
		tu = s / r / a / c;
		y = tu;
		test = 1.0;

		while (test > eps) {
			sy = Math.sin(y);
			cy = Math.cos(y);
			cz = Math.cos(baz + y);
			e = cz * cz * 2.0 - 1.0;
			c = y;
			x = e * cy;
			y = e + e - 1.0;
			y = (((sy * sy * 4.0 - 3.0) * y * cz * d / 6.0 + x) * d / 4.0 - cz)	* sy * d + tu;
			test = Math.abs(y - c);
		}

		baz = cu * cy * cf - su * sy;
		c = r * Math.sqrt(sa * sa + (baz) * (baz));
		d = su * cy + cu * sy * cf;
		elat = Math.atan2(d, c);
		c = cu * cy - su * sy * cf;
		x = Math.atan2(sy * sf, c);
		c = ((-3.0 * c2a + 4.0) * f + 4.0) * c2a * f / 16.0;
		d = ((e * cy * c + cz) * sy * c + y) * sa;
		elon = slon + x - (1.0 - c) * d * f;
		baz = Math.atan2(sa, baz) + pi;
		
	    return new double[] {elat, elon, baz};
	}
	
	public static double[] invers1(double slat, double slon, double elat, 
			                       double elon, int flat) {
		double s, baz, faz;

		double pi = 3.141592654, eps = 0.5e-13;
		double a = 6378137.0, f = 1.0 / 298.25722210088;
		double tu1, tu2, cu1, su1, cu2, x, sx = 0., cx = 0., sy = 0., cy = 0., y = 0., sa, c2a = 0., cz = 0.;
		double e = 0., c, d, r;

		if (flat == 1)
			f = 0.0;

		r = 1.0 - f;
		tu1 = r * Math.sin(slat) / Math.cos(slat);
		tu2 = r * Math.sin(elat) / Math.cos(elat);
		cu1 = 1.0 / Math.sqrt(tu1 * tu1 + 1.0);
		su1 = cu1 * tu1;
		cu2 = 1.0 / Math.sqrt(tu2 * tu2 + 1.0);
		s = cu1 * cu2;
		baz = s * tu2;
		faz = baz * tu1;
		x = elon - slon;

		d = x + 1.0;

		while (Math.abs(d - x) > eps) {
			sx = Math.sin(x);
			cx = Math.cos(x);
			tu1 = cu2 * sx;
			tu2 = baz - su1 * cu2 * cx;
			sy = Math.sqrt(tu1 * tu1 + tu2 * tu2);
			cy = s * cx + faz;
			y = Math.atan2(sy, cy);
			sa = s * sx / sy;
			c2a = -sa * sa + 1.0;
			cz = faz + faz;
			if (c2a > 0.0)
				cz = -cz / c2a + cy;
			e = cz * cz * 2.0 - 1.0;
			c = ((-3.0 * c2a + 4.0) * f + 4.0) * c2a * f / 16.0;
			d = x;
			x = ((e * cy * c + cz) * sy * c + y) * sa;
			x = (1.0 - c) * x * f + elon - slon;
		}

		faz = Math.atan2(tu1, tu2);
		baz = Math.atan2(cu1 * sx, baz * cx - su1 * cu2) + pi;
		x = Math.sqrt((1.0 / r / r - 1.0) * c2a + 1.0) + 1.0;
		x = (x - 2.0) / x;
		c = 1.0 - x;
		c = (x * x / 4.0 + 1.0) * x;
		d = (0.375 * x * x - 1.0) * x;
		x = e * cy;
		s = 1.0 - e - e;
		s = ((((sy * sy * 4.0 - 3.0) * (s) * cz * d / 6.0 - x) * d / 4.0 + cz) * sy * d + y) * c * a * r;

		return new double[] { faz, baz, s };
	}
	
	public static ArrayList geodesic(double slat, double slon, double elat, double elon,
			                             double ds, int ind) {
		return null;
	}
	
	public static void main(String[] args) throws Exception {
		double slat = 0., slon = 0., faz = 0., ds = 0.;
		int flat = 0;
		String line = null;
		
		BufferedReader bfr = new BufferedReader(new InputStreamReader(System.in));
		
		while (true) {
			System.out.println("Enter slat, slon, faz, ds, flat:");
			line = bfr.readLine();
			
			StringTokenizer st = new StringTokenizer(line);
			int token_count = 0;
			while (st.hasMoreTokens()) {
				String token = st.nextToken().trim();
//				System.out.println("token = '"+ token+"'");
				if (token_count == 0) {
					slat = Double.parseDouble(token);
				} else if (token_count == 1) {
					slon = Double.parseDouble(token);
				} else if (token_count == 2) {
					faz = Double.parseDouble(token);
				} else if (token_count == 3) {
					ds = Double.parseDouble(token);
				} else if (token_count == 4) {
					flat = Integer.parseInt(token);
				}
				token_count++;
			}
			
			System.out.println("You entered: slat="+slat+" slon="+slon+" faz="+faz+" ds="+ds+" flat="+flat);
			
			// call direct routine
			System.out.println("Calling direct rountine...");
			
			double result[] = direct1(slat*(Math.PI/180.), slon*(Math.PI/180.), faz*(Math.PI/180.), ds, flat);
			
			// print results
			System.out.println("Results:");
			System.out.println("elat="+(result[0]*(180./Math.PI))+" elon="+(result[1]*(180./Math.PI))+" baz="+(result[2]*(180./Math.PI)));
			
			
		}
	}

}
