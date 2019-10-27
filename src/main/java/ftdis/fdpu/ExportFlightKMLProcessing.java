package ftdis.fdpu;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.Config.*;

/**
 * Process end to end flight data to .kml file for visualization in Google Earth.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */

/**
 * Callable classes to enable parallel flight plan processing
 */
class KMLFlapsStdCallable implements Callable<Integer> {

    private AircraftControl arcrftCtrl;
    private Waypoint pos;
    private int planId;

    public KMLFlapsStdCallable(AircraftControl arcrftCtrl, Waypoint pos, int planId){
        this.arcrftCtrl = arcrftCtrl;
        this.pos = pos;
        this.planId = planId;
    }

    @Override
    public Integer call() throws Exception {
        return this.arcrftCtrl.getFlapsAtWpt(pos,planId);
    }
}


class KMLSpoilersStdCallable implements Callable<Integer> {

    private AircraftControl arcrftCtrl;
    private Waypoint pos;
    private int phase;

    public KMLSpoilersStdCallable(AircraftControl arcrftCtrl, Waypoint pos, int phase){
        this.arcrftCtrl = arcrftCtrl;
        this.pos = pos;
        this.phase = phase;
    }

    @Override
    public Integer call() throws Exception {
        return arcrftCtrl.getSpoilersAtWpt(pos, phase);
    }
}


class KMLAltAtWptCallable implements Callable<Double> {

    private AircraftAxis arcrftAxis;
    private Waypoint pos;

    public KMLAltAtWptCallable(AircraftAxis arcrftAxis, Waypoint pos){
        this.arcrftAxis = arcrftAxis;
        this.pos = pos;
    }

    @Override
    public Double call() throws Exception {
        return arcrftAxis.getAltAtWpt(pos);
    }
}


class KMLPitchAtWptCallable implements Callable<Double> {

    private AircraftAxis arcrftAxis;
    private Waypoint pos;

    public KMLPitchAtWptCallable(AircraftAxis arcrftAxis, Waypoint pos){
        this.arcrftAxis = arcrftAxis;
        this.pos = pos;
    }

    @Override
    public Double call() throws Exception {
        return arcrftAxis.getPitchAngleAtWpt(pos);
    }
}


class KMLBankAtWptCallable implements Callable<Double> {

    private AircraftAxis arcrftAxis;
    private Waypoint pos;

    public KMLBankAtWptCallable(AircraftAxis arcrftAxis, Waypoint pos){
        this.arcrftAxis = arcrftAxis;
        this.pos = pos;
    }

    @Override
    public Double call() throws Exception {
        return arcrftAxis.getBankAngleAtWpt(pos);
    }
}


class KMLHeadingAtWptCallable implements Callable<Double> {

    private AircraftAxis arcrftAxis;
    private Waypoint pos;

    public KMLHeadingAtWptCallable(AircraftAxis arcrftAxis, Waypoint pos){
        this.arcrftAxis = arcrftAxis;
        this.pos = pos;
    }

    @Override
    public Double call() throws Exception {
        return arcrftAxis.getHeadingAtWpt(pos);
    }
}


 
public class ExportFlightKMLProcessing {


