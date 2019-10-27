package ftdis.fdpu;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test DirectSegment methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class DirectSegmentTest {

    LateralPlan lateralPlan = new LateralPlan(1);

    @Before
    public void setUp() throws Exception {
        // transform
        lateralPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 FlightPlan.xml",1);

        // transform flight plan
        lateralPlan.transform();
    }

    @Test
    public void testGetLength() throws Exception{
        assertEquals(5998.14, lateralPlan.getSgmt(0).getDist(),0.01);
        assertEquals(7501.94, lateralPlan.getSgmt(2).getDist(), 0.01);
        assertEquals(8701.90, lateralPlan.getSgmt(5).getDist(), 0.01);
    }

    @Test
    public void testGetDist() throws Exception{
        Waypoint testWpt = new Waypoint();

        //01 Test Wpt 2
        testWpt.setLat(47.452398);
        testWpt.setLon(-122.307838);
        assertEquals(2288.55, lateralPlan.getSgmt(0).getDist(lateralPlan.getSgmt(0).getStartPt(), testWpt), 0.01);
        assertEquals(-3709.60, lateralPlan.getSgmt(0).getDist(lateralPlan.getSgmt(0).getEndPt(),testWpt),0.01);
    }

    @Test
    public void testGetCourseAtWpt() throws Exception{
        Waypoint testWpt = new Waypoint();

        // 01
        testWpt.setLat(47.452398);
        testWpt.setLon(-122.307838);
        lateralPlan.getSgmt(0).alignToPlan(testWpt);
        assertEquals(0.28, lateralPlan.getSgmt(0).getCourseAtWpt(testWpt), 0.01);

        //02
        testWpt.setLat(47.478125);
        testWpt.setLon(-122.419640);
        lateralPlan.getSgmt(4).alignToPlan(testWpt);
        assertEquals(179.46, lateralPlan.getSgmt(4).getCourseAtWpt(testWpt), 0.01);

        //03
        testWpt.setLat(47.382577);
        testWpt.setLon(-122.311195);
        lateralPlan.getSgmt(5).alignToPlan(testWpt);
        assertEquals(74.81, lateralPlan.getSgmt(5).getCourseAtWpt(testWpt), 0.01);
    }

    @Test
    public void testGetItmWaypoint() throws Exception{
        Waypoint itmWpt;

        // 01
        itmWpt = lateralPlan.getSgmt(3).getItmWpt(lateralPlan.getSgmt(3).getStartPt(), 500);
        assertEquals(47.584438, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307884, itmWpt.getLon(), 0.0001);
    }

    @Test
    public void testGetPlanError() throws Exception{
        double wptError;
        Waypoint testWpt, refWpt;

        // 01
        testWpt = new Waypoint();
        testWpt.setLat(47.541171);
        testWpt.setLon(-122.423247);
        wptError = lateralPlan.getSgmt(3).getPlanError(testWpt);

        /*
        refWpt = new Waypoint();
        refWpt.setLat(47.532950);
        refWpt.setLon(-122.414520);
        System.out.println(NavCalc.getDirectDist(testWpt, refWpt));
        */

        assertEquals(1124.59, wptError, 0.01);
        }

    @Test
    public void testAlignToPlan() throws Exception{
        Waypoint testWpt = new Waypoint();

        // 01
        testWpt.setLat(47.459766);
        testWpt.setLon(-122.316444);
        lateralPlan.getSgmt(0).alignToPlan(testWpt);
        assertEquals(47.459737, testWpt.getLat(), 0.0001);
        assertEquals(-122.307836, testWpt.getLon(), 0.0001);

        // 02
        testWpt.setLat(47.609804);
        testWpt.setLon(-122.285226);
        lateralPlan.getSgmt(2).alignToPlan(testWpt);
        assertEquals(47.587052, testWpt.getLat(), 0.0001);
        assertEquals(-122.302460, testWpt.getLon(), 0.0001);

        // 03
        testWpt.setLat(47.541171);
        testWpt.setLon(-122.423247);
        lateralPlan.getSgmt(3).alignToPlan(testWpt);
        assertEquals(47.532950, testWpt.getLat(), 0.0001);
        assertEquals(-122.414520, testWpt.getLon(), 0.0001);

    }
}