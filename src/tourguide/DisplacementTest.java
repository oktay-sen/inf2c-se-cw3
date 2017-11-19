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

    @Test
    public void testSouthBearing() {
        double bearing = new Displacement(0.0, -1.0).bearing();
        assertEquals(180.0, bearing, EPS);
    }

    @Test
    public void testEastBearing() {
        double bearing = new Displacement(1.0, 0.0).bearing();
        assertEquals(90.0, bearing, EPS);
    }

    @Test
    public void testWestBearing() {
        double bearing = new Displacement(-1.0, 0.0).bearing();
        assertEquals(270.0, bearing, EPS);
    }

    @Test
    public void testNEBearing() {
        double bearing = new Displacement(1.0, 1.0).bearing();
        assertEquals(45.0, bearing, EPS);
    }
    @Test
    public void testSEBearing() {
        double bearing = new Displacement(1.0, -1.0).bearing();
        assertEquals(135.0, bearing, EPS);
    }
    @Test
    public void testNWBearing() {
        double bearing = new Displacement(-1.0, 1.0).bearing();
        assertEquals(315.0, bearing, EPS);
    }
    @Test
    public void testSWBearing() {
        double bearing = new Displacement(-1.0, -1.0).bearing();
        assertEquals(225.0, bearing, EPS);
    }

    @Test
    public void testDistance1() {
        double distance = new Displacement(3.0, 4.0).distance();
        assertEquals(5.0, distance, EPS);
    }

    @Test
    public void testDistance2() {
        double distance = new Displacement(-3.0, 4.0).distance();
        assertEquals(5.0, distance, EPS);
    }

    @Test
    public void testDistance3() {
        double distance = new Displacement(3.0, -4.0).distance();
        assertEquals(5.0, distance, EPS);
    }

    @Test
    public void testDistance4() {
        double distance = new Displacement(-3.0, -4.0).distance();
        assertEquals(5.0, distance, EPS);
    }
}
