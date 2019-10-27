package ftdis.fdpu;

//import jdk.nashorn.internal.ir.annotations.Ignore;
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
import java.util.Date;
import java.util.Locale;

import static ftdis.fdpu.DOMUtil.createChildElement;
import static ftdis.fdpu.DOMUtil.setElementValue;
import static org.junit.Assert.*;

/**
 * Unit test VelocityPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocityPlanTest {
    private LateralPlan latPlan = new LateralPlan();
    private VelocityPlan velPlan = new VelocityPlan();

    private String ioDir, planFileName, eventCollectionFileName;
    private int planId;
    private Path localDir = Paths.get("").toAbsolutePath();


    @Before
    public void setUp() throws Exception {
        // Define paths and file name
        ioDir = localDir + "\\IO\\";
        planFileName = "KSEA KSEA FlightPlan.xml";
        eventCollectionFileName = "KSEA KSEA EventCollection.xml";
        planId = 3;

        // Transform and transform lateral plan
        latPlan.load(ioDir + planFileName,planId);
        latPlan.transform();

        // Load change airspeed events to velocity plan
        //velPlan.assignLat(latPlan);
        //velPlan.load(ioDir + inputFileName,planId);
        //velPlan.transform();
        //velPlan.validate();
    }

    @Test
    public void testLoadVelocityPlan() throws Exception {
        VelocityPlan velPlan = new VelocityPlan();
        velPlan.assignLat(latPlan);
        velPlan.load(ioDir + eventCollectionFileName,planId);
    }

    @Test
    public void testTransformVelocityPlan() throws Exception {
        VelocityPlan velPlan = new VelocityPlan();
        velPlan.assignLat(latPlan);
        velPlan.load(ioDir + eventCollectionFileName,planId);
        velPlan.transform();
        velPlan.validate();

        System.out.println("Data Valid: " + velPlan.dataValid);
    }

    @Test
    public void exportEventKML() throws Exception {

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 - Prepare and Validate Data Sets
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        VelocityPlan velPlan = new VelocityPlan();
        velPlan.assignLat(latPlan);
        velPlan.load(ioDir + eventCollectionFileName,planId);
        velPlan.transform();
        velPlan.validate();


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 Configure and prepare kml file and document
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = eventCollectionFileName + " Velocity Plan " + dateFormat.format(date) + ".kml";
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

        Element velEventsFolder = createChildElement(doc, "Folder");
        Element folderName = createChildElement(velEventsFolder,"name");
        setElementValue(folderName, "Velocity Plan");


        /////////////////////////////////////////////////////////////////////////////
        // 07 Add data to document
        ////////////////////////////////////////////////////////////////////////////

        VelocitySegment velEvent;

        for(int i = 0; i < velPlan.getSgmtCount(); i++){
            velEvent = velPlan.getSgmt(i);
            addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.2f", velEvent.getVasi()) + " ms", velEvent.getStartPt().getLat(),velEvent.getStartPt().getLon(), 0);
            addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.2f", velEvent.getVasf()) + " ms", velEvent.getEndPt().getLat(),velEvent.getEndPt().getLon(), 0);
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

    }




    @Test
    public void testGetWptSgmt() throws Exception {

        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(1,velPlan.getWptSgmt(testWpt).id,0);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),15000);
        assertEquals(4, velPlan.getWptSgmt(testWpt).id, 0);

        testWpt = latPlan.getEndWpt();
        assertEquals(7, velPlan.getWptSgmt(testWpt).id, 0);
    }

    @Test
    public void testGetVasAtWpt() throws Exception {

        Waypoint testWpt = latPlan.getStartWpt();
        assertEquals(0,velPlan.getVasAtWpt(testWpt),0.01);

        testWpt = latPlan.getItmWpt(latPlan.getStartWpt(),7000);
        assertEquals(87.07, velPlan.getVasAtWpt(testWpt), 0.01);

        testWpt = latPlan.getEndWpt();
        assertEquals(10.29, velPlan.getVasAtWpt(testWpt), 0.01);

    }

    @Test
    public void testGetDist() throws Exception {
        Waypoint wpt1 = latPlan.getItmWpt(latPlan.getStartWpt(),0);
        //assertEquals(14294.54, velPlan.getDist(wpt1, 200), 0.01);

        wpt1 = latPlan.getItmWpt(latPlan.getStartWpt(),10000);
        assertEquals(8294.86, velPlan.getDist(wpt1, -100), 0.01);
    }

    @Test
    public void testGetVasu() throws Exception {
        assertEquals(88.932,velPlan.getVasu(latPlan.getStartWpt(),latPlan.getEndWpt()),0.001);

        Waypoint wpt1 = latPlan.getItmWpt(latPlan.getStartWpt(),2000);
        Waypoint wpt2 = latPlan.getItmWpt(latPlan.getStartWpt(),9000);
        assertEquals(82.498, velPlan.getVasu(wpt1,wpt2), 0.001);

        wpt1 = latPlan.getItmWpt(latPlan.getStartWpt(),7000);
        wpt2 = latPlan.getItmWpt(latPlan.getStartWpt(),9000);
        assertEquals(91.482, velPlan.getVasu(wpt1, wpt2), 0.001);
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