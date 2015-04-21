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
 * Created on Dec 29, 2003
 */

package org.bluear.cg.hull;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Implements a gift wrap strategy for computing convex hull. If the input
 * consists of n points and the output hull contains m &lt= n points this
 * algorithm finds a convex hull in O(n m) time. In the worst case the 
 * algorithm requires thus <i>O(n<sup>2</sup>)</i> time, while the best
 * case is when <i>m = 3</i> and the running time of the algorithm is 
 * then <i>O(n)</i>. The algorithm was originally published by R. A. Jarvis,
 * On the identification of the convex hull of a finite set of points 
 * in the plane, <i>Information Processing Letters</i>, 2 18-21 (1973).
 * 
 * @author Bo Majewski
 * @version 1.0
 */
public class JarvisMarchHull extends HullAlgorithm {
    /**
     * Returns a list definiting a convex hull for the collection of points 
     * given as the parameter of the call. This method implements the gift
     * wrapping convex hull algorithm. It starts with the leftmost point and
     * extends the hull by adding the edge to the leftmost point from the 
     * remaining points. The collection should contain <code>Point2D</code>
     * object, although the method will function correctly if it contains
     * <code>null</code> object or object of other types. 
     * 
     * @see HullAlgorithm#getHullPoints(java.util.Collection)
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
        
        // copy the non-null points ...
        int j = 0;
        Point2D[] pt = new Point2D[n];
        
        for (Iterator iter = points.iterator(); iter.hasNext(); ) {
            Object obj = iter.next();
            
            if (obj instanceof Point2D) {
                pt[j ++] = (Point2D) obj;
            }
        }

        // step 1: find the leftmost point ...
        int next = 0;
        
        // leftmost x and leftmost y in the entire collection ...
        Point2D lm = pt[0];

        for (i = 1; i < n; ++ i) {
            Point2D pi = pt[i];

            if (pi.getX() < lm.getX() || pi.getX() == lm.getX() && pi.getY() < lm.getY()) {
                next = i;
                lm = pi;
            }
        }
        
        // best point in the current iteration ...
        Point2D bp = lm;
        
        // step 2: keep adding leftmost points to the hull
        ArrayList hull = new ArrayList();
        
        for (i = 0; i < n; ++ i) {
            if (i != next) {
                pt[next] = pt[i];
                pt[i] = bp;
            }
            
            // insert the leftmost point into the hull ...
            hull.add(bp);
            
            if (i+1 >= n) {
                break;
            }
        
            next = i + 1;
            bp = pt[i + 1];
            
            for (j = i+2; j < n; ++ j) {
                Point2D pj = pt[j];
                int where = classify(pt[i], bp, pj);
                
                if (where == LEFT || where == BEYOND) {
                    next = j;
                    bp = pj;
                }
            }
            
            // the best point might be the one closing the polygon ...
            int where = classify(pt[i], bp, lm);
            
            if (where == LEFT || where == BEYOND) {
                break;
            }
        }

        return (hull);
    }
}