    /**
     * End to end flight data processing and export to .kml file
     */
    public static void main(String[] args) {
        try{
            Waypoint pos, prevPos;
            kmlWaypoint waypoint;
            double ailerons, flaps, spoilers;
            double altAtWpt, pitchAtWpt, bankAtWpt, headingAtWpt;
            double cycleLn = 2.0, timeStmp = 0, timeMrk = 0, timeFile = 0, trackLn, timeStart = 0, timeEnd = 99999, timeOffset = 0, trackT;
            String ioDir, inputFileName = "", flightPlanFile, eventCollectionFile, flightTrack = "", latitude, longitude, altitude;
            boolean inProcess;
            List<kmlWaypoint> waypoints = new ArrayList<kmlWaypoint>();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Set processing parameters
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath().getParent().getParent();
            if (os.contains("Windows"))
            {
                ioDir = "\\IO\\";
            }
            else
            {
                ioDir = "/IO/";
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

            // Set offset time
            if(args.length == 8) {
                if (!Double.isNaN(Double.parseDouble(args[7])));
                    timeOffset = Double.parseDouble(args[7]);
            }


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 00 Configure and prepare kml file and document
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Prep date format
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

            // Configure filename and file output stream
            String fileName = inputFileName + " Flight Track " + dateFormat.format(date) + ".kml";
            File fout = new File(localDir + ioDir + fileName);
            FileOutputStream fos = new FileOutputStream(fout);

            // Initialize document builder factory and create new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document kmlDoc = docBuilder.newDocument();

            // Create KML document root element
            Element kml = createChildElement(kmlDoc, "kml");
            kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");
            kml.setAttribute("xmlns:gx", "http://www.google.com/kml/ext/2.2");

            Element doc = createChildElement(kml,"Document");
            Element docName = createChildElement(doc,"name");
            setElementValue(docName, fileName);

            // Set Flight Track style
            Element trackStyle = createChildElement(doc,"Style");
            trackStyle.setAttribute("id","linePoly");

            Element trackLineStyle = createChildElement(trackStyle,"LineStyle");

            Element trackLineColor = createChildElement(trackLineStyle,"color");
            setElementValue(trackLineColor,"7f00ffff");

            Element trackLineWidth = createChildElement(trackLineStyle,"width");
            setElementValue(trackLineWidth, "4");

            Element trackPolyStyle = createChildElement(trackLineStyle, "PolyStyle");

            Element trackPolyColor = createChildElement(trackPolyStyle,"color");
            setElementValue(trackPolyColor, "7f00ff00");


            // Set Timed Track style
            Element timedTrackStyle = createChildElement(doc,"Style");
            timedTrackStyle.setAttribute("id","timedLinePoly");

            Element timedTrackLineStyle = createChildElement(timedTrackStyle,"LineStyle");

            Element timedTrackLineColor = createChildElement(timedTrackLineStyle,"color");
            setElementValue(timedTrackLineColor,"00c80000");

            Element timedTrackLineWidth = createChildElement(timedTrackLineStyle,"width");
            setElementValue(timedTrackLineWidth, "8");

            Element timedTrackPolyStyle = createChildElement(timedTrackLineStyle, "PolyStyle");

            Element timedTrackPolyColor = createChildElement(timedTrackPolyStyle,"color");
            setElementValue(timedTrackPolyColor, "#c80000");

             // Timed Track Icon
            Element timedTrackIconStyle = createChildElement(doc,"StyleMap");
            timedTrackIconStyle.setAttribute("id","msn_airports");

            Element timedTrackIconPairNorm = createChildElement(timedTrackIconStyle,"Pair");

            Element timedTrackIconPairNormKey = createChildElement(timedTrackIconPairNorm,"key");
            setElementValue(timedTrackIconPairNormKey,"normal");

            Element timedTrackIconPairNormUrl = createChildElement(timedTrackIconPairNorm,"styleUrl");
            setElementValue(timedTrackIconPairNormUrl,"#sn_airports");

            Element timedTrackIconPairHigh = createChildElement(timedTrackIconStyle,"Pair");

            Element timedTrackIconPairHighKey = createChildElement(timedTrackIconPairHigh,"key");
            setElementValue(timedTrackIconPairHighKey,"highlight");

            Element timedTrackIconPairHighUrl = createChildElement(timedTrackIconPairHigh,"styleUrl");
            setElementValue(timedTrackIconPairHighUrl,"#sh_airports");


            // Timed Track Icon Style: Norm Icon
            Element timedTrackIconStyleNorm = createChildElement(doc,"Style");
            timedTrackIconStyleNorm.setAttribute("id","sn_airports");

            Element timedTrackIconStyleNormIcon = createChildElement(timedTrackIconStyleNorm,"IconStyle");

            Element timedTrackIconStyleNormIconScale = createChildElement(timedTrackIconStyleNormIcon,"scale");
            setElementValue(timedTrackIconStyleNormIconScale,"1.4");

            Element timedTrackIconStyleNormIconFolder = createChildElement(timedTrackIconStyleNormIcon,"Icon");

            Element timedTrackIconStyleNormIconHref = createChildElement(timedTrackIconStyleNormIconFolder,"href");
            setElementValue(timedTrackIconStyleNormIconHref,"http://maps.google.com/mapfiles/kml/shapes/airports.png");

            Element timedTrackIconStyleNormIconHotSpot = createChildElement(timedTrackIconStyleNormIcon,"hotspot");
            timedTrackIconStyleNormIconHotSpot.setAttribute("x","0.5");
            timedTrackIconStyleNormIconHotSpot.setAttribute("y","0");
            timedTrackIconStyleNormIconHotSpot.setAttribute("xunits","fraction");
            timedTrackIconStyleNormIconHotSpot.setAttribute("yunits","fraction");

            Element timedTrackIconStyleNormBalloon = createChildElement(timedTrackIconStyleNorm,"BalloonStyle");
            Element timedTrackIconStyleNormList = createChildElement(timedTrackIconStyleNorm,"ListStyle");

            Element timedTrackIconStyleNormLineStyle = createChildElement(timedTrackIconStyleNorm,"LineStyle");

            Element timedTrackIconStyleNormLineColor = createChildElement(timedTrackIconStyleNormLineStyle,"color");
            setElementValue(timedTrackIconStyleNormLineColor,"00c80000");

            Element timedTrackIconStyleNormLineWidth = createChildElement(timedTrackIconStyleNormLineStyle,"width");
            setElementValue(timedTrackIconStyleNormLineWidth, "4");

            Element timedTrackIconStyleNormPolyStyle = createChildElement(timedTrackIconStyleNormLineStyle, "PolyStyle");

            Element timedTrackIconStyleNormPolyColor = createChildElement(timedTrackIconStyleNormPolyStyle,"color");
            setElementValue(timedTrackIconStyleNormPolyColor, "#c80000");


            // Time Track Icon Style: Highlight Icon
            Element timedTrackIconStyleHigh = createChildElement(doc,"Style");
            timedTrackIconStyleHigh.setAttribute("id","sh_airports");

            Element timedTrackIconStyleHighIcon = createChildElement(timedTrackIconStyleHigh,"IconStyle");

            Element timedTrackIconStyleHighIconScale = createChildElement(timedTrackIconStyleHighIcon,"scale");
            setElementValue(timedTrackIconStyleHighIconScale,"1.4");

            Element timedTrackIconStyleHighIconFolder = createChildElement(timedTrackIconStyleHighIcon,"Icon");

            Element timedTrackIconStyleHighIconHref = createChildElement(timedTrackIconStyleHighIconFolder,"href");
            setElementValue(timedTrackIconStyleHighIconHref,"http://maps.google.com/mapfiles/kml/shapes/airports.png");

            Element timedTrackIconStyleHighIconHotSpot = createChildElement(timedTrackIconStyleHighIcon,"hotspot");
            timedTrackIconStyleHighIconHotSpot.setAttribute("x","0.5");
            timedTrackIconStyleHighIconHotSpot.setAttribute("y","0");
            timedTrackIconStyleHighIconHotSpot.setAttribute("xunits","fraction");
            timedTrackIconStyleHighIconHotSpot.setAttribute("yunits","fraction");

            Element timedTrackIconStyleHighBalloon = createChildElement(timedTrackIconStyleHigh,"BalloonStyle");
            Element timedTrackIconStyleHighList = createChildElement(timedTrackIconStyleHigh,"ListStyle");

            Element timedTrackIconStyleHighLineStyle = createChildElement(timedTrackIconStyleHigh,"LineStyle");

            Element timedTrackIconStyleHighLineColor = createChildElement(timedTrackIconStyleHighLineStyle,"color");
            setElementValue(timedTrackIconStyleHighLineColor,"00c80000");

            Element timedTrackIconStyleHighLineWidth = createChildElement(timedTrackIconStyleHighLineStyle,"width");
            setElementValue(timedTrackIconStyleHighLineWidth, "4");

            Element timedTrackIconStyleHighPolyStyle = createChildElement(timedTrackIconStyleHighLineStyle, "PolyStyle");

            Element timedTrackIconStyleHighPolyColor = createChildElement(timedTrackIconStyleHighPolyStyle,"color");
            setElementValue(timedTrackIconStyleHighPolyColor, "#00c80000");



            // Set Events place mark style
            Element wptStyle = createChildElement(doc,"Style");
            wptStyle.setAttribute("id","msn_red-diamond");

            Element iconStyle = createChildElement(wptStyle,"IconStyle");

            Element scale = createChildElement(iconStyle,"scale");
            setElementValue(scale,"0.8");

            Element icon = createChildElement(iconStyle,"Icon");
            Element href = createChildElement(icon,"href");
            setElementValue(href,"http://maps.google.com/mapfiles/kml/paddle/red-diamond.png");

            Element hotSpot = createChildElement(iconStyle,"hotSpot");
            hotSpot.setAttribute("x","32");
            hotSpot.setAttribute("y","1");
            hotSpot.setAttribute("xunits","pixels");
            hotSpot.setAttribute("yunits","pixels");

            Element labelStyle = createChildElement(wptStyle, "LabelStyle");
            scale = createChildElement(labelStyle,"scale");
            setElementValue(scale,"0.7");

            Element listStyle = createChildElement(wptStyle,"ListStyle");
            icon = createChildElement(listStyle,"ItemIcon");
            href = createChildElement(icon,"href");
            setElementValue(href,"http://maps.google.com/mapfiles/kml/paddle/red-diamond-lv.png");




            // Add folders to document
            Element mainFolder = createChildElement(doc, "Folder");
            Element folderName = createChildElement(mainFolder,"name");
            setElementValue(folderName, "Flight Track");

            Element velEventsFolder = createChildElement(mainFolder, "Folder");
            folderName = createChildElement(velEventsFolder,"name");
            setElementValue(folderName, "Velocity Track");

            Element vertEventsFolder = createChildElement(mainFolder, "Folder");
            folderName = createChildElement(vertEventsFolder,"name");
            setElementValue(folderName, "Altitude Track");



            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 Process Pushback
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

            // 01 Load pushback plan and transform to lateral plan
            latPlan.load(flightPlanFile, 1);
            latPlan.transform();
            latPlan.validate();
            System.out.println("Pushback Plan data: " + latPlan.dataValid);

            // 02 Load change velocity events and transform to velocity plan
            velPlan.assignLat(latPlan);
            velPlan.load(eventCollectionFile, 1);
            velPlan.transform();
            velPlan.validate();
            System.out.println("Velocity Plan data: " + velPlan.dataValid);

            // 03 Load change altitude events and transform to vertical plan
            vertPlan.assignLat(latPlan);
            vertPlan.assignVel(velPlan);
            vertPlan.load(eventCollectionFile, 2);
            vertPlan.transform();
            vertPlan.validate();
            System.out.println("Vertical Plan data: " + vertPlan.dataValid);

            // 04 Transform lateral plan to lateral track and validate
            latTrack.assignVel(velPlan);
            latTrack.transform(latPlan);
            latTrack.validate();
            System.out.println("Ground Track data: " + latTrack.dataValid);

            // 05 Transform lateral ground track to velocity track and validate
            velTrack.assignLat(latTrack);
            velTrack.transform(velPlan);
            //velTrack.adjustToGroundTrack();
            velTrack.validate();
            System.out.println("Velocity Track data: " + velTrack.dataValid);

            // 06 Transform vertical plan to vertical track and validate
            vertTrack.assignLat(latTrack);
            vertTrack.assignVel(velTrack);
            vertTrack.transform(vertPlan);
            vertTrack.validate();
            System.out.println("Vertical Track data: " + velTrack.dataValid);

            // 07 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);

            // 08 Prepare Aircraft Systems
            arcrftSyst.reset();

            // Process and Write Data
            prevPos = latTrack.getStartWpt();
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if((trackLn - trackDist) < 1.0) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos,latTrack.getEndWpt()) / velTrack.getVasu(prevPos,latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    waypoint = new kmlWaypoint();
                    waypoint.setLat(pos.getLat());
                    waypoint.setLon(pos.getLon());
                    waypoint.setAlt(vertTrack.getAltAtWpt(pos));
                    waypoint.setTime(timeFile);
                    waypoints.add(waypoint);

                    System.out.println("Pushback - Processing cycle " + String.format(Locale.US, "%10.2f", timeFile) + " Progress: " + String.format(Locale.US, "%10.4f", (trackDist / trackLn) * 100) + " %");

                } else{
                    inProcess = false;
                    System.out.println("Pushback - Skipping cycle " + String.format(Locale.US, "%10.2f", timeFile));
                    timeFile += cycleLn;
                }
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 02 Process Taxi to Runway
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // 00 Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            arcrftAxis = new AircraftAxis();
            arcrftCtrl = new AircraftControl();

            // 01 Load taxi plan and transform to lateral plan
            latPlan.load(flightPlanFile, 2);
            latPlan.transform();
            latPlan.validate();
            System.out.println("Taxi to Runway Plan data: " + latPlan.dataValid);

            // 02 Load change velocity events and transform to velocity plan
            velPlan.assignLat(latPlan);
            velPlan.load(eventCollectionFile, 2);
            velPlan.transform();
            velPlan.validate();
            System.out.println("Velocity Plan data: " + velPlan.dataValid);

            // 03 Load change altitude events and transform to vertical plan
            vertPlan.assignLat(latPlan);
            vertPlan.assignVel(velPlan);
            vertPlan.load(eventCollectionFile, 2);
            vertPlan.transform();
            vertPlan.validate();
            System.out.println("Vertical Plan data: " + vertPlan.dataValid);

            // 04 Transform lateral plan to lateral track and validate
            latTrack.assignVel(velPlan);
            latTrack.transform(latPlan);
            latTrack.validate();
            System.out.println("Ground Track data: " + latTrack.dataValid);

            // 05 Transform lateral ground track to velocity track and validate
            velTrack.assignLat(latTrack);
            velTrack.transform(velPlan);
            //velTrack.adjustToGroundTrack();
            velTrack.validate();
            System.out.println("Velocity Track data: " + velTrack.dataValid);

            // 06 Transform vertical plan to vertical track and validate
            vertTrack.assignLat(latTrack);
            vertTrack.assignVel(velTrack);
            vertTrack.transform(vertPlan);
            vertTrack.validate();
            System.out.println("Vertical Track data: " + velTrack.dataValid);

            // 07 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);

            // 08 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);

            // 09 Prepare Aircraft Systems
            arcrftSyst.reset();

            // Process and Write Data
            prevPos = latTrack.getStartWpt();
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if((trackLn - trackDist) < 1.0) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos,latTrack.getEndWpt()) / velTrack.getVasu(prevPos,latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    waypoint = new kmlWaypoint();
                    waypoint.setLat(pos.getLat());
                    waypoint.setLon(pos.getLon());
                    waypoint.setAlt(vertTrack.getAltAtWpt(pos));
                    waypoint.setTime(timeFile);
                    waypoints.add(waypoint);

                    System.out.println("Taxi to Runway - Processing cycle " + String.format(Locale.US, "%10.2f", timeFile) + " Progress: " + String.format(Locale.US, "%10.4f", (trackDist / trackLn) * 100) + " %");
                    //Runtime.getRuntime().exec(clsCmd);
                } else{
                    inProcess = false;
                    System.out.println("Taxi to Runway - Skipping cycle " + String.format(Locale.US, "%10.2f", timeFile));
                    timeFile += cycleLn;
                }
            }

