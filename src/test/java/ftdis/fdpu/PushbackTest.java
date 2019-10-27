package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Process Ground/Taxi Test Data
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class PushbackTest {
    LateralPlan grndPlan = new LateralPlan();
    LateralTrack grndTrack = new LateralTrack();

    VelocityPlan velPlan = new VelocityPlan();
    VelocityTrack velTrack = new VelocityTrack();

    VerticalPlan vertPlan = new VerticalPlan();
    VerticalTrack vertTrack = new VerticalTrack();

    WeatherPlan wxPlan = new WeatherPlan();
    WeatherTrack wxTrack = new WeatherTrack();

    AircraftAxis arcrftAxis = new AircraftAxis();

    @Before
    public void setUp() throws Exception {

        String flightPlanFile, eventCollectionFile;
        flightPlanFile = "/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/Pushback KEWR KTPA FlightPlan 20151024 2033.xml";
        eventCollectionFile = "/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/Pushback KEWR KTPA EventCollection 20151024 2033.xml";

        // 01 Load taxi plan and transform to lateral plan
        grndPlan.load(flightPlanFile, 1);
        grndPlan.transform();
        grndPlan.validate();
        System.out.println("Taxi to Runway Plan data: " + grndPlan.dataValid);

        // 02 Load change velocity events and transform to velocity plan
        velPlan.assignLat(grndPlan);
        velPlan.load(eventCollectionFile, 1);
        velPlan.transform();
        velPlan.validate();
        System.out.println("Velocity Plan data: " + velPlan.dataValid);

        // 03 Load change altitude events and trasform to vertical plan
        vertPlan.assignLat(grndPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load(eventCollectionFile, 1);
        vertPlan.transform();
        vertPlan.validate();
        System.out.println("Vertical Plan data: " + vertPlan.dataValid);

        // 04 Load change weather events and transform to weather plan
        wxPlan.assignLat(grndPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load(eventCollectionFile, 1);
        wxPlan.transform();
        wxPlan.validate();
        System.out.println("Weather Plan data: " + wxPlan.dataValid);

        // 05 Transform lateral plan to lateral track and validate
        grndTrack.assignVel(velPlan);
        grndTrack.transform(grndPlan);
        grndTrack.validate();
        System.out.println("Ground Track data: " + grndTrack.dataValid);

        // 06 Transform lateral ground track to velocity track and validate
        velTrack.assignLat(grndTrack);
        velTrack.transform(velPlan);
        //velTrack.adjustToGroundTrack();
        velTrack.validate();
        System.out.println("Velocity Track data: " + velTrack.dataValid);

        // 07 Transform vertical plan to vertical track and validate
        vertTrack.assignLat(grndTrack);
        vertTrack.assignVel(velTrack);
        vertTrack.transform(vertPlan);
        vertTrack.validate();
        System.out.println("Vertical Track data: " + velTrack.dataValid);

        // 08 Transform weather plan to vertical track and validate
        wxTrack.assignLat(grndTrack);
        wxTrack.assignVel(velTrack);
        wxTrack.transform(wxPlan);
        wxTrack.validate();
        System.out.println("Weather Track data: " + wxTrack.dataValid);

        // 09 Prepare Aircraft Axis
        arcrftAxis.assignLat(grndTrack);
        arcrftAxis.assignVel(velTrack);
        arcrftAxis.assignVert(vertTrack);
        arcrftAxis.assignWx(wxTrack);

        // 06 Prepare Aircraft Control
        //arcrftCtrl.assignLat(grndTrack);
        //arcrftCtrl.assignVel(velTrack);

        // 07 Prepare Aircraft Systems
        //arcrftSyst.reset();
    }


    @Test
    public void testPushBackTrackProcessing737NGX() throws Exception{
        Waypoint pos, prevPos;
        double[] flapsNGX, spoilersNGX;
        double cycleLn = 0.20, timeStmp = 0, trackLn, throttle;

        ///////////////////////////////////////////////////////////
        // Configure output file
        ///////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = "F11 Pushback Test " + dateFormat.format(date) + ".txt";
        File fout = new File("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/" + fileName);
        FileOutputStream fos = new FileOutputStream(fout);

        // Write to file
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        // Write header and add 737 NGX Custom Vars
        bw.write("#Aircraft: \"PMDG_737_NGX\"\r\n");
        bw.write("#EngineType: Jet\r\n");

        // Write data header
        bw.write("#Data:\t" +
                String.format("%-10s\t", "latitude") +
                String.format("%-10s\t", "longitude") +
                String.format("%-10s\t", "altitude") +
                String.format("%-10s\t", "pitch") +
                String.format("%-10s\t", "bank") +
                String.format("%-10s\t", "heading") +
                //String.format("%-10s\t", "OnGround") +
                String.format("%-10s\t", "ailerons") +
                String.format("%-10s\t", "spoilers") +
                String.format("%-10s\t", "throttle(1)") +
                String.format("%-10s\t", "throttle(2)") +
                String.format("%-10s\t", "timestamp"));
        bw.newLine();

        ///////////////////////////////////////////////////////////
        // Process and Write Data
        ///////////////////////////////////////////////////////////
        // Process and Write Data
        prevPos = grndTrack.getStartWpt();
        trackLn = grndTrack.getDist(grndTrack.getStartWpt(),grndTrack.getEndWpt());

        for(double trackDist = 0; trackDist < trackLn; timeStmp += cycleLn){

            // Calculate travelled distance on flight track
            trackDist = velTrack.getDist(grndTrack.getStartWpt(), timeStmp);

            // Check if end of track has been reached
            if((trackLn - trackDist) < 0.001) {
                trackDist = trackLn;
                // Ajdust time stamp to match time required to reach end of track
                timeStmp = (timeStmp - cycleLn) + grndTrack.getDist(prevPos,grndTrack.getEndWpt()) / velTrack.getVasu(prevPos,grndTrack.getEndWpt());
            }

            // Flight track
            pos = grndTrack.getItmWpt(grndTrack.getStartWpt(), trackDist);
            prevPos = pos;

            // Get PMDG 737 custom variables
            //flapsNGX = arcrftCtrl.getPMDG737FlapsAtWpt(pos, timeStmp);
            //spoilersNGX =  //arcrftCtrl.getPMDG737SpoilersAtWpt(pos, timeStmp);

            // Set NGX engine variables: N1 – 0.2 * 162 * 100 = THR
            throttle = 0;

            // Write data to file
            bw.write("\t" +
                    String.format(Locale.US, "%10.7f", pos.getLat()) + "\t" +
                    String.format(Locale.US, "%10.7f", pos.getLon()) + "\t" +
                    String.format(Locale.US, "%10.3f", PerfCalc.convertFt(arcrftAxis.getAltAtWpt(vertTrack.getSgmt(0).getStartPt()), "m")) + "\t" +
                    String.format(Locale.US, "%10.3f", 0.0) + "\t" +
                    String.format(Locale.US, "%10.3f", 0.0) + "\t" +
                    String.format(Locale.US, "%10.3f", NavCalc.getNewCourse(arcrftAxis.getHeadingAtWpt(pos),180)) + "\t" +
                    String.format(Locale.US, "%10.0f", 0.0) + "\t" +
                    String.format(Locale.US, "%10.0f", 0.0) + "\t" +
                    String.format(Locale.US, "%10.0f", throttle) + "\t" +
                    String.format(Locale.US, "%10.0f", throttle) + "\t" +
                    String.format(Locale.US, "%10.2f", timeStmp));

            bw.newLine();

            System.out.println("Processing cycle " + timeStmp);
        }
        // Close file
        bw.close();
    }

}
