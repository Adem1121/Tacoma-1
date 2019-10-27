package ftdis.fdpu;

import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static ftdis.fdpu.DOMUtil.createChildElement;
import static ftdis.fdpu.DOMUtil.setElementValue;
import static java.lang.Math.*;
import static org.junit.Assert.*;

/**
 * Unit test VelocityTrack methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocityTrackTest {
    private String ioDir, planFileName, eventCollectionFileName;
    private int planId;
    private Path localDir = Paths.get("").toAbsolutePath();


    private LateralPlan latPlan = new LateralPlan();
    private LateralTrack latTrack = new LateralTrack();

    private VelocityPlan velPlan = new VelocityPlan();
    private VelocityTrack velTrack = new VelocityTrack();

    @Before
    public void setUp() throws Exception {

        // Define paths and file name
        ioDir = localDir + "\\IO\\";
        planFileName = "KSEA KSEA FlightPlan.xml";
        eventCollectionFileName = "KSEA KSEA EventCollection.xml";
        planId = 4;

        // 01 Load and transform lateral plan
        latPlan.load(ioDir + planFileName,planId);
        latPlan.transform();
        latPlan.validate();
        System.out.println("Lateral Plan data: " + latPlan.dataValid);

        // 02 Load and transform velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load(ioDir + eventCollectionFileName,planId);
        velPlan.transform();
        velPlan.validate();
        System.out.println("Velocity Plan data: " + velPlan.dataValid);

        // 03 Transform lateral plan to lateral track and validate
        latTrack.assignVel(velPlan);
        latTrack.transform(latPlan);
        latTrack.validate();
        System.out.println("Lateral Track data: " + latTrack.dataValid);

    }

    @Test
    public void testTransformVelocityTrack() throws Exception{
        // Transform velocity plan to velocity track and validate
        velTrack.assignLat(latTrack);
        velTrack.transform(velPlan);
        velTrack.validate();
        System.out.println("Velocity Track data: " + velTrack.dataValid);

        // Loop through lateral track
        Waypoint pos, prevPos = null;
        VelocitySegment velSgmt;
        double trackDist = 0, deltaDist = 0, trackLn, cycleLn = 0.02, velocity, acc, accEq, latDist;
        int velSgmtPos;

        trackLn = latTrack.getLength();

        for(double timeStmp = 170; trackDist < trackLn; timeStmp += cycleLn){

            if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
            else
                trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

            if ((trackLn - trackDist) <= 1.0E-4)
                trackDist = trackLn;

            pos = latTrack.getItmWpt(latTrack.getStartWpt(),trackDist);
            prevPos = pos;

            velSgmt = velTrack.getWptSgmt(pos);

            velocity = velTrack.getVasAtWpt(pos);
            accEq = sqrt(pow(velSgmt.getVasi(), 2) + (2 * velSgmt.getAcc() * latTrack.getDist(velSgmt.getStartPt(),pos)));


            acc = velSgmt.getAcc();
            latDist = trackLn - trackDist;
            //deltaDist = latTrack.getDist(pos, velSgmt.getEndPt());
            deltaDist = trackDist - deltaDist;



            velSgmtPos = velTrack.getSgmtPos(velSgmt);

            System.out.println("Time Stamp: " + timeStmp + "  Segment: " + velSgmtPos + " Velocity: " + velocity + " Delta Dist: " + deltaDist + " Remain Dist: " + latDist);

            deltaDist = trackDist;
        }

    }

    @Test
    public void exportEventKML() throws Exception {

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 - Prepare and Validate Data Sets
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        velTrack.assignLat(latTrack);
        velTrack.transform(velPlan);
        velTrack.validate();
        System.out.println("Velocity Track data: " + velTrack.dataValid);


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 Configure and prepare kml file and document
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = eventCollectionFileName + " Velocity Track " + dateFormat.format(date) + ".kml";
        File fout = new File(ioDir + fileName);
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
        //Element mainFolder = createChildElement(doc, "Folder");
        //Element folderName = createChildElement(mainFolder,"name");
        //setElementValue(folderName, "Flight Track");



        /////////////////////////////////////////////////////////////////////////////
        // 07 Add velocity track to document
        ////////////////////////////////////////////////////////////////////////////

        VelocitySegment velEvent;

        Element velEventsFolder = createChildElement(doc, "Folder");
        Element folderName = createChildElement(velEventsFolder,"name");
        setElementValue(folderName, "Velocity Track");


        for(int i = 0; i < velTrack.getSgmtCount(); i++){
            velEvent = velTrack.getSgmt(i);
            addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.2f", velEvent.getVasi()) + " ms", velEvent.getStartPt().getLat(),velEvent.getStartPt().getLon(), 0);
            addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.2f", velEvent.getVasf()) + " ms", velEvent.getEndPt().getLat(),velEvent.getEndPt().getLon(), 0);
        }



        /////////////////////////////////////////////////////////////////////////////
        // 07 Add waypoints of lateral track to document
        ////////////////////////////////////////////////////////////////////////////
        Waypoint wpt;

        Element wptsLatPlanFolder = createChildElement(doc, "Folder");
        Element wptsLatPlanFolderName = createChildElement(wptsLatPlanFolder,"name");
        setElementValue(wptsLatPlanFolderName, "Waypoints Lateral Plan");

        for(int i = 0; i < latPlan.getWptSize(); i++){
            wpt = latPlan.getWpt(i);
            addPlaceMark(wptsLatPlanFolder, i + "W ", wpt.getLat(),wpt.getLon(), 0);

        }



        /////////////////////////////////////////////////////////////////////////////
        // 07 Add waypoints of lateral track to document
        ////////////////////////////////////////////////////////////////////////////
        LateralSegment latSegment;
        //Waypoint wpt;

        Element wptsFolder = createChildElement(doc, "Folder");
        Element wptsFolderName = createChildElement(wptsFolder,"name");
        setElementValue(wptsFolderName, "Waypoints Lateral Track");

        for(int i = 0; i < latTrack.getSgmtCount(); i++){
            latSegment = latTrack.getSgmt(i);
            addPlaceMark(wptsFolder, i + "S ", latSegment.getStartPt().getLat(),latSegment.getStartPt().getLon(), 0);

            if(latSegment instanceof TurnSegment)
                addPlaceMark(wptsFolder, i + "T ", ((TurnSegment) latSegment).getTurnPt().getLat(),((TurnSegment) latSegment).getTurnPt().getLon(), 0);

            addPlaceMark(wptsFolder, i + "E ", latSegment.getEndPt().getLat(),latSegment.getEndPt().getLon(), 0);
        }


        /////////////////////////////////////////////////////////////////////////////
        // 07 Add lateral track to document
        ////////////////////////////////////////////////////////////////////////////

        Waypoint pos;
        kmlWaypoint waypoint;
        List<kmlWaypoint> waypoints = new ArrayList<kmlWaypoint>();
        double trackLn, timeStmp,cycleLn;
        String flightTrack = "", latitude, longitude, altitude;

        trackLn =  latTrack.getLength();
        timeStmp = 0;
        cycleLn = 2;

        Element latTrackFolder = createChildElement(doc,"Folder");
        Element trackFolderName = createChildElement(latTrackFolder,"name");
        setElementValue(trackFolderName, "Lateral Track");

        Element trackMark = createChildElement(latTrackFolder,"Placemark");

        Element trackName = createChildElement(trackMark, "name");
        setElementValue(trackName,"Lateral Track");

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


        for(double trackDist = 0; trackDist < trackLn; trackDist += cycleLn){

            pos = latTrack.getItmWpt(latTrack.getStartWpt(),trackDist);

            waypoint = new kmlWaypoint();
            waypoint.setLat(pos.getLat());
            waypoint.setLon(pos.getLon());
            waypoint.setAlt(0);
            waypoint.setTime(trackDist);
            waypoints.add(waypoint);

        }

        //Build lateral track
        StringBuilder sb = new StringBuilder();

        for(kmlWaypoint thisWaypoint : waypoints){

            latitude = String.format(Locale.US, "%.6f", thisWaypoint.getLat());
            longitude = String.format(Locale.US, "%.6f",thisWaypoint.getLon());
            altitude = String.format(Locale.US, "%.2f", thisWaypoint.getAlt());

            flightTrack  = sb.append(longitude).append(",").append(latitude).append(",").append(altitude).append(" ").toString();
        }

        Element trackCoordinates = createChildElement(trackLineString,"coordinates");
        setElementValue(trackCoordinates,flightTrack);




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