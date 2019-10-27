package ftdis.fdpu;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.lang.String;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static ftdis.fdpu.Config.*;
import static ftdis.fdpu.DOMUtil.*;
import static java.lang.Integer.parseInt;


/**
 * The ImportKMLFileProcessing class loads the "Taxi to Runway", "Flight Plan" and "Taxi to Gate" .KML input files
 * and transforms them into a single .XML file that can be processed further.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ImportKMLFileProcessing {

    /**
     * End to end flight plan .kml processing and export to .xml file
     */
    public static void main(String[] args) {
        try {
            int wptID, turnID;
            double turnDist;
             String ioDir, outputFileName, taxiToRunwayFilePath, flightPlanFilePath, taxiToGateFilePath, timeOffset, speed, comment;
            Waypoint planEndPoint = new Waypoint();
            List<Waypoint> waypoints  = new ArrayList<Waypoint>();
            List<Waypoint> events = new ArrayList<Waypoint>();


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 Set processing parameters
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

            // Set default processing parameters
            outputFileName = "Flight Plan v1.xml";
            taxiToRunwayFilePath = localDir + ioDir + "Taxi to Runway.kml";
            flightPlanFilePath = localDir + ioDir + "Flight Plan.kml";
            taxiToGateFilePath = localDir + ioDir + "Taxi to Gate.kml";

            // Set input files as per command line parameters
            if(args.length == 4) {
                outputFileName = args[0];
                taxiToRunwayFilePath = localDir + ioDir + args[1];
                flightPlanFilePath = localDir + ioDir + args[2];
                taxiToGateFilePath = localDir + ioDir + args[3];
            }


            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 - Prepare Output Files
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Configure output file parameters
            Date date = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

            // Initialize document builder factory and create new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Configure filename and file output stream
            File flightPlanOut = new File(localDir + ioDir + outputFileName);

            // Create new document
            Document flightPlanXML = docBuilder.newDocument();

            // Create Flight Plan root element, add sub plan nodes and IDs
            Element masterPlanNode = createChildElement(flightPlanXML, "Masterplan");
            Attr masterPlanId = createAttribute(masterPlanNode, "ID");
            masterPlanId.setValue("1");

            // Add nodes for Pushback Plan
            Element pushbackPlanNode = createChildElement(masterPlanNode,"Plan");
            Attr pushbackPlanId = createAttribute(pushbackPlanNode, "ID");
            pushbackPlanId.setValue("1");

            Element pushBackPlanWptNode = createChildElement(pushbackPlanNode,"Waypoints");
            Element pushBackPlanEventNode = createChildElement(pushbackPlanNode,"Events");

            Comment pushbackPlanComment = flightPlanXML.createComment(" ++++++++++++++++++++ Pushback Plan ++++++++++++++++++++ ");
            masterPlanNode.insertBefore(pushbackPlanComment, pushbackPlanNode);

            // Add nodes for Taxi to Runway Plan
            Element taxiToRunwayPlanNode = createChildElement(masterPlanNode,"Plan");
            Attr taxiToRunwayPlanId = createAttribute(taxiToRunwayPlanNode, "ID");
            taxiToRunwayPlanId.setValue("2");

            Element taxiToRunwayPlanWptNode = createChildElement(taxiToRunwayPlanNode,"Waypoints");
            Element taxiToRunwayPlanEventNode = createChildElement(taxiToRunwayPlanNode,"Events");

            Comment taxiToRunwayComment = flightPlanXML.createComment(" ++++++++++++++++++++ Taxi to Runway Plan ++++++++++++++++++++ ");
            masterPlanNode.insertBefore(taxiToRunwayComment, taxiToRunwayPlanNode);

            // Add nodes to Flight Plan
            Element flightPlanNode = createChildElement(masterPlanNode, "Plan");
            Attr flightPlanId = createAttribute(flightPlanNode, "ID");
            flightPlanId.setValue("3");

            Element flightPlanWptNode = createChildElement(flightPlanNode,"Waypoints");
            Element flightPlanEventNode = createChildElement(flightPlanNode,"Events");

            Comment flightPlanComment = flightPlanXML.createComment(" ++++++++++++++++++++ Flight Plan ++++++++++++++++++++ ");
            masterPlanNode.insertBefore(flightPlanComment, flightPlanNode);

            // Add nodes for Taxi to Gate Plan
            Element taxiToGatePlanNode = createChildElement(masterPlanNode,"Plan");
            Attr taxiToGatePlanId = createAttribute(taxiToGatePlanNode, "ID");
            taxiToGatePlanId.setValue("4");

            Element taxiToGatePlanWptNode = createChildElement(taxiToGatePlanNode,"Waypoints");
            Element taxiToGatePlanEventNode = createChildElement(taxiToGatePlanNode,"Events");

            Comment taxiToGateComment = flightPlanXML.createComment(" ++++++++++++++++++++ Taxi to Gate Plan ++++++++++++++++++++ ");
            masterPlanNode.insertBefore(taxiToGateComment, taxiToGatePlanNode);


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 Process Pushback Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            importKmlFile(waypoints, events, taxiToRunwayFilePath);
            wptID = 1;

            // Add Waypoints
            for(int w = 0; w < 5; w++){
                // Define time offset, speed, comment
                if(w == 0){
                    comment = "Parking Position";
                    timeOffset = Double.toString(AIRCRAFT_PARK_TIME);
                    speed = "0";
                } else if (w == 1){
                    comment = "Turn";
                    timeOffset = "";
                    speed = "2";
                } else if (w == 4){
                    comment = "End of Pushback";
                    timeOffset = "";
                    speed = "0";
                } else {
                    comment = "";
                    timeOffset = "";
                    speed = "2";
                }
                // Add waypoint
                addWaypoint(flightPlanXML, pushBackPlanWptNode, wptID, waypoints.get(w).getLat(), waypoints.get(w).getLon(), timeOffset, speed, comment);
                wptID++;
            }

            // TODO ADD SEPARATE EVENTS FOR PUSHBACK

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 Process Taxi to Runway Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            wptID = 1;
            turnID = 1;

            // Add Waypoints
            for(int w = 4; w < waypoints.size(); w++){
                // Define time offset, speed, comment
                if(w == 4){
                    comment = "End of Pushback";
                    timeOffset = "90";
                    speed = "0";

                } else if ( w == waypoints.size()-1){
                    comment = "Take off: Start";
                    timeOffset = "";
                    speed = "0";
                } else if(wptID == (turnID*3)-1){
                    comment = "Turn " + turnID;
                    timeOffset = "";
                    speed = "";
                } else if(wptID == turnID*3){
                    comment = "";
                    timeOffset = "";

                    // Calculate distance between turn start and end points and determine turn speed
                    turnDist = NavCalc.getDirectDist(waypoints.get(w-1),waypoints.get(w+1));

                    if(turnDist > 50)
                        speed = "10";
                    else if(turnDist < 50 && turnDist > 30)
                        speed = "8";
                    else
                        speed = "5";

                    /*
                    Revise speeds shallow / small turns
                    else if(turnDist < 30 && turnDist > 20)
                        speed = "5";
                    else if(turnDist < 20 && turnDist > 15)
                        speed = "2";
                    else
                        speed = "1";
                    */

                    // Set speed for final turn onto runway
                    if(wptID == (waypoints.size()-4)-2)
                        speed = "2";

                    turnID++;
                } else {
                    comment = "";
                    timeOffset = "";
                    speed = "";
                }

                // Add waypoint
                addWaypoint(flightPlanXML, taxiToRunwayPlanWptNode, wptID, waypoints.get(w).getLat(), waypoints.get(w).getLon(), timeOffset, speed, comment);
                wptID++;
            }

            // Add Events
            wptID = 1;
            for(int e = 0; e < events.size(); e++){
                // Add waypoint
                timeOffset = Integer.toString(events.get(e).id);
                addWaypoint(flightPlanXML, taxiToRunwayPlanEventNode, wptID, events.get(e).getLat(), events.get(e).getLon(), timeOffset, "0", "");
                wptID++;
            }

            // Save plan end point and clear waypoints list
            planEndPoint.setLat(waypoints.get(waypoints.size()-1).getLat());
            planEndPoint.setLon(waypoints.get(waypoints.size()-1).getLon());
            waypoints.clear();
            events.clear();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 Process Flight Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            importKmlFile(waypoints, events, flightPlanFilePath);
            wptID = 1;

            for(int w = 0; w < waypoints.size(); w++){
                // Define time offset, speed, comment
                if(w == 0){
                    comment = "Take off: Start";
                    timeOffset = "20";
                    speed = "0";

                    //Adjust start of flight plan to match end of taxi to runway plan
                    waypoints.get(w).setLat(planEndPoint.getLat());
                    waypoints.get(w).setLon(planEndPoint.getLon());
                } else if (w == 1){
                    comment = "Takeoff: Rotate";
                    timeOffset = "";
                    speed = "";
                } else if (w == 2){
                    comment = "Flight Plan";
                    timeOffset = "";
                    speed = "";
                } else if (w == waypoints.size() - 3){
                    comment = "Final Approach Fix: 10nm / 17km out";
                    timeOffset = "";
                    speed = "";
                } else if (w == waypoints.size() - 2){
                    comment = "Landing: Rwy touchdown point";
                    timeOffset = "";
                    speed = "";
                } else if (w == waypoints.size() - 1){
                    comment = "Landing: End of roll out";
                    timeOffset = "";
                    speed = "20";
                } else {
                    comment = "";
                    timeOffset = "";
                    speed = "";
                }
                // Add waypoint
                addWaypoint(flightPlanXML, flightPlanWptNode, wptID, waypoints.get(w).getLat(), waypoints.get(w).getLon(), timeOffset, speed, comment);
                wptID++;
            }

            // Clear waypoints list
            planEndPoint.setLat(waypoints.get(waypoints.size()-1).getLat());
            planEndPoint.setLon(waypoints.get(waypoints.size()-1).getLon());
            waypoints.clear();
            events.clear();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 05 Process Taxi to Gate Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            importKmlFile(waypoints, events, taxiToGateFilePath);
            wptID = 1;
            turnID = 1;

            for(int w = 0; w < waypoints.size(); w++){
                // Define time offset, speed, comment
                if(w == 0){
                    comment = "Landing: End of roll out";
                    timeOffset = "";
                    speed = "20";

                    //Adjust start of flight plan to match end of taxi to runway plan
                    waypoints.get(w).setLat(planEndPoint.getLat());
                    waypoints.get(w).setLon(planEndPoint.getLon());

                } else if ( w == waypoints.size()-1){
                    comment = "Parking Position";
                    timeOffset = "180";
                    speed = "0";
                } else if(wptID == (turnID*3)-1){
                    comment = "Turn " + turnID;
                    timeOffset = "";
                    speed = "";
                } else if(wptID == turnID*3){
                    comment = "";
                    timeOffset = "";

                    // Calculate distance between turn start and end points and determine turn speed
                    turnDist = NavCalc.getDirectDist(waypoints.get(w-1),waypoints.get(w+1));

                    if(turnDist > 50)
                        speed = "10";
                    else if(turnDist < 50 && turnDist > 30)
                        speed = "8";
                    else
                        speed = "5";

                    turnID++;
                } else {
                    comment = "";
                    timeOffset = "";
                    speed = "";
                }

                // Add waypoint
                addWaypoint(flightPlanXML, taxiToGatePlanWptNode, wptID, waypoints.get(w).getLat(), waypoints.get(w).getLon(), timeOffset, speed, comment);
                wptID++;
            }

            // Add Events
            wptID = 1;
            for(int e = 0; e < events.size(); e++){
                // Add waypoint
                timeOffset = Integer.toString(events.get(e).id);
                addWaypoint(flightPlanXML, taxiToGatePlanEventNode, wptID, events.get(e).getLat(), events.get(e).getLon(), timeOffset, "0", "");
                wptID++;
            }

             ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
             // 06 - Write data to xml files
             ///////////////////////////////////////////////////////////////////////////////////////////////////////////////
             TransformerFactory transformerFactory = TransformerFactory.newInstance();
             Transformer transformer = transformerFactory.newTransformer();
             transformer.setOutputProperty(OutputKeys.INDENT, "yes");
             transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

             // Write data to flightPlan.xml
             DOMSource source = new DOMSource(flightPlanXML);
             StreamResult result = new StreamResult(flightPlanOut);
             transformer.transform(source, result);

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * This method loads and assigns all of the config variables stored in the config .xml file.
     *
     * @param filePath  Path and file name of local config .xml file
     *
     */
    public static void importKmlFile(List<Waypoint> waypoints, List<Waypoint> events, String filePath) {
        try {
            Waypoint waypoint;
            Node coordinates;
            NodeList holdPoint;
            List<Node> placeMarks;

            // Parse .kml file
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document inputKML = dBuilder.parse(new File(filePath));

            // Normalize
            inputKML.getDocumentElement().normalize();

            try {
                // Get coordinates: Simple file structure
                coordinates = findNode(findNode(findNode(findNode(inputKML.getDocumentElement().getChildNodes(), "Document").getChildNodes(), "Placemark").getChildNodes(), "LineString").getChildNodes(), "coordinates");
            } catch (DOMUtilException e){
                // Get coordinates and events: Complex file structure
                placeMarks = getChildElementsByTagName(findNode(findNode(inputKML.getDocumentElement().getChildNodes(), "Document").getChildNodes(),"Folder"),"Placemark");

                // Get coordinates
                coordinates = findNode(findNode(placeMarks.get(0).getChildNodes(),"LineString").getChildNodes(), "coordinates");

                // Get hold points
                for(int i = 1; i < placeMarks.size(); i++){

                    String[] kmlWaypointItems = getElementValue(findNode(findNode(placeMarks.get(i).getChildNodes(),"Point").getChildNodes(), "coordinates")).split(",");

                    waypoint = new Waypoint(parseInt(getElementValue(findNode(placeMarks.get(i).getChildNodes(),"name"))));
                    waypoint.setLon(Double.parseDouble(kmlWaypointItems[0]));
                    waypoint.setLat(Double.parseDouble(kmlWaypointItems[1]));
                    events.add(waypoint);

                }
            }

            // Loop through list off coordinates and assign waypoints to list of waypoints
            String[] kmlWaypointList = getElementValue(coordinates).split(" ");
            for (String kmlWaypoint : kmlWaypointList) {

                String[] kmlWaypointItems = kmlWaypoint.split(",");
                waypoint = new Waypoint();
                waypoint.setLon(Double.parseDouble(kmlWaypointItems[0]));
                waypoint.setLat(Double.parseDouble(kmlWaypointItems[1]));
                waypoints.add(waypoint);
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }


    /**
     * This method creates and adds a waypoint element including the corresponding parameters to the defined root node,
     * as per the .xml schema definition
     *
     * @param doc           Document .xml output file
     * @param planNode      Plan root node of .xml output file
     * @param wptId         Waypoint ID
     * @param wptLat        Waypoint latitude coordinates
     * @param wptLon        Waypoint longitude coordinates
     * @param timeOffset    Wait time at waypoint
     * @param speed         Speed at waypoint in knots
     * @param comment       Comment to be entered before waypoint node in .xml (not mandatory)
     */
    public static void addWaypoint(Document doc, Element planNode, int wptId, double wptLat, double wptLon, String timeOffset, String speed, String comment){
        try{
            // Add waypoint node and attributes
            Element waypoint = createChildElement(planNode,"Waypoint");
            Attr waypointID = createAttribute(waypoint, "ID");
            waypointID.setValue(String.valueOf(wptId));

            Attr waypointOffset = createAttribute(waypoint, "timeOffset");
            waypointOffset.setValue(timeOffset);

            Attr waypointSpeed = createAttribute(waypoint, "spd");
            waypointSpeed.setValue(speed);

            // Add latitude and longitude coordinates
            Element latitude = createChildElement(waypoint,"Latitude");
            Attr latUnit = createAttribute(latitude, "unit");
            latUnit.setValue("dec");
            setElementValue(latitude, String.format(Locale.US, "%.6f", wptLat));

            Element longitude = createChildElement(waypoint,"Longitude");
            Attr longUnit = createAttribute(longitude, "unit");
            longUnit.setValue("dec");
            setElementValue(longitude, String.format(Locale.US, "%.6f", wptLon));

            // Add comment
            if(comment != ""){
                Comment commentXML = doc.createComment(comment);
                waypoint.getParentNode().insertBefore(commentXML, waypoint);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}
