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
        //logger.fine("CONTROLLER CREATED. Mode:" + MODE.BROWSE + ", Waypoint Radius:" + waypointRadius + ", Waypoint Separation: " + waypointSeparation);
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
        logger.entering("tourguide.ControllerImp", "startNewTour", new Object[]{id, title, annotation});
        if (currentMode != MODE.BROWSE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.BROWSE + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.BROWSE + ", got " + currentMode + ".");
        }
        //logger.fine(startBanner("startNewTour"));
        currentTour = new Tour(id, title, annotation);
        logger.info("TOUR CREATED: " + currentTour);
        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.CREATE);
        currentMode = MODE.CREATE;
        return Status.OK;
    }

    @Override
    public Status addWaypoint(Annotation annotation) {
        logger.entering("tourguide.ControllerImp", "addWaypoint", annotation);
        if (currentMode != MODE.CREATE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
        }
        //logger.fine(startBanner("addWaypoint"));

        //Check if waypoint is too close.
        if (currentTour.waypoints.size() > 0) {
            Waypoint last = currentTour.waypoints.get(currentTour.waypoints.size()-1);
            Displacement d = new Displacement(last.east - currentEast, last.north - currentNorth);
            if (d.distance() < waypointSeparation) {
                Waypoint cur = new Waypoint(currentEast, currentNorth, annotation);
                logger.fine("ERROR: Waypoint ("+cur+") too close to the last ("+last+").");
                return new Status.Error("ERROR: Waypoint ("+cur+") too close to the last ("+last+").");
            }
        }

        //Add new waypoint
        currentTour.waypoints.add(new Waypoint(currentEast, currentNorth, annotation));
        logger.info(currentTour.waypoints.get(currentTour.waypoints.size()-1) + " added to tour " + currentTour.id);
        //If waypoint doesn't have annotation, add default annotation.
        if (currentTour.legAnnotations.size() < currentTour.waypoints.size()) {
            logger.info("Leg annotation " + Annotation.DEFAULT + " added to tour " + currentTour.id);
            currentTour.legAnnotations.add(Annotation.DEFAULT);
        }
        return Status.OK;
    }

    @Override
    public Status addLeg(Annotation annotation) {
        logger.entering("tourguide.ControllerImp", "addLeg", annotation);
        if (currentMode != MODE.CREATE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
        }

        //logger.fine(startBanner("addLeg"));

        if (currentTour.legAnnotations.size() > currentTour.waypoints.size()) {
            logger.fine("ERROR: Too many leg annotations in tour "+ currentTour.id +".");
            return new Status.Error("ERROR: Too many leg annotations in tour "+ currentTour.id +".");
        }
        logger.info("Leg annotation " + annotation + " added to tour " + currentTour.id);
        currentTour.legAnnotations.add(annotation);
        return Status.OK;
    }

    @Override
    public Status endNewTour() {
        logger.entering("tourguide.ControllerImp", "endNewTour");
        if (currentMode != MODE.CREATE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.CREATE + ", got " + currentMode + ".");
        }
        if (currentTour.waypoints.size() < 1) {
            logger.fine("ERROR: Can't create tour without waypoints.");
            return new Status.Error("ERROR: Can't create tour without waypoints.");
        }
        if (currentTour.legAnnotations.size() != currentTour.waypoints.size()) {
            logger.fine(
                    "ERROR: Number of legs must be same as number of waypoints." +
                            "Legs: " + currentTour.legAnnotations.size() + ", Waypoints: " + currentTour.legAnnotations.size()
            );
            return new Status.Error(
                    "ERROR: Number of legs must be same as number of waypoints." +
                            "Legs: " + currentTour.legAnnotations.size() + ", Waypoints: " + currentTour.legAnnotations.size()
            );
        }
        //logger.fine(startBanner("endNewTour"));

        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.BROWSE);
        currentMode = MODE.BROWSE;

        logger.info("TOUR ADDED: " + currentTour);
        allTours.put(currentTour.id, currentTour);
        return Status.OK;
    }

    //--------------------------
    // Browse tours mode
    //--------------------------

    @Override
    public Status showTourDetails(String tourID) {
        logger.entering("tourguide.ControllerImp", "showTourDetails", tourID);
        if (currentMode != MODE.BROWSE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.BROWSE + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.BROWSE + ", got " + currentMode + ".");
        }
        if (!allTours.containsKey(tourID)) {
            logger.fine("ERROR: Tour " + tourID + " not found.");
            return new Status.Error("ERROR: Tour " + tourID + " not found.");
        }

        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.DETAILS);
        currentMode = MODE.DETAILS;

        currentTour = allTours.get(tourID);
        logger.info("VIEWING TOUR: " + currentTour);
        return Status.OK;
    }
  
    @Override
    public Status showToursOverview() {
        logger.entering("tourguide.ControllerImp", "showTourDetails");
        if (currentMode == MODE.CREATE) {
            logger.fine("ERROR: Incorrect mode, can't browse while creating new tour.");
            return new Status.Error("ERROR: Incorrect mode, can't browse while creating new tour.");
        }

        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.BROWSE);
        currentMode = MODE.BROWSE;
        return Status.OK;
    }

    //--------------------------
    // Follow tour mode
    //--------------------------
    
    @Override
    public Status followTour(String id) {
        logger.entering("tourguide.ControllerImp", "followTour", id);
        if (currentMode != MODE.DETAILS && currentMode != MODE.BROWSE) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.BROWSE + " or " + MODE.DETAILS + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.BROWSE + " or " + MODE.DETAILS + ", got " + currentMode + ".");
        }
        if (!allTours.containsKey(id)) {
            logger.fine("ERROR: Tour " + id + " not found.");
            return new Status.Error("ERROR: Tour " + id + " not found.");
        }
        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.FOLLOW);
        currentMode = MODE.FOLLOW;

        currentTour = allTours.get(id);
        logger.info("FOLLOWING TOUR: " + currentTour);
        
        currentStage = 0;
        logger.info("CURRENT STAGE: " + currentStage);
        return Status.OK;
    }

    @Override
    public Status endSelectedTour() {
        logger.entering("tourguide.ControllerImp", "endSelectedTour");
        if (currentMode != MODE.FOLLOW) {
            logger.fine("ERROR: Incorrect mode, expected " + MODE.FOLLOW + ", got " + currentMode + ".");
            return new Status.Error("ERROR: Incorrect mode, expected " + MODE.FOLLOW + ", got " + currentMode + ".");
        }
        
        logger.info("MODE CHANGED: " + currentMode + " -> " + MODE.BROWSE);
        currentMode = MODE.BROWSE;
        return Status.OK;
    }

    //--------------------------
    // Multi-mode methods
    //--------------------------
    @Override
    public void setLocation(double easting, double northing) {
        logger.entering("tourguide.ControllerImp", "setLocation", new Object[]{easting, northing});
        logger.info("LOCATION CHANGED: (e:"+currentEast+", n:"+currentNorth+") -> (e:"+easting+", n:"+northing+")");
        currentEast = easting;
        currentNorth = northing;

        if (currentMode == MODE.FOLLOW
                && currentStage < currentTour.waypoints.size()) {
            Displacement next = new Displacement(
                    currentTour.waypoints.get(currentStage).east - currentEast,
                    currentTour.waypoints.get(currentStage).north - currentNorth
            );
            if (next.distance() <= waypointRadius) {
                logger.info("REACHED NEXT WAYPOINT. " +
                        "Location: (e:"+currentEast+", n:"+currentNorth+"), " +
                        "Waypoint: (e:"+currentTour.waypoints.get(currentStage).east+", n:"+currentTour.waypoints.get(currentStage).north+"), " +
                        "Distance: " + next.distance()
                );
                currentStage++;
                logger.info("CURRENT STAGE: " + currentStage);
            }
        }
    }

    @Override
    public List<Chunk> getOutput() {
        //logger.entering("tourguide.ControllerImp", "getOutput");
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
