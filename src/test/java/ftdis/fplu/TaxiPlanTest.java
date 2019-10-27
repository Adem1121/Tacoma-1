package ftdis.fplu;

import org.junit.Test;
import ftdis.fdpu.*;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.w3c.dom.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.Config.*;

/**
 * Unit test TaxiPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class TaxiPlanTest {
    TaxiPlan testPlan = new TaxiPlan();

    @Test
    public void testLoad() throws Exception {
        testPlan.load("/Users/FinnR/OneDrive/Documents/Development/Flight Tracking Data Integration System/05 Java/Tacoma/18.0/IO/KSEA KSEA FlightPlan.xml",2);
    }

    @Test
    public void testTransform() throws Exception {
        Path localDir = Paths.get("").toAbsolutePath();

        testPlan.load(localDir + "\\IO\\KSEA KSEA.xml",2);
        testPlan.transform();
    }

    @Test
    public void exportEventKML() throws Exception {

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 - Prepare and Validate Data Sets
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        String ioDir, inputFileName;
        Path localDir = Paths.get("").toAbsolutePath();

        // Define paths and file name
        ioDir = localDir + "\\IO\\";
        inputFileName = "KSEA KSEA.xml";

        // Load taxi plan
        testPlan.load(ioDir + inputFileName,2);

        // Transform taxi plan
        testPlan.transform();

        // Validate taxi plan
        testPlan.validate();


        ////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 Configure and prepare kml file and document
        ////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = inputFileName + " Velocity Events Taxi Plan " + dateFormat.format(date) + ".kml";
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
        setElementValue(folderName, "Velocity Track");


        /////////////////////////////////////////////////////////////////////////////
        // 07 Add data to document
        ////////////////////////////////////////////////////////////////////////////

        EventChgAirspeed velEvent;

        for(int i = 0; i < testPlan.getVelEventCount(); i++){
            velEvent = testPlan.getVelEvent(i);
            addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.2f", velEvent.getVAsi()) + " ms", velEvent.getStartPt().getLat(),velEvent.getStartPt().getLon(), 0);
            addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.2f", velEvent.getVAsf()) + " ms", velEvent.getEndPt().getLat(),velEvent.getEndPt().getLon(), 0);

            //addPlaceMark(velEventsFolder, i + "S: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velEvent.getVAsi(),"ms")) + " kts", velEvent.getStartPt().getLat(),velEvent.getStartPt().getLon(), 0);
            //addPlaceMark(velEventsFolder, i + "E: " + String.format(Locale.US, "%10.1f", PerfCalc.convertKts(velEvent.getVAsf(),"ms")) + " kts", velEvent.getEndPt().getLat(),velEvent.getEndPt().getLon(), 0);
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
    public void testEndtoEnd() throws Exception {

        Waypoint wpt;
        DirectSegment tempDirSgmt = new DirectSegment();
        EventChgAirspeed thisVelEvent;
        EventChgAltitude thisVertEvent;
        EventChgWeather thisWxEvent;
        double alt;
        int taxiPlanID;
        String aircraft, departure, destination, fileMasterPlan;

        TaxiPlan taxiPlan = new TaxiPlan();

        // Set Parameters
        fileMasterPlan = "/Users/FinnR/OneDrive/Documents/Development/Flight Tracking Data Integration System/05 Java/Tacoma/18.0/IO/KSEA KSEA.xml";
        aircraft = AIRCRAFT_TYPE;
        departure = DEP_ICAO;
        destination = DEST_ICAO;

        taxiPlanID = 2;
        alt = PerfCalc.convertFt(18.0,"ft");

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 - Prepare Output Files
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Configure output file parameters
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Initialize document builder factory and create new document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

        //////////////////////////////////////////////////////////////////////////////////////
        // FlightPlan.xml
        //////////////////////////////////////////////////////////////////////////////////////

        // Configure filename and file output stream
        String fileName = "Taxi " + departure + " " + destination + " FlightPlan " + dateFormat.format(date) + ".xml";
        File flightPlanOut = new File("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/" + fileName);

        // Create new document
        Document flightPlanXML = docBuilder.newDocument();

        // Create flight plan root element, add ID, flight child and  plan nodes
        Element flightPlanNode = createChildElement(flightPlanXML, "Flightplan");
        Attr flightPlanId = createAttribute(flightPlanNode, "ID");
        flightPlanId.setValue("1");

        Element flight = createChildElement(flightPlanNode,"Flight");
        Attr aircraftType = createAttribute(flight,"AircraftType");
        aircraftType.setValue(aircraft);

        // Add Departure and Destination objects to Flight node
        Element dept = createChildElement(flight,"Departure");
        Attr type = createAttribute(dept,"type");
        type.setValue("ICAO");
        setElementValue(dept, departure);

        Element dest = createChildElement(flight,"Destination");
        type = createAttribute(dest,"type");
        type.setValue("ICAO");
        setElementValue(dest, destination);

        //////////////////////////////////////////////////////////////////////////////////////
        // EventCollection.xml
        //////////////////////////////////////////////////////////////////////////////////////

        // Configure filename and file output stream
        fileName = "Taxi " + departure + " " + destination + " EventCollection " + dateFormat.format(date) + ".xml";
        File eventCollectionOut = new File("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/" + fileName);

        // Create new document
        Document eventCollectionXML = docBuilder.newDocument();

        // Create flight plan root element, add ID and  plan nodes
        Element eventCollection = createChildElement(eventCollectionXML, "EventCollection");
        Attr eventCollectionId = createAttribute(eventCollection, "ID");
        eventCollectionId.setValue("1");


        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 00 - Prepare and Validate Data Sets
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

        // Load plans
        taxiPlan.load(fileMasterPlan, taxiPlanID);

        // Transform and validate taxi to runway plan
        taxiPlan.transform();
        taxiPlan.validate();

        //////////////////////////////////////////////////////////////////////////////////////
        // Write waypoints
        //////////////////////////////////////////////////////////////////////////////////////

        // Add Plan node to Flight Plan
        Element plan = createChildElement(flightPlanNode,"Plan");
        Attr planId = createAttribute(plan, "ID");
        planId.setValue(String.valueOf(taxiPlanID));

        // Add waypoints
        for(int w = 0; w < taxiPlan.getLateralPlan().getWptSize(); w++){
            // Get waypoint and parameters
            wpt = taxiPlan.getLateralPlan().getWpt(w);

            // Add waypoint node
            Element waypoint = createChildElement(plan,"Waypoint");
            Attr waypointID = createAttribute(waypoint, "ID");
            waypointID.setValue(String.valueOf(w + 1));

            // Add latitude and longitude coordinates
            Element latitude = createChildElement(waypoint,"Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", wpt.getLat()));

            Element longitude = createChildElement(waypoint,"Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", wpt.getLon()));
        }

        //////////////////////////////////////////////////////////////////////////////////////
        // Write Events
        //////////////////////////////////////////////////////////////////////////////////////

        // Add Plan node to event collection
        plan = createChildElement(eventCollection,"Plan");
        planId = createAttribute(plan, "ID");
        planId.setValue(String.valueOf(taxiPlanID));

        // Add Change Airspeed events
        for(int e = 0; e < taxiPlan.getVelEventCount(); e++){
            thisVelEvent = taxiPlan.getVelEvent(e);

            Element event = createChildElement(plan,"Event");
            Attr eventType = createAttribute(event,"type");
            eventType.setValue("chgAirspeed");
            Attr eventId = createAttribute(event,"ID");
            eventId.setValue(String.valueOf(e + 1));

            // Add waypoints node
            Element eventWaypoints = createChildElement(event,"Waypoints");

            // Add start waypoint node
            Element startWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr startWptId = createAttribute(startWpt,"ID");
            startWptId.setValue("1");

            Element latitude = createChildElement(startWpt,"Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", thisVelEvent.getStartPt().getLat()));

            Element longitude = createChildElement(startWpt,"Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", thisVelEvent.getStartPt().getLon()));

            // Add end waypoint node
            Element endWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr endWptId = createAttribute(endWpt,"ID");
            endWptId.setValue("2");

            latitude = createChildElement(endWpt,"Latitude");
            latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", thisVelEvent.getEndPt().getLat()));

            longitude = createChildElement(endWpt,"Longitude");
            longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", thisVelEvent.getEndPt().getLon()));

            // Add variables node
            Element eventVars = createChildElement(event,"Variables");

            // Add init speed node
            Element eventVasi = createChildElement(eventVars,"Var");
            Attr eventVasiType = createAttribute(eventVasi,"type");
            eventVasiType.setValue("init");
            Attr eventVasiUnit = createAttribute(eventVasi,"unit");
            eventVasiUnit.setValue("kts");
            setElementValue(eventVasi, String.valueOf(PerfCalc.convertKts(thisVelEvent.getVAsi(), "ms")));

            // Add target speed node
            Element eventVasf = createChildElement(eventVars,"Var");
            Attr eventVasfType = createAttribute(eventVasf,"type");
            eventVasfType.setValue("target");
            Attr eventVasfUnit = createAttribute(eventVasf,"unit");
            eventVasfUnit.setValue("kts");
            setElementValue(eventVasf, String.valueOf(PerfCalc.convertKts(thisVelEvent.getVAsf(), "ms")));

            // Add acceleration node
            Element eventAcc = createChildElement(eventVars,"Var");
            Attr eventAccType = createAttribute(eventAcc,"type");
            eventAccType.setValue("acceleration");
            Attr eventAccUnit = createAttribute(eventAcc,"unit");
            eventAccUnit.setValue("ms2");
            setElementValue(eventAcc, String.valueOf(thisVelEvent.getAcc()));

            // Add time offset node
            Element eventOffset = createChildElement(eventVars,"Var");
            Attr eventOffsetType = createAttribute(eventOffset,"type");
            eventOffsetType.setValue("timeOffset");
            Attr eventOffsetUnit = createAttribute(eventOffset,"unit");
            eventOffsetUnit.setValue("s");
            setElementValue(eventOffset, String.valueOf(thisVelEvent.getOffset()));
        }

        // Add default change altitude event
        {
            Element event = createChildElement(plan, "Event");
            Attr eventType = createAttribute(event, "type");
            eventType.setValue("chgAltitude");
            Attr altEventId = createAttribute(event, "ID");
            altEventId.setValue("1");

            // Add waypoints node
            Element eventWaypoints = createChildElement(event, "Waypoints");

            // Add start waypoint node
            Element startWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr startWptId = createAttribute(startWpt, "ID");
            startWptId.setValue("1");

            Element latitude = createChildElement(startWpt, "Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getStartWpt().getLat()));

            Element longitude = createChildElement(startWpt, "Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getStartWpt().getLon()));

            // Add end waypoint node
            Element endWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr endWptId = createAttribute(endWpt, "ID");
            endWptId.setValue("2");

            latitude = createChildElement(endWpt, "Latitude");
            latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getEndWpt().getLat()));

            longitude = createChildElement(endWpt, "Longitude");
            longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getEndWpt().getLon()));

            // Add variables node
            Element eventVars = createChildElement(event, "Variables");

            // Add init altitude node
            Element eventAlti = createChildElement(eventVars, "Var");
            Attr eventAltiType = createAttribute(eventAlti, "type");
            eventAltiType.setValue("init");
            Attr eventAltiUnit = createAttribute(eventAlti, "unit");
            eventAltiUnit.setValue("m");
            setElementValue(eventAlti, String.valueOf(alt));

            // Add target altitude node
            Element eventAltf = createChildElement(eventVars, "Var");
            Attr eventAltfType = createAttribute(eventAltf, "type");
            eventAltfType.setValue("target");
            Attr eventAltfUnit = createAttribute(eventAlti, "unit");
            eventAltfUnit.setValue("m");
            setElementValue(eventAltf, String.valueOf(alt));

            // Add vertical speed node
            Element eventAcc = createChildElement(eventVars, "Var");
            Attr eventAccType = createAttribute(eventAcc, "type");
            eventAccType.setValue("vs");
            Attr eventAccUnit = createAttribute(eventAcc, "unit");
            eventAccUnit.setValue("ms");
            setElementValue(eventAcc, "0");
        }

        // Add default change weather event
        {
            Element event = createChildElement(plan,"Event");
            Attr eventType = createAttribute(event,"type");
            eventType.setValue("chgWeather");
            Attr eventId = createAttribute(event,"ID");
            eventId.setValue(String.valueOf("1"));

            // Add waypoints node
            Element eventWaypoints = createChildElement(event,"Waypoints");

            // Add start waypoint node
            Element startWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr startWptId = createAttribute(startWpt, "ID");
            startWptId.setValue("1");

            Element latitude = createChildElement(startWpt, "Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getStartWpt().getLat()));

            Element longitude = createChildElement(startWpt, "Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getStartWpt().getLon()));

            // Add end waypoint node
            Element endWpt = createChildElement(eventWaypoints, "Waypoint");
            Attr endWptId = createAttribute(endWpt, "ID");
            endWptId.setValue("2");

            latitude = createChildElement(endWpt, "Latitude");
            latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getEndWpt().getLat()));

            longitude = createChildElement(endWpt, "Longitude");
            longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.7f", taxiPlan.getLateralPlan().getEndWpt().getLon()));

            // Add variables node
            Element eventVars = createChildElement(event,"Variables");

            // Add magnitude node
            Element eventMagn = createChildElement(eventVars,"Var");
            Attr eventMagnType = createAttribute(eventMagn,"type");
            eventMagnType.setValue("magn");
            Attr eventMagnUnit = createAttribute(eventMagn,"unit");
            eventMagnUnit.setValue("dec");
            setElementValue(eventMagn, "0.0");

            // Add frequency node
            Element eventFreq = createChildElement(eventVars,"Var");
            Attr eventFreqType = createAttribute(eventFreq,"type");
            eventFreqType.setValue("freq");
            Attr eventFreqUnit = createAttribute(eventFreq,"unit");
            eventFreqUnit.setValue("dec");
            setElementValue(eventFreq, "0.0");

            // Add vertical speed node
            Element eventWindDir = createChildElement(eventVars,"Var");
            Attr eventWindDirType = createAttribute(eventWindDir,"type");
            eventWindDirType.setValue("windDir");
            Attr eventWindDirUnit = createAttribute(eventWindDir,"unit");
            eventWindDirUnit.setValue("dec");
            setElementValue(eventWindDir, "0");

            // Add vertical speed node
            Element eventWindSpd = createChildElement(eventVars,"Var");
            Attr eventWindSpdType = createAttribute(eventWindSpd,"type");
            eventWindSpdType.setValue("windSpd");
            Attr eventWindSpdUnit = createAttribute(eventWindSpd,"unit");
            eventWindSpdUnit.setValue("dec");
            setElementValue(eventWindSpd, "0");
        }

        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        // 03 - Write data to xml file
        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

        // Write data to flightPlan.xml
        DOMSource source = new DOMSource(flightPlanXML);
        StreamResult result = new StreamResult(flightPlanOut);
        transformer.transform(source, result);

        // Write data to eventCollection.xml
        source = new DOMSource(eventCollectionXML);
        result = new StreamResult(eventCollectionOut);
        transformer.transform(source, result);
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