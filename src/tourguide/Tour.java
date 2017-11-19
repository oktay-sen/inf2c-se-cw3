package tourguide;

import java.util.ArrayList;
import java.util.List;

public class Tour {

    public String id;
    public String title;
    public Annotation annotation;
    public List<Annotation> legAnnotations = new ArrayList<>();
    public List<Waypoint> waypoints = new ArrayList<>();

    public Tour(String id, String title, Annotation annotation) {
        this.id = id;
        this.title = title;
        this.annotation = annotation;
    }

    @Override
    public String toString() {
        return "Tour{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", annotation=" + annotation +
                ", legAnnotations=" + legAnnotations +
                ", waypoints=" + waypoints +
                '}';
    }
}
