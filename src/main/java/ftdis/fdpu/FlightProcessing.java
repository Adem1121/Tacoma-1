package ftdis.fdpu;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

import static ftdis.fdpu.Config.*;
import static java.lang.Math.abs;

/**
 * Process end to end flight data as per JarDesign A330 specifications.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */

/**
 * Callable classes to enable parallel flight plan processing
 */


class FlightProcessingThreadCallable implements Callable<Void> {

    private FlightProcessingThread flightProcessingThread;
    private int threadId;
    private Vector<FlightProcessingLineItem> lineItems;
    private Vector<FlightProcessingPlanSet> processedPlans;
    private String flightPlanFile;
    private String eventCollectionFile;
    private double timeStart;
    private double timeEnd;
    private double absDuration;

    public FlightProcessingThreadCallable(FlightProcessingThread flightProcessingThread, int threadId, Vector<FlightProcessingLineItem> lineItems, Vector<FlightProcessingPlanSet> processedPlans, String flightPlanFile, String eventCollectionFile, double timeStart, double timeEnd, double absDuration){
        this.flightProcessingThread = flightProcessingThread;
        this.threadId = threadId;
        this.lineItems = lineItems;
        this.processedPlans = processedPlans;
        this.flightPlanFile = flightPlanFile;
        this.eventCollectionFile = eventCollectionFile;
        this.timeStart = timeStart;
        this.timeEnd = timeEnd;
        this.absDuration = absDuration;
    }

    @Override
    public Void call() throws Exception {
        this.flightProcessingThread.processFile(threadId, lineItems, processedPlans, flightPlanFile, eventCollectionFile, timeStart, timeEnd, absDuration);
        return null;
    }
}

/**
 * Main Flight Processing class
 */
public class FlightProcessing {

