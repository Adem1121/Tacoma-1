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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ftdis.fdpu.Config.*;

/**
 * Process end to end flight data as per JarDesign A330 specifications.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */


/**
 * Main JarDesign A330 Flight Processing class
 */
public class DebugFlightProcessing {

    /**
     * End to end flight data processing
     */
    public static void main(String[] args) {
        try{
            Waypoint pos, prevPos;
            double cycleLn, timeStmp = 0, timeMrk = 0, timeFile = 0, timeStart = 0, timeEnd = 99999, trackLn, trackDif, trackT;
            double tmpPrevReconDist, tmpPrevPosDist, prevTrackDist = 0;
            String ioDir, clsCmd, inputFileName ="", flightPlanFile, eventCollectionFile;
            boolean inProcess;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Configure processing parameters and input files
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

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

            // Set input files
            flightPlanFile = localDir + ioDir + "FlightPlan.xml";
            eventCollectionFile = localDir + ioDir + "EventCollection.xml";

            if(args.length == 2 || args.length == 4) {
                flightPlanFile = localDir + ioDir + args[0];
                eventCollectionFile = localDir + ioDir + args[1];

                inputFileName = args[0].split("\\.(?=[^\\.]+$)")[0];
            }

            // Set start and end time
            if(args.length == 4) {
                if (!Double.isNaN(Double.parseDouble(args[args.length - 2])))
                    timeStart = Double.parseDouble(args[args.length - 2]);

                if (!Double.isNaN(Double.parseDouble(args[args.length - 1])))
                    timeEnd = Double.parseDouble(args[args.length - 1]);
            }

            // Load config file and set vars
            cycleLn = 1; //FLT_PROC_CYCLE_LN

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Configure output file
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Prep date format
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

            // Configure filename and file output stream
            String fileName = inputFileName + " Debug FlightData " + dateFormat.format(date) + ".txt";
            File fout = new File(localDir + ioDir + fileName);
            FileOutputStream fos = new FileOutputStream(fout);

            // Write to file
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 Process Flight Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // 00 Initialize
            LateralPlan latPlan = new LateralPlan();
            LateralTrack latTrack = new LateralTrack();

            VelocityPlan velPlan = new VelocityPlan();
            VelocityTrack velTrack = new VelocityTrack();

            VerticalPlan vertPlan = new VerticalPlan();
            VerticalTrack vertTrack = new VerticalTrack();

            AircraftAxis arcrftAxis = new AircraftAxis();
            AircraftControl arcrftCtrl = new AircraftControl();
            AircraftSystem arcrftSyst = new AircraftSystem();

            WeatherPlan wxPlan = new WeatherPlan();
            WeatherTrack wxTrack = new WeatherTrack();

            AircraftEngine arcrftEng = new AircraftEngine();

            // 01 Load flight plan and transform to lateral plan
            latPlan.load(flightPlanFile, 3);
            latPlan.transform();
            latPlan.validate();
            System.out.println("Lateral Plan data: " + latPlan.dataValid);

            // 02 Load change velocity events and transform to velocity plan
            velPlan.assignLat(latPlan);
            velPlan.load(eventCollectionFile, 3);
            velPlan.transform();
            velPlan.validate();
            System.out.println("Velocity Plan data: " + velPlan.dataValid);

            // 03 Load change altitude events and transform to vertical plan
            vertPlan.assignLat(latPlan);
            vertPlan.assignVel(velPlan);
            vertPlan.load(eventCollectionFile, 3);
            vertPlan.transform();
            vertPlan.validate();
            System.out.println("Vertical Plan data: " + vertPlan.dataValid);

            // 04 Load change weather events and transform to weather plan
            wxPlan.assignLat(latPlan);
            wxPlan.assignVel(velPlan);
            wxPlan.load(eventCollectionFile, 3);
            wxPlan.transform();
            wxPlan.validate();
            System.out.println("Weather Plan data: " + wxPlan.dataValid);

            // 05 Transform lateral plan to lateral track and validate
            latTrack.assignVel(velPlan);
            latTrack.transform(latPlan);
            latTrack.validate();
            System.out.println("Lateral Track data: " + latTrack.dataValid);

            // 06 Transform velocity plan to velocity track and validate
            velTrack.assignLat(latTrack);
            velTrack.transform(velPlan);
            velTrack.validate();
            System.out.println("Velocity Track data: " + velTrack.dataValid);

            // 07 Transform vertical plan to vertical track and validate
            vertTrack.assignLat(latTrack);
            vertTrack.assignVel(velTrack);
            vertTrack.transform(vertPlan);
            vertTrack.validate();
            System.out.println("Vertical Track data: " + velTrack.dataValid);

            // 08 Transform weather plan to vertical track and validate
            wxTrack.assignLat(latTrack);
            wxTrack.assignVel(velTrack);
            wxTrack.transform(wxPlan);
            wxTrack.validate();
            System.out.println("Weather Track data: " + wxTrack.dataValid);

            // 09 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);
            arcrftAxis.assignControl(arcrftCtrl);

            // 10 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);
            arcrftCtrl.assignVert(vertTrack);
            arcrftCtrl.assignWx(wxTrack);

            // 11 Prepare Aircraft Propulsion
            arcrftEng.assignLat(latTrack);
            arcrftEng.assignVel(velTrack);
            arcrftEng.assignVert(vertTrack);
            arcrftEng.assignWx(wxTrack);

            // 12 Reset Aircraft Systems
            arcrftSyst.reset();
            float velTrackDist = 0, latTrackDist = 0;

            // Process and Write Data
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt()); // 6531
            prevPos = latTrack.getStartWpt();
            inProcess = false;


            int s = 0;

            VelocitySegment thisVelSgmt;
            while(ListUtil.inBound(s,velTrack.velocitySegments)){
                thisVelSgmt = velTrack.velocitySegments.get(s);
                velTrackDist += latTrack.getDist(thisVelSgmt.getStartPt(),thisVelSgmt.getEndPt());

                s++;
            }


            s=0;
            LateralSegment thisLatSgmt;
            while(s < latTrack.getSgmtCount()){
                thisLatSgmt = latTrack.getSgmt(s);
                latTrackDist += latTrack.getDist(thisLatSgmt.getStartPt(),thisLatSgmt.getEndPt());

                s++;
            }


            //for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn)
            for(double trackDist = 0; trackDist < trackLn; timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    if(trackDist == 0)
                        prevPos = latTrack.getItmWpt(latTrack.getStartWpt(),velTrack.getDist(latTrack.getStartWpt(), timeStmp));
                        //prevPos = latTrack.getStartWpt();
                        //

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    //trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);
                    trackDif = velTrack.getDist(prevPos, cycleLn);
                    trackDist += trackDif;

                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********
                    // tmpPrevReconDist = velTrack.getDist(latTrack.getStartWpt(), (timeStmp-cycleLn));
                    // tmpPrevPosDist = velTrack.getDist(prevPos, cycleLn);
                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********

                    // Check if end of track has been reached
                    if ((trackLn - trackDist) <= 1) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(prevPos, trackDif);
                    //pos = latTrack.getItmWpt(prevPos, velTrack.getDist(prevPos, cycleLn));

                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********
                    tmpPrevReconDist = velTrack.getDist(prevPos, cycleLn);
                    tmpPrevPosDist = latTrack.getDist(prevPos,pos);


                    // tmpPrevReconDist = velTrack.getDist(prevPos, cycleLn);
                    //pos = latTrack.getItmWpt(prevPos, velTrack.getDist(prevPos, cycleLn));
                    //velTrack.getDist(latTrack.getStartWpt(), timeStmp+1002)-velTrack.getDist(latTrack.getStartWpt(), timeStmp+1001)

                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********

                    // Write data to file
                    bw.write(// Position Vars: Lat, Long, Alt, Heading
                            String.format(Locale.US, "%10.8f", trackDist) + "," +
                            String.format(Locale.US, "%10.6f", vertTrack.getAltAtWpt(pos)) + "," +
                            String.format(Locale.US, "%10.6f", velTrack.getVasAtWpt(pos)) + "," +
                            // Time Stamp
                            String.format(Locale.US, "%10.3f", timeFile)  + "," +
                            String.format(Locale.US, "%10.3f", tmpPrevPosDist) + "," +
                            String.format(Locale.US, "%10.3f", (trackDist - prevTrackDist))  + "," +
                            String.format(Locale.US, "%10.3f", tmpPrevReconDist)
                    );
                    bw.newLine();

                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********
                    prevTrackDist = trackDist;
                    // DEBUG ******** DEBUG ******** DEBUG ******** DEBUG ********

                    System.out.println("Flight - Processing cycle " + String.format(Locale.US, "%10.2f", timeFile) + " Progress: " + String.format(Locale.US, "%10.4f", (trackDist / trackLn) * 100) + " %");
                    //Runtime.getRuntime().exec(clsCmd);
                } else{
                    inProcess = false;
                    System.out.println("Flight - Skipping cycle " + String.format(Locale.US, "%10.2f", timeFile));
                    timeFile += cycleLn;
                }
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;


        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}


