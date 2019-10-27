package ftdis.fdpu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Process Parking Plan .xml file (either prepared by the fplu/ParkingProcessing class, or prepared manually)
 * and transform/export data to .txt file compatible with FSRecorder.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingProcessing {
    public static void main(String[] args) {
        try{
            // Define and init vars
            String ioDir, inputFileName = "", outputFileName, outputFile = "";
            ParkingPlan parkingPlan = new ParkingPlan();
            ParkingSegment parkingSgmt;
            double cycleLn = 1;

            // Set input directories
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath();

            if (os.contains("Windows"))
                ioDir = "\\IO\\";
            else
                ioDir = "/IO/";

            if(args.length == 1) {
                // Define output filename
                Date date = new Date() ;
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

                inputFileName = args[0];
                outputFileName = inputFileName.split("\\.(?=[^\\.]+$)")[0] + " Final " + dateFormat.format(date) + ".txt";

                // Define output directory
                outputFile = localDir + ioDir + outputFileName;
            }

            // Configure filename and file output stream
            File fout = new File(outputFile);
            FileOutputStream fos = new FileOutputStream(fout);

            // Write to file
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            // Load Parking Plan
            System.out.println("Processing " + inputFileName);
            parkingPlan.load(localDir + ioDir + inputFileName);

            // Loop through parking segments
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
            System.out.println("Processing complete!");

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
