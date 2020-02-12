package ftdis.fdpu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

import static ftdis.fdpu.Config.*;
import static java.lang.Math.abs;

/**
 * Process end to end ground services specifications.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class GroundServicesProcessing {

    /**
     * End to end flight data processing
     */
    public static void main(String[] args) {
        try{
            Waypoint pos, prevPos, nwPos;
            double cycleLn = FLT_PROC_CYCLE_LN, timeStart = 0, timeEnd = 99999, timeMrk = 0, timeFile = 0,  trackLn, trackT = 0, trackDist;
            String ioDir, clsCmd, inputFileName ="", flightPlanFile, eventCollectionFile;
            LocalDateTime execStartDateTime, progrStartDateTime;

            Vector<FlightProcessingLineItem> multiThreadReturnlist = new Vector<>();
            Vector<FlightProcessingPlanSet> processedPlans = new Vector<>();
            //FlightProcessingPlanSet planSet;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 Configure processing parameters and input files
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Set input directories
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath().getParent().getParent();

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

            // Set processing parameters
            flightPlanFile = localDir + ioDir + "FlightPlan.xml";
            eventCollectionFile = localDir + ioDir + "EventCollection.xml";

            // Set input files
            if(args.length >= 2) {
                flightPlanFile = localDir + ioDir + args[0];
                eventCollectionFile = localDir + ioDir + args[1];
                inputFileName = args[0].split("\\.(?=[^\\.]+$)")[0];
            }

            // Set start and end time
            if(args.length >= 6) {
                if (!Double.isNaN(Double.parseDouble(args[3])))
                    timeStart = Double.parseDouble(args[3]);


                if (!Double.isNaN(Double.parseDouble(args[5])))
                    timeEnd = Double.parseDouble(args[5]);
            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 02 Configure output file
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Prep date format
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;
            execStartDateTime = progrStartDateTime = LocalDateTime.now();

            // Configure filename and file output stream
            String fileName = inputFileName + " PushbackData " + dateFormat.format(date) + ".txt";
            File fout = new File(localDir + ioDir + fileName);
            FileOutputStream fos = new FileOutputStream(fout);

            // Write to file
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 Load and Prepare Data for Processing
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            LateralPlan latPlan; //= new LateralPlan();
            LateralTrack latTrack; //= new LateralTrack();

            VelocityPlan velPlan; //= new VelocityPlan();
            VelocityTrack velTrack; //= new VelocityTrack();

            VerticalPlan vertPlan; //= new VerticalPlan();
            VerticalTrack vertTrack; //= new VerticalTrack();

            WeatherPlan wxPlan; //= new WeatherPlan();
            WeatherTrack wxTrack; //= new WeatherTrack();

            //AircraftAxis arcrftAxis; //= new AircraftAxis();
            //AircraftControl arcrftCtrl; //= new AircraftControl();

            //AircraftEngine arcrftEng; //= new AircraftEngine();
            //AircraftSystem arcrftSyst; //= new AircraftSystem();



            // Loop through flight phases and prepare data
            for (int i = 1; i <= 4; i++){
                // 00 Initialize
                trackLn = 0;
                FlightProcessingPlanSet planSet = new FlightProcessingPlanSet();
                planSet.phase = i;

                latPlan = new LateralPlan();
                latTrack = new LateralTrack();

                velPlan = new VelocityPlan();
                velTrack = new VelocityTrack();

                vertPlan = new VerticalPlan();
                vertTrack = new VerticalTrack();

                wxPlan = new WeatherPlan();
                wxTrack = new WeatherTrack();

                //arcrftAxis = new AircraftAxis();
                //arcrftCtrl = new AircraftControl();

                //arcrftEng = new AircraftEngine();
                //arcrftSyst = new AircraftSystem();

                // 01 Load flight plan and transform to lateral plan
                latPlan.load(flightPlanFile, i);
                latPlan.transform();
                latPlan.validate();
                planSet.latPlan = latPlan;

                // 02 Load change velocity events and transform to velocity plan
                velPlan.assignLat(latPlan);
                velPlan.load(eventCollectionFile, i);
                velPlan.transform();
                velPlan.validate();
                planSet.velPlan = velPlan;

                // 03 Load change altitude events and transform to vertical plan
                vertPlan.assignLat(latPlan);
                vertPlan.assignVel(velPlan);
                vertPlan.load(eventCollectionFile, i);
                vertPlan.transform();
                vertPlan.validate();
                planSet.vertPlan = vertPlan;

                // 04 Load change weather events and transform to weather plan
                wxPlan.assignLat(latPlan);
                wxPlan.assignVel(velPlan);
                wxPlan.load(eventCollectionFile, i);
                wxPlan.transform();
                wxPlan.validate();
                planSet.wxPlan = wxPlan;

                // 05 Transform lateral plan to lateral track and validate
                latTrack.assignVel(velPlan);
                latTrack.transform(latPlan);
                latTrack.validate();
                planSet.latTrack = latTrack;

                // 06 Transform lateral ground track to velocity track and validate
                velTrack.assignLat(latTrack);
                velTrack.transform(velPlan);
                velTrack.validate();
                planSet.velTrack = velTrack;

                // 07 Transform vertical plan to vertical track and validate
                vertTrack.assignLat(latTrack);
                vertTrack.assignVel(velTrack);
                vertTrack.transform(vertPlan);
                vertTrack.validate();
                planSet.vertTrack = vertTrack;

                // 08 Transform weather plan to vertical track and validate
                wxTrack.assignLat(latTrack);
                wxTrack.assignVel(velTrack);
                wxTrack.transform(wxPlan);
                wxTrack.validate();
                planSet.wxTrack = wxTrack;

                processedPlans.add(planSet);

            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 Process Pushback
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Load and plans
            latTrack = processedPlans.get(0).latTrack;
            velTrack = processedPlans.get(0).velTrack;
            vertTrack = processedPlans.get(0).vertTrack;
            wxTrack = processedPlans.get(0).wxTrack;

            // Assign plans
            AircraftAxis arcrftAxis = new AircraftAxis();
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);


            // Define time to complete pushback track
            int pbPhase = 1;
            double returnT = 0, disconnectT = 30, pbHeading = 0, twbHeading = 0, twbOffset = 2.5; //3.1;
            trackT = 400;
            trackLn = latTrack.getLength();
            pos = prevPos = latTrack.getStartWpt();


            // Add model parameters
            bw.write("#MODEL:Resources/default scenery/sim objects/apt_vehicles/pushback/Tug_GT110.obj");
            bw.newLine();

            for(double timeStmp = 0;  timeStmp < trackT; timeStmp += cycleLn) {

                // Phase 1: Push back
                if(pbPhase == 1){
                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);

                    // Offset position by wheel base
                    nwPos = NavCalc.getRadWpt(pos,(AIRCRAFT_WHEELBASE),NavCalc.getNewCourse(arcrftAxis.getHeadingAtWpt(pos), 180));
                    pos = NavCalc.getRadWpt(pos,(AIRCRAFT_WHEELBASE + twbOffset),NavCalc.getNewCourse(arcrftAxis.getHeadingAtWpt(pos), 180));

                    // Calculate headings
                    pbHeading = NavCalc.getInitBearing(prevPos,pos);
                    twbHeading = NavCalc.getCourseChange(pbHeading, NavCalc.getInitBearing(pos,nwPos));

                    prevPos = pos;

                    // Check if end of track has been reached
                    if ((trackLn - trackDist) <= 0.01) {
                        returnT = timeStmp;
                        pbPhase = 2;
                    }

                // Phase 2: Wait and disconnect
                } else if(pbPhase == 2) {
                    //pos = prevPos;
                    //pbHeading = NavCalc.getNewCourse(arcrftAxis.getHeadingAtWpt(pos), 180);

                    // Wait to disconnect
                    if(disconnectT <= 1)
                        pbPhase = 3;
                    else
                        disconnectT -= cycleLn;

                // Phase 3: Return to position
                } else if(pbPhase == 3){
                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), returnT);
                    returnT -= cycleLn;

                    // Get position and save position for calculations in next cycle
                    nwPos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist - (AIRCRAFT_WHEELBASE));
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist - (AIRCRAFT_WHEELBASE + twbOffset));

                    // Offset position by wheel base
                    //pos = latTrack.getItmWpt(pos,AIRCRAFT_WHEELBASE);

                    // Calculate headings
                    //pbHeading = NavCalc.getInitBearing(pos,prevPos);
                    pbHeading = arcrftAxis.getHeadingAtWpt(pos);
                    twbHeading = NavCalc.getCourseChange(pbHeading, NavCalc.getInitBearing(pos,nwPos));

                    if(abs(twbHeading) > 90)
                        twbHeading = 0;

                    prevPos = pos;

                    if(trackDist <= 0.1)
                        pbPhase = 4;

                // Phase 4: Park at position
                } else {
                    pos = latTrack.getStartWpt();
                    pbHeading = arcrftAxis.getHeadingAtWpt(latTrack.getStartWpt());
                    twbHeading = 0;
                }

                // Write data to file
                bw.write(// Position Vars: Lat, Long, Alt, Heading, Speed
                        String.format(Locale.US, "%10.15f", pos.getLat()) + "," +
                                String.format(Locale.US, "%10.15f", pos.getLon()) + "," +
                                String.format(Locale.US, "%10.6f", vertTrack.getAltAtWpt(vertTrack.getSgmt(0).getStartPt())) + "," +
                                String.format(Locale.US, "%10.6f", pbHeading) + "," +
                                String.format(Locale.US, "%10.6f", twbHeading) + "," +
                                // Phase & Time Stamp
                                "1," +
                                String.format(Locale.US, "%10.2f", timeStmp));

                bw.newLine();

                System.out.println("Pushback - Processing cycle " + String.format(Locale.US, "%10.2f", timeStmp) + " Progress: " + String.format(Locale.US, "%10.4f", (timeStmp / trackT) * 100) + " %");
                //Runtime.getRuntime().exec(clsCmd);

            }
            // Close file
            bw.close();
            System.out.println("File saved!");

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}



