package ftdis.fplu;

import ftdis.fdpu.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

import static java.lang.Math.*;
import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.Config.*;

/**
 * The PushbackPlan class represents the lateral and velocity data for the aircraft's planned pushback from  the gate.
 * It is a key component of the Flight Planning Unit.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.2
 */
public class PushbackPlan {
    public int id;
    public boolean dataValid = false;
    private int planID;
    private double pushbackSpd;
    private double acceleration;
    private LateralPlan latPlan;
    private List<EventChgAirspeed> velocityEvents;
    private List<Double> timeOffset;
    private List<Double> velocity;

    /**
     * Constructor(s)
     */
    PushbackPlan(){
        this.acceleration = ACC_PUSHBACK;
        this.latPlan = new LateralPlan();
        this.velocityEvents = new ArrayList<EventChgAirspeed>();
        this.timeOffset = new ArrayList<Double>();
        this.velocity = new ArrayList<Double>();
    }

    PushbackPlan(Integer planID){
        this();
        this.id = planID;
    }

    /**
     * This method sets the  acceleration/deceleration for the pushback plan
     * @param acceleration  Acceleration in meters per second
     */
    public void setAcc(double acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * This method returns the defined acceleration/deceleration for the pushback plan
     * @return  Acceleration in meters per second
     */
    public double getAcc() {
        return acceleration;
    }

    /**
     * This method returns the lateral plan assigned to the lateral track
     * @return  Reference to lateral plan
     */
    public LateralPlan getLateralPlan(){
        return this.latPlan;
    }

    /**
     * This method adds a change velocity event to the velocity event list
     *
     * @param velEvent  Change velocity event to be added.
     */
    public void addVelEvent(EventChgAirspeed velEvent){
        this.velocityEvents.add(velEvent);
    }

    /**
     * This method returns a velocity event of a specific event number
     *
     * @param velEventNum   Number of the change velocity event, starts with 0
     * @return The change velocity event
     */
    public EventChgAirspeed getVelEvent(int velEventNum){
        return ListUtil.getListItem(velEventNum, this.velocityEvents);
    }

    /**
     * @param velEvent The change velocity event
     * @return The number of the change velocity event in the list, index starts with 0
     */
    public int getVelEventPos(EventChgAirspeed velEvent){
        return ListUtil.getListItemNum(velEvent, this.velocityEvents);
    }

    /**
     * @return The total number of change velocity events
     */
    public int getVelEventCount(){
        return this.velocityEvents.size();
    }

    /**
     * This method loads the lateral plan and additional variables from an external master plan xml file.
     *
     * @param fileName  The complete path and file name of the external master plan xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            double cTimeOffset = 0.0, cVelocity = 0.0;

            if(fileName != null && !fileName.isEmpty()) {
                this.planID = planID;

                // Load lateral plan
                latPlan.load(fileName, this.planID);
                latPlan.transform();
                latPlan.validate();

                if (latPlan.dataValid) {
                    // Parse xml file and load variables
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document masterPlanXML = dBuilder.parse(new File(fileName));

                    // normalize
                    masterPlanXML.getDocumentElement().normalize();

                    // Get variables
                    this.pushbackSpd = PerfCalc.convertKts(SPD_PUSHBACK,"kts");

                    // Get additional waypoint data
                    Node plan = findNode(findNode(masterPlanXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID)).getChildNodes(),"Waypoints");
                    List<Node> waypoints = getChildElementsByTagName(plan, "Waypoint");

                    for(Node node : waypoints){
                        // Get time offset
                        try{
                            cTimeOffset = Double.parseDouble(getAttributeValue(node, "timeOffset"));
                        } catch (Exception e) {
                            cTimeOffset = 0.0;
                        }
                        timeOffset.add(cTimeOffset);

                        // Get taxi speed
                        try{
                            if(cTimeOffset == 0.0)
                                cVelocity = PerfCalc.convertKts(Double.parseDouble(getAttributeValue(node, "spd")),"kts");
                            else
                                cVelocity = 0.0;
                        } catch (DOMUtilException e) {
                            cVelocity = PerfCalc.convertKts(SPD_TAXI,"kts");
                        }
                        velocity.add(cVelocity);
                    }

                    /*
                    for(Node node : waypoints){
                        timeOffset.add(Double.parseDouble(getAttributeValue(node, "timeOffset")));
                        velocity.add(PerfCalc.convertKts(Double.parseDouble(getAttributeValue(node, "spd")),"kts"));
                    }*/
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the set of waypoints and waypoint data into a set of consecutive velocity events. Deceleration and
     * acceleration events are being created to match the taxi speed and the specific speeds defined at each waypoint. The events
     * are validated and processed further downstream. Ref. validate() method.
     *
     */
    public void transform(){
        try{
            EventChgAirspeed velEvent;
            DirectSegment prevSgmt, nextSgmt;
            Waypoint wpt, refWpt, turnWpt;
            double wptHoldT, wptVel, velEventDist, velEventAcc, crsChg, turnRad, turnDist;

            for(int w = 0; w < latPlan.getWptSize(); w++){
                // Get waypoint and parameters
                wpt = latPlan.getWpt(w);
                wptHoldT = timeOffset.get(w);
                wptVel = velocity.get(w);

                // Identify turn points and course change
                if(w > 0 && w < (latPlan.getWptSize() - 1)){
                    prevSgmt = (DirectSegment) latPlan.getSgmt(w - 1);
                    nextSgmt = (DirectSegment) latPlan.getSgmt(w);

                    crsChg = abs(NavCalc.getCourseChange(prevSgmt.getCourseEnd(), nextSgmt.getCourseStart()));
                }else
                    crsChg = 0;

                // Create initial acceleration/deceleration event
                if(w == 0 && wptVel != this.pushbackSpd){
                    velEventDist = (pow(this.pushbackSpd,2) - pow(wptVel,2))/(2 * this.acceleration);
                    refWpt = latPlan.getItmWpt(wpt,velEventDist);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(wpt.getLat(),wpt.getLon());
                    velEvent.setEndPt(refWpt.getLat(),refWpt.getLon());
                    velEvent.setvAsi(wptVel);
                    velEvent.setvAsf(this.pushbackSpd);
                    //velEvent.setAcc(this.acceleration);
                    velEvent.setOffset(wptHoldT);
                    velocityEvents.add(velEvent);
                }

                // Create deceleration and acceleration events for turn points
                if(w > 0 && w < (latPlan.getWptSize() - 1) && wptHoldT == 0 && crsChg > 0){
                    // Create deceleration event
                    turnWpt = wpt;

                    velEventAcc = this.acceleration * -1;
                    velEventDist = (pow(wptVel,2) - pow(this.pushbackSpd,2))/(2 * velEventAcc);
                    refWpt = latPlan.getItmWpt(turnWpt,velEventDist*-1);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(refWpt.getLat(), refWpt.getLon());
                    velEvent.setEndPt(turnWpt.getLat(), turnWpt.getLon());
                    velEvent.setvAsi(this.pushbackSpd);
                    velEvent.setvAsf(wptVel);
                    //velEvent.setAcc(velEventAcc);
                    velEvent.setOffset(wptHoldT);
                    velocityEvents.add(velEvent);
                    w = w +2;

                    // Create acceleration event
                    turnWpt = latPlan.getWpt(w);
                    velEventDist = (pow(this.pushbackSpd,2) - pow(wptVel,2))/(2 * this.acceleration);
                    refWpt = latPlan.getItmWpt(turnWpt,velEventDist);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(turnWpt.getLat(),turnWpt.getLon());
                    velEvent.setEndPt(refWpt.getLat(),refWpt.getLon());
                    velEvent.setvAsi(wptVel);
                    velEvent.setvAsf(this.pushbackSpd);
                    //velEvent.setAcc(this.acceleration);
                    velEvent.setOffset(wptHoldT);
                    velocityEvents.add(velEvent);
                }

                // Create final deceleration event
                if(w == (latPlan.getWptSize() - 1)){
                    velEventDist = (pow(wptVel,2) - pow(this.pushbackSpd,2))/(2 * this.acceleration);
                    refWpt = latPlan.getItmWpt(wpt,velEventDist);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(refWpt.getLat(),refWpt.getLon());
                    velEvent.setEndPt(wpt.getLat(),wpt.getLon());
                    velEvent.setvAsi(this.pushbackSpd);
                    velEvent.setvAsf(wptVel);
                    //velEvent.setAcc(this.acceleration);
                    velEvent.setOffset(wptHoldT);
                    velocityEvents.add(velEvent);
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the set of velocity events created by the transform method. The method specifically checks for overlaps
     * and mismatches between consecutive segments and, in case of a positive match, aims to resolve them to ensure the integrity and
     * validity of the event collection data set.
     *
     */
    public void validate(){
        try{
            EventChgAirspeed thisEvent,nextEvent;
            double startToStart,endToEnd,endToStart,startToEnd, velEventDist, velEventAcc;

            this.dataValid = true;

            // Loop through events and check for overlap between to consecutive events
            for(int e = 0; e < (velocityEvents.size() - 1); e++){
                thisEvent = velocityEvents.get(e);
                nextEvent = velocityEvents.get(e + 1);

                // Calculate distances between events' start and end points
                startToStart = latPlan.getDist(thisEvent.getStartPt(),nextEvent.getStartPt());
                startToEnd = latPlan.getDist(thisEvent.getStartPt(),nextEvent.getEndPt());
                endToEnd = latPlan.getDist(thisEvent.getEndPt(),nextEvent.getEndPt());
                endToStart = latPlan.getDist(thisEvent.getEndPt(),nextEvent.getStartPt());

                /** A. Identify and resolve overlap
                 *
                 *  Event 1    S=============E          or        S=============E     or    S===========================E
                 *  Event 2           S=============E        S=============E                       S=============E
                 */
                if(startToEnd > 0 && endToStart < 0){
                    if(thisEvent.getVAsi() != nextEvent.getVAsf()) {
                        thisEvent.setEndPt(nextEvent.getEndPt().getLat(), nextEvent.getEndPt().getLon());
                        thisEvent.setvAsf(nextEvent.getVAsf());

                        velEventDist = latPlan.getDist(thisEvent.getStartPt(), thisEvent.getEndPt());
                        velEventAcc = (pow(thisEvent.getVAsf(), 2) - pow(thisEvent.getVAsi(), 2)) / (2 * velEventDist);
                        thisEvent.setAcc(velEventAcc);
                    }else {
                        velocityEvents.remove(e);
                        e--;
                    }
                    // Remove next segment
                    velocityEvents.remove(e + 1);
                }

                /** B. Identify and resolve inverse overlap
                 *
                 *  Event 1           S=============E
                 *  Event 2    S===========================E
                 */
                if(startToStart < 0 && endToEnd > 0){
                    nextEvent.setEndPt(thisEvent.getEndPt().getLat(),thisEvent.getEndPt().getLon());
                    nextEvent.setvAsf(thisEvent.getVAsf());

                    velEventDist = latPlan.getDist(nextEvent.getStartPt(),nextEvent.getEndPt());
                    velEventAcc =  (pow(nextEvent.getVAsf(),2) - pow(nextEvent.getVAsi(),2))/(2 * velEventDist);
                    thisEvent.setAcc(velEventAcc);

                    // Remove this segment
                    velocityEvents.remove(e);
                }

                /** c. Identify mismatch
                 *
                 *  Event 1                     S=============E
                 *  Event 2    S=============E
                 */
                if(startToEnd < 0 && startToStart < 0 && endToEnd < 0){
                    this.dataValid = false;
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}
