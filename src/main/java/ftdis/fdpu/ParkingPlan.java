package ftdis.fdpu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ftdis.fdpu.DOMUtil.*;

/**
 * The Parking Plan class represents aircraft parked at the gate, as defined in the external xml file.
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
        this.parkingSegments = new ArrayList<ParkingSegment>();
    }

    ParkingPlan(int planID){
        this();
        this.id = planID;
    }

    /**
     * This method adds a parking segment to the parking plan's segment list
     *
     * @param sgmt Parking segment segment to be added.
     */
    public void addSgmt(ParkingSegment sgmt){
        this.parkingSegments.add(sgmt);
    }

    /**
     * This method returns the parking plan'segment of a specific segment number
     *
     * @param sgmtNum Number of the parking segment, starts with 0
     * @return The parking segment
     */
    public ParkingSegment getSgmt(int sgmtNum){
        return ListUtil.getListItem(sgmtNum, this.parkingSegments);
    }

    /**
     * This method returns the total number of segments in the list of parking segments
     *
     * @return The total number of parking segments
     */
    public int getSgmtCount(){
        return this.parkingSegments.size();
    }

    /**
     * This method loads the parking from an external xml file, creating and adding corresponding
     * parking segments to the parking plan
     *
     * @param fileName    The complete path and file name of the parking plan xml file.
     */
    public void load(String fileName){
        try{
            Waypoint parkingPos;
            ParkingSegment parkingSgmt;

            if(fileName != null && !fileName.isEmpty()){
                // Create Parking Plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document parkingPlanXML = dBuilder.parse(new File(fileName));

                // Normalize
                parkingPlanXML.getDocumentElement().normalize();

                // loop through collection and find change airspeed events
                NodeList xmlEvents = parkingPlanXML.getDocumentElement().getChildNodes();

                for (int n = 0; n < xmlEvents.getLength(); n++) {
                    Node node = xmlEvents.item(n);
                    if (node.getNodeName().equalsIgnoreCase("Aircraft")) {
                        // Set aircraft type
                        parkingSgmt = new ParkingSegment();
                        parkingSgmt.setType(getAttributeValue(node, "type"));
                        parkingSgmt.setStaticCG(Double.parseDouble(getAttributeValue(node, "staticCG")));

                        // Set parking position
                        parkingPos = new Waypoint();
                        parkingPos.setLat(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Latitude", "unit", "dec"))));
                        parkingPos.setLon(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Longitude", "unit", "dec"))));

                        parkingSgmt.setPosition(parkingPos);

                        // Set variables
                        parkingSgmt.setHeading(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Heading", "unit", "deg"))));

                        parkingSgmt.setAltitude(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Altitude", "unit", "ft"))));

                        parkingSgmt.setParkingTime(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Timer", "unit", "s"))));

                        this.parkingSegments.add(parkingSgmt);
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
