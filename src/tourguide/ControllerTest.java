/**
 * 
 */
package tourguide;

import java.util.*;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author pbj
 *
 */
public class ControllerTest {

    private Controller controller;
    private static final double WAYPOINT_RADIUS = 10.0;
    private static final double WAYPOINT_SEPARATION = 25.0;
   
    // Utility methods to help shorten test text.
    private static Annotation ann(String s) { return new Annotation(s); }
    private static void checkStatus(Status status) { 
        Assert.assertEquals(Status.OK, status);
    }
    private static void checkStatusNotOK(Status status) { 
        Assert.assertNotEquals(Status.OK, status);
    }
    private void checkOutput(int numChunksExpected, int chunkNum, Chunk expected) {
        List<Chunk> output = controller.getOutput();
        Assert.assertEquals("Number of chunks", numChunksExpected, output.size());
        Chunk actual = output.get(chunkNum);
        Assert.assertEquals(expected, actual);  
    }
    
    
    /*
     * Logging functionality
     */
    
    // Convenience field.  Saves on getLogger() calls when logger object needed.
    private static Logger logger;
    
    // Update this field to limit logging.
    public static Level loggingLevel = Level.ALL;
    
    private static final String LS = System.lineSeparator();

    @BeforeClass
    public static void setupLogger() {
         
        logger = Logger.getLogger("tourguide"); 
        logger.setLevel(loggingLevel);
        
        // Ensure the root handler passes on all messages at loggingLevel and above (i.e. more severe)
        Logger rootLogger = Logger.getLogger("");
        Handler handler = rootLogger.getHandlers()[0];
        handler.setLevel(loggingLevel);
    }

    private String makeBanner(String testCaseName) {
        return  LS 
          + "#############################################################" + LS
          + "TESTCASE: " + testCaseName + LS
          + "#############################################################";
    }


    
    @Before
    public void setup() {
        controller = new ControllerImp(WAYPOINT_RADIUS, WAYPOINT_SEPARATION);
    }
    
    @Test
    public void noTours() {  
        logger.info(makeBanner("noTours"));
        
        checkOutput(1, 0, new Chunk.BrowseOverview() );
     }
    
    // Locations roughly based on St Giles Cathedral reference.
    
    private void addOnePointTour() {
        
        checkStatus( controller.startNewTour(
                "T1", 
                "Informatics at UoE", 
                ann("The Informatics Forum and Appleton Tower\n"))
                );
        
        checkOutput(1, 0, new Chunk.CreateHeader("Informatics at UoE", 0,  0));
      
        controller.setLocation(300, -500);
  
        checkStatus( controller.addLeg(ann("Start at NE corner of George Square\n")) );
       
        checkOutput(1, 0, new Chunk.CreateHeader("Informatics at UoE", 1,  0));
        
        checkStatus( controller.addWaypoint(ann("Informatics Forum")) );     
        
        checkOutput(1, 0, new Chunk.CreateHeader("Informatics at UoE", 1,  1));
  
        checkStatus( controller.endNewTour() );
        
    }
    
    @Test
    public void testAddOnePointTour() { 
        logger.info(makeBanner("testAddOnePointTour"));
        
        addOnePointTour(); 
    }
    

    private void addTwoPointTour() {
         checkStatus(
                controller.startNewTour("T2", "Old Town", ann("From Edinburgh Castle to Holyrood\n"))
                );
        
        checkOutput(1, 0, new Chunk.CreateHeader("Old Town", 0,  0));
      
        controller.setLocation(-500, 0);
        
        // Leg before this waypoint with default annotation added at same time
        checkStatus( controller.addWaypoint(ann("Edinburgh Castle\n")) );     
        
        checkOutput(1, 0, new Chunk.CreateHeader("Old Town", 1,  1));
  
        checkStatus( controller.addLeg(ann("Royal Mile\n")) );
  
        checkOutput(1, 0, new Chunk.CreateHeader("Old Town", 2,  1) );
      
        checkStatusNotOK( 
                controller.endNewTour()
                );
  
        controller.setLocation(1000, 300);
               
        checkStatus( controller.addWaypoint(ann("Holyrood Palace\n")) );

        checkOutput(1, 0, new Chunk.CreateHeader("Old Town", 2,  2) );
  
        checkStatus( controller.endNewTour() );
        
    }
    
