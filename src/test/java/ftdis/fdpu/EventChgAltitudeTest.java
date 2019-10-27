package ftdis.fdpu;

import org.junit.*;
import static org.junit.Assert.*;

/**
 * Unit test Change Altitude methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventChgAltitudeTest {
    double alti, altf, vs;
    Waypoint startWpt = new Waypoint(), endWpt = new Waypoint();
    LateralPlan latPlan = new LateralPlan();
    LateralTrack latTrack = new LateralTrack();
    VelocityPlan velPlan = new VelocityPlan();
    VelocityTrack velTrack = new VelocityTrack();


    @Before
    public void setUp() throws Exception {
        // 01 Load and transform lateral plan
        latPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 06/IO/FlightPlanV04.xml",1);
        latPlan.transform();

        // 02 Load and transform velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 06/IO/EventCollection.xml",1);
        velPlan.transform();

        // 03 Load, transform and validate lateral track
        latTrack.assignVel(velPlan);
        latTrack.transform(latPlan);
        latTrack.validate();
        assertTrue("Validation of Lateral Track data unsuccessful", latTrack.dataValid);

        // 04 Load, transform and validate velocity track
        velTrack.assignLat(latTrack);
        velTrack.transform(velPlan);
        velTrack.transform();
        velTrack.validate();
        assertTrue("Validation of Velocity Track data unsuccessful", velTrack.dataValid);

        // 05 Test Event Vars
        startWpt.setLat(47.452398);
        startWpt.setLon(-122.307838);

        endWpt.setLat(47.438139);
        endWpt.setLon(-122.311205);
    }

    @Ignore
    public void testGetStartPt() throws Exception {
    }

    @Ignore
    public void testGetEndPt() throws Exception {
    }

    @Test
    public void testValidate() throws Exception{
        double dist,vAsu,t;

        ////////////////////////////////////////////////////////////
        // Test Case 01
        ///////////////////////////////////////////////////////////

        // Define event
        EventChgAltitude chgAlt = new EventChgAltitude();
        chgAlt.assignLat(latPlan);
        chgAlt.assignVel(velPlan);
        chgAlt.setStartPt(startWpt.getLat(), startWpt.getLon());
        chgAlt.setEndPt(Double.NaN, Double.NaN);

        // Define parameters
        chgAlt.setAlti(0);
        chgAlt.setAltf(762);
        chgAlt.setVs(10);

        // Execute
        chgAlt.validate();
        assertTrue(chgAlt.dataValid);
        assertEquals(6316.2864, chgAlt.getDist(), 0.001);
        assertEquals(6.8789, Math.toDegrees(chgAlt.getAlpha()), 0.001);
        assertEquals(381, chgAlt.getAlt(chgAlt.getDist()/2), 0.001);

        // Validate
        dist = latPlan.getDist(chgAlt.getStartPt(), chgAlt.getEndPt());
        vAsu = velPlan.getVasu(chgAlt.getStartPt(), chgAlt.getEndPt());
        assertEquals(762, (dist / vAsu) * chgAlt.getVs(), 0.001);

        ////////////////////////////////////////////////////////////
        // Test Case 02
        ///////////////////////////////////////////////////////////

        // Define event
        chgAlt = new EventChgAltitude();
        chgAlt.assignLat(latPlan);
        chgAlt.assignVel(velPlan);
        chgAlt.setStartPt(Double.NaN, Double.NaN);
        chgAlt.setEndPt(endWpt.getLat(), endWpt.getLon());

        // Define parameters
        chgAlt.setAlti(762);
        chgAlt.setAltf(0);
        chgAlt.setVs(-4);

        // Execute
        chgAlt.validate();
        assertTrue(chgAlt.dataValid);
        assertEquals(16348.085, chgAlt.getDist(), 0.001);
        assertEquals(-2.66, Math.toDegrees(chgAlt.getAlpha()), 0.01);
        assertEquals(-381, chgAlt.getAlt(chgAlt.getDist() / 2), 0.001);

        // Validate
        dist = latPlan.getDist(chgAlt.getStartPt(), chgAlt.getEndPt());
        vAsu = velPlan.getVasu(chgAlt.getStartPt(), chgAlt.getEndPt());
        assertEquals(-762,(dist/vAsu) * chgAlt.getVs(),0.001);

        ////////////////////////////////////////////////////////////
        // Test Case 03
        ///////////////////////////////////////////////////////////

        // Define event
        endWpt = latPlan.getItmWpt(startWpt,6316.2864);

        chgAlt = new EventChgAltitude();
        chgAlt.assignLat(latPlan);
        chgAlt.assignVel(velPlan);
        chgAlt.setStartPt(startWpt.getLat(), startWpt.getLon());
        chgAlt.setEndPt(endWpt.getLat(), endWpt.getLon());

        // Define parameters
        chgAlt.setAlti(0);
        chgAlt.setAltf(762);
        chgAlt.setVs(Double.NaN);

        // Execute
        chgAlt.validate();
        assertTrue(chgAlt.dataValid);
        assertEquals(10, chgAlt.getVs(), 0.001);
    }

}