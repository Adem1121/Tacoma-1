package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test WeatherTrack methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherTrackTest {
    LateralPlan latPlan = new LateralPlan();
    LateralTrack latTrack = new LateralTrack();

    VelocityPlan velPlan = new VelocityPlan();
    VelocityTrack velTrack = new VelocityTrack();

    VerticalPlan vertPlan = new VerticalPlan();
    VerticalTrack vertTrack = new VerticalTrack();

    WeatherPlan wxPlan = new WeatherPlan();
    WeatherTrack wxTrack = new WeatherTrack();

    @Before
    public void setUp() throws Exception {
        // 01 Load flight plan and transform to lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 FlightPlan.xml",1);
        latPlan.transform();
        latPlan.validate();

        // 02 Load change velocity events and transform to velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        velPlan.transform();
        velPlan.validate();

        // 03 Load change altitude events and transform to vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        vertPlan.transform();
        vertPlan.validate();

        // 04 Load change weather events and transform to weather plan
        wxPlan.assignLat(latPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 08/IO/F05 EventCollection.xml",1);
        wxPlan.transform();
        wxPlan.validate();

        // 05 Transform lateral plan to lateral track and validate
        latTrack.assignVel(velPlan);
        latTrack.transform(latPlan);
        latTrack.validate();

        // 06 Transform velocity plan to velocity track and validate
        velTrack.assignLat(latTrack);
        velTrack.transform(velPlan);
        velTrack.validate();

        // 07 Transform vertical plan to vertical track and validate
        vertTrack.assignLat(latTrack);
        vertTrack.assignVel(velTrack);
        vertTrack.transform(vertPlan);
        vertTrack.validate();

        // 08 Transform weather plan to vertical track and validate
        wxTrack.assignLat(latTrack);
        wxTrack.assignVel(velTrack);
        wxTrack.transform(wxPlan);
        wxTrack.validate();
    }

    @Test
    public void testTransform() throws Exception {
        assertTrue("Validation of Weather Track data unsuccessful", wxTrack.dataValid);
    }

    @Test
    public void testGetPitchDeviationAtWpt() throws Exception {

    }

    @Test
    public void testGetBankDeviationAtWpt() throws Exception {

    }

    @Test
    public void testGetAltDeviationAtWpt() throws Exception {

    }
}