    @Test
    public void testAddTwoPointTour() { 
        logger.info(makeBanner("testAddTwoPointTour"));
       
        addTwoPointTour(); 
    }
    
    @Test
    public void testAddOfTwoTours() {
        logger.info(makeBanner("testAddOfTwoTour"));
        
        addOnePointTour();
        addTwoPointTour();
    }
    
    @Test
    public void browsingTwoTours() {
        logger.info(makeBanner("browsingTwoTours"));
        
        addOnePointTour();
        addTwoPointTour();
 
        Chunk.BrowseOverview overview = new Chunk.BrowseOverview(); 
        overview.addIdAndTitle("T1", "Informatics at UoE");
        overview.addIdAndTitle("T2", "Old Town");
        checkOutput(1, 0, overview);
        
        checkStatusNotOK( controller.showTourDetails("T3") );
        checkStatus( controller.showTourDetails("T1") );
            
        checkOutput(1, 0, new Chunk.BrowseDetails(
                "T1", 
                "Informatics at UoE", 
                ann("The Informatics Forum and Appleton Tower\n")
                ));
    }
    
    @Test 
    public void followOldTownTour() {
        logger.info(makeBanner("followOldTownTour"));
       
        addOnePointTour();
        addTwoPointTour();

        checkStatus( controller.followTour("T2") );
        
        controller.setLocation(0.0, 0.0);
  
        checkOutput(3,0, new Chunk.FollowHeader("Old Town", 0, 2) );      
        checkOutput(3,1, new Chunk.FollowLeg(Annotation.DEFAULT));
        checkOutput(3,2, new Chunk.FollowBearing(270.0, 500.0));
         
        controller.setLocation(-490.0, 0.0);
      
        checkOutput(4,0, new Chunk.FollowHeader("Old Town", 1, 2) );  
        checkOutput(4,1, new Chunk.FollowWaypoint(ann("Edinburgh Castle\n")));
        checkOutput(4,2, new Chunk.FollowLeg(ann("Royal Mile\n")));
        checkOutput(4,3, new Chunk.FollowBearing(79.0, 1520.0));
 
        controller.setLocation(900.0, 300.0);
        
        checkOutput(3,0, new Chunk.FollowHeader("Old Town", 1, 2) );  
        checkOutput(3,1, new Chunk.FollowLeg(ann("Royal Mile\n")));
        checkOutput(3,2, new Chunk.FollowBearing(90.0, 100.0));
        
        controller.setLocation(1000.0, 300.0);
  
        checkOutput(2,0, new Chunk.FollowHeader("Old Town", 2, 2) );  
        checkOutput(2,1, new Chunk.FollowWaypoint(ann("Holyrood Palace\n")));
                      
        controller.endSelectedTour();
        
        Chunk.BrowseOverview overview = new Chunk.BrowseOverview(); 
        overview.addIdAndTitle("T1", "Informatics at UoE");
        overview.addIdAndTitle("T2", "Old Town");
        checkOutput(1, 0, overview);
    
    }

    /**
     * Tests if controller.showToursOverview works correctly when there is no tours in the system.
     */
    @Test
    public void browseTourInit() {
        logger.info(makeBanner("browseTourInit"));

        Status status = controller.showToursOverview();
        checkStatus(status);
        Chunk.BrowseOverview expected = new Chunk.BrowseOverview();
        expected.overviewLines = new ArrayList<>();
        checkOutput(1,0, expected);
    }

