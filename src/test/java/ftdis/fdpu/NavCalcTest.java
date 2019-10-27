package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
//import static ftdis.fdpu.NavCalc.*;

/**
 * Unit test NavCalc methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class NavCalcTest {

    LateralPlan lateralPlan = new LateralPlan(1);

    @Before
    public void setUp(){
        // transform flight plan
        //lateralPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 04/IO/FlightPlan.xml",1);
        //lateralPlan.transform();
    }


    @Test
    public void testGetNewCourse(){
        double startCourse;
        double courseChange;

        startCourse = 0;
        courseChange = 90;
        assertEquals(90,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -90;
        assertEquals(270,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 180;
        assertEquals(180,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -180;
        assertEquals(180,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 200;
        assertEquals(200,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -200;
        assertEquals(160,NavCalc.getNewCourse(startCourse,courseChange),0);


        startCourse = 270;
        courseChange = 90;
        assertEquals(0,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -90;
        assertEquals(180,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 180;
        assertEquals(90,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -180;
        assertEquals(90,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 200;
        assertEquals(110,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -200;
        assertEquals(70,NavCalc.getNewCourse(startCourse,courseChange),0);


        startCourse = 180;
        courseChange = 90;
        assertEquals(270,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -90;
        assertEquals(90,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 180;
        assertEquals(0,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -180;
        assertEquals(0,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 200;
        assertEquals(20,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = -200;
        assertEquals(340,NavCalc.getNewCourse(startCourse,courseChange),0);

        courseChange = 360;
        assertEquals(180,NavCalc.getNewCourse(startCourse,courseChange),0);
    }


    @Test
    public void getDirectDist(){
        double testDist;

        // create test waypoints
        //Waypoint w1 = lateralPlan.getDirSegment(1).getStartPoint();
        //Waypoint w2 = lateralPlan.getDirSegment(1).getEndPoint();
        Waypoint w1 = new Waypoint(1);
        Waypoint w2 = new Waypoint(2);

        w1.setLat(47.476641);
        w1.setLon(-122.307703);

        w2.setLat(47.485540);
        w2.setLon(-122.320961);

        // calculate distance between the waypoints
        testDist = NavCalc.getDirectDist(w1, w2);

        assertEquals(1404,testDist,0.5);

        System.out.println("============================================");
        System.out.println("DirectSegment Distance");
        System.out.println("============================================");
        System.out.println("Waypoint 1 ---------------------------------");
        System.out.println("Latitude.: " + w1.getLat());
        System.out.println("Longitude: " + w1.getLon());
        System.out.println("Waypoint 2 ---------------------------------");
        System.out.println("Latitude.: " + w2.getLat());
        System.out.println("Longitude: " + w2.getLon());
        System.out.println("Data ---------------------------------------");
        System.out.println("Distance....: " + testDist + " m");
    }

    /**
     * Caclulacte
     */
    @Test
    public void getInitBearing(){
        double testInitBearing;

        // create test waypoints
        Waypoint w1 = lateralPlan.getSgmt(1).getStartPt();
        Waypoint w2 = lateralPlan.getSgmt(1).getEndPt();

        // calculate initial bearing at waypoint 1
        testInitBearing = NavCalc.getInitBearing(w1, w2);


        System.out.println("============================================");
        System.out.println("Initial Bearing at Waypoint 1");
        System.out.println("============================================");
        System.out.println("Waypoint 1 ---------------------------------");
        System.out.println("Latitude.: " + w1.getLat());
        System.out.println("Longitude: " + w1.getLon());
        System.out.println("Waypoint 2 ---------------------------------");
        System.out.println("Latitude.: " + w2.getLat());
        System.out.println("Longitude: " + w2.getLon());
        System.out.println("Data ---------------------------------------");
        System.out.println("Init Bearing: " + testInitBearing + " deg");
    }

    /**
     * Find intermediary waypoint
     */
    @Test
    public void testGetItmWaypoint(){
        Waypoint itmWpt;

        // create test waypoints
        Waypoint w1 = lateralPlan.getSgmt(3).getStartPt();
        Waypoint w2 = lateralPlan.getSgmt(3).getEndPt();

        // get intermediate waypoint half way between waypoint 1 and 2
        itmWpt = NavCalc.getItmWpt(w1, w2, 0.0);

        // write results to console
        System.out.println("============================================");
        System.out.println("Intermediate Waypoint between W1 and W2");
        System.out.println("============================================");
        System.out.println("Waypoint 1 ---------------------------------");
        System.out.println("Latitude.: " + w1.getLat());
        System.out.println("Longitude: " + w1.getLon());
        System.out.println("Waypoint 2 ---------------------------------");
        System.out.println("Latitude.: " + w2.getLat());
        System.out.println("Longitude: " + w2.getLon());
        System.out.println("Intermediary Waypoint ----------------------");
        System.out.println("Latitude.: " + itmWpt.getLat());
        System.out.println("Longitude: " + itmWpt.getLon());
        System.out.println("Error....: " + lateralPlan.getSgmt(3).getPlanError(itmWpt));
    }

    @Test
    public void testGetRadWaypoint(){
        Waypoint startWpt = new Waypoint(), endWpt;
        double radial = 0, distance = 5000;

        startWpt.setLat(47.4451105);
        startWpt.setLon(-122.3082052);

        endWpt = NavCalc.getRadWpt(startWpt, distance, radial);

        // write results to console
        System.out.println("============================================");
        System.out.println("New radial waypoint");
        System.out.println("============================================");
        System.out.println("Start Waypoint -----------------------------");
        System.out.println("Latitude.: " + startWpt.getLat());
        System.out.println("Longitude: " + startWpt.getLon());
        System.out.println("Parameters ---------------------------------");
        System.out.println("Radial...: " + radial);
        System.out.println("Distance.: " + distance);
        System.out.println("Radial Waypoint ----------------------------");
        System.out.println("Latitude.: " + endWpt.getLat());
        System.out.println("Longitude: " + endWpt.getLon());
    }


    @Test
    public void testGetIntersectWaypoint(){
        Waypoint startWpt = new Waypoint();
        Waypoint endWpt = new Waypoint();
        Waypoint intersectWpt = new Waypoint();
        double startWptHeading, endWptHeading, turnDist;

        /*
        startWpt.setLat(47.453612);
        startWpt.setLon(-122.305007);
        startWptHeading = 356.4196724116118;

        //Waypoint testStartTangent = NavCalc.getRadWpt(startWpt,100,startWptHeading);

        endWpt.setLat(47.453739);
        endWpt.setLon(-122.30502);
        endWptHeading = 0.2739403437039472;

        //Waypoint testEndTangent = NavCalc.getRadWpt(endWpt,100,endWptHeading);
        */

        startWpt.setLat(47.461742);
        startWpt.setLon(-122.301732);
        startWptHeading = 91.28879361238434;

        endWpt.setLat(47.461879);
        endWpt.setLon(-122.301615);
        endWptHeading = 0.2739403437039472;

        intersectWpt = NavCalc.getIntersectWpt(startWpt,startWptHeading,endWpt,NavCalc.getNewCourse(endWptHeading,180));

        // In case intersect point is not inbetween turn start and end points, adjust start/end waypoints and retry
        if(intersectWpt == null){
            // Find alternative intersect waypoint -> after turn end waypoint
            intersectWpt = NavCalc.getIntersectWpt(startWpt,startWptHeading,endWpt,endWptHeading);
        }
        if(intersectWpt == null){
            // Find alternative intersect waypoint -> in front of turn start waypoint
            intersectWpt = NavCalc.getIntersectWpt(startWpt,NavCalc.getNewCourse(startWptHeading,180),endWpt,NavCalc.getNewCourse(endWptHeading,180));
        }

        // (Re)define outbound turn point, based on inbound turn segment distance
        turnDist = NavCalc.getDirectDist(startWpt,intersectWpt);
        endWpt = NavCalc.getRadWpt(intersectWpt,turnDist,endWptHeading);

        // write results to console
        System.out.println("============================================");
        System.out.println("New intersect waypoint");
        System.out.println("============================================");
        System.out.println("Start Waypoint -----------------------------");
        System.out.println("Latitude.: " + startWpt.getLat());
        System.out.println("Longitude: " + startWpt.getLon());
        System.out.println("Heading..: " + startWptHeading);
        System.out.println("Intersect Waypoint -------------------------");
        System.out.println("Latitude.: " + intersectWpt.getLat());
        System.out.println("Longitude: " + intersectWpt.getLon());
        System.out.println("End Waypoint -------------------------------");
        System.out.println("Latitude.: " + endWpt.getLat());
        System.out.println("Longitude: " + endWpt.getLon());
        System.out.println("Heading..: " + endWptHeading);


    }

}