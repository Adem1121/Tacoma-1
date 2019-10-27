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
import static java.lang.Math.*;

/**
 * The Velocity Plan class represents the planned velocity along the flight plan as per the change airspeed events,
 * defined in the external xml file. The class is on of the three key components of the flight plan class.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VerticalPlan implements Vertical{
    public int id;
    public boolean dataValid = false;
    protected Lateral lateral;
    protected Velocity velocity;
    private List<EventChgAltitude> events;
    protected List<VerticalSegment> verticalSegments;

    /**
     * Constructor(s)
     */
    VerticalPlan(){
        this.events = new ArrayList<EventChgAltitude>();
        this.verticalSegments = new ArrayList<VerticalSegment>();
    }

    VerticalPlan(int planID){
        this();
        this.id = planID;
    }

    /**
     * This method assigns a lateral plan/track to the vertical plan.
     *
     * @param latPlan   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral latPlan){
        this.lateral = latPlan;
    }

    /**
     * This method assigns a velocity plan/track to the vertical plan.
     *
     * @param velPlan   Reference to a lateral plan/track.
     */
    public void assignVel(Velocity velPlan){
        this.velocity = velPlan;
    }

    /**
     * This method adds a vertical segment to the vertical plan's segment list
     *
     * @param sgmt Vertical segment segment to be added.
     */
    public void addSgmt(VerticalSegment sgmt){
        this.verticalSegments.add(sgmt);
    }

    /**
     * This method returns the vertical plan'segment of a specific segment number
     *
     * @param sgmtNum Number of the vertical segment, starts with 0
     * @return The vertical segment
     */
    public VerticalSegment getSgmt(int sgmtNum){
        return ListUtil.getListItem(sgmtNum, this.verticalSegments);
    }

    /**
     * This method finds and returns the corresponding vertical segment of a waypoint positioned
     * on the lateral plan/track, i.e. plan/track error must be zero.
     *
     * @param wpt   Waypoint
     * @return      Corresponding vertical segment of the waypoint
     */
    public VerticalSegment getWptSgmt(Waypoint wpt){
        try{
            // loop through each direct segment and calculate track error
            int sgmt, i = 0;
            double startDist,endDist,smlst;
            double[] planError = new double[this.verticalSegments.size()];

            for(VerticalSegment vertSgmt : this.verticalSegments){
                //startDist = abs(lateral.getDist(vertSgmt.getStartPt(),wpt));
                //endDist = abs(lateral.getDist(wpt,vertSgmt.getEndPt()));
                startDist = lateral.getDist(vertSgmt.getStartPt(), wpt);
                endDist = lateral.getDist(wpt,vertSgmt.getEndPt());

                if(startDist < 0 || (endDist <= 0 && i < this.verticalSegments.size() - 1))
                    planError[i] = 9999;
                else
                    planError[i] = (startDist + endDist) / vertSgmt.getDist();

                i++;
            }

            // find index of segment with smallest track error
            smlst = planError[0];
            sgmt = 0;
            for(i = 0; i < planError.length; i++) {
                if(smlst > planError[i]) {
                    smlst = planError[i];
                    sgmt = i;
                }
            }
            // return segment
            return this.verticalSegments.get(sgmt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the position of a specific vertical segment in a list of segments
     *
     * @param sgmt      The vertical segment
     * @return          The position of the vertial segment in the list, index starts at 0
     */
    public int getSgmtPos(VerticalSegment sgmt){
        return ListUtil.getListItemNum(sgmt, verticalSegments);
    }

    /**
     * This method returns the total number of segments in the list of vertical segments
     *
     * @return The total number of vertical segments
     */
    public int getSgmtCount(){
        return this.verticalSegments.size();
    }

    /**
     * This method returns the object's altitude at a specific waypoint along the vertical plan/track.
     *
     * @param wpt   Waypoint along the vertical segment's plan/track
     * @return      Altitude at waypoint position in meters
     */
    public double getAltAtWpt(Waypoint wpt){
        try{
            VerticalSegment vertSgmt = this.getWptSgmt(wpt);
            return vertSgmt.getAlt(this.lateral.getDist(vertSgmt.getStartPt(), wpt));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }


    /**
     * This method returns the vertical segment's alpha at a specific waypoint along the vertical plan/track.
     *
     * @param wpt   Waypoint along the vertical segment's plan/track
     * @return      Airspeed at waypoint position
     */
    public double getAlphaAtWpt(Waypoint wpt){
        try{
            double alpha;

            VerticalSegment vertSgmt = this.getWptSgmt(wpt);
            alpha = vertSgmt.getAlpha();

            // Catch negative alphas
           // if(toDegrees(alpha) <= -1.5)
           //     alpha = toRadians(-1.5);

            return alpha;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method loads the vertical events from an external xml file, creating and adding corresponding
     * Change Altitude events to the vertical plan
     *
     * @param fileName  The complete path and file name of the external event collection xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            Waypoint waypoint;
            EventChgAltitude chgAltitude;

            if(fileName != null && !fileName.isEmpty()){
                // create vertical plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document eventCollectionXML = dBuilder.parse(new File(fileName));

                // normalize
                eventCollectionXML.getDocumentElement().normalize();

                // Get flight plan and loop through corresponding change altitude events
                Node plan = findNode(eventCollectionXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID));
                NodeList xmlEvents = plan.getChildNodes();

                for (int n = 0; n < xmlEvents.getLength(); n++) {
                    Node node = xmlEvents.item(n);
                    if (node.getNodeName().equalsIgnoreCase("Event")) {
                        //check for attribute name and value
                        if (getAttributeValue(node, "type").equalsIgnoreCase("chgAltitude")) {
                            chgAltitude = new EventChgAltitude();
                            chgAltitude.assignLat(lateral);
                            chgAltitude.assignVel(velocity);

                            // Get Waypoints
                            Node waypoints = findNode(node.getChildNodes(),"Waypoints");
                            List<Node> wptList = getChildElementsByTagName(waypoints, "Waypoint");

                            for(Node wptNode : wptList){
                                // get reference to new waypoint and set latitude and longitude coordinates
                                waypoint = new Waypoint((Integer.parseInt(getAttributeValue(wptNode,"ID"))));

                                waypoint.setLat(Double.parseDouble(
                                        getElementValue(
                                                findNode(wptNode.getChildNodes(), "Latitude", "unit", "dec"))));

                                waypoint.setLon(Double.parseDouble(
                                        getElementValue(
                                                findNode(wptNode.getChildNodes(), "Longitude", "unit", "dec"))));

                                if(waypoint.id == 1)
                                    chgAltitude.setStartPt(waypoint.getLat(),waypoint.getLon());
                                else if(waypoint.id ==2)
                                    chgAltitude.setEndPt(waypoint.getLat(), waypoint.getLon());
                            }

                            // Get Variables
                            Node variables = findNode(node.getChildNodes(),"Variables");

                            chgAltitude.setAlti(Double.parseDouble(
                                    getElementValue(
                                            findNode(variables.getChildNodes(), "Var", "type", "init"))));

                            chgAltitude.setAltf(Double.parseDouble(
                                    getElementValue(
                                            findNode(variables.getChildNodes(), "Var", "type", "target"))));

                            chgAltitude.setVs(Double.parseDouble(
                                    getElementValue(
                                            findNode(variables.getChildNodes(), "Var", "type", "vs"))));

                            // Validate event and add to velocity plan
                            chgAltitude.validate();
                            this.events.add(chgAltitude);
                        }
                    }
                }

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the set of change altitude events into a consecutive set of vertical segments.
     */
    public void transform(){
        VerticalSegment vertSgmt,itmSgmt;
        int s = 0, i = 1;

        // Loop through change altitude events and create consecutive set of vertical segments
        for(EventChgAltitude event : this.events){
            // Transform change airspeed events
            vertSgmt = new VerticalSegment();
            vertSgmt.setStartPt(event.getStartPt().getLat(), event.getStartPt().getLon());
            vertSgmt.setEndPt(event.getEndPt().getLat(), event.getEndPt().getLon());
            vertSgmt.setDist(lateral.getDist(vertSgmt.getStartPt(), vertSgmt.getEndPt()));
            vertSgmt.setAlti(event.getAlti());
            vertSgmt.setAltf(event.getAltf());
            vertSgmt.setVs(event.getVs());
            vertSgmt.setAlpha(event.getAlpha());
            this.verticalSegments.add(vertSgmt);

            // Build intermediate segment
            if(s < (this.events.size() - 1) && lateral.getDist(this.events.get(s).getEndPt(),this.events.get(s +1).getStartPt()) > 0){
                itmSgmt = new VerticalSegment();
                itmSgmt.setStartPt(this.events.get(s).getEndPt().getLat(), this.events.get(s).getEndPt().getLon());
                itmSgmt.setEndPt(this.events.get(s + 1).getStartPt().getLat(), this.events.get(s + 1).getStartPt().getLon());
                itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                itmSgmt.setAlti(this.events.get(s).getAltf());
                itmSgmt.setAltf(this.events.get(s + 1).getAlti());
                itmSgmt.setVs(0);
                itmSgmt.setAlpha(0);
                this.verticalSegments.add(itmSgmt);
            }
            s++;
        }

        // Check for gap between start of lateral plan/track and first vertical segment
        if(lateral.getDist(lateral.getStartWpt(),this.verticalSegments.get(0).getStartPt()) > 0){
            itmSgmt = new VerticalSegment();
            itmSgmt.setStartPt(lateral.getStartWpt().getLat(),lateral.getStartWpt().getLon());
            itmSgmt.setEndPt(this.verticalSegments.get(0).getStartPt().getLat(), this.verticalSegments.get(0).getStartPt().getLon());
            itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
            itmSgmt.setAlti(this.verticalSegments.get(0).getAlti());
            itmSgmt.setAltf(this.verticalSegments.get(0).getAlti());
            itmSgmt.setVs(0);
            itmSgmt.setAlpha(0);
            this.verticalSegments.add(0,itmSgmt);
        }

        // Check for gap between final vertical segment and end of lateral plan/track
        s = this.verticalSegments.size() - 1;
        if(lateral.getDist(this.verticalSegments.get(s).getEndPt(),lateral.getEndWpt()) > 0){
            itmSgmt = new VerticalSegment();
            itmSgmt.setStartPt(this.verticalSegments.get(s).getEndPt().getLat(), this.verticalSegments.get(s).getEndPt().getLon());
            itmSgmt.setEndPt(lateral.getEndWpt().getLat(),lateral.getEndWpt().getLon());
            itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
            itmSgmt.setAlti(this.verticalSegments.get(s).getAltf());
            itmSgmt.setAltf(this.verticalSegments.get(s).getAltf());
            itmSgmt.setVs(0);
            itmSgmt.setAlpha(0);
            this.verticalSegments.add(itmSgmt);
        }

        // Set ids of vertical segments
        for(VerticalSegment sgmt : this.verticalSegments) {
            sgmt.id = i++;
        }
    }

    /*
     * This method validates the integrity of the vertical segment set. In the case of an overlap between two
     * separate segments, the method aims to find a suitable transition point. If the method can't find a transition
     * point the data set is marked as invalid.
     */
    public void validate(){
        try{
            double prevAlt,prevAlpha,nextAlt,nextAlpha,altTrn;
            Waypoint refWpt;
            VerticalSegment prevSgmt, thisSgmt, nextSgmt;
            dataValid = true;

            for(int s = 0; s < this.verticalSegments.size() - 1; s++){
                // Check whether connecting segment between two vertical segments runs in opposite direction of lateral plan
                if(this.verticalSegments.get(s).getDist() < 0){
                    dataValid = false;

                    prevSgmt = this.verticalSegments.get(s - 1);
                    thisSgmt = this.verticalSegments.get(s);
                    nextSgmt = this.verticalSegments.get(s + 1);

                    // Find entry and exit altitudes of segment overlap, i.e. start of next segment and end of previous segment
                    prevAlt = prevSgmt.getAlt(lateral.getDist(prevSgmt.getStartPt(), nextSgmt.getStartPt()));
                    prevAlpha = prevSgmt.getAlpha();

                    nextAlt = nextSgmt.getAlt(lateral.getDist(nextSgmt.getStartPt(), prevSgmt.getEndPt()));
                    nextAlpha = nextSgmt.getAlpha();

                    // Check if transition point can be found
                    if (nextAlt < prevSgmt.getAltf()) {

                        // Find transition altitude
                        altTrn = ((nextAlt * tan(prevAlpha) * tan(nextAlpha))-(prevAlt * tan(nextAlpha))/
                                (tan(prevAlpha) * tan(nextAlpha) - tan(nextAlpha)));

                        // Find transition waypoint
                        refWpt = lateral.getItmWpt(prevSgmt.getStartPt(), altTrn/tan(prevAlpha));

                        prevSgmt.setEndPt(refWpt.getLat(), refWpt.getLon());
                        prevSgmt.setAltf(altTrn);

                        nextSgmt.setStartPt(refWpt.getLat(), refWpt.getLon());
                        nextSgmt.setAlti(altTrn);

                        // Remove connecting segment and update ids
                        verticalSegments.remove(thisSgmt);

                        for (int i = 0; i < this.verticalSegments.size(); i++)
                            this.verticalSegments.get(i).id = i + 1;

                        dataValid = true;
                    } else {
                        System.out.println("Vertical Plan Validation Error! End Segment 1: " + prevSgmt.getEndPt().getLat() + "/" + prevSgmt.getEndPt().getLon() + " Segment 2: " + nextSgmt.getStartPt().getLat() + "/" + nextSgmt.getStartPt().getLon());
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}