            // Add change velocity event(s)
            VelocitySegment velSgmt;

            for(int i = 0; i < velTrack.getSgmtCount(); i++){
                velSgmt = velTrack.getSgmt(i);
                addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velSgmt.getVasi(),"ms")) + " kts", velSgmt.getStartPt().getLat(),velSgmt.getStartPt().getLon(),vertTrack.getAltAtWpt(velSgmt.getStartPt()));
                addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velSgmt.getVasf(),"ms")) + " kts", velSgmt.getEndPt().getLat(),velSgmt.getEndPt().getLon(),vertTrack.getAltAtWpt(velSgmt.getEndPt()));
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 Process Flight Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // 00 Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            WeatherPlan wxPlan = new WeatherPlan();
            WeatherTrack wxTrack = new WeatherTrack();

            arcrftAxis = new AircraftAxis();
            arcrftCtrl = new AircraftControl();

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

            // Process and Write Data
            prevPos = latTrack.getStartWpt();
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if((trackLn - trackDist) < 1.0) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos,latTrack.getEndWpt()) / velTrack.getVasu(prevPos,latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    // Compute airframe parameters (multi threading)
                    ExecutorService es = Executors.newFixedThreadPool(5);

                    //flaps = es.submit(new KMLFlapsStdCallable(arcrftCtrl,pos,3)).get();
                    //ailerons = arcrftCtrl.getAileron();
                    //spoilers = es.submit(new KMLSpoilersStdCallable(arcrftCtrl,pos)).get();
                    altAtWpt = es.submit(new KMLAltAtWptCallable(arcrftAxis,pos)).get();
                    //pitchAtWpt = es.submit(new KMLPitchAtWptCallable(arcrftAxis,pos)).get();
                    //bankAtWpt = es.submit(new KMLBankAtWptCallable(arcrftAxis,pos)).get();
                    //headingAtWpt = es.submit(new KMLHeadingAtWptCallable(arcrftAxis,pos)).get();

                    es.shutdown();


                    waypoint = new kmlWaypoint();
                    waypoint.setLat(pos.getLat());
                    waypoint.setLon(pos.getLon());
                    waypoint.setAlt(altAtWpt);

                    /*
                    waypoint.setLat(pos.getLat());
                    waypoint.setLon(pos.getLon());
                    waypoint.setAlt(vertTrack.getAltAtWpt(pos));*/
                    waypoint.setTime(timeFile);
                    waypoints.add(waypoint);

                    System.out.println("Flight - Processing cycle " + String.format(Locale.US, "%10.2f", timeFile) + " Progress: " + String.format(Locale.US, "%10.4f", (trackDist / trackLn) * 100) + " %");
                    //Runtime.getRuntime().exec(clsCmd);
                } else{
                    inProcess = false;
                    System.out.println("Flight - Skipping cycle " + String.format(Locale.US, "%10.2f", timeFile));
                    timeFile += cycleLn;
                }
            }

            // Add change velocity event(s)
            //VelocitySegment velSgmt;
            
            for(int i = 0; i < velTrack.getSgmtCount(); i++){
                velSgmt = velTrack.getSgmt(i);
                addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velSgmt.getVasi(),"ms")) + " kts", velSgmt.getStartPt().getLat(),velSgmt.getStartPt().getLon(),vertTrack.getAltAtWpt(velSgmt.getStartPt()));
                addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velSgmt.getVasf(),"ms")) + " kts", velSgmt.getEndPt().getLat(),velSgmt.getEndPt().getLon(),vertTrack.getAltAtWpt(velSgmt.getEndPt()));
            }

            // Add change altitude event(s)
            VerticalSegment vertSgmt;

            for(int i = 0; i < vertTrack.getSgmtCount(); i++){
                vertSgmt = vertTrack.getSgmt(i);
                addPlaceMark(vertEventsFolder, i + "S: " + String.format(Locale.US, "%10.1f", PerfCalc.convertFt(vertSgmt.getAlti(),"m")) + " ft", vertSgmt.getStartPt().getLat(),vertSgmt.getStartPt().getLon(),vertSgmt.getAlti());
                addPlaceMark(vertEventsFolder, i + "E: " + String.format(Locale.US, "%10.1f", PerfCalc.convertFt(vertSgmt.getAltf(),"m")) + " ft", vertSgmt.getEndPt().getLat(),vertSgmt.getEndPt().getLon(),vertSgmt.getAltf());
            }
            
            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 Process Taxi to the Gate
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 00 Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            arcrftAxis = new AircraftAxis();

            // 01 Load taxi plan and transform to lateral plan
            latPlan.load(flightPlanFile, 4);
            latPlan.transform();
            latPlan.validate();
            System.out.println("Taxi to Gate Plan data: " + latPlan.dataValid);

            // 02 Load change velocity events and transform to velocity plan
            velPlan.assignLat(latPlan);
            velPlan.load(eventCollectionFile, 4);
            velPlan.transform();
            velPlan.validate();
            System.out.println("Velocity Plan data: " + velPlan.dataValid);

            // 03 Load change altitude events and transform to vertical plan
            vertPlan.assignLat(latPlan);
            vertPlan.assignVel(velPlan);
            vertPlan.load(eventCollectionFile, 4);
            vertPlan.transform();
            vertPlan.validate();
            System.out.println("Vertical Plan data: " + vertPlan.dataValid);

            // 04 Transform lateral plan to lateral track and validate
            latTrack.assignVel(velPlan);
            latTrack.transform(latPlan);
            latTrack.validate();
            System.out.println("Ground Track data: " + latTrack.dataValid);

            // 05 Transform lateral ground track to velocity track and validate
            velTrack.assignLat(latTrack);
            velTrack.transform(velPlan);
            //velTrack.adjustToGroundTrack();
            velTrack.validate();
            System.out.println("Velocity Track data: " + velTrack.dataValid);

            // 06 Transform vertical plan to vertical track and validate
            vertTrack.assignLat(latTrack);
            vertTrack.assignVel(velTrack);
            vertTrack.transform(vertPlan);
            vertTrack.validate();
            System.out.println("Vertical Track data: " + velTrack.dataValid);

            // 07 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);

            // 08 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);

            // 09 Prepare Aircraft Systems
            arcrftSyst.reset();

            // Process and Write Data
            prevPos = latTrack.getStartWpt();
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if((trackLn - trackDist) < 1.0) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos,latTrack.getEndWpt()) / velTrack.getVasu(prevPos,latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    waypoint = new kmlWaypoint();
                    waypoint.setLat(pos.getLat());
                    waypoint.setLon(pos.getLon());
                    waypoint.setAlt(vertTrack.getAltAtWpt(pos));
                    waypoint.setTime(timeFile);
                    waypoints.add(waypoint);

                    System.out.println("Taxi to Gate - Processing cycle " + String.format(Locale.US, "%10.2f", timeFile) + " Progress: " + String.format(Locale.US, "%10.4f", (trackDist / trackLn) * 100) + " %");
                    //Runtime.getRuntime().exec(clsCmd);
                } else{
                    inProcess = false;
                    System.out.println("Taxi to Gate - Skipping cycle " + String.format(Locale.US, "%10.2f", timeFile));
                    timeFile += cycleLn;
                }
            }

            /////////////////////////////////////////////////////////////////////////////
            // 05 Add flight track to document
            ////////////////////////////////////////////////////////////////////////////

            //Build flight track
            StringBuilder sb = new StringBuilder();

            for(kmlWaypoint thisWaypoint : waypoints){

                latitude = String.format(Locale.US, "%.6f", thisWaypoint.getLat());
                longitude = String.format(Locale.US, "%.6f",thisWaypoint.getLon());
                altitude = String.format(Locale.US, "%.2f", thisWaypoint.getAlt());

                flightTrack  = sb.append(longitude).append(",").append(latitude).append(",").append(altitude).append(" ").toString();
            }

            // Add elements to kml document
            Element trackMark = createChildElement(mainFolder,"Placemark");

            Element trackName = createChildElement(trackMark, "name");
            setElementValue(trackName,"Flight Track");

            Element trackStyleUrl = createChildElement(trackMark, "styleUrl");
            setElementValue(trackStyleUrl, "#linePoly");

            // Set linestring configuration elements
            Element trackLineString = createChildElement(trackMark,"LineString");

            Element trackExtrude = createChildElement(trackLineString,"extrude");
            setElementValue(trackExtrude, "1");

            Element trackTessellate = createChildElement(trackLineString,"tessellate");
            setElementValue(trackTessellate,"1");

            Element trackAltMode = createChildElement(trackLineString,"altitudeMode");
            setElementValue(trackAltMode,"absolute");

            Element trackCoordinates = createChildElement(trackLineString,"coordinates");
            setElementValue(trackCoordinates,flightTrack);


            /////////////////////////////////////////////////////////////////////////////
            // 06 Add timed track to document
            ////////////////////////////////////////////////////////////////////////////

            // Add elements to kml document
            Element timedTrackMark = createChildElement(mainFolder,"Placemark");

            Element timedTrackName = createChildElement(timedTrackMark, "name");
            setElementValue(timedTrackName,"Timed Track");

            Element timedTrackDesc = createChildElement(timedTrackMark, "description");
            setElementValue(timedTrackDesc,"Time Offset: " + timeOffset);


            Element timedTrackStyleUrl = createChildElement(timedTrackMark, "styleUrl");
            setElementValue(timedTrackStyleUrl, "#msn_airports");

            Element gxTrack = createChildElement(timedTrackMark,"gx:Track");

            Element gxTrackAltMode = createChildElement(gxTrack,"altitudeMode");
            setElementValue(gxTrackAltMode,"absolute");

            // Define date time format YYYY-MM-DDTHH:MM:SSZ, e.g. 2007-01-14T21:06:04Z
            SimpleDateFormat kmlDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat kmlTimeFormat = new SimpleDateFormat("HH:mm:ss");
            Calendar kmlCalendar = new GregorianCalendar(2018,1,1,0,0,0);
            double prevTime = 0;

            //Date kmlDate = new Date();

            // Add time points
            for(kmlWaypoint thisWaypoint : waypoints){
                kmlCalendar.add(Calendar.SECOND, (int) (thisWaypoint.getTime() + timeOffset - prevTime));

                Element timePoint = createChildElement(gxTrack,"when");
                setElementValue(timePoint, kmlDateFormat.format(kmlCalendar.getTime()) + "T" + kmlTimeFormat.format(kmlCalendar.getTime()) + "Z");

                prevTime = thisWaypoint.getTime() + timeOffset;
            }

            // Add corresponding track points
            for(kmlWaypoint thisWaypoint : waypoints){
                Element trackPoint = createChildElement(gxTrack,"gx:coord");

                setElementValue(trackPoint,String.format(Locale.US, "%.6f", thisWaypoint.getLon()) + " " + String.format(Locale.US, "%.6f", thisWaypoint.getLat()) + " " +  String.format(Locale.US, "%.6f", thisWaypoint.getAlt()));
            }


            /////////////////////////////////////////////////////////////////////////////
            // 07 Write document to kml file
            ////////////////////////////////////////////////////////////////////////////

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(kml);
            StreamResult result = new StreamResult(fout);

            transformer.transform(source, result);

            System.out.println("File saved!");

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method adds a place mark as per Google Earth .kml schema specifications.
     *
     * @param node      Root node element
     * @param wptName   Name of waypoint
     * @param wptLat    Latitude coordinate of waypoint
     * @param wptLon    Longitude coordinate of waypoint
     */
    public static void addPlaceMark(Element node, String wptName, double wptLat, double wptLon, double altitude){
        try{
            Element placeMark = createChildElement(node,"Placemark");

            Element name = createChildElement(placeMark,"name");
            setElementValue(name, wptName);

            Element style = createChildElement(placeMark,"styleUrl");
            setElementValue(style, "#msn_red-diamond");

            Element point = createChildElement(placeMark,"Point");

            Element trackAltMode = createChildElement(point,"altitudeMode");
            setElementValue(trackAltMode,"clampToGround");

            Element drawOrder = createChildElement(point,"gx:drawOrder");
            setElementValue(drawOrder,"1");

            Element coordinates = createChildElement(point, "coordinates");
            setElementValue(coordinates,wptLon + "," + wptLat + "," + altitude);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
