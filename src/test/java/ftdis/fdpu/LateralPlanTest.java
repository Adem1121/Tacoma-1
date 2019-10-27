package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

/**
 * Unit test LateralPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class LateralPlanTest {
    LateralPlan lateralPlan = new LateralPlan(1);

    @Before
    public void setUp() throws Exception {
        // transform
        lateralPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 06/IO/FlightPlanV04.xml",1);
        assertEquals("Lateral plan must contain 9 waypoints", 9, lateralPlan.getWptSize());

        // transform flight plan
        lateralPlan.transform();
        assertEquals("Lateral plan must contain  8 segments ", 8, lateralPlan.getSgmtCount());
    }


    @Test
    public void testGetLength() throws Exception{
        assertEquals(65742.15, lateralPlan.getLength(), 0.01);
    }

    @Test
    public void testGetDist() throws Exception{
        Waypoint startWpt, endWpt;

        // 01
        startWpt = new Waypoint();
        startWpt.setLat(47.452398);
        startWpt.setLon(-122.307838);

        endWpt = new Waypoint();
        endWpt.setLat(47.560571 );
        endWpt.setLon(-122.357368);

        assertEquals(22350.06, lateralPlan.getDist(startWpt, endWpt), 0.01);
        assertEquals(-22350.06, lateralPlan.getDist(endWpt, startWpt), 0.01);
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
    public void testGetItmWpt() throws Exception{
        Waypoint itmWpt, testWpt = new Waypoint();

        // 01
        testWpt.setLat(47.452398);
        testWpt.setLon(-122.307838);
        lateralPlan.alignWpt(testWpt);

        itmWpt = lateralPlan.getItmWpt(testWpt, 700);
        assertEquals(47.458693, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307844, itmWpt.getLon(), 0.0001);

        itmWpt = lateralPlan.getItmWpt(testWpt, -700);
        assertEquals(47.446102, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307935, itmWpt.getLon(), 0.0001);

        // 02
        testWpt.setLat(47.560571);
        testWpt.setLon(-122.357368);
        lateralPlan.alignWpt(testWpt);

        itmWpt = lateralPlan.getItmWpt(testWpt, 12000);
        assertEquals(47.476100, itmWpt.getLat(), 0.0001);
        assertEquals(-122.424250, itmWpt.getLon(), 0.0001);

        itmWpt = lateralPlan.getItmWpt(testWpt, -20000);
        assertEquals(47.473532, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307735, itmWpt.getLon(), 0.0001);

        // 03
        testWpt.setLat(47.382577);
        testWpt.setLon(-122.311195);
        lateralPlan.alignWpt(testWpt);

        itmWpt = lateralPlan.getItmWpt(testWpt, 8000);
        assertEquals(47.451966, itmWpt.getLat(), 0.0001);
        assertEquals(-122.311097, itmWpt.getLon(), 0.0001);

        itmWpt = lateralPlan.getItmWpt(testWpt, -30000);
        assertEquals(47.542837, itmWpt.getLat(), 0.0001);
        assertEquals(-122.394075, itmWpt.getLon(), 0.0001);
    }

   @Test
    public void testGetWptError() throws Exception{
       Waypoint testWpt;
       double wptError;

       // 01
       testWpt = new Waypoint();
       testWpt.setLat(47.541171);
       testWpt.setLon(-122.423247);
       wptError = lateralPlan.getWptError(testWpt);

       assertEquals(1124.59, wptError, 0.01);
   }

    @Test
    public void testalignToPlan() throws Exception{
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