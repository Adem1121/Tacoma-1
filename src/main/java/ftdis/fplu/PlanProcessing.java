package ftdis.fplu;

import ftdis.fdpu.*;
import java.io.File;
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
 * Process manually defined flight master plan.xml and prepare flight plan.xml and event collection.xml,
 * i.e. input files for the flight data processing unit.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class PlanProcessing {

    /**
     * End to end flight data processing
     */
    public static void main(String[] args) {
        try{
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Define and init vars
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Waypoint wpt;
            DirectSegment tempDirSgmt = new DirectSegment();
            int pushBackPlanID, taxiToRwyPlanID, flightPlanID, taxiToGatePlanID;
            String ioDir, clsCmd, aircraft, departure, destination, fileMasterPlan, inputFileName = "";
            StringBuilder kmlFlightTrack = new StringBuilder();

            PushbackPlan pushbackPlan = new PushbackPlan();
            TaxiPlan taxiRwyPlan = new TaxiPlan();
            FlightPlan flightPlan = new FlightPlan();
            TaxiPlan taxiGatePlan = new TaxiPlan();

            aircraft = AIRCRAFT_TYPE;
            departure = DEP_ICAO;
            destination = DEST_ICAO;
            pushBackPlanID = 1;
            taxiToRwyPlanID = 2;
            flightPlanID = 3;
            taxiToGatePlanID = 4;

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

            // Set input files
            fileMasterPlan = localDir + ioDir + "FlightMasterPlan.xml";

            if(args.length == 1) {
                fileMasterPlan = localDir + ioDir + args[0];
                inputFileName = args[0].split("\\.(?=[^\\.]+$)")[0];
            }

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 - Prepare Output Files
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Configure output file parameters
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd") ;

            // Initialize document builder factory and create new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            //////////////////////////////////////////////////////////////////////////////////////
            // FlightPlan.xml
            //////////////////////////////////////////////////////////////////////////////////////

            // Configure filename and file output stream
            //String fileName = inputFileName + " FlightPlan " + dateFormat.format(date) + ".xml";
            String fileName = inputFileName + " FlightPlan.xml";
            File flightPlanOut = new File(localDir + ioDir + fileName);

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
            //fileName = inputFileName + " EventCollection " + dateFormat.format(date) + ".xml";
            fileName = inputFileName + " EventCollection.xml";
            File eventCollectionOut = new File(localDir + ioDir + fileName);

            // Create new document
            Document eventCollectionXML = docBuilder.newDocument();

            // Create flight plan root element, add ID and  plan nodes
            Element eventCollection = createChildElement(eventCollectionXML, "EventCollection");
            Attr eventCollectionId = createAttribute(eventCollection, "ID");
            eventCollectionId.setValue("1");

            //////////////////////////////////////////////////////////////////////////////////////
            // FlightPlan.kml
            //////////////////////////////////////////////////////////////////////////////////////

            // Configure filename and file output stream
            //fileName = inputFileName + " FlightPlan " + dateFormat.format(date) + ".kml";
            fileName = inputFileName + " FlightPlan.kml";
            File kmlOut = new File(localDir + ioDir + fileName);

            // Create new document
            Document kmlXML = docBuilder.newDocument();

            // Create KML document root element
            Element kml = createChildElement(kmlXML, "kml");
            kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");

            Element kmlDoc = createChildElement(kml,"Document");
            Element kmlDocName = createChildElement(kmlDoc,"name");
            setElementValue(kmlDocName, fileName);

            Element kmlFlightPlan = createChildElement(kmlDoc, "Folder");
            Element folderName = createChildElement(kmlFlightPlan,"name");
            setElementValue(folderName, "Waypoints");

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 02 - Prepare and Validate Data Sets
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Load plans
            pushbackPlan.load(fileMasterPlan, pushBackPlanID);
            taxiRwyPlan.load(fileMasterPlan, taxiToRwyPlanID);
            flightPlan.load(fileMasterPlan, flightPlanID);
            taxiGatePlan.load(fileMasterPlan, taxiToGatePlanID);

            // Validate transition point: Pushback plan - taxi to runway plan
            wpt = taxiRwyPlan.getLateralPlan().getWpt(0);
            tempDirSgmt.setStartPt(wpt.getLat(), wpt.getLon());

            wpt = taxiRwyPlan.getLateralPlan().getWpt(1);
            tempDirSgmt.setEndPt(wpt.getLat(), wpt.getLon());

            tempDirSgmt.alignToTrack(pushbackPlan.getLateralPlan().getWpt(pushbackPlan.getLateralPlan().getWptSize()-2));

            // Validate transition point: Taxi to runway plan - flight plan
            wpt = taxiRwyPlan.getLateralPlan().getWpt(taxiRwyPlan.getLateralPlan().getWptSize()-2);
            tempDirSgmt.setStartPt(wpt.getLat(), wpt.getLon());

            wpt = flightPlan.getLateralPlan().getWpt(1);
            tempDirSgmt.setEndPt(wpt.getLat(), wpt.getLon());

            tempDirSgmt.alignToTrack(taxiRwyPlan.getLateralPlan().getWpt(taxiRwyPlan.getLateralPlan().getWptSize() - 1));
            tempDirSgmt.alignToTrack(taxiRwyPlan.getLateralPlan().getEndWpt());

            tempDirSgmt.alignToTrack(flightPlan.getLateralPlan().getWpt(0));
            tempDirSgmt.alignToTrack(flightPlan.getLateralPlan().getStartWpt());

            // Validate transition point: Flight plan to Taxi to gate plan
            wpt = flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize()-2);
            tempDirSgmt.setStartPt(wpt.getLat(), wpt.getLon());

            wpt = taxiGatePlan.getLateralPlan().getWpt(1);
            tempDirSgmt.setEndPt(wpt.getLat(), wpt.getLon());

            tempDirSgmt.alignToTrack(flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 1));
            tempDirSgmt.alignToTrack(flightPlan.getLateralPlan().getEndWpt());

            tempDirSgmt.alignToTrack(taxiGatePlan.getLateralPlan().getWpt(0));
            tempDirSgmt.alignToTrack(taxiGatePlan.getLateralPlan().getStartWpt());

            // Validate that heading of first waypoint after take off matches rwy heading
            double wptDist = flightPlan.getLateralPlan().getDist(flightPlan.getLateralPlan().getWpt(1), flightPlan.getLateralPlan().getWpt(2));
            double rwyRadial = NavCalc.getInitBearing(flightPlan.getLateralPlan().getStartWpt(), flightPlan.getLateralPlan().getWpt(1));

            wpt = NavCalc.getRadWpt(flightPlan.getLateralPlan().getWpt(1),wptDist,rwyRadial);

            flightPlan.getLateralPlan().getWpt(2).setLat(wpt.getLat());
            flightPlan.getLateralPlan().getWpt(2).setLon(wpt.getLon());

            // Validate that final approach heading matches rwy heading
            wptDist = flightPlan.getLateralPlan().getDist(flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 3),flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 2));
            rwyRadial = NavCalc.getInitBearing(flightPlan.getLateralPlan().getEndWpt(), flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 2));

            wpt = NavCalc.getRadWpt(flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 2),wptDist,rwyRadial);

            flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 3).setLat(wpt.getLat());
            flightPlan.getLateralPlan().getWpt(flightPlan.getLateralPlan().getWptSize() - 3).setLon(wpt.getLon());

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 - Transform, validate and process data sets
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Transform and validate flight plan
            flightPlan.transform();
            flightPlan.validate();

            // Transform, validate and process Pushback Plan
            pushbackPlan.transform();
            pushbackPlan.validate();
            processPushbackPlan(flightPlanNode, eventCollection, pushbackPlan, pushBackPlanID, flightPlan.getVertEvent(0).getAlti(), kmlFlightTrack, kmlFlightPlan);

            // Transform, validate and process Taxi to Rwy Plan
            taxiRwyPlan.transform();
            taxiRwyPlan.validate();
            processTaxiPlan(flightPlanNode, eventCollection, taxiRwyPlan, taxiToRwyPlanID, flightPlan.getVertEvent(0).getAlti(), kmlFlightTrack, kmlFlightPlan);

            // Process Flight Plan
            processFlightPlan(flightPlanNode, eventCollection, flightPlan, flightPlanID, kmlFlightTrack, kmlFlightPlan);

            // Transform, validate and process Taxi to the Gate Plan
            taxiGatePlan.transform();
            taxiGatePlan.validate();
            processTaxiPlan(flightPlanNode, eventCollection, taxiGatePlan, taxiToGatePlanID, flightPlan.getVertEvent(flightPlan.getVertEventCount() - 1).getAltf(), kmlFlightTrack, kmlFlightPlan);

            // Process kml flight track and prepare .kml file
            processKmlFlightTrack(kmlDoc, kmlFlightTrack.toString());

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 - Write data to xml files
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

            // Write data to flightPlan.kml
            source = new DOMSource(kmlXML);
            result = new StreamResult(kmlOut);
            transformer.transform(source, result);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method processes the pushback plan as defined in the flight master plan.xml, by adding the corresponding
     * waypoints and events to the flight plan.xml and event collection.xml documents.
     *
     * @param flightPlanNode    Flight plan root node of Flight Plan.xml output file
     * @param eventCollection   Event collection root node of Event Collection.xml output file
     * @param pushbackPlan      Pushback plan
     * @param pushBackPlanID    Id of puschback plan
     */
    public static void processPushbackPlan(Element flightPlanNode, Element eventCollection, PushbackPlan pushbackPlan, int pushBackPlanID, double alt, StringBuilder kmlFlightTrack, Element kmlFlightPlan){
        try{
            // Define and init vars
            Waypoint wpt;
            EventChgAirspeed thisVelEvent;
            int n;

            //////////////////////////////////////////////////////////////////////////////////////
            // Write waypoints to flight plan.xml
            //////////////////////////////////////////////////////////////////////////////////////

            // Add Plan node to Flight Plan
            Element plan = createChildElement(flightPlanNode,"Plan");
            Attr planId = createAttribute(plan, "ID");
            planId.setValue(String.valueOf(pushBackPlanID));
            Element waypoints = createChildElement(plan,"Waypoints");

            // Add waypoints
            for(int w = 0; w < pushbackPlan.getLateralPlan().getWptSize(); w++){
                n = w+1;
                // Add waypoint node
                wpt = pushbackPlan.getLateralPlan().getWpt(w);
                addWaypoint(waypoints, n, wpt.getLat(), wpt.getLon());

                // Add waypoint to kml track
                appendToKmlTrack(kmlFlightTrack, wpt, 0);
                addPlaceMark(kmlFlightPlan, "Wpt " + n, wpt, 0);

            }

            //////////////////////////////////////////////////////////////////////////////////////
            // Write Events to Event Collection.xml
            //////////////////////////////////////////////////////////////////////////////////////

            // Add Plan node to Event Collection
            plan = createChildElement(eventCollection,"Plan");
            planId = createAttribute(plan, "ID");
            planId.setValue(String.valueOf(pushBackPlanID));

            // Add Change Airspeed events
            for(int e = 0; e < pushbackPlan.getVelEventCount(); e++){
                addChgAirspeedEvent(plan, pushbackPlan.getVelEvent(e), e+1);
            }

            // Add default change altitude event
            EventChgAltitude defAltEvent = new EventChgAltitude();
            defAltEvent.setStartPt(pushbackPlan.getLateralPlan().getStartWpt().getLat(),pushbackPlan.getLateralPlan().getStartWpt().getLon());
            defAltEvent.setEndPt(pushbackPlan.getLateralPlan().getEndWpt().getLat(),pushbackPlan.getLateralPlan().getEndWpt().getLon());
            defAltEvent.setAlti(alt);
            defAltEvent.setAltf(alt);
            defAltEvent.setVs(0);

            addChgAltitudeEvent(plan,defAltEvent,1);

            // Add default change weather event
            EventChgWeather defWxEvent = new EventChgWeather();
            defWxEvent.setStartPt(pushbackPlan.getLateralPlan().getStartWpt().getLat(),pushbackPlan.getLateralPlan().getStartWpt().getLon());
            defWxEvent.setEndPt(pushbackPlan.getLateralPlan().getEndWpt().getLat(),pushbackPlan.getLateralPlan().getEndWpt().getLon());
            defWxEvent.setTurbulence(0,0);
            defWxEvent.setWind(0,0);

            addChgWeatherEvent(plan, defWxEvent,1);


        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     ** This method processes the taxi plan as defined in the flight master plan.xml, by adding the corresponding
     * waypoints and events to the flight plan.xml and event collection.xml documents.
     *
     * @param flightPlanNode    Flight plan root node of Flight Plan.xml output file
     * @param eventCollection   Event collection root node of Event Collection.xml output file
     * @param taxiPlan          Taxi plan (taxi to runway / taxi to gate)
     * @param taxiPlanID        Id of taxi plan
     * @param alt               Departure/destination field altitude in mtrs
     */
    public static void processTaxiPlan(Element flightPlanNode, Element eventCollection, TaxiPlan taxiPlan, int taxiPlanID, double alt, StringBuilder kmlFlightTrack, Element kmlFlightPlan){
        try{
            // Define and init vars
            Waypoint wpt;

            //////////////////////////////////////////////////////////////////////////////////////
            // Write waypoints
            //////////////////////////////////////////////////////////////////////////////////////

            // Add Plan node to Flight Plan
            Element plan = createChildElement(flightPlanNode,"Plan");
            Attr planId = createAttribute(plan, "ID");
            planId.setValue(String.valueOf(taxiPlanID));
            Element waypoints = createChildElement(plan,"Waypoints");

            int n;

            // Add waypoints
            for(int w = 0; w < taxiPlan.getLateralPlan().getWptSize(); w++){
                n = w+1;
                // Add waypoint node
                wpt = taxiPlan.getLateralPlan().getWpt(w);
                addWaypoint(waypoints, n, wpt.getLat(), wpt.getLon());

                // Add waypoint to kml track
                appendToKmlTrack(kmlFlightTrack, wpt, 0);
                addPlaceMark(kmlFlightPlan, "Wpt " + n, wpt, 0);

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
                addChgAirspeedEvent(plan, taxiPlan.getVelEvent(e), e+1);
            }

            // Add default change altitude event
            EventChgAltitude defAltEvent = new EventChgAltitude();
            defAltEvent.setStartPt(taxiPlan.getLateralPlan().getStartWpt().getLat(),taxiPlan.getLateralPlan().getStartWpt().getLon());
            defAltEvent.setEndPt(taxiPlan.getLateralPlan().getEndWpt().getLat(),taxiPlan.getLateralPlan().getEndWpt().getLon());
            defAltEvent.setAlti(alt);
            defAltEvent.setAltf(alt);
            defAltEvent.setVs(0);

            addChgAltitudeEvent(plan,defAltEvent,1);

            // Add default change weather event
            EventChgWeather defWxEvent = new EventChgWeather();
            defWxEvent.setStartPt(taxiPlan.getLateralPlan().getStartWpt().getLat(),taxiPlan.getLateralPlan().getStartWpt().getLon());
            defWxEvent.setEndPt(taxiPlan.getLateralPlan().getEndWpt().getLat(),taxiPlan.getLateralPlan().getEndWpt().getLon());
            defWxEvent.setTurbulence(0,0);
            defWxEvent.setWind(0,0);

            addChgWeatherEvent(plan, defWxEvent,1);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     ** This method processes the flight plan as defined in the flight master plan.xml, by adding the corresponding
     * waypoints and events to the flight plan.xml and event collection.xml documents.
     *
     * @param flightPlanNode    Flight plan root node of Flight Plan.xml output file
     * @param eventCollection   Event collection root node of Event Collection.xml output file
     * @param flightPlan        Flight plan
     * @param flightPlanID      Id of flight plan
     */
    public static void processFlightPlan(Element flightPlanNode, Element eventCollection, FlightPlan flightPlan, int flightPlanID, StringBuilder kmlFlightTrack, Element kmlFlightPlan){
        try{
            // Define and init vars
            Waypoint wpt;

            //////////////////////////////////////////////////////////////////////////////////////
            // Write waypoints
            //////////////////////////////////////////////////////////////////////////////////////

            // Add Plan node to Flight Plan
            Element plan = createChildElement(flightPlanNode,"Plan");
            Attr planId = createAttribute(plan, "ID");
            planId.setValue(String.valueOf(flightPlanID));
            Element waypoints = createChildElement(plan,"Waypoints");

            int n;

            // Add waypoints
            for(int w = 0; w < flightPlan.getLateralPlan().getWptSize(); w++){
                n = w+1;
                // Add waypoint node
                wpt = flightPlan.getLateralPlan().getWpt(w);
                addWaypoint(waypoints, n, wpt.getLat(), wpt.getLon());

                // Add waypoint to kml track
                appendToKmlTrack(kmlFlightTrack, wpt, 0);
                addPlaceMark(kmlFlightPlan, "Wpt " + n, wpt, 0);
            }

            //////////////////////////////////////////////////////////////////////////////////////
            // Write Events
            //////////////////////////////////////////////////////////////////////////////////////

            // Add Plan node to event collection
            plan = createChildElement(eventCollection,"Plan");
            planId = createAttribute(plan, "ID");
            planId.setValue(String.valueOf(flightPlanID));

            // Add Change Airspeed events
            for(int e = 0; e < flightPlan.getVelEventCount(); e++){
                addChgAirspeedEvent(plan, flightPlan.getVelEvent(e), e+1);
            }

            // Add Change Altitude events
            for(int e = 0; e < flightPlan.getVertEventCount(); e++){
                addChgAltitudeEvent(plan,flightPlan.getVertEvent(e),e+1);
            }

            // Add Change Weather events
            for(int e = 0; e < flightPlan.getWxEventCount(); e++){
                addChgWeatherEvent(plan, flightPlan.getWxEvent(e),e+1);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method creates and adds a waypoint element including the corresponding parameters to the defined root node,
     * as per the .xml schema definition
     *
     * @param planNode  Plan root node of .xml output file
     * @param wptId     Waypoint ID
     * @param wptLat    Waypoint latitude coordinates
     * @param wptLon    Waypoint longitute coordinates
     */
    public static void addWaypoint(Element planNode, int wptId, double wptLat, double wptLon){
        try{
            // Add waypoint node
            Element waypoint = createChildElement(planNode,"Waypoint");
            Attr waypointID = createAttribute(waypoint, "ID");
            waypointID.setValue(String.valueOf(wptId));

            // Add latitude and longitude coordinates
            Element latitude = createChildElement(waypoint,"Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.8f", wptLat));

            Element longitude = createChildElement(waypoint,"Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.8f", wptLon));

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method creates and adds a change airspeed event element including the corresponding parameters
     * to the defined root node, as per the .xml schema definition
     *
     * @param planNode      Plan root node of .xml output file
     * @param velEvent      Change airspeed event
     * @param velEventId    Change airspeed event id
     */
    public static void addChgAirspeedEvent(Element planNode, EventChgAirspeed velEvent, int velEventId){
        try{
            Element event = createChildElement(planNode,"Event");
            Attr eventType = createAttribute(event,"type");
            eventType.setValue("chgAirspeed");
            Attr eventId = createAttribute(event,"ID");
            eventId.setValue(String.valueOf(velEventId));

            // Add waypoints root node
            Element eventWaypoints = createChildElement(event,"Waypoints");

            addWaypoint(eventWaypoints,1,velEvent.getStartPt().getLat(),velEvent.getStartPt().getLon());
            addWaypoint(eventWaypoints,2,velEvent.getEndPt().getLat(),velEvent.getEndPt().getLon());

            // Add variables roots node
            Element eventVars = createChildElement(event,"Variables");

            // Add init speed node
            Element eventVasi = createChildElement(eventVars,"Var");
            Attr eventVasiType = createAttribute(eventVasi,"type");
            eventVasiType.setValue("init");
            Attr eventVasiUnit = createAttribute(eventVasi,"unit");
            eventVasiUnit.setValue("kts");
            setElementValue(eventVasi, String.valueOf(PerfCalc.convertKts(velEvent.getVAsi(), "ms")));

            // Add target speed node
            Element eventVasf = createChildElement(eventVars,"Var");
            Attr eventVasfType = createAttribute(eventVasf,"type");
            eventVasfType.setValue("target");
            Attr eventVasfUnit = createAttribute(eventVasf,"unit");
            eventVasfUnit.setValue("kts");
            setElementValue(eventVasf, String.valueOf(PerfCalc.convertKts(velEvent.getVAsf(), "ms")));

            // Add acceleration node
            Element eventAcc = createChildElement(eventVars,"Var");
            Attr eventAccType = createAttribute(eventAcc,"type");
            eventAccType.setValue("acceleration");
            Attr eventAccUnit = createAttribute(eventAcc,"unit");
            eventAccUnit.setValue("ms2");
            setElementValue(eventAcc, String.valueOf(velEvent.getAcc()));

            // Add time offset node
            Element eventOffset = createChildElement(eventVars,"Var");
            Attr eventOffsetType = createAttribute(eventOffset,"type");
            eventOffsetType.setValue("timeOffset");
            Attr eventOffsetUnit = createAttribute(eventOffset,"unit");
            eventOffsetUnit.setValue("s");
            setElementValue(eventOffset, String.valueOf(velEvent.getOffset()));

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method creates and adds a change altitude event element including the corresponding parameters
     * to the defined root node, as per the .xml schema definition
     *
     * @param planNode      Plan root node of .xml output file
     * @param altEvent      Change airspeed event
     * @param altEventId    Change airspeed event id
     */
    public static void addChgAltitudeEvent(Element planNode, EventChgAltitude altEvent, int altEventId){
        try{
            Element event = createChildElement(planNode,"Event");
            Attr eventType = createAttribute(event, "type");
            eventType.setValue("chgAltitude");
            Attr eventId = createAttribute(event, "ID");
            eventId.setValue(String.valueOf(altEventId));

            // Add waypoints root node
            Element eventWaypoints = createChildElement(event,"Waypoints");

            addWaypoint(eventWaypoints,1,altEvent.getStartPt().getLat(),altEvent.getStartPt().getLon());
            addWaypoint(eventWaypoints,2,altEvent.getEndPt().getLat(),altEvent.getEndPt().getLon());

            // Add variables root node
            Element eventVars = createChildElement(event, "Variables");

            // Add init altitude node
            Element eventAlti = createChildElement(eventVars, "Var");
            Attr eventAltiType = createAttribute(eventAlti, "type");
            eventAltiType.setValue("init");
            Attr eventAltiUnit = createAttribute(eventAlti, "unit");
            eventAltiUnit.setValue("m");
            setElementValue(eventAlti, String.valueOf(altEvent.getAlti()));

            // Add target altitude node
            Element eventAltf = createChildElement(eventVars, "Var");
            Attr eventAltfType = createAttribute(eventAltf, "type");
            eventAltfType.setValue("target");
            Attr eventAltfUnit = createAttribute(eventAlti, "unit");
            eventAltfUnit.setValue("m");
            setElementValue(eventAltf, String.valueOf(altEvent.getAltf()));

            // Add vertical speed node
            Element eventAcc = createChildElement(eventVars, "Var");
            Attr eventAccType = createAttribute(eventAcc, "type");
            eventAccType.setValue("vs");
            Attr eventAccUnit = createAttribute(eventAcc, "unit");
            eventAccUnit.setValue("ms");
            setElementValue(eventAcc, String.valueOf(altEvent.getVs()));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method creates and adds a change weather event element including the corresponding parameters
     * to the defined root node, as per the .xml schema definition
     *
     * @param planNode     Plan root node of .xml output file
     * @param wxEvent      Change weather event
     * @param wxEventId    Change weather event id
     */
    public static void addChgWeatherEvent(Element planNode, EventChgWeather wxEvent, int wxEventId){
        try{
            Element event = createChildElement(planNode,"Event");
            Attr eventType = createAttribute(event, "type");
            eventType.setValue("chgWeather");
            Attr eventId = createAttribute(event, "ID");
            eventId.setValue(String.valueOf(wxEventId));

            // Add waypoints root node
            Element eventWaypoints = createChildElement(event,"Waypoints");

            addWaypoint(eventWaypoints,1,wxEvent.getStartPt().getLat(),wxEvent.getStartPt().getLon());
            addWaypoint(eventWaypoints,2,wxEvent.getEndPt().getLat(),wxEvent.getEndPt().getLon());

            // Add variables root node
            Element eventVars = createChildElement(event, "Variables");

            // Add magnitude node
            Element eventMagn = createChildElement(eventVars,"Var");
            Attr eventMagnType = createAttribute(eventMagn,"type");
            eventMagnType.setValue("magn");
            Attr eventMagnUnit = createAttribute(eventMagn,"unit");
            eventMagnUnit.setValue("dec");
            setElementValue(eventMagn, String.valueOf(wxEvent.getTurbulenceMagn()));

            // Add frequency node
            Element eventFreq = createChildElement(eventVars,"Var");
            Attr eventFreqType = createAttribute(eventFreq,"type");
            eventFreqType.setValue("freq");
            Attr eventFreqUnit = createAttribute(eventFreq,"unit");
            eventFreqUnit.setValue("dec");
            setElementValue(eventFreq, String.valueOf(wxEvent.getTurbulenceFreq()));

            // Add vertical speed node
            Element eventWindDir = createChildElement(eventVars,"Var");
            Attr eventWindDirType = createAttribute(eventWindDir,"type");
            eventWindDirType.setValue("windDir");
            Attr eventWindDirUnit = createAttribute(eventWindDir,"unit");
            eventWindDirUnit.setValue("dec");
            setElementValue(eventWindDir, String.valueOf(wxEvent.getWindDir()));

            // Add vertical speed node
            Element eventWindSpd = createChildElement(eventVars,"Var");
            Attr eventWindSpdType = createAttribute(eventWindSpd,"type");
            eventWindSpdType.setValue("windSpd");
            Attr eventWindSpdUnit = createAttribute(eventWindSpd,"unit");
            eventWindSpdUnit.setValue("dec");
            setElementValue(eventWindSpd, String.valueOf(wxEvent.getWindDir()));
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }


    /**
     * This method processes the kml flight track information and appends the corresponding .kml structure
     * to the kml document.
     *
     * @param kmlFlightTrack    Flight track containing lat, long and alt information for each waypoint as per kml spec
     */
    public static void processKmlFlightTrack(Element kmlDoc, String kmlFlightTrack){
        try{
            // Set Flight Track style
            Element trackStyle = createChildElement(kmlDoc,"Style");
            trackStyle.setAttribute("id","linePoly");

            Element trackLineStyle = createChildElement(trackStyle,"LineStyle");

            Element trackLineColor = createChildElement(trackLineStyle,"color");
            setElementValue(trackLineColor,"7f00ffff");

            Element trackLineWidth = createChildElement(trackLineStyle,"width");
            setElementValue(trackLineWidth, "4");

            Element trackPolyStyle = createChildElement(trackLineStyle, "PolyStyle");

            Element trackPolyColor = createChildElement(trackPolyStyle,"color");
            setElementValue(trackPolyColor, "7f00ff00");

            // Set Events place mark style
            Element wptStyle = createChildElement(kmlDoc,"Style");
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

            // Add flight track to document
            Element trackMark = createChildElement(kmlDoc,"Placemark");

            Element trackName = createChildElement(trackMark, "name");
            setElementValue(trackName,"Flight Plan");

            Element trackStyleUrl = createChildElement(trackMark, "styleUrl");
            setElementValue(trackStyleUrl, "#linePoly");

            // Set linestring configuration elements
            Element trackLineString = createChildElement(trackMark,"LineString");

            Element trackExtrude = createChildElement(trackLineString,"extrude");
            setElementValue(trackExtrude, "1");

            Element trackTessellate = createChildElement(trackLineString,"tessellate");
            setElementValue(trackTessellate,"1");

            Element trackAltMode = createChildElement(trackLineString,"altitudeMode");
            setElementValue(trackAltMode,"Relative to ground");

            Element trackCoordinates = createChildElement(trackLineString,"coordinates");
            setElementValue(trackCoordinates, kmlFlightTrack);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method appends a waypoint and altitude to a kml flight track. The kml flight track is the main input
     * component for the export to .kml method, to allow for the flight plan's visualization in Google Earth
     *
     * @param kmlFlightTrack    kml flight track
     * @param wpt               Waypoint to be added
     * @param alt               Altitude at waypoint in meters
     */
    public static void appendToKmlTrack(StringBuilder kmlFlightTrack, Waypoint wpt, double alt){
        try{
            kmlFlightTrack.append(String.format(Locale.US, "%.6f", wpt.getLon()));
            kmlFlightTrack.append(",");
            kmlFlightTrack.append(String.format(Locale.US, "%.6f", wpt.getLat()));
            kmlFlightTrack.append(",");
            kmlFlightTrack.append(String.format(Locale.US, "%.2f", alt));
            kmlFlightTrack.append(" ");
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method adds a place mark as per Google Earth .kml schema specifications.
     *
     * @param node      Root node element
     * @param wptName   Name of waypoint
     * @param wpt               Waypoint to be added
     * @param alt               Altitude at waypoint in meters
     */
    public static void addPlaceMark(Element node, String wptName, Waypoint wpt, double alt){
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
            setElementValue(coordinates,String.format(Locale.US, "%.6f", wpt.getLon()) + "," + String.format(Locale.US, "%.6f", wpt.getLat()) + "," + alt);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
