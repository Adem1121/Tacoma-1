package ftdis.fdpu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ftdis.fdpu.Config.FLT_PROC_CYCLE_LN;
import static ftdis.fdpu.Config.SPD_TAXI;
import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.DOMUtil.findNode;
import static ftdis.fdpu.DOMUtil.getElementValue;
import static java.lang.Math.*;

/**
 * The Velocity Plan class represents the planned velocity along the flight plan as per the change airspeed events,
 * defined in the external xml file. The class is on of the three key components of the flight plan class.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocityPlan implements Velocity{
    public Integer id;
    public boolean dataValid = false;
    protected Lateral lateral;
    private List<EventChgAirspeed> events;
    protected List<VelocitySegment> velocitySegments;

    /**
     * Constructor(s)
     */
    VelocityPlan(){
        this.events = new ArrayList<EventChgAirspeed>();
        this.velocitySegments = new ArrayList<VelocitySegment>();
    }

    VelocityPlan(Integer planID){
        this();
        this.id = planID;
    }

    /**
     * This method assigns the Velocity Plan to a lateral plan.
     *
     * @param latPlan   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral latPlan){
        this.lateral = latPlan;
    }

    /**
     * This method adds a velocity segment to the velocity plan's segment list
     *
     * @param sgmt VelocitySegment segment to be added.
     */
    public void addSgmt(VelocitySegment sgmt){
        this.velocitySegments.add(sgmt);
    }

    /**
     * This method returns the velocity plan'segment of a specific segment number
     *
     * @param sgmtNum Number of the velocity segment, starts with 0
     * @return The velocity segment
     */
    public VelocitySegment getSgmt(int sgmtNum){
        return ListUtil.getListItem(sgmtNum, this.velocitySegments);
    }

    /**
     * This method finds and returns the corresponding segment of a waypoint. The waypoint must be
     * positioned on the lateral plan/track, i.e. plan/track error must be zero.
     *
     * @param wpt   Waypoint
     * @return      Corresponding segment of the waypoint
     */
    public VelocitySegment getWptSgmt(Waypoint wpt){
        try{
            // loop through each segment and calculate track error
            int sgmt, i = 0;
            double startDist,endDist,smlst;
            double[] planError = new double[this.velocitySegments.size()];

            for(VelocitySegment velSgmt : this.velocitySegments){
                startDist = lateral.getDist(velSgmt.getStartPt(), wpt);
                endDist = lateral.getDist(wpt, velSgmt.getEndPt());

                if(startDist < 0 || (endDist < 0 && i < this.velocitySegments.size() - 1))
                    planError[i] = 9999;
                else
                    planError[i] = (startDist + endDist) / velSgmt.getDist();

                i++;
            }

            // find index of segment with smallest track error
            smlst = planError[0];
            sgmt = 0;
            for(i = 0; i < planError.length; i++) {
                if(smlst >= planError[i]) {
                    smlst = planError[i];
                    sgmt = i;
                }
            }
            // return segment
            return this.velocitySegments.get(sgmt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the position of a specific segment in a list of segments
     *
     * @param sgmt      The segment
     * @return          The position of the segment in the list, index starts at 0
     */
    public int getSgmtPos(VelocitySegment sgmt){
        return ListUtil.getListItemNum(sgmt, velocitySegments);
    }

    /**
     * This method returns the total number of segments in the list of velocity segments
     *
     * @return The total number of direct segments
     */
    public int getSgmtCount(){
        return this.velocitySegments.size();
    }

    /**
     * This method returns the object's airspeed at a specific waypoint along the velocity plan/track
     * in meters per second.
     *
     * @param wpt   Waypoint along the velocity segment's plan/track
     * @return      Airspeed at waypoint position
     */
    public double getVasAtWpt(Waypoint wpt){
        try{

            // todo Adjust to acceleration curve?

            VelocitySegment velSgmt = this.getWptSgmt(wpt);
            return velSgmt.getVas(this.lateral.getDist(velSgmt.getStartPt(), wpt));
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method calculates and returns the total distance an object has traveled along
     * the flight plan in a given time
     *
     * @param wpt     Start waypoint on the flight plan/track.
     * @param time    Total time traveled in seconds
     * @return        The distance between the start and end waypoint in meters.
     */
    public double getDist(Waypoint wpt, double time){
        try{
            Waypoint startPt, endPt, cPt;
            VelocitySegment thisSgmt;
            double d, t, vAsi, vAsf, a, totalDist = 0.0, totalT = 0.0, cDist;
            boolean endPointReached = false;
            int s;

            // Find position of start segment
            s = this.getSgmtPos(this.getWptSgmt(wpt));

            // Loop through segments until time parameter has been reached
            while(!endPointReached && ListUtil.inBound(s,this.velocitySegments)) {
                thisSgmt = this.velocitySegments.get(s);

                // Get parameters for this segment
                if(time >= 0  && lateral.getDist(thisSgmt.getStartPt(),wpt) >=0){
                    vAsi = this.getVasAtWpt(wpt);
                    vAsf = thisSgmt.getVasf();
                    d = lateral.getDist(wpt, thisSgmt.getEndPt());
                }else if(time < 0 && lateral.getDist(wpt,thisSgmt.getEndPt()) >= 0){
                    vAsi = thisSgmt.getVasf();
                    vAsf = this.getVasAtWpt(wpt);
                    d = abs(lateral.getDist(thisSgmt.getEndPt(), wpt));
                } else{
                    vAsi = thisSgmt.getVasi();
                    vAsf = thisSgmt.getVasf();
                    d = lateral.getDist(thisSgmt.getStartPt(),thisSgmt.getEndPt());
                }

                // Calculate total time required to complete this segment
                a = thisSgmt.getAcc();

                if(a != 0)
                    t = abs((vAsf - vAsi)/a);
                else
                    t = d / vAsi;

                // Check if total time t , i.e. end point, has been reached
                  if((totalT + t) > abs(time)){
                    endPointReached = true;
                    t = abs(time) - totalT;
                    d = (vAsi * t) + ((a / 2) * pow(t,2));
                }else if(s == this.velocitySegments.size() - 1){
                    endPointReached = true;
                    d = (vAsi * t) + ((a / 2) * pow(t,2));
                }else if(time >= 0){
                    // If not continue with next segment...
                    s += 1;
                }else {
                    // ...or previous segment
                    s -= 1;
                }

                totalT += t;
                totalDist += d;
            }
               return totalDist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }


    /**
     * This method calculates the time required to cover the distance between two waypoints
     * that are positioned on the velocity/plan track.
     *
     * @param wpt1  1st waypoint
     * @param wpt2  2nd waypoint
     * @return      The time required to cover the distance between two waypoints in seconds
     */
    public double getTime(Waypoint wpt1, Waypoint wpt2){
        try{
            double wptDist, wptVasu;

            wptDist = lateral.getDist(wpt1, wpt2);

            wptVasu = getVasu(wpt1,wpt2);

            return wptDist/wptVasu;
        } catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the uniform airspeed between two waypoints along the velocity plan/track.
     *
     * @param wpt1  Start waypoint along the velocity plan/track
     * @param wpt2  End waypoint along the velocity plan/track;
     * @return      Uniform airspeed in m/s
     */
    public double getVasu(Waypoint wpt1, Waypoint wpt2){
        try{
            VelocitySegment strtSgmt, thisSgmt, endSgmt;
            double dist, t, vAsi, vAsf, a, totalDist = 0, totalT = 0;
            boolean endPointReached = false;
            int s;

            // Find segments for start and end waypoints
            strtSgmt = this.getWptSgmt(wpt1);
            endSgmt = this.getWptSgmt(wpt2);
            s = getSgmtPos(strtSgmt);

            // In case start and end waypoint are in the same segment, calc vAsu
            if(strtSgmt == endSgmt){
                dist = abs(this.lateral.getDist(wpt1, wpt2));
                vAsi = this.getVasAtWpt(wpt1);
                vAsf = this.getVasAtWpt(wpt2);

                if(vAsi != vAsf) {
                    t = (vAsf-vAsi)/strtSgmt.getAcc();
                    return dist/t;
                }else
                    return vAsi;
            }

            // In case start and end waypoints are in different segments, loop through each segment and accumulate distance
            while(!endPointReached && ListUtil.inBound(s,this.velocitySegments)){
                thisSgmt = this.velocitySegments.get(s);

                // Get distance and t between w1 and end of start segment
                if(thisSgmt == strtSgmt){
                    dist = lateral.getDist(wpt1, thisSgmt.getEndPt());
                    vAsi = this.getVasAtWpt(wpt1);
                    vAsf = thisSgmt.getVasf();

                    if(vAsi != vAsf)
                        t = (vAsf-vAsi)/strtSgmt.getAcc();
                    else
                        t = dist / vAsi;

                    totalDist += dist;
                    totalT += t;

                // Get distance and t between start of end segment and w2
                }else if(thisSgmt == endSgmt){
                    endPointReached = true;
                    dist = lateral.getDist(thisSgmt.getStartPt(), wpt2);
                    vAsi = thisSgmt.getVasi();
                    vAsf = this.getVasAtWpt(wpt2);

                    if(vAsi != vAsf)
                        t = (vAsf-vAsi)/thisSgmt.getAcc();
                    else
                        t = dist / vAsi;

                    totalDist += dist;
                    totalT += t;

                    // get distance and t of all segments inbetween start and end segments
                }else {
                    dist = thisSgmt.getDist();
                    vAsi = thisSgmt.getVasi();
                    vAsf = thisSgmt.getVasf();

                    if(vAsi != vAsf)
                        t = (vAsf-vAsi)/thisSgmt.getAcc();
                    else
                        t = dist / vAsi;

                    totalDist += dist;
                    totalT += t;
                }
                s++;
            }
              return totalDist/totalT;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method loads the velocity events from  an external xml file, creating and adding corresponding
     * Change Airspeed events to the velocity plan
     *
     * @param fileName    The complete path and file name of the external flight plan xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            Waypoint waypoint;
            EventChgAirspeed chgAirspeed;

            if(fileName != null && !fileName.isEmpty()){
                // create Velocity Plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document eventCollectionXML = dBuilder.parse(new File(fileName));

                // normalize
                eventCollectionXML.getDocumentElement().normalize();

                // Get flight plan and loop through corresponding change airspeed events
                Node plan = findNode(eventCollectionXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID));
                NodeList xmlEvents = plan.getChildNodes();

                for (int n = 0; n < xmlEvents.getLength(); n++) {
                    Node node = xmlEvents.item(n);
                    if (node.getNodeName().equalsIgnoreCase("Event")) {
                        //check for attribute name and value
                        if (getAttributeValue(node, "type").equalsIgnoreCase("chgAirspeed")) {
                            chgAirspeed = new EventChgAirspeed();
                            chgAirspeed.assign(lateral);

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
                                   chgAirspeed.setStartPt(waypoint.getLat(),waypoint.getLon());
                                else if(waypoint.id == 2)
                                    chgAirspeed.setEndPt(waypoint.getLat(), waypoint.getLon());
                            }

                            // Get Variables
                             Node variables = findNode(node.getChildNodes(),"Variables");

                            chgAirspeed.setvAsi(Double.parseDouble(
                                    getElementValue(
                                            findNode(variables.getChildNodes(), "Var", "type", "init"))));

                            if(getAttributeValue(findNode(variables.getChildNodes(), "Var", "type", "init"), "unit").equalsIgnoreCase("kts"))
                                chgAirspeed.setvAsi(PerfCalc.convertKts(chgAirspeed.getVAsi(),"kts"));

                            chgAirspeed.setvAsf(Double.parseDouble(
                                        getElementValue(
                                                findNode(variables.getChildNodes(), "Var", "type", "target"))));

                            if(getAttributeValue(findNode(variables.getChildNodes(), "Var", "type", "target"),"unit").equalsIgnoreCase("kts"))
                                chgAirspeed.setvAsf(PerfCalc.convertKts(chgAirspeed.getVAsf(), "kts"));

                            chgAirspeed.setAcc(Double.parseDouble(
                                        getElementValue(
                                                findNode(variables.getChildNodes(), "Var", "type", "acceleration"))));

                            chgAirspeed.setOffset(Double.parseDouble(
                                    getElementValue(
                                            findNode(variables.getChildNodes(), "Var", "type", "timeOffset"))));

                            // Validate event and add to velocity plan
                            chgAirspeed.validate();

                            this.events.add(chgAirspeed);
                        }
                    }
                }

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the set of change airspeed events into a consecutive set of airspeed segments.
     * Change Airspeed events must be defined at the start and end position of the flight plan,
     * otherwise the object will not be able to start and stop the lateral movement.
     */
    public void transform(){
        VelocitySegment velSgmt, itmSgmt;
        Waypoint startWpt, endWpt;
        double startSpd, endSpd, waitSpd = 0.01, waitDist; // = 0.01;
        int s = 0, i = 1;

        // Loop through change airspeed events and create consecutive set of velocity segments
        for(EventChgAirspeed event : this.events){

            // If required, build intermediate segment to bridge the gap between the start of the lateral track and the first segment
            if(s== 0 && lateral.getDist(lateral.getStartWpt(),this.events.get(s).getStartPt()) > 0){
                itmSgmt = new VelocitySegment(0);
                itmSgmt.setStartPt(lateral.getStartWpt().getLat(), lateral.getStartWpt().getLon());
                itmSgmt.setEndPt(this.events.get(s).getStartPt().getLat(), this.events.get(s).getStartPt().getLon());
                itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                itmSgmt.setVasi(this.events.get(s).getVAsi());
                itmSgmt.setVasf(this.events.get(s).getVAsi());
                itmSgmt.setAcc(0);
                this.velocitySegments.add(itmSgmt);
                i++;
            }

            // In case a time offset is defined for a deceleration segment, insert a wait segment
            if(event.getOffset() > 0 && event.getAcc() > 0){
                // Calc parameters
                waitDist = event.getOffset() * waitSpd;
                startWpt = event.getStartPt();
                endWpt = lateral.getItmWpt(startWpt,waitDist);
                //waitSpd = waitDist/event.getOffset();

                // Create new velocity wait segment and add parameters
                velSgmt = new VelocitySegment(i);
                velSgmt.setStartPt(startWpt.getLat(), startWpt.getLon());
                velSgmt.setEndPt(endWpt.getLat(), endWpt.getLon());
                velSgmt.setDist(waitDist);
                velSgmt.setVasf(waitSpd);
                velSgmt.setVasi(waitSpd);
                velSgmt.setAcc(0.0);
                velSgmt.setOffset(event.getOffset());
                this.velocitySegments.add(velSgmt);

                // set start and end wpt's for following segment
                startWpt = endWpt;
                endWpt = event.getEndPt();

                // Adjust end/start speed of previous/next segment
                if(i > 1)
                    this.velocitySegments.get(i - 2).setVasf(waitSpd);
                startSpd = waitSpd;

                i++;
            }else{
                startWpt = event.getStartPt();
                endWpt = event.getEndPt();
                startSpd = event.getVAsi();
            }

            // Ensure that waypoints are aligned to flight plan
            lateral.alignWpt(startWpt);
            lateral.alignWpt(endWpt);

            // Adjust for VAsf of zero in end/final velocity segment
            if(s == (this.events.size() - 1) && event.getVAsf() == 0)
                endSpd = waitSpd;
            else
                endSpd = event.getVAsf();

            // Add velocity segment
            velSgmt = new VelocitySegment(i);
            velSgmt.setStartPt(startWpt.getLat(), startWpt.getLon());
            velSgmt.setEndPt(endWpt.getLat(), endWpt.getLon());
            velSgmt.setDist(lateral.getDist(startWpt,endWpt));
            velSgmt.setVasi(startSpd);
            velSgmt.setVasf(endSpd);
            velSgmt.setAcc((pow(endSpd,2) - pow(startSpd,2))/(2 * velSgmt.getDist()));
            this.velocitySegments.add(velSgmt);
            i++;

            // Build intermediate segment, to bridge the gap between two segments
            if(s < (this.events.size() - 1) && lateral.getDist(this.events.get(s).getEndPt(),this.events.get(s + 1).getStartPt()) > 0) {
                itmSgmt = new VelocitySegment(i);
                itmSgmt.setStartPt(this.events.get(s).getEndPt().getLat(), this.events.get(s).getEndPt().getLon());
                itmSgmt.setEndPt(this.events.get(s + 1).getStartPt().getLat(), this.events.get(s + 1).getStartPt().getLon());
                itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                itmSgmt.setVasi(this.events.get(s).getVAsf());
                itmSgmt.setVasf(this.events.get(s + 1).getVAsi());
                itmSgmt.setAcc(0);
                this.velocitySegments.add(itmSgmt);
                i++;

            // Build intermediate segment, to bridge the gap between the last segment and the end of the lateral track
            }  else if(s == (this.events.size() - 1) && lateral.getDist(this.events.get(s).getEndPt(),lateral.getEndWpt()) > 0){
                itmSgmt = new VelocitySegment(i);
                itmSgmt.setStartPt(this.events.get(s).getEndPt().getLat(), this.events.get(s).getEndPt().getLon());
                itmSgmt.setEndPt(lateral.getEndWpt().getLat(), lateral.getEndWpt().getLon());
                itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                itmSgmt.setVasi(this.events.get(s).getVAsf());
                itmSgmt.setVasf(this.events.get(s).getVAsf());
                itmSgmt.setAcc(0);
                this.velocitySegments.add(itmSgmt);
                i++;
            }
            s++;
        }
    }

    /*
     * This method validates the integrity of the velocity segment set. In the case of an overlap between two
     * separate segments, the method aims to find a suitable transition point. If the method can't find a transition
     * point the data set is marked as invalid.
     */
    public void validate(){
        try{
            double prevVasi, prevAcc, nextVasf, nextAcc, velTrn, nextWptDist, velInterm, velEventAcc, velEventDist, taxiSpd;
            Waypoint refWpt;
            VelocitySegment prevSgmt, thisSgmt, nextSgmt, itmSgmt;

            dataValid = true;
            taxiSpd = PerfCalc.convertKts(SPD_TAXI,"kts");

            for(int s = 1; s < this.velocitySegments.size() - 1; s++){

                // Get segments
                prevSgmt = this.velocitySegments.get(s - 1);
                thisSgmt = this.velocitySegments.get(s);
                nextSgmt = this.velocitySegments.get(s + 1);

                // A. Check whether start and end points of two consecutive segments overlap
                nextWptDist = lateral.getDist(thisSgmt.getStartPt(),nextSgmt.getEndPt());

                if((thisSgmt.getDist() + nextSgmt.getDist() - nextWptDist) > 1 && thisSgmt.getVasf() == nextSgmt.getVasi()){
                    dataValid = false;

                    // Calculate max intermediary velocity that can be achieved between both waypoints
                    velInterm = sqrt(pow(thisSgmt.getVasi(),2) + 2 * thisSgmt.getAcc() * nextWptDist / 4);

                    if(velInterm >= taxiSpd && thisSgmt.getVasi() <= taxiSpd)
                        velInterm = taxiSpd;

                    // Update data first waypoint
                    velEventAcc = thisSgmt.getAcc();
                    velEventDist = (pow(velInterm, 2) - pow(thisSgmt.getVasi(), 2)) / (2 * velEventAcc);
                    refWpt = lateral.getItmWpt(thisSgmt.getStartPt(), velEventDist);

                    thisSgmt.setEndPt(refWpt.getLat(), refWpt.getLon());
                    thisSgmt.setVasf(velInterm);
                    thisSgmt.setDist(velEventDist);

                    // Update data second waypoint point
                    velEventAcc = nextSgmt.getAcc();
                    velEventDist = (pow(nextSgmt.getVasf(), 2) - pow(velInterm, 2)) / (2 * velEventAcc);
                    refWpt = lateral.getItmWpt(nextSgmt.getEndPt(), velEventDist * -1);

                    nextSgmt.setStartPt(refWpt.getLat(), refWpt.getLon());
                    nextSgmt.setVasi(velInterm);
                    nextSgmt.setDist(velEventDist);

                    // Build intermediate segment, to bridge the gap between two segments
                    itmSgmt = new VelocitySegment(s + 1);
                    itmSgmt.setStartPt(thisSgmt.getEndPt().getLat(), thisSgmt.getEndPt().getLon());
                    itmSgmt.setEndPt(nextSgmt.getStartPt().getLat(), nextSgmt.getStartPt().getLon());
                    itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                    itmSgmt.setVasi(velInterm);
                    itmSgmt.setVasf(velInterm);
                    itmSgmt.setAcc(0);
                    this.velocitySegments.add(s + 1, itmSgmt);
                    s++;

                    dataValid = true;
                }

                // B. Check whether velocity segment runs in opposite direction of lateral plan
                if(this.velocitySegments.get(s).getDist() < 0){
                    dataValid = false;

                    // Find entry and exit velocities of segment overlap, i.e. start of next segment and end of previous segment
                    prevVasi = prevSgmt.getVas(lateral.getDist(prevSgmt.getStartPt(), nextSgmt.getStartPt()));
                    prevAcc = prevSgmt.getAcc();

                    nextVasf = nextSgmt.getVas(lateral.getDist(nextSgmt.getStartPt(), prevSgmt.getEndPt()));
                    nextAcc = nextSgmt.getAcc();

                    // Check if transition point can be found
                    if (nextVasf < prevSgmt.getVasf()) {

                        // Find transition velocity
                         velTrn = sqrt(((pow(nextVasf, 2) * 4 * prevAcc * nextAcc) - (pow(prevVasi, 2) * 2 * nextAcc)) /
                                ((4 * prevAcc * nextAcc) - (2 * nextAcc)));

                        // Find transition waypoint
                        refWpt = lateral.getItmWpt(prevSgmt.getStartPt(), (pow( velTrn, 2) - pow(prevSgmt.getVasi(), 2)) / (2 * prevAcc));

                        prevSgmt.setEndPt(refWpt.getLat(), refWpt.getLon());
                        prevSgmt.setVasf( velTrn);

                        nextSgmt.setStartPt(refWpt.getLat(), refWpt.getLon());
                        nextSgmt.setVasi( velTrn);

                        // Remove connecting segment and update ids
                        velocitySegments.remove(thisSgmt);

                        for (int i = 0; i < this.velocitySegments.size(); i++)
                            this.velocitySegments.get(i).id = i + 1;

                        dataValid = true;
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

    }
}
