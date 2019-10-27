package ftdis.fdpu;

import org.w3c.dom.*;
import javax.xml.parsers.*;
import java.io.File;
import java.io.Serializable;
import java.util.*;
import static java.lang.Math.*;
import static ftdis.fdpu.DOMUtil.*;

/**
 * The Lateral Plan class represents the the lateral flight plan that is defined in an xml file.
 * The class is the foundation for creating the Lateral Track and the enrichment and validation processes
 * of the Event Collection.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class LateralPlan implements Lateral, Serializable {
    public Integer id;
    public boolean dataValid = false;
    private List<Waypoint> waypoints;
    private List<DirectSegment> directSegments;

    /**
     * Constructors
     */
    public LateralPlan(){
        this.waypoints = new ArrayList<Waypoint>();
        this.directSegments = new ArrayList<DirectSegment>();
    }

    public LateralPlan(Integer flightPlanID){
        this();
        this.id = flightPlanID;
    }

    /**
     * This method adds a waypoint to the flight plan's waypoint list
     *
     * @param wpt Waypoint to be added.
     */
    public void addWpt(Waypoint wpt){
        this.waypoints.add(wpt);
    }

    public void addWpt(int position, Waypoint wpt){this.waypoints.add(position,wpt);}

    /**
     * This method removes a waypoint to the flight plan's waypoint list
     *
     * @param no Index number of Waypoint to be removed.
     */
    public void delWpt(int no){
        this.waypoints.remove(no);
    }

    public void delWpt(Waypoint wpt){
        this.waypoints.remove(wpt);
    }

    /**
     * This method returns the flight plan's waypoint of a specific waypoint number
     *
     * @param wptNum Number of the waypoint, starts with 0
     * @return The waypoint
     */
    public Waypoint getWpt(int wptNum){
        return this.waypoints.get(wptNum);
    }

    /**
     * @param wpt The wpt
     * @return The number of the wpt, index starts with 0
     */
    public int getWptPos(Waypoint wpt){
        return ListUtil.getListItemNum(wpt, waypoints);
    }

    /**
     * @return The total number of waypoints
     */
    public int getWptSize(){
        return this.waypoints.size();
    }

    /**
     * This method adds a direct segment to the flight plan's direct segment list
     *
     * @param sgmt DirectSegment segment to be added.
     */
    public void addSgmt(LateralSegment sgmt){
        this.directSegments.add((DirectSegment) sgmt);
    }

    /**
     * This method returns the flight plan's direct segment of a specific segment number
     *
     * @param sgmtNum Number of the direct segment, starts with 0
     * @return The direct segment
     */
    public LateralSegment getSgmt(int sgmtNum){
        return ListUtil.getListItem(sgmtNum, this.directSegments);
    }

    /**
     * @param sgmt The direct segment
     * @return The number of the direct segment, index starts with 0
     */
    public int getSgmtPos(LateralSegment sgmt){
        return ListUtil.getListItemNum(sgmt, directSegments);
    }

    /**
     * @return The total number of direct segments
     */
    public int getSgmtCount(){
        return this.directSegments.size();
    }

    /**
     * This method returns the total length of the flight plan, i.e. start of the first segment
     * until end of the last segment, in meters.
     *
     * @return The total length of the flight plan in meters
     */
    public double getLength(){
        return ListUtil.getListLength(this.directSegments);
    }

    /**
     * This method calculates and returns the distance between two waypoints on the lateral plan. A prerequisite
     * for the method is that the transformFlightPlan method has been performed successfully, i.e. that the
     * lateral plan contains a coherent set of direct segments.
     *
     * The order of the waypoints is considered, i.e. a movement in the direction of the lateral plan/track
     * returns a positive distance and a movement in the opposite direction will return a negative distance.
     *
     * @param wpt1  Start waypoint on the lateral plan.
     * @param wpt2  End waypoint on the lateral plan.
     * @return      The distance between the start and end waypoint along the lateral plan in meters.
     */
    public double getDist(Waypoint wpt1, Waypoint wpt2){
        try{
            DirectSegment strtSgmt, thisSgmt, endSgmt;
            boolean endPointReached = false;
            int s;
            double dist = 0.0;

            // find segments for start and end waypoints
            strtSgmt = (DirectSegment) getWptSgmt(wpt1);
            endSgmt = (DirectSegment) getWptSgmt(wpt2);
            s = getSgmtPos(strtSgmt);

            // in case start and end waypoint are in the same segment, get direct distance
            if(strtSgmt == endSgmt){
                dist = strtSgmt.getDist(wpt1, wpt2);
                endPointReached = true;
            }

            // in case start and end waypoints are in different segments, loop through each segment and accumulate distance
            while(!endPointReached && ListUtil.inBound(s,this.directSegments)){
                thisSgmt = this.directSegments.get(s);
                // get distance between w1 and start/end of start segment
                if(thisSgmt == strtSgmt){
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt)){
                        // moving forwards: Dist = w1 -> end of start segment
                        dist += thisSgmt.getDist(wpt1, thisSgmt.getEndPt());
                        s++;
                    } else {
                        // moving backwards: Dist = start of start segment <- w1
                        dist += thisSgmt.getDist(wpt1, thisSgmt.getStartPt());
                        s--;
                    }
                    // get distance between w2 and start/end of end segment
                }else if(thisSgmt == endSgmt){
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt))
                        // moving forwards: Dist = start of end segment -> w2
                        dist += thisSgmt.getDist(thisSgmt.getStartPt(), wpt2);
                    else
                        // moving backwards: Dist = w2 <- end of end segment
                        dist += thisSgmt.getDist(thisSgmt.getEndPt(), wpt2);
                    endPointReached = true;
                    // get distance of all segments inbetween start and end segments
                }else {
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt)){
                        // moving forwards
                        dist += thisSgmt.getLength();
                        s++;
                    }
                    else {
                        // moving backwards
                        dist -= thisSgmt.getLength();
                        s--;
                    }
                }
            }

            // Set non relevant/measurable distances to zero. Otherwise small deviations in calculations
            // can lead to unforeseen knock-on effects during further processing
            if(abs(dist) < 0.001)
                dist = 0;

            return dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the course of a waypoint along the flight plan. The position of a waypoint that
     * doesn't match the flight plan 1 to 1 is calibrated to the flight plan temporarily for the calculation
     * of the course.
     *
     * @param wpt Waypoint along the flight plan.
     * @return The course at the waypoint in degrees
     */
    public double getCourseAtWpt(Waypoint wpt){
        try{
            // find corresponding segment of waypoint
            LateralSegment wptSgm = getWptSgmt(wpt);
            // return course segment at wpt
            return wptSgm.getCourseAtWpt(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the initial start waypoint of the lateral plan/track
     *
     * @return  Start waypoint of the lateral plan/track
     */
    public Waypoint getStartWpt(){
        try{
            return this.directSegments.get(0).getStartPt();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the final end waypoint of the lateral plan/track
     *
     * @return  End waypoint of the lateral plan/track
     */
    public Waypoint getEndWpt(){
        try{
            return this.directSegments.get(this.getSgmtCount() - 1).getEndPt();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns an intermediate waypoint on a flight plan's route, based on the distance
     * from another waypoint positioned on the plan's route. Movement can be either forward or backwards. In
     * case the distance is positive the method assumes that the object is heading towards the end point. In case
     * the distance is negative it assume that the object is heading towards the start point of the flight plan.
     *
     * A prerequisite for the method is that that transformFlightPlan method has been performed successfully,
     * i.e. that the flight plan contains a coherent set of direct segments that represent the flight plan's route.
     *
     * @param wpt  Waypoint positioned on the direct segment's track
     * @param dist      Distance from the waypoint in meters
     * @return A waypoint positioned on the flight plan
     */
    public Waypoint getItmWpt(Waypoint wpt, double dist){
        try{
            Waypoint thisWpt;
            DirectSegment thisSgmt;
            boolean endPointReached = false;
            int s;

            // find segment number of start waypoint and set this waypoint to start waypoint
            s = ListUtil.getListItemNum(getWptSgmt(wpt), this.directSegments);
            thisWpt = wpt;

            // iterate through segments (forwards/backwards) and accumulate distance
            while(!endPointReached && ListUtil.inBound(s, this.directSegments)){
                thisSgmt = this.directSegments.get(s);

                if(dist >= 0){
                    // moving forwards: Check if distance between this  waypoint and end of this segment is larger than remaining distance
                    if(thisSgmt.getDist(thisWpt, thisSgmt.getEndPt()) >= dist){
                        // return intermediary waypoint on direct segment based on remaining distance from this waypoint
                        thisWpt = thisSgmt.getItmWpt(thisWpt, dist);
                        endPointReached = true;
                    }else{
                        // calculate remaining distance between this waypoint and end of segment and move to next segment
                        dist -= thisSgmt.getDist(thisWpt, thisSgmt.getEndPt());
                        thisWpt = thisSgmt.getEndPt();
                        s++;
                    }
                }else{
                    // moving backwards: Check if distance between intermediary waypoint and start of this segment is larger than remaining distance
                    if(abs(thisSgmt.getDist(thisWpt, thisSgmt.getStartPt())) >= abs(dist)){
                        // return intermediary waypoint on direct segment based on remaining distance from this waypoint
                        thisWpt = thisSgmt.getItmWpt(thisWpt, dist);
                        endPointReached = true;
                    }else{
                        // calculate remaining distance between this waypoint and start of segment and move to next segment
                        dist -= thisSgmt.getDist(thisWpt, thisSgmt.getStartPt());
                        thisWpt = thisSgmt.getStartPt();
                        s--;
                    }
                }
            }
            return thisWpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method calculates and returns the plan error between a random waypoint
     * and the lateral flight plan.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error, i.e. the variation between the direct distance
     *              of the segment and the distance via the waypoint, expressed in percent.
     */
    public double getWptError(Waypoint wpt){
        try{
             // find corresponding segment of waypoint and return error
            DirectSegment wptSgm = (DirectSegment) getWptSgmt(wpt);
            return wptSgm.getPlanError(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method realigns a waypoint that isn't positioned on the flight plan, i.e. waypoint error,
     * to the flight plan.
     *
     * @param wpt  Waypoint that isn't positioned on the plan
     */
    public void alignWpt(Waypoint wpt){
        try{
            // find corresponding segment of waypoint
            DirectSegment wptSgm = (DirectSegment) getWptSgmt(wpt);
            // calibrate waypoint
            wptSgm.alignToPlan(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method finds and returns the corresponding segment of a waypoint along the flight plan.
     * As per the definition, the corresponding segment of a waypoint is the segment which has
     * a track error of zero / the smallest track error among all segments.
     *
     * @param wpt Waypoint
     * @return Corresponding direct segment of the waypoint
     */
    public LateralSegment getWptSgmt(Waypoint wpt){
        try{
            // loop through each direct segment and calculate track error
            int sgmt;
            double smlst;
            double[] planError = new double[this.directSegments.size()];

            for(int i = 0; i < this.directSegments.size(); i++){
                planError[i] = this.directSegments.get(i).getPlanError(wpt);
            }

            // find index of segment with smallest track error
            smlst = planError[0];
            sgmt = 0;
            for(int i = 0; i < planError.length; i++) {
                if(smlst >= planError[i]) {
                    smlst = planError[i];
                    sgmt = i;
                }
            }
            // return segment
            return this.directSegments.get(sgmt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method loads an external flight plan xml file, creating and adding corresponding
     * Waypoint objects to the Lateral Plan class.
     *
     * @param fileName  The complete path and file name of the external flight plan xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            Waypoint waypoint;

            if(fileName != null && !fileName.isEmpty()){
                // create Lateral Plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document flightPlanXML = dBuilder.parse(new File(fileName));

                // normalize
                flightPlanXML.getDocumentElement().normalize();

                // get flight plan and loop through corresponding waypoints
                Node plan = findNode(findNode(flightPlanXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID)).getChildNodes(),"Waypoints");
                List<Node> waypoints = getChildElementsByTagName(plan, "Waypoint");

                 for(Node node : waypoints){
                     // get reference to new waypoint and set latitude and longitude coordinates
                     waypoint = new Waypoint((Integer.parseInt(getAttributeValue(node,"ID"))));

                     waypoint.setLat(Double.parseDouble(
                             getElementValue(
                                     findNode(node.getChildNodes(), "Latitude", "unit", "dec"))));

                     waypoint.setLon(Double.parseDouble(
                             getElementValue(
                                     findNode(node.getChildNodes(), "Longitude", "unit", "dec"))));

                     // add waypoint reference to flight plan
                     this.addWpt(waypoint);
                 }
                 // sort waypoint list by waypoint id
                 if(! this.waypoints.isEmpty())
                     Collections.sort(this.waypoints, new Waypoint());
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the list of waypoints added by the loadFlightPlan method into a list of
     * direct segments and clears the waypoints list accordingly. The list of direct segments forms the
     * foundation for the event processing further downstream.
     *
     */
    public void transform(){
        try{
            DirectSegment directSegment;

            // clear any existing direct segments
            this.directSegments.clear();

            // loop through list of waypoints and create a new DirectSegment for each waypoint pair
            for(int s = 1, w = 0; w < (this.waypoints.size() - 1);w++,s++){
                directSegment = new DirectSegment(s);

                directSegment.setStartPt(this.getWpt(w).getLat(), this.getWpt(w).getLon());

                directSegment.setEndPt(this.getWpt(w + 1).getLat(), this.getWpt(w + 1).getLon());

                // add direct segment to flight plan
                this.addSgmt(directSegment);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the completeness and integrity of the lateral plan
     */
    public void validate(){
        try{
            if(this.directSegments.size() == (this.waypoints.size() - 1))
                dataValid = true;
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}
