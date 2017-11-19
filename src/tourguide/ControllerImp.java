/**
 * 
 */
package tourguide;

import java.util.*;
import java.util.logging.Logger;

/**
 * @author pbj
 */
public class ControllerImp implements Controller {
    private static Logger logger = Logger.getLogger("tourguide");
    private static final String LS = System.lineSeparator();


    private double currentEast;
    private double currentNorth;

    private enum MODE {CREATE, BROWSE, DETAILS, FOLLOW}

    private MODE currentMode;

    private Map<String, Tour> allTours = new HashMap<>();

    private Tour currentTour;
    private int currentStage;

    private double waypointRadius, waypointSeparation;

    private String startBanner(String messageName) {
        return  LS 
                + "-------------------------------------------------------------" + LS
                + "MESSAGE: " + messageName + LS
                + "-------------------------------------------------------------";
    }

    public ControllerImp(double waypointRadius, double waypointSeparation) {
        currentMode = MODE.BROWSE;
        this.waypointRadius = waypointRadius;
        this.waypointSeparation = waypointSeparation;
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

        //Check if waypoint is too close.
        if (currentTour.waypoints.size() > 0) {
            Waypoint last = currentTour.waypoints.get(currentTour.waypoints.size()-1);
            Displacement d = new Displacement(last.east - currentEast, last.north - currentNorth);
            if (d.distance() < waypointSeparation) return new Status.Error("Waypoint too close to the last.");
        }

        //Add new waypoint
        currentTour.waypoints.add(new Waypoint(currentEast, currentNorth, annotation));
        //If waypoint doesn't have annotation, add default annotation.
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
        if (currentMode != MODE.BROWSE) return new Status.Error("Incorrect state");
        if (!allTours.containsKey(tourID)) return new Status.Error("Tour " + tourID + " not found.");

        currentTour = allTours.get(tourID);
        currentMode = MODE.DETAILS;
        return Status.OK;
    }
  
    @Override
    public Status showToursOverview() {
        if (currentMode == MODE.CREATE) return new Status.Error("Can't browse while creating new tour.");
        currentMode = MODE.BROWSE;
        return Status.OK;
    }

    //--------------------------
    // Follow tour mode
    //--------------------------
    
    @Override
    public Status followTour(String id) {
        if (currentMode != MODE.DETAILS && currentMode != MODE.BROWSE) return new Status.Error("Incorrect state");
        if (!allTours.containsKey(id)) return new Status.Error("Tour " + id + " not found.");
        currentTour = allTours.get(id);
        currentMode = MODE.FOLLOW;
        currentStage = 0;
        return Status.OK;
    }

    @Override
    public Status endSelectedTour() {
        if (currentMode != MODE.FOLLOW) return new Status.Error("Incorrect state");
        currentMode = MODE.BROWSE;
        return Status.OK;
    }

    //--------------------------
    // Multi-mode methods
    //--------------------------
    @Override
    public void setLocation(double easting, double northing) {
        currentEast = easting;
        currentNorth = northing;

        if (currentMode == MODE.FOLLOW
                && currentStage < currentTour.waypoints.size()) {
            Displacement next = new Displacement(
                    currentTour.waypoints.get(currentStage).east - currentEast,
                    currentTour.waypoints.get(currentStage).north - currentNorth
            );
            if (next.distance() <= waypointRadius) {
                currentStage++;
            }
        }
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
            case FOLLOW: {
                output.add(
                        new Chunk.FollowHeader(currentTour.title, currentStage, currentTour.waypoints.size())
                );
                if (currentStage > 0) {
                    Displacement current = new Displacement(
                            currentTour.waypoints.get(currentStage-1).east - currentEast,
                            currentTour.waypoints.get(currentStage-1).north - currentNorth
                    );
                    if (current.distance() <= waypointRadius) {
                        output.add(
                                new Chunk.FollowWaypoint(currentTour.waypoints.get(currentStage-1).annotation)
                        );
                    }
                }
                if (currentStage < currentTour.waypoints.size()) {
                    output.add(
                            new Chunk.FollowLeg(currentTour.legAnnotations.get(currentStage))
                    );
                    Displacement next = new Displacement(
                            currentTour.waypoints.get(currentStage).east - currentEast,
                            currentTour.waypoints.get(currentStage).north - currentNorth
                    );
                    output.add(
                            new Chunk.FollowBearing(next.bearing(), next.distance())
                    );
                }

            }
        }
        return output;
    }


}
