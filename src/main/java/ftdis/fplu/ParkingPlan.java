package ftdis.fplu;

import ftdis.fdpu.ListUtil;
import ftdis.fdpu.ParkingSegment;
import ftdis.fdpu.Waypoint;
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
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static ftdis.fdpu.Config.AIRCRAFT_PARK_TIME;
import static ftdis.fdpu.DOMUtil.*;

/**
 * The ParkingPlan class processes FS Recorder data to retrieve aircraft parked and transforms the corresponding
 * data into parking plan .xml file that can be processed by the Flight Data Processing Unit
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingPlan {

    public int id;
    public boolean dataValid = false;
    private List<ParkingSegment> parkingSegments;

    /**
     * Constructor(s)
     */
    ParkingPlan(){
        this.id = 0;
        this.parkingSegments = new ArrayList<ParkingSegment>();
    }

    ParkingPlan(Integer planID){
        this();
        this.id = planID;
    }

    /**
     * This method adds a parking segment to the parkging segment  list
     *
     * @param parkSgmt  Change velocity event to be added.
     */
    public void addParkSgmt(ParkingSegment parkSgmt){
        this.parkingSegments.add(parkSgmt);
    }

    /**
     * This method returns a parking segment of a specific segment number
     *
     * @param parkSgmtNum   Number of the parking segment, starts with 0
     * @return The parking segment
     */
    public ParkingSegment getParkSegment(int parkSgmtNum){
        return ListUtil.getListItem(parkSgmtNum, this.parkingSegments);
    }

    /**
     * @param parkSgmt The parking segment
     * @return The number of the parking segment in the list, index starts with 0
     */
    public int getParkSegmtPos(ParkingSegment parkSgmt){
        return ListUtil.getListItemNum(parkSgmt, this.parkingSegments);
    }

    /**
     * @return The total number of parking segments
     */
    public int getParkSegmentCount(){
        return this.parkingSegments.size();
    }

    /**
     * This method loads an external FS Recorder text file, processes the file line by line to identify
     * aircraft parked at the gate/ramp and adds the data to parking plan segment list accordingly.
     *
     * @param fileName  The complete path and file name of the external FS Recorder text file.
     */
    private void load(String fileName){
        try{
            String thisLine, thisAircraftType = "";
            List<String> thisWordSet = new ArrayList<String>();
            String[] tmpWordSet;
            double thisStaticCG = 0, thisLat = 0, thisLon = 0, thisHdg = 0, thisAlt = 0;
            int thisTrackLn = 0, thisTrack = 0;

            // Open file
            FileReader input = new FileReader(fileName);
            BufferedReader bufRead = new BufferedReader(input);

            // Loop through file line by line
            while ( (thisLine = bufRead.readLine()) != null)
            {
                if(!thisLine.isEmpty()){
                    // Parse line and remove tabs
                    thisWordSet.clear();
                    tmpWordSet = thisLine.split(" ");

                    for(int i = 0; i < tmpWordSet.length; i++){
                        if(!tmpWordSet[i].isEmpty()){
                            thisWordSet.add(tmpWordSet[i]);
                        }
                    }

                    // Get data and add data to set
                    if(thisTrackLn == 2){

                        // Check for speed of aircraft, i.e. only extract parked/stationary aircraft from input data
                        if(Double.parseDouble(thisWordSet.get(7).trim()) == 1){

                            thisLat = Double.parseDouble(thisWordSet.get(1).trim());
                            thisLon = Double.parseDouble(thisWordSet.get(2).trim());
                            thisAlt = Double.parseDouble(thisWordSet.get(3).trim());
                            thisHdg = Double.parseDouble(thisWordSet.get(6).trim());

                            // Add parking segment
                            Waypoint newParkPos = new Waypoint();
                            newParkPos.setLat(thisLat);
                            newParkPos.setLon(thisLon);

                            ParkingSegment newParkSegment = new ParkingSegment();
                            newParkSegment.id = thisTrack;
                            newParkSegment.setType(thisAircraftType);
                            newParkSegment.setStaticCG(thisStaticCG);
                            newParkSegment.setPosition(newParkPos);
                            newParkSegment.setAltitude(thisAlt);
                            newParkSegment.setHeading(thisHdg);
                            newParkSegment.setParkingTime(AIRCRAFT_PARK_TIME);

                            this.parkingSegments.add(newParkSegment);
                        }
                        // Initialize vars for next track
                        thisTrackLn = 0;
                    }

                    // Check for new track and aircraft type
                    if (thisWordSet.get(0).equalsIgnoreCase("#Aircraft:") && thisTrackLn == 0){
                        thisTrack += 1;
                        thisTrackLn = 1;
                        thisAircraftType = thisLine.split("\"")[1];
                    }

                    // Check for static CG
                    if (thisWordSet.get(0).equalsIgnoreCase("#StaticCGtoGround:") && thisTrackLn > 0){
                        thisStaticCG = Double.parseDouble(thisWordSet.get(1).trim());
                    }

                    // Check for start of data section
                    if (thisWordSet.get(0).equalsIgnoreCase("#Data:") && thisTrackLn > 0){
                        thisTrackLn = 2;
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method exports the set of parking segments into a parking plan xml file that can be processed
     * further by the Flight Data Processing Unit.
     *
     */
    public void transform(String inputFileName){
        try{
            String ioDir;
            ParkingSegment parkSgmt;

            // Initialize document builder factory and create new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

            // Define output filename
            String fileName = Paths.get(inputFileName).getFileName().toString();
            fileName = fileName.split("\\.(?=[^\\.]+$)")[0] + ".xml";

            // Set output directories
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath();

            if (os.contains("Windows"))
                ioDir = "\\IO\\";
            else
                ioDir = "/IO/";

            File parkingPlanOut = new File(localDir + ioDir + fileName);

            // Create new document
            Document parkingPlanXML = docBuilder.newDocument();

            // Create parking plan root element, add ID
            Element parkingPlanNode = createChildElement(parkingPlanXML, "ParkingPlan");
            Attr parkingPlanId = createAttribute(parkingPlanNode, "ID");
            parkingPlanId.setValue("1");

            // Prepare and Validate Data Sets
            this.load(inputFileName);

            // Add parking segments
            for(int s = 0; s < parkingSegments.size(); s++){
                // Get parking segment and parameters
                parkSgmt = parkingSegments.get(s);

                // Add aircraft node and attributes
                Element aircraft = createChildElement(parkingPlanNode, "Aircraft");
                Attr aircraftID = createAttribute(aircraft, "ID");
                aircraftID.setValue(String.valueOf(s + 1));

                Attr aircraftType = createAttribute(aircraft, "type");
                aircraftType.setValue(parkSgmt.getType());

                Attr staticCG = createAttribute(aircraft, "staticCG");
                staticCG.setValue(String.format(Locale.US, "%.4f", parkSgmt.getStaticCG()));

                // Add latitude and longitude coordinates
                Element latitude = createChildElement(aircraft, "Latitude");
                Attr latUnit = createAttribute(latitude, "unit");
                latUnit.setValue("dec");
                setElementValue(latitude, String.format(Locale.US, "%.7f", parkSgmt.getPosition().getLat()));

                Element longitude = createChildElement(aircraft,"Longitude");
                Attr longUnit = createAttribute(longitude, "unit");
                longUnit.setValue("dec");
                setElementValue(longitude, String.format(Locale.US, "%.7f", parkSgmt.getPosition().getLon()));

                // Add heading
                Element heading = createChildElement(aircraft,"Heading");
                Attr hdgUnit = createAttribute(heading, "unit");
                hdgUnit.setValue("deg");
                setElementValue(heading, String.format(Locale.US, "%.3f", parkSgmt.getHeading()));

                // Add altitude
                Element altitude = createChildElement(aircraft,"Altitude");
                Attr altUnit = createAttribute(altitude, "unit");
                altUnit.setValue("ft");
                setElementValue(altitude, String.format(Locale.US, "%.3f", parkSgmt.getAltitude()));

                // Add timer
                Element timer = createChildElement(aircraft,"Timer");
                Attr timerUnit = createAttribute(timer, "unit");
                timerUnit.setValue("s");
                setElementValue(timer, String.format(Locale.US, "%.3f", parkSgmt.getParkingTime()));
            }

            // Write data to xml file
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

            DOMSource source = new DOMSource(parkingPlanXML);
            StreamResult result = new StreamResult(parkingPlanOut);
            transformer.transform(source, result);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}
