package tourguide;

public class Waypoint {
    public double east, north;
    public Annotation annotation;

    public Waypoint(double east, double north, Annotation annotation) {
        this.east = east;
        this.north = north;
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "Waypoint{" +
                "east=" + east +
                ", north=" + north +
                ", annotation=" + annotation +
                '}';
    }
}
