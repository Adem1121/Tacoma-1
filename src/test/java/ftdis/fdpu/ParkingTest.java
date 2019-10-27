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

import static org.junit.Assert.*;

/**
 * Process Parking Test Data
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingTest {

    ParkingPlan parkingPlan = new ParkingPlan();

    @Before
    public void setUp() throws Exception {
        parkingPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/KTPA Traffic Static.xml");
    }

    @Test
    public void testParkingPlanProcessing() throws Exception{
        Waypoint pos;
        ParkingSegment parkingSgmt;
        double cycleLn = 1, timeStmp = 0, trackLn;

        ///////////////////////////////////////////////////////////
        // Configure output file
        ///////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = "KTPA Traffic Static Final" + ".txt";
        File fout = new File("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/" + fileName);
        FileOutputStream fos = new FileOutputStream(fout);

        // Write to file
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        ///////////////////////////////////////////////////////////
        // Loop through parking segments
        ///////////////////////////////////////////////////////////
        for(int s = 0; s < parkingPlan.getSgmtCount(); s++) {

            parkingSgmt = parkingPlan.getSgmt(s);

            // Write aircraft header
            bw.write("#Aircraft: \"" + parkingSgmt.getType() + "\"\r\n");
            bw.write("#StaticCGtoGround: " + parkingSgmt.getStaticCG() + "\r\n");
            bw.write("#EngineType: Jet\r\n");

            // Write data header
            bw.write("#Data:\t" +
                    String.format("%-10s\t", "latitude") +
                    String.format("%-10s\t", "longitude") +
                    String.format("%-10s\t", "altitude") +
                    String.format("%-10s\t", "pitch") +
                    String.format("%-10s\t", "bank") +
                    String.format("%-10s\t", "heading") +
                    String.format("%-10s\t", "timestamp"));
            bw.newLine();

            // Write data
            for(double t = 0; t <= parkingSgmt.getParkingTime(); t += cycleLn) {
                bw.write("\t" +
                        String.format(Locale.US, "%10.7f", parkingSgmt.getPosition().getLat()) + "\t" +
                        String.format(Locale.US, "%10.7f", parkingSgmt.getPosition().getLon()) + "\t" +
                        String.format(Locale.US, "%10.3f", parkingSgmt.getAltitude()) + "\t" +
                        String.format(Locale.US, "%10.3f", 0.0) + "\t" +
                        String.format(Locale.US, "%10.3f", 0.0) + "\t" +
                        String.format(Locale.US, "%10.3f", parkingSgmt.getHeading()) + "\t" +
                        String.format(Locale.US, "%10.2f", t));
                bw.newLine();
            }
        }
        // Close file
        bw.close();
    }
}