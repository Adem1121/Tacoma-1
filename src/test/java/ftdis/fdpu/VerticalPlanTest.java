package ftdis.fdpu;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit test VerticalPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VerticalPlanTest {

    LateralPlan latPlan = new LateralPlan();
    VelocityPlan velPlan = new VelocityPlan();
    VerticalPlan vertPlan = new VerticalPlan();

    @Before
    public void setUp() throws Exception {
        // 01 Load and transform lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 FlightPlan.xml",1);
        latPlan.transform();

        // 02 Load and transform velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 EventCollection.xml",1);
        velPlan.transform();
        velPlan.validate();

        // 03 Load and transform vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 EventCollection.xml",1);
        vertPlan.transform();
        vertPlan.validate();
    }

    @Test
    public void testGetWptSgmt() throws Exception {
        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(1,vertPlan.getWptSgmt(testWpt).id,0);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),15000);
        assertEquals(3, vertPlan.getWptSgmt(testWpt).id, 0);

        testWpt = latPlan.getEndWpt();
        assertEquals(5, vertPlan.getWptSgmt(testWpt).id, 0);
    }

    @Test
    public void testGetAltAtWpt() throws Exception {
        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(131.98,vertPlan.getAltAtWpt(testWpt),0.01);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),5000);
        assertEquals(448.68, vertPlan.getAltAtWpt(testWpt), 0.01);

        testWpt = latPlan.getItmWpt(latPlan.getEndWpt(),-5000);
        assertEquals(286.02, vertPlan.getAltAtWpt(testWpt), 0.01);

        testWpt = latPlan.getEndWpt();
        assertEquals(131.98, vertPlan.getAltAtWpt(testWpt), 0.01);
    }

    @Test
    public void testGetAlphaAtWpt() throws Exception {
        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(0,vertPlan.getAlphaAtWpt(testWpt),0.01);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),5000);
        assertEquals(0.12, vertPlan.getAlphaAtWpt(testWpt), 0.01);

        testWpt = latPlan.getItmWpt(latPlan.getEndWpt(),-5000);
        assertEquals(-0.04, vertPlan.getAlphaAtWpt(testWpt), 0.01);

        testWpt = latPlan.getEndWpt();
        assertEquals(0, vertPlan.getAlphaAtWpt(testWpt), 0.01);
    }
}