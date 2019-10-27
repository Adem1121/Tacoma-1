package ftdis.fdpu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.DOMUtil.getAttributeValue;
import static ftdis.fdpu.Config.*;

public class EventCollectionChgAirspeed extends EventCollection {

    private List<EventChgAirspeed> eventsSpeed = new ArrayList<EventChgAirspeed>();

    /**
     * This method adds a event to the event collection's event list
     *
     * @param event DirectSegment segment to be added.
     */
    public void addEvent(EventChgAirspeed event){
        this.eventsSpeed.add(event);
    }


    /**
     * This method returns the event collection's event of a specific event number
     *
     * @param eventNum Number of the direct segment, starts with 0
     * @return The direct segment
     */
    public EventChgAirspeed getEvent(int eventNum){
        return ListUtil.getListItem(eventNum,this.eventsSpeed);
    }


    /**
     * @return The total number of events in the event collection
     */
    public int getEventCount(){
        return this.eventsSpeed.size();
    }


    /**
     * This method loads an external flight plan xml file, creating and adding corresponding
     * holding point event objects to the Event Collection class.
     *
     * @param fileName  The complete path and file name of the external flight plan xml file
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            EventChgAirspeed event;

            if(fileName != null && !fileName.isEmpty()){
                // create Lateral Plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document flightPlanXML = dBuilder.parse(new File(fileName));

                // normalize
                flightPlanXML.getDocumentElement().normalize();

                try {
                    // get flight plan and loop through corresponding waypoints
                    Node plan = findNode(findNode(flightPlanXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID)).getChildNodes(), "Events");
                    List<Node> waypoints = getChildElementsByTagName(plan, "Waypoint");

                    for (Node node : waypoints) {
                        // get reference to new waypoint and set latitude and longitude coordinates
                        event = new EventChgAirspeed((Integer.parseInt(getAttributeValue(node, "ID"))));

                        event.setEndPt(Double.parseDouble(
                                getElementValue(
                                        findNode(node.getChildNodes(), "Latitude", "unit", "dec"))),
                                Double.parseDouble(
                                        getElementValue(
                                                findNode(node.getChildNodes(), "Longitude", "unit", "dec"))));

                        event.setOffset((Integer.parseInt(getAttributeValue(node, "timeOffset"))));

                        event.setvAsf(0);

                        event.setAcc(ACC_TAXI);

                        // add event to event collection
                        this.addEvent(event);
                    }
                    // sort waypoint list by waypoint id
                    //if(! this.events.isEmpty())
                    //    Collections.sort(events, new EventChgAirspeed());
                } catch(Exception e){
                    // NA
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}