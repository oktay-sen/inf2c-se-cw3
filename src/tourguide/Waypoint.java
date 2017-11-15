package tourguide;

public class Waypoint {
    public double east, north;
    public Annotation annotation;

    public Waypoint(double east, double north, Annotation annotation) {
        this.east = east;
        this.north = north;
        this.annotation = annotation;
    }
}
