/**
 * 
 */
package tourguide;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * @author pbj
 */
public class ControllerImp implements Controller {
    private static Logger logger = Logger.getLogger("tourguide");
    private static final String LS = System.lineSeparator();
    private double currentEast;
    private double currentNorth;

    private enum MODE {CREATE, BROWSE, DETAILS}

    private MODE currentMode;

    private HashMap<String, Tour> allTours = new HashMap<>();

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
        if (currentMode != MODE.BROWSE) return new Status.Error("Incorrect state");
        logger.fine(startBanner("startNewTour"));
        currentTour = new Tour(id, title, annotation);
        currentMode = MODE.CREATE;
        return Status.OK;
    }

    @Override
    public Status addWaypoint(Annotation annotation) {
        if (currentMode != MODE.CREATE) return new Status.Error("Incorrect state");

        logger.fine(startBanner("addWaypoint"));

        currentTour.waypoints.add(new Waypoint(currentEast, currentNorth, annotation));
        if (currentTour.legAnnotations.size() < currentTour.waypoints.size())
            currentTour.legAnnotations.add(Annotation.DEFAULT);
        return Status.OK;
    }

    @Override
    public Status addLeg(Annotation annotation) {
        if (currentMode != MODE.CREATE) return new Status.Error("Incorrect state");

        logger.fine(startBanner("addLeg"));

        if (currentTour.legAnnotations.size() > currentTour.waypoints.size())
            return new Status.Error("Too many legs.");
        currentTour.legAnnotations.add(annotation);
        return Status.OK;
    }

    @Override
    public Status endNewTour() {
        if (currentMode != MODE.CREATE) return new Status.Error("Incorrect state");
        if (currentTour.waypoints.size() < 1)
            return new Status.Error("Can't create tour without waypoints.");
        if (currentTour.legAnnotations.size() != currentTour.waypoints.size())
            return new Status.Error("Number of legs must be same as number of waypoints.");

        logger.fine(startBanner("endNewTour"));
        currentMode = MODE.BROWSE;
        allTours.put(currentTour.id, currentTour);
        return Status.OK;
    }

    //--------------------------
    // Browse tours mode
    //--------------------------

    @Override
    public Status showTourDetails(String tourID) {
        currentTour = allTours.get(tourID);
        currentMode = MODE.DETAILS;
        return Status.OK;
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
        currentEast = easting;
        currentNorth = northing;
    }

    @Override
    public List<Chunk> getOutput() {
        List<Chunk> output = new ArrayList<>();
        switch (currentMode) {
            case CREATE: {
                output.add(new Chunk.CreateHeader(currentTour.title,currentTour.legAnnotations.size(),currentTour.waypoints.size()));
                break;
            }
            case BROWSE: {
                Chunk.BrowseOverview overview = new Chunk.BrowseOverview();
                for (Tour tour : allTours.values()) {
                    overview.addIdAndTitle(tour.id, tour.title);
                }
                output.add(overview);
                break;
            }
            case DETAILS: {
                Chunk.BrowseDetails details = new Chunk.BrowseDetails(currentTour.id, currentTour.title, currentTour.annotation);
                output.add(details);
                break;
            }
        }
        return output;
    }


}
