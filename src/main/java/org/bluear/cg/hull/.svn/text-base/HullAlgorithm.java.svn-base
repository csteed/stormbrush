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
import java.util.Collection;
import java.util.List;

/**
 * Defines a generic convex hull algorithm.
 * 
 * @author Bo Majewski
 */
public abstract class HullAlgorithm {
    
    // -------------------------------------------------------------------------
    // Constants
    // -------------------------------------------------------------------------
    
    /**
     * Indicates that the given point is to the left of the segment.
     */
    public static final int LEFT = 0;
    
    /**
     * Indicates that the given point is to the right of the segment.
     */
    public static final int RIGHT = 1;
    
    /**
     * Indicates that the given point is beyond the segment.
     */
    public static final int BEYOND = 2;
    
    /**
     * Indicates that the given point is behind the segment.
     */
    public static final int BEHIND = 3;
    
    /**
     * Indicates that the given point is between the segment end points.
     */
    public static final int BETWEEN = 4;
    
    /**
     * Indicates that the given point is at the origin of the segment.
     */
    public static final int ORIGIN = 5;
    
    /**
     * Indicates that the given point is at the destination of the segment.
     */
    public static final int DESTINATION = 6;

    // -------------------------------------------------------------------------
    // Methods
    // -------------------------------------------------------------------------
    
    /**
     * Returns the array of indices of points that are part of the convex
     * hull.
     * 
     * @param points A collection of <code>Point2D</code> objects.
     * @return The list of points that are vertices of a convex hull.
     */
    public abstract List getHullPoints(Collection points);
    
    // -------------------------------------------------------------------------
    // Helper methods
    // -------------------------------------------------------------------------
    
    /**
     * Classifies the location of the point (x2, y2) relatively to the segment
     * defined by points (x0, y0) and (x1, y1). The following illustrates
     * regions considered by this method:
     * <pre>
     * 
     *           LEFT
     *                         BEYOND
     *                          .
     *                     DESTINATION
     *                        /
     *                    BETWEEN          
     *                      /             RIGHT
     *                  ORIGIN
     *                 .
     *          BEHIND
     * </pre>
     * @param x0 The x of the starting point of the segment.
     * @param y0 The y of the starting point of the segment.
     * @param x1 The x of the final point of the segment.
     * @param y1 The y of the final point of the segment.
     * @param x2 The x of the point whose position we are testing.
     * @param y2 The y of the point whose position we are testing.
     */
    public static int classify(double x0, double y0,
                               double x1, double y1,
                               double x2, double y2) {
        double dx1 = x1 - x0;
        double dx2 = x2 - x0;
        double dy1 = y1 - y0;
        double dy2 = y2 - y0;
        double sa = dx1 * dy2 - dx2 * dy1;
        
        if (sa > 0.0) {
            return (LEFT);
        }
        
        if (sa < 0.0) {
            return (RIGHT);
        }
        
        if (dx1 * dx2 < 0.0 || dy1 * dy2 < 0.0) {
            return (BEHIND);
        }
        
        if (dx1 * dx1 + dy1 * dy1 < dx2 * dx2 + dy2 * dy2) {
            return (BEYOND);
        }
        
        if (x2 == x0 && y2 == y0) {
            return (ORIGIN);
        }
        
        if (x2 == x1 && y2 == y1) {
            return (DESTINATION);
        }

        return (BETWEEN);                
    }

    /**
     * Returns a code indicating where point <code>c</code> is located with 
     * respect to the segment formed by points <code>a</code> and <code>b</code>.
     * 
     * @param a The first point of the segment.
     * @param b The second point of the segment.
     * @param c The point classified with respect to the segment.
     * @return The code indicating the relative location of c.
     */    
    public static int classify(Point2D a, Point2D b, Point2D c) {
        return (classify(a.getX(), a.getY(), b.getX(), b.getY(), c.getX(), c.getY()));
    }
    
    /**
     * Returns whether or not the tree given points are co-linear.
     * 
     * @param p1 The first point.
     * @param p2 The second points.
     * @param p3 The third point.
     * @return Whether or not the three points are co-linear.
     */
    public static boolean colinear(Point2D p1, Point2D p2, Point2D p3) {
        return (p1.getX()*(p2.getY() - p3.getY()) 
                + p2.getX()*(p3.getY() - p1.getY())
                + p3.getX()*(p1.getY() - p2.getY())) == 0.0;
    }
}
