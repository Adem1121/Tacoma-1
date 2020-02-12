package ftdis.fplu;

//import com.sun.xml.internal.bind.v2.runtime.output.StAXExStreamWriterOutput;

import ftdis.fdpu.*;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.*;

import static ftdis.fdpu.ListUtil.deepClone;
import static java.lang.Math.*;
import static ftdis.fdpu.DOMUtil.*;
import static ftdis.fdpu.Config.*;

/**
 * The TaxiPlan class represents the lateral and velocity data for the aircraft's planned taxi to the runway
 * and taxi to the gate. It is a key component of the Flight Planning Unit.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class TaxiPlan {
    public int id;
    public boolean dataValid = false;
    private int planID;
    private double taxiSpd;
    private double acceleration;
    private LateralPlan latPlan;
    private EventCollectionChgAirspeed holdPoints;
    private List<EventChgAirspeed> velocityEvents;
    private List<Double> timeOffset;
    private List<Double> velocity;

    /**
     * Constructor(s)
     */
    TaxiPlan(){
        this.acceleration = ACC_TAXI;
        this.latPlan = new LateralPlan();
        this.holdPoints = new EventCollectionChgAirspeed();
        this.velocityEvents = new ArrayList<EventChgAirspeed>();
        this.timeOffset = new ArrayList<Double>();
        this.velocity = new ArrayList<Double>();
    }

    TaxiPlan(Integer planID){
        this();
        this.id = planID;
    }

    /**
     * This method sets the  acceleration/deceleration for the taxi plan
     * @param acceleration  Acceleration in meters per second
     */
    public void setAcc(double acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * This method returns the defined acceleration/deceleration for the taxi plan
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

                // Load events
                holdPoints.load(fileName, this.planID);

                if (latPlan.dataValid) {
                    // Parse xml file and load variables
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document masterPlanXML = dBuilder.parse(new File(fileName));

                    // normalize
                    masterPlanXML.getDocumentElement().normalize();

                    // Get variables
                    this.taxiSpd = PerfCalc.convertKts(SPD_TAXI,"kts");

                    // Get additional waypoint data
                    Node plan = findNode(findNode(masterPlanXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(this.planID)).getChildNodes(),"Waypoints");
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
                            cVelocity = this.taxiSpd;
                        }
                        velocity.add(cVelocity);
                    }
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
            Waypoint thisWpt, nextWpt, refWpt, holdWpt;
            double thisWptHoldT, thisWptVel, nextWptHoldT, nextWptVel, nextWptDist, forcVel, velEventDist, velEventAcc, velInterm, crsChg, turnRad, turnDist, accDist, decDist;
            int sgmtPos;

            // Create temporary lateral plan and add hold points to velocity data points
            LateralPlan tmpLatPlan = (LateralPlan) deepClone(latPlan);

            for (int e = 0; e < holdPoints.getEventCount(); e++){
                holdWpt = holdPoints.getEvent(e).getEndPt();
                tmpLatPlan.alignWpt(holdWpt);
                sgmtPos = tmpLatPlan.getSgmtPos(tmpLatPlan.getWptSgmt(holdWpt));
                tmpLatPlan.addWpt(sgmtPos+1, holdWpt);
                timeOffset.add(sgmtPos+1, holdPoints.getEvent(e).getOffset());
                velocity.add(sgmtPos+1,0.0);
                tmpLatPlan.transform();
            }


            // Loop through temporary lateral plan / velocity data points and create corresponding velocity events
            for(int w = 0; w < tmpLatPlan.getWptSize() - 1; w++){
                // Get waypoint and parameters
                thisWpt = tmpLatPlan.getWpt(w);
                thisWptHoldT = timeOffset.get(w);
                thisWptVel = velocity.get(w);

                // Get next waypoint and parameters
                nextWpt = tmpLatPlan.getWpt(w+1);
                nextWptHoldT = timeOffset.get(w+1);
                nextWptVel = velocity.get(w+1);
                nextWptDist = tmpLatPlan.getDist(thisWpt,nextWpt);

                if (w < tmpLatPlan.getWptSize() - 2)
                    forcVel = velocity.get(w+2);
                else
                    forcVel = 0;

                // Create acceleration/deceleration events
                if(thisWptVel != nextWptVel){

                    // Calculate distances
                    accDist = (pow(this.taxiSpd, 2) - pow(thisWptVel, 2)) / (2 * this.acceleration);
                    decDist = (pow(nextWptVel, 2) - pow(this.taxiSpd, 2)) / (2 * this.acceleration * -1);

                    if(accDist + decDist > nextWptDist) {

                        // Acceleration event
                        if (thisWptVel < nextWptVel) {
                            // Match velocity at end of event to available acceleration distance
                            //if(accDist > nextWptDist){
                            if(nextWptVel <= forcVel)
                                // Accelerate / Decelerate until next waypoint
                                nextWptVel = sqrt(pow(thisWptVel,2) + 2 * this.acceleration * nextWptDist);
                            else
                                // Calculate max intermediary velocity that can be achieved between both waypoints
                                nextWptVel = sqrt(pow(thisWptVel,2) + 2 * this.acceleration * nextWptDist / 2);

                            if(nextWptVel > this.taxiSpd)
                                nextWptVel = taxiSpd;

                            velocity.set(w + 1, nextWptVel);
                            //}

                            velEventAcc = this.acceleration;
                            velEventDist = (pow(nextWptVel, 2) - pow(thisWptVel, 2)) / (2 * velEventAcc);
                            refWpt = tmpLatPlan.getItmWpt(thisWpt, velEventDist);

                            velEvent = new EventChgAirspeed();
                            velEvent.setStartPt(thisWpt.getLat(), thisWpt.getLon());
                            velEvent.setEndPt(refWpt.getLat(), refWpt.getLon());
                            velEvent.setvAsi(thisWptVel);
                            velEvent.setvAsf(nextWptVel);
                            velEvent.setAcc(velEventAcc);
                            velEvent.setOffset(thisWptHoldT);
                            velocityEvents.add(velEvent);

                            // Deceleration event
                        } else if(thisWptVel > nextWptVel) {
                            // Remove previous event to provide ample deceleration distance

                            /*if(decDist > nextWptDist && w > 1){
                                // Remove previous event and velocity data points
                                velocityEvents.remove(velocityEvents.size()-1);
                                timeOffset.remove(w-1);
                                velocity.remove(w-1);
                                tmpLatPlan.delWpt(w-1);
                                w--;

                                // Update waypoint and parameters
                                thisWpt = tmpLatPlan.getWpt(w);
                                thisWptHoldT = timeOffset.get(w);
                                thisWptVel = velocity.get(w);
                            }*/

                            velEventAcc = this.acceleration * -1;
                            velEventDist = (pow(nextWptVel, 2) - pow(thisWptVel, 2)) / (2 * velEventAcc);
                            refWpt = tmpLatPlan.getItmWpt(nextWpt, velEventDist * -1);

                            // thisWptVel = sqrt(pow(nextWptVel, 2) - (2 * this.acceleration * (nextWptDist/4)));
                            // refWpt = tmpLatPlan.getItmWpt(nextWpt, (nextWptDist/4) * -1);

                            velEvent = new EventChgAirspeed();
                            velEvent.setStartPt(refWpt.getLat(), refWpt.getLon());
                            velEvent.setEndPt(nextWpt.getLat(), nextWpt.getLon());
                            velEvent.setvAsi(thisWptVel);
                            velEvent.setvAsf(nextWptVel);
                            velEvent.setAcc(velEventAcc);
                            velEvent.setOffset(thisWptHoldT);
                            velocityEvents.add(velEvent);
                        }

                    } else {

                        // Accelerate from this waypoint
                        if (thisWptVel < nextWptVel) {

                            if(nextWptVel <= forcVel)
                                // Accelerate / Decelerate until next waypoint
                                velInterm = sqrt(pow(thisWptVel,2) + 2 * this.acceleration * nextWptDist);
                            else
                                // Calculate max intermediary velocity that can be achieved between both waypoints
                                velInterm = sqrt(pow(thisWptVel,2) + 2 * this.acceleration * nextWptDist / 2);

                            if (velInterm >= this.taxiSpd)
                                velInterm = this.taxiSpd;

                            velocity.set(w + 1, velInterm);

                            velEventAcc = this.acceleration;
                            velEventDist = (pow(velInterm, 2) - pow(thisWptVel, 2)) / (2 * velEventAcc);
                            refWpt = tmpLatPlan.getItmWpt(thisWpt, velEventDist);

                            velEvent = new EventChgAirspeed();
                            velEvent.setStartPt(thisWpt.getLat(), thisWpt.getLon());
                            velEvent.setEndPt(refWpt.getLat(), refWpt.getLon());
                            velEvent.setvAsi(thisWptVel);
                            velEvent.setvAsf(velInterm);
                            velEvent.setAcc(velEventAcc);
                            velEvent.setOffset(thisWptHoldT);
                            velocityEvents.add(velEvent);

                        } else
                            velInterm = thisWptVel;

                        if (velInterm > nextWptVel) {
                            // Decelerate to next waypoint
                            velEventAcc = this.acceleration * -1;
                            velEventDist = (pow(nextWptVel, 2) - pow(velInterm, 2)) / (2 * velEventAcc);
                            refWpt = tmpLatPlan.getItmWpt(nextWpt, velEventDist * -1);

                            velEvent = new EventChgAirspeed();
                            velEvent.setStartPt(refWpt.getLat(), refWpt.getLon());
                            velEvent.setEndPt(nextWpt.getLat(), nextWpt.getLon());
                            velEvent.setvAsi(velInterm);
                            velEvent.setvAsf(nextWptVel);
                            velEvent.setAcc(velEventAcc);
                            velEvent.setOffset(0);
                            velocityEvents.add(velEvent);
                        } else
                            velocity.set(w + 1, velInterm);
                    }
                }
                // Stop and Go: Deceleration, Acceleration, Deceleration events in case this and next wpt, are both holding points (i.e. velocity 0)
                else if(thisWptVel == 0 && nextWptVel == 0){
                    // Calculate max intermediary velocity that can be achieved between both waypoints
                    velInterm = sqrt(pow(thisWptVel,2) + 2 * this.acceleration * nextWptDist / 4);

                    if(velInterm >= this.taxiSpd)
                        velInterm = this.taxiSpd;


                    // Accelerate from first holding point
                    velEventAcc = this.acceleration;
                    velEventDist = (pow(velInterm, 2) - pow(thisWptVel, 2)) / (2 * velEventAcc);
                    refWpt = tmpLatPlan.getItmWpt(thisWpt, velEventDist);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(thisWpt.getLat(), thisWpt.getLon());
                    velEvent.setEndPt(refWpt.getLat(), refWpt.getLon());
                    velEvent.setvAsi(thisWptVel);
                    velEvent.setvAsf(velInterm);
                    velEvent.setAcc(velEventAcc);
                    velEvent.setOffset(thisWptHoldT);
                    velocityEvents.add(velEvent);


                    // Decelerate to second holding point
                    velEventAcc = this.acceleration * -1;
                    velEventDist = (pow(nextWptVel, 2) - pow(velInterm, 2)) / (2 * velEventAcc);
                    refWpt = tmpLatPlan.getItmWpt(nextWpt, velEventDist * -1);

                    velEvent = new EventChgAirspeed();
                    velEvent.setStartPt(refWpt.getLat(), refWpt.getLon());
                    velEvent.setEndPt(nextWpt.getLat(), nextWpt.getLon());
                    velEvent.setvAsi(velInterm);
                    velEvent.setvAsf(nextWptVel);
                    velEvent.setAcc(velEventAcc);
                    velEvent.setOffset(0);
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
            EventChgAirspeed thisEvent, nextEvent, startEvent, endEvent;
            Waypoint refWpt;
            double velEventAcc, velEventDist;
            int startEventPos;

            // Loop through events
            for(int e = 0; e < (velocityEvents.size() - 1); e++) {
                thisEvent = velocityEvents.get(e);
                nextEvent = velocityEvents.get(e+1);


                /** A. Identify and resolve overlap
                 *
                 *  Event 1    S=============E          or        S=============E     or    S===========================E
                 *  Event 2           S=============E        S=============E                       S=============E
                 */
                if(e > 0 && latPlan.getDist(velocityEvents.get(e-1).getEndPt(),thisEvent.getStartPt()) < 0){

                    startEvent = thisEvent;
                    startEventPos = e;
                    endEvent = thisEvent;


                    // Get event start point
                    for(int i = startEventPos; i >= 0; i--) {
                        startEvent = velocityEvents.get(i);

                        if(latPlan.getDist(startEvent.getStartPt(), thisEvent.getStartPt()) > 0) {
                            startEventPos = i;
                            break;
                        }
                    }

                    // Merge events
                    startEvent.setEndPt(thisEvent.getEndPt().getLat(),thisEvent.getEndPt().getLon());
                    startEvent.setvAsf(thisEvent.getVAsf());

                    velEventAcc = this.acceleration;
                    if(startEvent.getVAsi() > startEvent.getVAsf())
                        velEventAcc *= -1;

                    velEventDist = (pow(startEvent.getVAsf(), 2) - pow(startEvent.getVAsi(), 2)) / (2 * velEventAcc);
                    refWpt = latPlan.getItmWpt(startEvent.getEndPt(), velEventDist * -1);
                    startEvent.setStartPt(refWpt.getLat(),refWpt.getLon());

                    startEvent.setAcc(velEventAcc);

                    // Remove intermediary events
                    while(e > startEventPos){
                        velocityEvents.remove(e);
                        e--;
                    }
                }

            }

            // Loop through events
            for(int e = 0; e < (velocityEvents.size() - 1); e++) {
                thisEvent = velocityEvents.get(e);
                nextEvent = velocityEvents.get(e + 1);


                /** B. Identify and merge consecutive segments into one
                 *
                 *  Event 1    S=============E
                 *  Event 2                  S=============E
                 */
                if (thisEvent.getAcc() == nextEvent.getAcc() && latPlan.getDist(thisEvent.getEndPt(), nextEvent.getStartPt()) == 0) {

                    thisEvent.setEndPt(nextEvent.getEndPt().getLat(), nextEvent.getEndPt().getLon());
                    thisEvent.setvAsf(nextEvent.getVAsf());

                    // Remove redundant event
                    velocityEvents.remove(e + 1);
                    e--;
                }

                if (thisEvent.getVAsf() == nextEvent.getVAsf() && latPlan.getDist(thisEvent.getEndPt(), nextEvent.getStartPt()) == 0) {

                    thisEvent.setEndPt(nextEvent.getEndPt().getLat(), nextEvent.getEndPt().getLon());

                    velEventDist = latPlan.getDist(thisEvent.getStartPt(),thisEvent.getEndPt());
                    velEventAcc =  (pow(nextEvent.getVAsf(),2) - pow(nextEvent.getVAsi(),2))/(2 * velEventDist);
                    thisEvent.setAcc(velEventAcc);
                    //thisEvent.setvAsf(nextEvent.getVAsf());

                    // Remove redundant event
                    velocityEvents.remove(e + 1);
                    e--;
                }
            }

            /*
            EventChgAirspeed thisEvent,nextEvent;
            Waypoint refWpt;
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
                /*if(startToEnd > 0 && endToStart < 0){
                    if(thisEvent.getVAsi() != nextEvent.getVAsf()) {

                        // *********************************************
                        // Revised 06/02/19 Refactored logic to improve processing
                        // Left legacy code as reference until testing is complete
                        // *********************************************
                        thisEvent.setEndPt(nextEvent.getEndPt().getLat(), nextEvent.getEndPt().getLon());
                        thisEvent.setvAsi(thisEvent.getVAsi());
                        thisEvent.setvAsf(nextEvent.getVAsf());

                        velEventAcc = this.acceleration;
                        if(thisEvent.getVAsi() > thisEvent.getVAsf())
                            velEventAcc *= -1;

                        velEventDist = (pow(thisEvent.getVAsf(), 2) - pow(thisEvent.getVAsi(), 2)) / (2 * velEventAcc);
                        refWpt = latPlan.getItmWpt(thisEvent.getEndPt(), velEventDist * -1);
                        thisEvent.setStartPt(refWpt.getLat(),refWpt.getLon());

                        thisEvent.setAcc(velEventAcc);

                        /* Legacy Code 06/02/19
                        thisEvent.setEndPt(nextEvent.getEndPt().getLat(), nextEvent.getEndPt().getLon());
                        thisEvent.setvAsf(nextEvent.getVAsf());

                        velEventDist = latPlan.getDist(thisEvent.getStartPt(), thisEvent.getEndPt());
                        velEventAcc = (pow(thisEvent.getVAsf(), 2) - pow(thisEvent.getVAsi(), 2)) / (2 * velEventDist);
                        thisEvent.setAcc(velEventAcc);*/

                    /*}else {
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
                /*if(startToStart < 0 && endToEnd > 0){
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
                /*if(startToEnd < 0 && startToStart < 0 && endToEnd < 0){
                    this.dataValid = false;
                }
            }*/
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}
