package tourguide;

import java.util.List;

public interface Controller {

//    private List<Tour> tours;
//    Tour currentTour;
//    private int mode;

    /*
     * Create tour mode
     */
    Status startNewTour(String id, String title, Annotation annotation);

    Status addWaypoint(Annotation annotation);

    Status addLeg(Annotation annotation);

    Status endNewTour();
    
    /*
     * Browse tours mode
     */
    Status showTourDetails(String id);

    Status showToursOverview();

    /*
     * Follow tour
     */
    Status followTour(String id);

    Status endSelectedTour();

    /*
     *  All modes
     */
    void setLocation(double easting, double northing);

    List<Chunk> getOutput();
}