    /**
     * Tests if controller.showToursOverview works correctly when there is 1 tour in the system.
     */
    @Test
    public void browseTourSimple() {
        logger.info(makeBanner("browseTourSimple"));

        controller.startNewTour("01","tour1", Annotation.DEFAULT);
        controller.addLeg(Annotation.DEFAULT);
        controller.addWaypoint(Annotation.DEFAULT);
        controller.endNewTour();

        Status status = controller.showToursOverview();
        Assert.assertEquals(Status.OK, status);
        List<Chunk> output = controller.getOutput();


        Chunk.BrowseOverview myOverview = new Chunk.BrowseOverview();
        myOverview.addIdAndTitle("01","tour1");

        Assert.assertEquals(1, output.size());
        Assert.assertEquals(myOverview, output.get(0));
    }

    @Test
    public void browseTourFail() {
        logger.info(makeBanner("browseTourFail"));

        controller.startNewTour("01", "tour1", Annotation.DEFAULT);
        checkStatusNotOK(controller.showToursOverview());
    }

    /**
     * Tests if a simple tour can be created in the controller.
     */
    @Test
    public void createSimpleTour() {
        logger.info(makeBanner("createSimpleTour"));

        checkStatus(controller.startNewTour("01","tour1",Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",0,0)
        );

        checkStatus(controller.addLeg(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",1,0)
        );

        controller.setLocation(0,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",1,1)
        );

        controller.setLocation(-WAYPOINT_SEPARATION,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",2,2)
        );

        controller.setLocation(0,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",3,3)
        );

        controller.setLocation(WAYPOINT_SEPARATION,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",4,4)
        );

        controller.setLocation(0,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",5,5)
        );

        controller.setLocation(0,-WAYPOINT_SEPARATION);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",6,6)
        );

        controller.setLocation(0,0);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",7,7)
        );

        controller.setLocation(0,WAYPOINT_SEPARATION);
        checkStatus(controller.addWaypoint(Annotation.DEFAULT));
        checkOutput(1,0,
                new Chunk.CreateHeader("tour1",8,8)
        );

        checkStatus(controller.endNewTour());
        Chunk.BrowseOverview expected2 = new Chunk.BrowseOverview();
        expected2.addIdAndTitle("01", "tour1");
        checkOutput(1,0,expected2);
    }

    @Test
    public void browseTourDetails() {
        logger.info(makeBanner("browseTourDetails"));

        controller.startNewTour("01","tour1", new Annotation("testing"));
        controller.addWaypoint(Annotation.DEFAULT);
        controller.endNewTour();

        controller.showToursOverview();
        checkStatus(controller.showTourDetails("01"));

        checkOutput(1, 0,
                new Chunk.BrowseDetails("01", "tour1", new Annotation("testing"))
        );
    }

    @Test
    public void browseTourDetailsFail() {
        logger.info(makeBanner("browseTourDetailsFail"));

        checkStatusNotOK(controller.showTourDetails("01"));
        checkStatusNotOK(controller.showTourDetails(null));
        controller.startNewTour("01", "tour1", Annotation.DEFAULT);
        controller.addWaypoint(Annotation.DEFAULT);
        controller.endNewTour();
        controller.startNewTour("02", "tour2", Annotation.DEFAULT);
        checkStatusNotOK(controller.showTourDetails("01"));
    }

    @Test
    public void createTourFail() {
        logger.info(makeBanner("createTourFail"));

        controller.startNewTour("01", "tour1", Annotation.DEFAULT);
        checkStatusNotOK(controller.endNewTour());
        controller.setLocation(0,0);
        controller.addWaypoint(Annotation.DEFAULT);
        controller.addLeg(Annotation.DEFAULT);
        checkStatusNotOK(controller.endNewTour());
        checkStatusNotOK(controller.addLeg(Annotation.DEFAULT));

        controller.setLocation(-(WAYPOINT_SEPARATION*0.99),0);
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        controller.setLocation((WAYPOINT_SEPARATION*0.99),0);
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        controller.setLocation(0,-(WAYPOINT_SEPARATION*0.99));
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        controller.setLocation(0,(WAYPOINT_SEPARATION*0.99));
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        controller.setLocation(0,WAYPOINT_SEPARATION);
        controller.addWaypoint(Annotation.DEFAULT);
        controller.endNewTour();

        controller.showToursOverview();
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        checkStatusNotOK(controller.addLeg(Annotation.DEFAULT));
        checkStatusNotOK(controller.endNewTour());
        controller.showTourDetails("01");
        checkStatusNotOK(controller.startNewTour("02", "tour2", Annotation.DEFAULT));
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        checkStatusNotOK(controller.addLeg(Annotation.DEFAULT));
        checkStatusNotOK(controller.endNewTour());
        controller.followTour("01");
        checkStatusNotOK(controller.startNewTour("02", "tour2", Annotation.DEFAULT));
        checkStatusNotOK(controller.addWaypoint(Annotation.DEFAULT));
        checkStatusNotOK(controller.addLeg(Annotation.DEFAULT));
        checkStatusNotOK(controller.endNewTour());
    }

