package ftdis.fdpu;

import org.junit.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

import static org.junit.Assert.assertEquals;

/**
 * Unit test LateralPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class AircraftAxisTest {
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
        int planID = 3;
        String ioDir, clsCmd, inputFileName ="", flightPlanFile, eventCollectionFile;

        // Set input directories
        final String os = System.getProperty("os.name");

        Path localDir = Paths.get("").toAbsolutePath();

        if (os.contains("Windows"))
        {
            ioDir = "\\IO\\";
            clsCmd = "cls";
        }
        else
        {
            ioDir = "/IO/";
            clsCmd = "clear";
        }

        flightPlanFile = localDir + ioDir + "KSEA KSEA FlightPlan.xml";
        eventCollectionFile = localDir + ioDir + "KSEA KSEA EventCollection.xml";


        // 01 Load flight plan and transform to lateral plan
        latPlan.load(flightPlanFile, planID);
        latPlan.transform();
        latPlan.validate();

        // 02 Load change velocity events and transform to velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load(eventCollectionFile, planID);
        velPlan.transform();
        velPlan.validate();

        // 03 Load change altitude events and transform to vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load(eventCollectionFile, planID);
        vertPlan.transform();
        vertPlan.validate();

        // 04 Load change weather events and transform to weather plan
        wxPlan.assignLat(latPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load(eventCollectionFile, planID);
        wxPlan.transform();
        wxPlan.validate();

        // 05 Transform lateral plan to lateral track and validate
        latTrack.assignVel(velPlan);
        latTrack.transform(latPlan);
        latTrack.validate();

        // 06 Transform lateral ground track to velocity track and validate
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
    public void testGetPitchAngle() throws Exception {
        Double vAsu, trackDist, trackLn;
        Waypoint trackWpt, startWpt, endWpt;
        VerticalSegment testSgmt;
        LateralSegment thisSgmt;
        AircraftAxis testAxis = new AircraftAxis();
        AircraftControl testControl = new AircraftControl();

        testControl.assignLat(latTrack);
        testControl.assignVel(velTrack);
        testControl.assignVert(vertTrack);
        testControl.assignWx(wxTrack);

        testAxis.assignLat(latTrack);
        testAxis.assignVel(velTrack);
        testAxis.assignVert(vertTrack);
        testAxis.assignWx(wxTrack);
        testAxis.assignControl(testControl);


        System.out.println("Pitch Angle");
        System.out.println("---------------------------------------------------------------");
        System.out.println("|    Elapsed Time    |     Altitude      |     Pitch Angle    |");
        System.out.println("---------------------------------------------------------------");

        //startWpt = latTrack.getSgmt(22).getStartPt();
        startWpt = latTrack.getStartWpt();
        //endWpt = latTrack.getSgmt(28).getEndPt();
        endWpt = latTrack.getEndWpt();

        trackLn = latTrack.getDist(startWpt,endWpt);

        for(float timeStmp = 1500; timeStmp <= 1650; timeStmp += 0.2){
            trackDist = velTrack.getDist(startWpt, timeStmp);

            if ((trackLn - trackDist) <= 0.01)
                trackDist = trackLn;

            trackWpt = latTrack.getItmWpt(startWpt,trackDist);

            System.out.println("         " + timeStmp + "             "
                                + String.format(Locale.US, "%10.2f",vertTrack.getAltAtWpt(trackWpt))
                                + "            " + String.format(Locale.US, "%10.4f",testAxis.getPitchAngleAtWpt(trackWpt))
                                + "            " + String.format(Locale.US, "%10.4f",trackLn - trackDist)
            );
        }

    }


    @Test
    public void testGetBankAngle() throws Exception {
        Waypoint testWpt = new Waypoint();
        LateralSegment testSgmt;
        AircraftAxis testAxis = new AircraftAxis();
        testAxis.assignLat(latTrack);
        testAxis.assignVel(velTrack);
        testAxis.assignVert(vertTrack);

        System.out.println("Bank Angle");
        System.out.println("-----------------------------------------------------------------");

        //Loop through segments
        for(int s = 0; s < latTrack.getSgmtCount(); s++){
            testSgmt = latTrack.getSgmt(s);

            System.out.println("Sgmt " + s +  ", Start Pt: " + testAxis.getBankAngleAtWpt(testSgmt.getStartPt()));
            System.out.println("Sgmt " + s +  ", 1/4 Pt: " + testAxis.getBankAngleAtWpt(latTrack.getItmWpt(testSgmt.getStartPt(), testSgmt.getDist() * 0.25)));
            System.out.println("Sgmt " + s +  ", 1/2 Pt: " + testAxis.getBankAngleAtWpt(latTrack.getItmWpt(testSgmt.getStartPt(), testSgmt.getDist() * 0.50)));
            System.out.println("Sgmt " + s +  ", 3/4 Pt: " + testAxis.getBankAngleAtWpt(latTrack.getItmWpt(testSgmt.getStartPt(), testSgmt.getDist() * 0.75)));
            System.out.println("Sgmt " + s +  ", End Pt: " + testAxis.getBankAngleAtWpt(testSgmt.getEndPt()));
            System.out.println();
        }
    }

    @Ignore
    public void testGetHeading() throws Exception {

    }
}