    /**
     * End to end flight data processing
     */
    public static void main(String[] args) {
        try{
            int noOfThreads = 1, lineItems = 0;
            double cycleLn = FLT_PROC_CYCLE_LN, timeStart = 0, timeEnd = 99999, trackLn, trackT = 0;
            String ioDir, clsCmd, inputFileName ="", flightPlanFile, eventCollectionFile;

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

            // Set number of threads ( default 25)
            if(args.length == 8) {
                try{
                    noOfThreads = Integer.parseInt(args[7]);
                } catch (NumberFormatException e) {
                    noOfThreads = 25;
                }

            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 02 Configure output file
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Prep date format
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

            // Configure filename and file output stream
            String fileName = inputFileName + " FlightData " + dateFormat.format(date) + ".txt";
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

                /*
                // 09 Prepare Aircraft Axis
                arcrftAxis.assignLat(latTrack);
                arcrftAxis.assignVel(velTrack);
                arcrftAxis.assignVert(vertTrack);
                arcrftAxis.assignWx(wxTrack);
                arcrftAxis.assignControl(arcrftCtrl);
                planSet.arcrftAxis = arcrftAxis;

                // 10 Prepare Aircraft Control
                arcrftCtrl.assignLat(latTrack);
                arcrftCtrl.assignVel(velTrack);
                arcrftCtrl.assignVert(vertTrack);
                arcrftCtrl.assignWx(wxTrack);
                planSet.arcrftCtrl = arcrftCtrl;

                // 11 Prepare Aircraft Propulsion
                arcrftEng.assignLat(latTrack);
                arcrftEng.assignVel(velTrack);
                arcrftEng.assignVert(vertTrack);
                arcrftEng.assignWx(wxTrack);
                planSet.arcrftEng = arcrftEng;

                // 12 Prepare Aircraft Systems
                arcrftSyst.assignLat(latTrack);
                arcrftSyst.assignVel(velTrack);
                arcrftSyst.assignVert(vertTrack);
                planSet.arcrftSyst = arcrftSyst;
                */
                processedPlans.add(planSet);


                // Calculate time to complete flight track
                trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
                trackT += trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());

                // Add parking time at destination gate to track time
                trackT += AIRCRAFT_PARK_TIME;

                if(trackT > timeEnd)
                    trackT = timeEnd;
            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 Process Data
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            FlightProcessingThread flightProcessingThread = new FlightProcessingThread();

            MonitorUtil ftdisMonitor = new MonitorUtil();
            double threadStart = timeStart, threadEnd = 0, progress = 0;
            double absDuration = trackT - timeStart;
            int threadDuration = (int) absDuration/noOfThreads;

            ExecutorService es = Executors.newFixedThreadPool(noOfThreads);
            for (int i = 1; i <= noOfThreads; i++){

                if(i != noOfThreads)
                    threadEnd = threadStart + threadDuration;
                else
                    threadEnd = trackT;

                es.submit(new FlightProcessingThreadCallable(flightProcessingThread, i, multiThreadReturnlist, processedPlans, flightPlanFile, eventCollectionFile, threadStart, threadEnd, absDuration));

                threadStart = threadEnd + cycleLn;

                System.out.println("Starting thread... " + i);
            }

            // Initiate shut down and wait for threads to complete / terminate
            es.shutdown();

            // Print progress to console while threads are processing
            while(!es.isTerminated()) {

                if(multiThreadReturnlist.size() != lineItems){
                    lineItems = multiThreadReturnlist.size();

                    progress = (lineItems/(1/cycleLn))/absDuration * 100;

                    System.out.println("Progress... " + String.format(Locale.US, "%10.3f", progress)  + " %");

                    // Send out progress updates
                    // @50%
                }


            }

            // Wait until all threads have been terminated
            es.awaitTermination(7, TimeUnit.DAYS);

            // Send out completion notice
            ftdisMonitor.sendProgressMail(100);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 05 Write Data to Output File
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            System.out.println("Sorting...");
            Collections.sort(multiThreadReturnlist);

            for (FlightProcessingLineItem lineItem : multiThreadReturnlist)
            {

                // Write data to file
                bw.write(// Position Vars: Lat, Long, Alt, Heading, Speed
                        String.format(Locale.US, "%10.15f", lineItem.latitude) + "," +
                        String.format(Locale.US, "%10.15f", lineItem.longitude) + "," +
                        String.format(Locale.US, "%10.6f", lineItem.altAtWpt) + "," +
                        String.format(Locale.US, "%10.6f", lineItem.headingAtWpt) + "," +
                        String.format(Locale.US, "%10.2f", lineItem.ktsAtWpt) + "," +
                        // Axis Vars: Pitch, Bank
                        String.format(Locale.US, "%10.6f", lineItem.pitchAtWpt) + "," +
                        String.format(Locale.US, "%10.6f", lineItem.bankAtWpt) + "," +
                        // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                        String.format(Locale.US, "%10.2f", lineItem.aileronAtWpt) + "," +
                        lineItem.flapsAtWpt + "," +
                        lineItem.spoilersAtWpt + "," +
                        lineItem.gearAtWpt + "," +
                        String.format(Locale.US, "%10.2f", lineItem.throttleAtWpt) + "," +
                        String.format(Locale.US, "%10.2f", lineItem.noseWheelAtWpt) + "," +
                        // System Vars: Lights, Cabin
                        lineItem.lightsAtWpt[0] + "," +
                        lineItem.lightsAtWpt[1] + "," +
                        lineItem.lightsAtWpt[2] + "," +
                        lineItem.lightsAtWpt[3] + "," +
                        lineItem.lightsAtWpt[4] + "," +
                        lineItem.lightsAtWpt[5] + "," +
                        lineItem.lightsAtWpt[6] + "," +
                        lineItem.lightsAtWpt[7] + "," +
                        lineItem.signSmk + "," +
                        lineItem.signSeat + "," +
                        lineItem.signAtt + "," +
                        // Phase & Time Stamp
                        lineItem.fltPhase + "," +
                        String.format(Locale.US, "%10.2f", lineItem.timeAtWpt));
                    bw.newLine();

                    System.out.println("Writing to file... " + String.format(Locale.US, "%10.2f", lineItem.timeAtWpt));
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


