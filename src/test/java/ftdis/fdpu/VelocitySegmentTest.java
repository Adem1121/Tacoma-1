package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test VelocitySegment  methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocitySegmentTest {
    LateralPlan latPlan = new LateralPlan(1);
    VelocityPlan velPlan = new VelocityPlan();

    @Before
    public void setUp() throws Exception {
        // Transform and transform lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 FlightPlan.xml",1);
        latPlan.transform();

        // Load change airspeed events to velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 EventCollection.xml",1);
        velPlan.transform();
    }

    @Test
    public void testGetVas() throws Exception {
        assertEquals(0, velPlan.getSgmt(0).getVas(0), 0);
        assertEquals(82.31, velPlan.getSgmt(1).getVas(500), 0.01);
        assertEquals(87.06, velPlan.getSgmt(2).getVas(1000), 0.01);
        assertEquals(108.03, velPlan.getSgmt(3).getVas(999999), 0.01);
        assertEquals(63.39, velPlan.getSgmt(6).getVas(300), 0.01);
    }
}