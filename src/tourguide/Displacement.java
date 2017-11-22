package tourguide;

import java.util.logging.Logger;

/**
 * A class that represents the displacement between two points.
 * Contains methods that do two-dimentional calculations on this displacement.
 */
public class Displacement {
    private static Logger logger = Logger.getLogger("tourguide");

    /**
     * The amount of displacement due east.
     */
    public double east;

    /**
     * The amount of displacement due north.
     */
    public double north;

    /**
     * Creates a displacement object given the difference between two points' coordinates.
     * @param e The difference between the two points' eastings.
     * @param n The difference between the two points' northings.
     */
    public Displacement(double e, double n) {
        logger.finer("East: " + e + "  North: "  + n);
        
        east = e;
        north = n;
    }

    /**
     * Calculates the distance covered by this displacement.
     * @return The distance covered.
     */
    public double distance() {
        logger.finer("Entering");
        
        return Math.sqrt(east * east + north * north);
    }

    /**
     * Calculates the bearing of the displacement.
     * @return The bearing in degrees, starting from North, clockwise.
     */
    public double bearing() {
        logger.finer("Entering");
              
        // atan2(y,x) computes angle from x-axis towards y-axis, returning a negative result
        // when y is negative.
        
        double inRadians = Math.atan2(east, north);
        
        if (inRadians < 0) {
            inRadians = inRadians + 2 * Math.PI;
        }
        
        return Math.toDegrees(inRadians);
    }
        
    
    
}
