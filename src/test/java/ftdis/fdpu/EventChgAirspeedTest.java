package ftdis.fdpu;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test Change Airspeed methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventChgAirspeedTest {
    double vAsi, vAsf, a;
    Waypoint startWpt = new Waypoint(), endWpt = new Waypoint();
    LateralPlan lateralPlan = new LateralPlan(1);
    LateralTrack lateralTrack = new LateralTrack(1);

    @Before
    public void setUp() throws Exception {
        // 01 Load waypoints to flight plan from external file
        lateralPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 07/IO/F04 FlightPlan.xml",1);

        // 02 Transform waypoints to a consecutive set of direct segments
        lateralPlan.transform();

        // 03 Test Event Vars
        startWpt.setLat(47.431817);
        startWpt.setLon(-122.308039);

        endWpt.setLat(47.452398);
        endWpt.setLon(-122.307838);

        vAsi = 0;
        vAsf = 70.5;
        //Dist: 2288.552844620981
    }

    @Ignore
    public void testGetStartPt() throws Exception {
    }

    @Ignore
    public void testGetEndPt() throws Exception {
    }

    @Test
    public void testValidate() throws Exception{
        // Define event
        EventChgAirspeed chgAirspeed = new EventChgAirspeed();
        chgAirspeed.assign(lateralPlan);
        chgAirspeed.setStartPt(startWpt.getLat(), startWpt.getLon());
        chgAirspeed.setEndPt(endWpt.getLat(), endWpt.getLon());

        // 01 Define parameters
        chgAirspeed.setvAsi(vAsi);
        chgAirspeed.setvAsf(vAsf);
        chgAirspeed.setAcc(Double.NaN);

        // 01 Execute
        chgAirspeed.validate();
        assertTrue(chgAirspeed.dataValid);
        assertEquals(1.08, chgAirspeed.getAcc(), 0.01);

        // 02 Define parameters
        chgAirspeed.setvAsf(Double.NaN);

        // 02 Execute
        chgAirspeed.validate();
        assertTrue(chgAirspeed.dataValid);
        assertEquals(70.5, chgAirspeed.getVAsf(), 0);

        // 03 Define parameters
        chgAirspeed.setvAsi(Double.NaN);

        // 03 Execute
        chgAirspeed.validate();
        assertTrue(chgAirspeed.dataValid);
        assertEquals(0.0, chgAirspeed.getVAsi(), 0);

        // 04 Define parameters
        chgAirspeed.setStartPt(Double.NaN, Double.NaN);

        // 04 Execute
        chgAirspeed.validate();
        assertTrue(chgAirspeed.dataValid);
        assertEquals(2288.55, chgAirspeed.getDist(), 0.01);
    }

    @Test
    public void testGetLength() throws Exception {
       //return dist required
    }

}