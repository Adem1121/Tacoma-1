package ftdis.fdpu;

import org.junit.*;
import org.junit.Test;
import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.*;
//import sun.misc.Perf;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.junit.Assert.assertTrue;
import static ftdis.fdpu.DOMUtil.*;
/**
 * Process Flight Test Data
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ExportKMLTest {
    LateralPlan latPlan = new LateralPlan();
    LateralTrack latTrack = new LateralTrack();

    VelocityPlan velPlan = new VelocityPlan();
    VelocityTrack velTrack = new VelocityTrack();

    VerticalPlan vertPlan = new VerticalPlan();
    VerticalTrack vertTrack = new VerticalTrack();

    WeatherPlan wxPlan = new WeatherPlan();
    WeatherTrack wxTrack = new WeatherTrack();

    AircraftAxis arcrftAxis = new AircraftAxis();
    AircraftControl arcrftCtrl = new AircraftControl();

    @Before
    public void setUp() throws Exception {
        int planID = 3;
        String flightPlanFile, eventCollectionFile;

        flightPlanFile = "/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/KEWR KTPA FlightPlan 20151025 0041.xml";
        eventCollectionFile = "/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/KEWR KTPA EventCollection 20151025 0041.xml";


        // 01 Load flight plan and transform to lateral plan
        latPlan.load(flightPlanFile,planID);
        latPlan.transform();
        latPlan.validate();
        System.out.println("Lateral Plan data: " + latPlan.dataValid);

        // 02 Load change velocity events and transform to velocity plan
        velPlan.assignLat(latPlan);
        velPlan.load(eventCollectionFile,planID);
        velPlan.transform();
        velPlan.validate();
        System.out.println("Velocity Plan data: " + velPlan.dataValid);

        // 03 Load change altitude events and transform to vertical plan
        vertPlan.assignLat(latPlan);
        vertPlan.assignVel(velPlan);
        vertPlan.load(eventCollectionFile,planID);
        vertPlan.transform();
        vertPlan.validate();
        System.out.println("Vertical Plan data: " + vertPlan.dataValid);

        // 04 Load change weather events and transform to weather plan
        wxPlan.assignLat(latPlan);
        wxPlan.assignVel(velPlan);
        wxPlan.load(eventCollectionFile,planID);
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
    }

    @Test
    public void testExportToKML() throws Exception{
        Waypoint refWpt;
        String flightPlan = "", flightTrack = "", latitude, longitude, altitude;
        double cycleLn = 1, timeStmp = 0, planLn, trackLn;

        /////////////////////////////////////////////////////////////////////////////
        // Header
        ////////////////////////////////////////////////////////////////////////////

        // Prep date format
        Date date = new Date() ;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm") ;

        // Configure filename and file output stream
        String fileName = "F11 FlightPlan " + dateFormat.format(date) + ".kml";
        File fout = new File("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/" + fileName);

        // Initialize document builder factory and create new document
        DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
        Document kmlDoc = docBuilder.newDocument();

        // Create KML document root element
        Element kml = createChildElement(kmlDoc, "kml");
        kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");

        Element doc = createChildElement(kml,"Document");
        Element docName = createChildElement(doc,"name");
        setElementValue(docName, fileName);


        /////////////////////////////////////////////////////////////////////////////
        // FLight Plan
        ////////////////////////////////////////////////////////////////////////////

        // Set Flight Plan style
        Element planStyle = createChildElement(doc,"Style");
        planStyle.setAttribute("id","linePoly");
        
        Element planLineStyle = createChildElement(planStyle,"LineStyle");

        Element planLineColor = createChildElement(planLineStyle,"color");
        setElementValue(planLineColor,"55143EFF");

        Element planLineWidth = createChildElement(planLineStyle,"width");
        setElementValue(planLineWidth, "4");

        Element planPolyStyle = createChildElement(planLineStyle, "PolyStyle");

        Element planPolyColor = createChildElement(planPolyStyle,"color");
        setElementValue(planPolyColor, "55146AFF");

        // Process Data
        planLn = latPlan.getDist(latPlan.getStartWpt(),latPlan.getEndWpt());

        for(double planDist = 0; (planLn - planDist) > 0.1; timeStmp += cycleLn){

            // Flight track
            planDist = velPlan.getDist(latPlan.getStartWpt(),timeStmp);
            refWpt = latPlan.getItmWpt(latPlan.getStartWpt(), planDist);

            latitude = String.format(Locale.US, "%.6f", refWpt.getLat());
            longitude = String.format(Locale.US, "%.6f",refWpt.getLon());
            altitude = String.format(Locale.US, "%.2f", vertTrack.getAltAtWpt(refWpt));

            flightPlan  += longitude + "," + latitude + "," + altitude + " ";

            System.out.println("Export Flight Plan - Processing cycle " + timeStmp + "    Remaining / Total Dist: " + planDist + " / " + planLn);
        }

        timeStmp = 0;
        
        // Set Flight Plan elements
        Element planMark = createChildElement(doc,"Placemark");

        Element planName = createChildElement(planMark, "name");
        setElementValue(planName,"Flight Plan");

        Element planStyleUrl = createChildElement(planMark, "styleUrl");
        setElementValue(planStyleUrl, "#linePoly");

        // Set linestring configuration elements
        Element planLineString = createChildElement(planMark,"LineString");

        Element planExtrude = createChildElement(planLineString,"extrude");
        setElementValue(planExtrude, "1");

        Element planTessellate = createChildElement(planLineString,"tessellate");
        setElementValue(planTessellate,"1");

        Element planAltMode = createChildElement(planLineString,"altitudeMode");
        setElementValue(planAltMode,"absolute");

        Element planCoordinates = createChildElement(planLineString,"coordinates");
        setElementValue(planCoordinates,flightPlan);

        
        /////////////////////////////////////////////////////////////////////////////
        // FLight Track
        ////////////////////////////////////////////////////////////////////////////
        
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

        // Process Data
        trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());

        for(double trackDist = 0; (trackLn - trackDist) > 0.1; timeStmp += cycleLn){

            // Flight track
            trackDist = velTrack.getDist(latTrack.getStartWpt(),timeStmp);
            refWpt = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);

            latitude = String.format(Locale.US, "%.6f", refWpt.getLat());
            longitude = String.format(Locale.US, "%.6f",refWpt.getLon());
            altitude = String.format(Locale.US, "%.2f", vertTrack.getAltAtWpt(refWpt));

            flightTrack  += longitude + "," + latitude + "," + altitude + " ";

            System.out.println("Export Flight Track- Processing cycle " + timeStmp + "    Remaining / Total Dist: " + trackDist + " / " + trackLn);
        }

        // Set Flight Track elements
        Element trackMark = createChildElement(doc,"Placemark");

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
        // Write document to kml file
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
    public void dataValidation() throws Exception{
        Waypoint pos, prevPos;
        double cycleLn = 1, timeStmp = 0, trackLn;

        // Process Data
        prevPos = latTrack.getStartWpt();
        trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());

        for(double trackDist = 0; trackDist < trackLn; timeStmp += cycleLn){

            // Flight track
            trackDist = velTrack.getDist(latTrack.getStartWpt(),timeStmp);

            // Check if end of track has been reached
            if((trackLn - trackDist) < 0.001) {
                trackDist = trackLn;
                // Ajdust time stamp to match time required to reach end of track
                timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos,latTrack.getEndWpt()) / velTrack.getVasu(prevPos,latTrack.getEndWpt());
            }

            // Get lateral position
            pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
            prevPos = pos;

            System.out.println("Time: " + timeStmp + " Bank Angle: " + arcrftAxis.getBankAngleAtWpt(pos) +"  Altitude: " + String.format(Locale.US, "%.2f", vertTrack.getAltAtWpt(pos)));
        }
    }

}
