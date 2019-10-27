package ftdis.fplu;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Process FSRecorder .txt file and identify all of the individual aircraft, including their position, heading
 * and altitude, and save data to .xml file.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingProcessing {

    public static void main(String[] args) {
        try{
            String ioDir, fileMasterPlan = "";

            // Set input directories
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath();

            if (os.contains("Windows"))
                ioDir = "\\IO\\";
            else
                ioDir = "/IO/";

            if(args.length == 1) {
                fileMasterPlan = localDir + ioDir + args[0];
            }
            ParkingPlan parkingPlan = new ParkingPlan();

            System.out.println("Processing " + fileMasterPlan);

            parkingPlan.transform(fileMasterPlan);

            System.out.println("Processing complete!");

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
