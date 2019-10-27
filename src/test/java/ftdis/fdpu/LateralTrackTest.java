package ftdis.fdpu;

import org.junit.*;
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
import java.util.concurrent.ExecutionException;

import static ftdis.fdpu.DOMUtil.createChildElement;
import static ftdis.fdpu.DOMUtil.setElementValue;
import static java.lang.Math.*;
import static org.junit.Assert.*;

/**
 * Unit test LateralTrack methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class LateralTrackTest {

    private String ioDir, planFileName, eventCollectionFileName;
    private int planId;
    private Path localDir = Paths.get("").toAbsolutePath();

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

        // Define paths and file name
        final String os = System.getProperty("os.name");

        Path localDir;

        if (os.contains("Windows")) {
            ioDir = "\\IO\\";
            localDir = Paths.get("").toAbsolutePath();//.getParent().getParent();
        } else {
            ioDir = "/IO/";
            localDir = Paths.get("").toAbsolutePath();
        }

        ioDir = localDir + ioDir;
        planFileName = "KSEA KSEA FlightPlan.xml";
        eventCollectionFileName = "KSEA KSEA EventCollection.xml";
        planId = 2;

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

        // 03 Load and transform vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load(ioDir + eventCollectionFileName,planId);
        vertPlan.transform();
        vertPlan.validate();
        System.out.println("Vertical Plan data: " + vertPlan.dataValid);

        // 04 Load change weather events and transform to weather plan
        wxPlan.assignLat(latPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load(ioDir + eventCollectionFileName,planId);
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
    }

    @Test
    public void testGetSegment() throws Exception {
        LateralSegment sgmt = latTrack.getSgmt(2);
        assertEquals(2, latTrack.getSgmtPos(sgmt),0);
    }

    @Test
    public void testGetDirSegmentSize() throws Exception {
        assertEquals(15, latTrack.getSgmtCount(),0);
    }

    @Test
    public void testGetLength() throws Exception {
        assertEquals(59403.15, latTrack.getLength(),0.01);
    }

    @Test
    public void testGetCourseAtWpt() throws Exception {
        double fraction = 1.0;
        Waypoint testWpt = new Waypoint();
        testWpt = latTrack.getItmWpt(latTrack.getStartWpt(),latTrack.getLength() * fraction);
        System.out.println(latTrack.getCourseAtWpt(testWpt));

        /**
        // 01
        testWpt.setLat(47.452398);
        testWpt.setLon(-122.307838);
        lateralPlan.getSgmt(0).alignToPlan(testWpt);
        assertEquals(0.28, lateralPlan.getSgmt(0).getCourseAtWpt(testWpt), 0.01);

        //02
        testWpt.setLat(47.478125);
        testWpt.setLon(-122.419640);
        lateralPlan.getSgmt(4).alignToPlan(testWpt);
        assertEquals(179.46, lateralPlan.getSgmt(4).getCourseAtWpt(testWpt), 0.01);

        //03
        testWpt.setLat(47.382577);
        testWpt.setLon(-122.311195);
        lateralPlan.getSgmt(5).alignToPlan(testWpt);
        assertEquals(74.81, lateralPlan.getSgmt(5).getCourseAtWpt(testWpt), 0.01);
         */
    }


    @Test
    public void testUTurn() throws ExecutionException{
        Double startDist, startDeg, endDist, endDeg, a, c;
        Waypoint pos = new Waypoint();

        TurnSegment turnSegment = new TurnSegment();
        turnSegment.setStartPt(47.461701,-122.300597);
        turnSegment.setTurnPt(47.4618055999528,-122.3004119674782);
        turnSegment.setEndPt( 47.461919,-122.300596);
        turnSegment.setCourseStart(98.15212394998461);
        turnSegment.setCourseEnd(265.94076757041637);

        //Waypoint testWpt =  turnSegment.getItmWpt(turnSegment.getEndPt(),-1);
        //turnSegment.getCourseAtWpt(testWpt);


        pos.setLat(47.461919);
        pos.setLon(-122.300596);

        System.out.println(turnSegment.getDist(turnSegment.getStartPt(),pos));


        c = NavCalc.getDirectDist(turnSegment.getEndPt(), turnSegment.getStartPt());
        a = c / (2 * sin(toRadians(abs(turnSegment.getCourseChange()) / 2)));


        startDist = NavCalc.getDirectDist(turnSegment.getStartPt(),turnSegment.getTurnPt());
        endDist = NavCalc.getDirectDist(turnSegment.getEndPt(),turnSegment.getTurnPt());

        System.out.println(startDist + " " + endDist);
        //r = 12.189451798479322

        System.out.println("Course Change: " + turnSegment.getCourseChange());




        for(double dist = 0; dist < turnSegment.getDist(); dist += 1){
            pos = turnSegment.getItmWpt(turnSegment.getStartPt(),dist);

             /*
            // get distances
            startDist = NavCalc.getDirectDist(turnSegment.getStartPt(),turnSegment.getStartPt());
            endDist = NavCalc.getDirectDist(turnSegment.getEndPt(),turnSegment.getEndPt());

            // calc degs

            startDeg = acos((2 * pow(turnSegment.getRadius(),2) - pow(startDist,2))/(2 * pow(turnSegment.getRadius(),2)));

            endDeg = acos((2 * pow(turnSegment.getRadius(),2) - pow(endDist,2))/(2 * pow(turnSegment.getRadius(),2)));

            a = abs(toRadians(turnSegment.getCourseChange())) - startDeg - endDeg;

            a = a * turnSegment.getRadius();
            */


           System.out.println("Hdg at Wpt: " + turnSegment.getCourseAtWpt(pos) + " Dist: " + dist + " Remain Dist: " + turnSegment.getDist(pos,turnSegment.getEndPt()) + " Deg:");
        }


    }




    @Test
    public void testHeadingAtWpt() throws ExecutionException {

        // Loop through lateral track
        Waypoint pos, prevPos = null;
        LateralSegment latSgmt;

        double trackDist = 0, deltaDist = 0, trackLn, cycleLn = 0.2, velocity, hdg, latDist;
        int latSgmtPos;

        trackLn = latTrack.getLength();

        for(double timeStmp = 620; trackDist < trackLn; timeStmp += cycleLn){

            if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
            else
                trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

            pos = latTrack.getItmWpt(latTrack.getStartWpt(),trackDist);
            prevPos = pos;


            latDist = trackLn - trackDist;
            deltaDist = trackDist - deltaDist;

            latSgmt = latTrack.getWptSgmt(pos);
            latSgmtPos = latTrack.getSgmtPos(latSgmt);

            hdg = latTrack.getCourseAtWpt(pos);

            System.out.println("Time Stamp: " + timeStmp + "  Segment: " + latSgmtPos + " Heading: " + hdg + " Remain Dist: " + latDist);

            deltaDist = trackDist;
        }

    }


    @Test
    public void testGetItmWpt() throws Exception {
        Waypoint itmWpt, testWpt = new Waypoint();

        // 01
        testWpt.setLat(47.452398);
        testWpt.setLon(-122.307838);
        latTrack.alignWpt(testWpt);

        itmWpt = latTrack.getItmWpt(testWpt, 700);
        assertEquals(47.458693, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307844, itmWpt.getLon(), 0.0001);

        itmWpt = latTrack.getItmWpt(testWpt, -700);
        assertEquals(47.446102, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307935, itmWpt.getLon(), 0.0001);

        // 02
        testWpt.setLat(47.560571);
        testWpt.setLon(-122.357368);
        latTrack.alignWpt(testWpt);

        itmWpt = latTrack.getItmWpt(testWpt, 12000);
        assertEquals(47.473360, itmWpt.getLat(), 0.0001);
        assertEquals(-122.424212, itmWpt.getLon(), 0.0001);

        itmWpt = latTrack.getItmWpt(testWpt, -20000);
        assertEquals(47.447872, itmWpt.getLat(), 0.0001);
        assertEquals(-122.307922, itmWpt.getLon(), 0.0001);

        // 03
        testWpt.setLat(47.392149);
        testWpt.setLon(-122.311197);
        latTrack.alignWpt(testWpt);

        itmWpt = latTrack.getItmWpt(testWpt, 8000);
        assertEquals(47.451966, itmWpt.getLat(), 0.0001);
        assertEquals(-122.311097, itmWpt.getLon(), 0.0001);

        itmWpt = latTrack.getItmWpt(testWpt, -30000);
        assertEquals(47.555513, itmWpt.getLat(), 0.0001);
        assertEquals(-122.367841, itmWpt.getLon(), 0.0001);
    }

    @Test
    public void testGetWptError() throws Exception{
        Waypoint testWpt = new Waypoint();

        // 01
        testWpt.setLat(47.485759);
        testWpt.setLon(-122.307647);
        assertEquals(111.75, latTrack.getWptError(testWpt), 0.01);

        // 02
        testWpt.setLat(47.527899);
        testWpt.setLon(-122.424960);
        assertEquals(479.85, latTrack.getWptError(testWpt), 0.01);

        // 03
        testWpt.setLat(47.362018);
        testWpt.setLon(-122.422692);
        assertEquals(2408.29, latTrack.getWptError(testWpt), 0.01);
    }

    @Test
    public void testGetDist() throws Exception{
        double totalLatDist = 0, totalLatDistIndv = 0;
        int startS = 0, endS= 28;

        for(int s = startS; s <= endS; s++){
            totalLatDistIndv += latTrack.getDist(latTrack.getSgmt(s).getStartPt(),latTrack.getSgmt(s).getEndPt());
        }

        totalLatDist = latTrack.getDist(latTrack.getSgmt(startS).getStartPt(),latTrack.getSgmt(endS).getEndPt());

        System.out.println(" Lat Dist Indiv.:         " + String.format(Locale.US, "%10.4f",totalLatDistIndv)
                + " Lat Dist Total.:         " + String.format(Locale.US, "%10.4f",totalLatDist)
                + " Vel Dist Total:          " + String.format(Locale.US, "%10.4f",latTrack.getDist(velTrack.getSgmt(0).getStartPt(),velTrack.getSgmt(17).getEndPt()))
        );



        double totalVelDist = 0, totalVelDistIndv = 0;
        int startV = 0, endV = velTrack.getSgmtCount()-1;

        for(int s = startV; s <= endV; s++){
            totalVelDistIndv += latTrack.getDist(velTrack.getSgmt(s).getStartPt(),velTrack.getSgmt(s).getEndPt());

        }

        totalVelDist = latTrack.getDist(velTrack.getSgmt(startV).getStartPt(),velTrack.getSgmt(endV).getEndPt());

        System.out.println(" Vel Dist Indiv.:         " + String.format(Locale.US, "%10.4f",totalVelDistIndv)
                         + " Vel Dist Total.:         " + String.format(Locale.US, "%10.4f",totalVelDist)
                         + " Lat Dist Total:          " + String.format(Locale.US, "%10.4f",latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt()))
        );

    }

    @Test
    public void exportEventKML() throws Exception {

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
        // 07 Add waypoints of lateral plan to document
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
        // 08 Add waypoints of lateral track to document
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
        // 09 Add lateral track to document
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
        // 10 Write document to kml file
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