/**
 * 
 */
package tourguide;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author pbj
 */
public class ControllerImp implements Controller {
    private static Logger logger = Logger.getLogger("tourguide");
    private static final String LS = System.lineSeparator();

    private enum MODE {CREATE, BROWSE}

    private MODE currentMode;

    private List<Tour> allTours = new ArrayList<>();


    private Tour currentTour;

    private String startBanner(String messageName) {
        return  LS 
                + "-------------------------------------------------------------" + LS
                + "MESSAGE: " + messageName + LS
                + "-------------------------------------------------------------";
    }

    public ControllerImp(double waypointRadius, double waypointSeparation) {
    }

    //--------------------------
    // Create tour mode
    //--------------------------

    // Some examples are shown below of use of logger calls.  The rest of the methods below that correspond 
    // to input messages could do with similar calls.

    @Override
    public Status startNewTour(String id, String title, Annotation annotation) {
        logger.fine(startBanner("startNewTour"));
        currentTour = new Tour();
        currentTour.id = id;
        currentTour.title = title;
        currentTour.annotation = annotation;
        currentTour.numberLegs = 0;
        currentTour.numberWaypoints = 0;
        currentMode = MODE.CREATE;
        return Status.OK;
    }

    @Override
    public Status addWaypoint(Annotation annotation) {
        logger.fine(startBanner("addWaypoint"));
        currentTour.numberWaypoints++;
        return Status.OK;
    }

    @Override
    public Status addLeg(Annotation annotation) {
        logger.fine(startBanner("addLeg"));
        currentTour.numberLegs++;
        return Status.OK;
    }

    @Override
    public Status endNewTour() {
        logger.fine(startBanner("endNewTour"));
        currentMode = MODE.BROWSE;
        allTours.add(currentTour);
        return Status.OK;
    }

    //--------------------------
    // Browse tours mode
    //--------------------------

    @Override
    public Status showTourDetails(String tourID) {
        return new Status.Error("unimplemented");
    }
  
    @Override
    public Status showToursOverview() {
        currentMode = MODE.BROWSE;
        return Status.OK;
    }

    //--------------------------
    // Follow tour mode
    //--------------------------
    
    @Override
    public Status followTour(String id) {
        return new Status.Error("unimplemented");
    }

    @Override
    public Status endSelectedTour() {
        return new Status.Error("unimplemented");
    }

    //--------------------------
    // Multi-mode methods
    //--------------------------
    @Override
    public void setLocation(double easting, double northing) {
    }

    @Override
    public List<Chunk> getOutput() {
        List<Chunk> output = new ArrayList<>();
        if (currentMode == MODE.CREATE) {
            output.add(new Chunk.CreateHeader(currentTour.title,currentTour.numberLegs,currentTour.numberWaypoints));
        } else if (currentMode == MODE.BROWSE){
            Chunk.BrowseOverview overview = new Chunk.BrowseOverview();
            for (Tour tour : allTours) {
                overview.addIdAndTitle(tour.id, tour.title);
            }
            output.add(overview);
        }
        return output;
    }


}
