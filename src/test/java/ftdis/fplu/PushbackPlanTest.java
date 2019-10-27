package ftdis.fplu;

import ftdis.fdpu.*;
import org.junit.Test;
import org.w3c.dom.Attr;
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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static ftdis.fdpu.Config.*;
import static ftdis.fdpu.DOMUtil.*;

/**
 * Unit test TaxiPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class PushbackPlanTest {
    TaxiPlan testPlan = new TaxiPlan();

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
        fileMasterPlan = "/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/F11 KEWR KTPA v1.xml";
        aircraft = AIRCRAFT_TYPE;
        departure = DEP_ICAO;
        destination = DEST_ICAO;

        taxiPlanID = 1;
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
        String fileName = "Pushback " + departure + " " + destination + " FlightPlan " + dateFormat.format(date) + ".xml";
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
        fileName = "Pushback " + departure + " " + destination + " EventCollection " + dateFormat.format(date) + ".xml";
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

}