package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test VerticalTrack methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VerticalTrackTest {
    LateralPlan latPlan = new LateralPlan();
    LateralTrack latTrack = new LateralTrack();

    VelocityPlan velPlan = new VelocityPlan();
    VelocityTrack velTrack = new VelocityTrack();

    VerticalPlan vertPlan = new VerticalPlan();
    VerticalTrack vertTrack = new VerticalTrack();

    @Before
    public void setUp() throws Exception {
        // 01 Load and transform lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 FlightPlan.xml",1);
        latPlan.transform();

        // 02 Load and transform velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 EventCollection.xml",1);
        velPlan.transform();

        // 03 Load and transform vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 EventCollection.xml",1);
        vertPlan.transform();

        // 03 Load transform and validate lateral track
        latTrack.assignVel(velPlan);
        latTrack.transform(latPlan);
        latTrack.validate();

        // 04 Load and validate velocity track
        velTrack.assignLat(latTrack);
        velTrack.transform(velPlan);
        velTrack.validate();
    }

    @Test
    public void testLoad() throws Exception {
        // 05 transform transform and validate vertical track
        vertTrack.assignLat(latTrack);
        vertTrack.assignVel(velTrack);
        vertTrack.transform(vertPlan);
        vertTrack.validate();

        assertTrue("Validation of Vertical Track data unsuccessful", vertTrack.dataValid);
    }
}