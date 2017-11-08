/**
 * 
 */
package tourguide;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author pbj
 */
public class DisplacementTest {
    /**
     * EPS = Epsilon, the difference to allow in floating point numbers when 
     * comparing them for equality.
     */
    private static final double EPS = 0.01; 
    
    @Test
    public void testNorthBearing() {
        double bearing = new Displacement(0.0, 1.0).bearing();
        assertEquals(0.0, bearing, EPS);
    }
 
}
