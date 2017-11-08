package tourguide;

import java.util.logging.Logger;

public class Displacement {
    private static Logger logger = Logger.getLogger("tourguide");
       
    public double east;
    public double north;
    
    public Displacement(double e, double n) {
        logger.finer("East: " + e + "  North: "  + n);
        
        east = e;
        north = n;
    }
    
    public double distance() {
        logger.finer("Entering");
        
        return Math.sqrt(east * east + north * north);
    }
    
    // Bearings measured clockwise from north direction.
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
