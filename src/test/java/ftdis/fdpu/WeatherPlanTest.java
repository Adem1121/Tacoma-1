package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test WeatherPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherPlanTest {

    LateralPlan latPlan = new LateralPlan();
    VelocityPlan velPlan = new VelocityPlan();
    VerticalPlan vertPlan = new VerticalPlan();
    WeatherPlan wxPlan = new WeatherPlan();

    @Before
    public void setUp() throws Exception {
        // 01 Load and transform lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 FlightPlan.xml",1);
        latPlan.transform();
        latPlan.validate();

        // 02 Load and transform velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        velPlan.transform();
        velPlan.validate();

        // 03 Load and transform vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        vertPlan.transform();
        vertPlan.validate();

        // 04 Load and transform weather plan
        wxPlan.assignLat(latPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        wxPlan.transform();
        wxPlan.validate();
    }

    @Test
    public void testGetWptSgmt() throws Exception {
        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(1,wxPlan.getWptSgmt(testWpt).id,0);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),15000);
        assertEquals(3, wxPlan.getWptSgmt(testWpt).id, 0);

        testWpt = latPlan.getEndWpt();
        assertEquals(5, wxPlan.getWptSgmt(testWpt).id, 0);
    }
}