/*
 * Project Hull 
 * Copyright (C) 2003  Bo Majewski
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * 
 * Created on Dec 30, 2003
 */
package org.bluear.cg.hull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Implements the hull algorithm using the Graham Scan. The algorithm finds
 * an extreme point, the pivot. Next, it sorts all points around the pivot,
 * arranging them from right to left. Finally it adds points to the hull 
 * making sure that only the points that cause the hull's edges to turn
 * left are added. The complexity of the algorithm is <i>O(n log n)</i>, which
 * is close to the optimal <i>O(n log h)</i>, where <i>n</i> is the total 
 * number of points, and <i>h</i> is the number of vertices in the convex
 * hull. The algorithm was originally published in R. L. Graham, An efficient
 * algorithm for determining the convex hull of a finate planar set, 
 * <i>Information Processing Letters</i>, 1, 132-133 (1972).
 * 
 * @author Bo Majewski
 * @version 1.0
 */
public class GrahamScanHull extends HullAlgorithm {

	/**
	 * Implements Graham Scan algorithm for finding convex hull for a 
     * collection of points.
     * 
	 * @see org.bluear.cg.hull.HullAlgorithm#getHullPoints(java.util.Collection)
	 */
	public List getHullPoints(Collection points) {
        int i;
        
        // deal with abnormal cases first ...
        if (points == null || points.size() <= 0) {
            return (Collections.EMPTY_LIST);
        }
        
        int n = 0;
        
        // count the non-null points ...
        for (Iterator iter = points.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            
            if (obj instanceof Point2D) {
                ++ n;
            }
        }
        
        if (n <= 0) {
            return (Collections.EMPTY_LIST);
        }
        
        Point2D[] pt = new Point2D[n];
        i = 0;
        
        for (Iterator iter = points.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            
            if (obj instanceof Point2D) {
                pt[i ++] = (Point2D) obj;
            }
        }
        
        ArrayList hull = new ArrayList();

        // Graham scan does not do too well if there are only 1 or 2 points ...
        if (n < 3) {
            hull.add(pt[0]);
            
            if (n == 2) {
                hull.add(pt[1]);            
            }
        }
        else {
        
            // Step 1: find extreme point p0
            int m = 0;
            Point2D pi, p0 = pt[0];
        
            for (i = 1; i < n; i++) {
                pi = pt[i];
            
                if (pi.getY() < p0.getY() || (pi.getY() == p0.getY() && pi.getX() < p0.getX())) {
                    m = i;
                    p0 = pi;
                }
            }
        
            if (m != 0) {
                pt[m] = pt[0];
                pt[0] = p0;
            }
        
            // Step 2: sort points by their polar coordinate relative to p0
            Arrays.sort(pt, 1, n, new PolarComparator(p0));
        
            // Step 3: Find the second point of the hull ...
            i = 1;
        
            while (i+1 < n && colinear(p0, pt[i], pt[i+1])) {
                ++ i;
            }
        
            hull.add(p0);
            hull.add(pt[i]);
        
            // Step 4: Grow the current hull until it includes all points
            for (i = i + 1; i < n; ++ i) {
                pi = pt[i];
                Point2D p1 = (Point2D) hull.get(hull.size() - 1);
                p0 = (Point2D) hull.get(hull.size() - 2);
            
                while (LEFT != classify(p0, p1, pi)) {
                    p1 = p0;
                    hull.remove(hull.size() - 1);
                    p0 = (Point2D) hull.get(hull.size() - 2);
                }
            
                hull.add(pi);
            }
        }

		return (hull);
	}

    /**
     * Local class used by the sort method to classify points based on their
     * polar coordinates with respect to the specified pivot point, p0.
     * 
     * @author Bo Majewski
     */
    private static class PolarComparator implements Comparator {
        
        private Point2D p0;
        
        /**
         * Creates a new comparator that used the given pivot point.
         * 
         * @param p0 The pivot point.
         */
        public PolarComparator(Point2D p0) {
            this.p0 = p0;
        }

		/**
		 * Compares object o1 to object o2. This method returns a negative
         * value if o1 lies to the left of o2 with respect to the pivot p0.
         * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
            Point2D p1 = (Point2D) o1;
            Point2D p2 = (Point2D) o2;
            
            // take care of degenerate cases first ...
            if (p1.equals(this.p0)) {
                return (p2.equals(this.p0)? 0 : -1);
            }
            
            if (p2.equals(this.p0)) {
                return (1);
            }
            
            // this is when neither p1 nor p2 is identical to pivot ...
            double d = ((p2.getX() - p0.getX())*(p1.getY() - p0.getY()) 
                       - (p1.getX() - p0.getX())*(p2.getY() - p0.getY()));
            
            return (d < 0.0? -1 : d > 0.0? 1 : 0);
		}
    }
}