    @Test
    public void followTour() {
        logger.info(makeBanner("followTour"));

        controller.startNewTour("01", "tour1", new Annotation("tour_annotation"));
        for (int i=0; i < 50; i++) {
            controller.setLocation(0,i * WAYPOINT_SEPARATION);
            controller.addLeg(new Annotation("leg"+i));
            controller.addWaypoint(new Annotation("way"+i));
        }
        controller.endNewTour();

        controller.showToursOverview();
        controller.showTourDetails("01");
        checkStatus(controller.followTour("01"));

        for (int i=0; i < 51; i++) {
            controller.setLocation(0, (i-1)*WAYPOINT_SEPARATION);
            int numExpected = i > 0 ? i < 50 ? 4 : 2 : 3;
            checkOutput(numExpected, 0,
                    new Chunk.FollowHeader("tour1", i, 50)
            );
            if (i < 50) {
                checkOutput(numExpected, i>0?2:1,
                        new Chunk.FollowLeg(new Annotation("leg"+i))
                );

                Displacement d = new Displacement(0, WAYPOINT_SEPARATION);
                checkOutput(numExpected,numExpected-1,
                        new Chunk.FollowBearing(d.bearing(), d.distance())
                );
            }
            if (i > 0) {
                checkOutput(numExpected, 1,
                        new Chunk.FollowWaypoint(new Annotation("way"+(i-1)))
                );
            }

            if (i > 0 && i < 50) {
                //User leaves waypoint
                controller.setLocation(WAYPOINT_RADIUS+1, (i-1)*WAYPOINT_SEPARATION);
                checkOutput(3, 0,
                        new Chunk.FollowHeader("tour1", i, 50)
                );
                checkOutput(3, 1,
                        new Chunk.FollowLeg(new Annotation("leg"+i))
                );
                Displacement d = new Displacement(-(WAYPOINT_RADIUS+1), WAYPOINT_SEPARATION);
                checkOutput(3,2,
                        new Chunk.FollowBearing(d.bearing(), d.distance())
                );
            }
        }
        checkStatus(controller.endSelectedTour());
        Chunk.BrowseOverview expected = new Chunk.BrowseOverview();
        expected.addIdAndTitle("01", "tour1");
        checkOutput(1,0,expected);

        checkStatus(controller.followTour("01"));
        checkStatus(controller.endSelectedTour());
    }

    @Test
    public void followTourFail() {
        logger.info(makeBanner("followTourFail"));

        controller.startNewTour("01", "title1", Annotation.DEFAULT);
        controller.addWaypoint(Annotation.DEFAULT);
        checkStatusNotOK(controller.followTour("01"));
        checkStatusNotOK(controller.endSelectedTour());
        controller.endNewTour();

        controller.showToursOverview();
        checkStatusNotOK(controller.followTour("02"));
        checkStatusNotOK(controller.followTour(null));
        checkStatusNotOK(controller.endSelectedTour());
        controller.showTourDetails("01");
        checkStatusNotOK(controller.followTour("02"));
        checkStatusNotOK(controller.followTour(null));
        checkStatusNotOK(controller.endSelectedTour());
        controller.followTour("01");
        checkStatusNotOK(controller.followTour("01"));
    }


    
}
