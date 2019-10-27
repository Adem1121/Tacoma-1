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
 * The FlightPlan class represents the lateral, velocity and vertical data for the aircraft's planned flight.
 * It is a key component of the Flight Planning Unit.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.2
 */
public class FlightPlan {
    public int id;
    public boolean dataValid = false;
    private int planID;
    private double depAlt, destAlt, initCrzAlt, midCrzAlt, crzAlt, maxVertSpd, timeOffset, climboutFixAlt; //climboutFixVel, finalAppVel;
    private Waypoint climboutFix, finalAppFix;
    private LateralPlan latPlan;
    private List<EventChgAirspeed> velEvents;
    private List<EventChgAltitude> vertEvents;
    private List<EventChgWeather> wxEvents;
    private List<Double> velocity;

    /**
     * Constructor(s)
     */
    FlightPlan(){
        this.climboutFixAlt = PerfCalc.convertFt(ALT_THRUST_RED,"ft");
        //this.climboutFixVel = PerfCalc.convertKts(160,"kts");
        this.maxVertSpd = 0;
        this.initCrzAlt = 0;
        this.crzAlt = 0;
        this.timeOffset = 0;
        this.latPlan = new LateralPlan();
        this.velEvents = new ArrayList<EventChgAirspeed>();
        this.vertEvents = new ArrayList<EventChgAltitude>();
        this.wxEvents = new ArrayList<EventChgWeather>();
        this.velocity = new ArrayList<Double>();
    }

    FlightPlan(Integer planID){
        this();
        this.id = planID;
    }

    /**
     * This method sets the cruise altitude of the flight plan
     * @param altitude  Altitude in meters
     */
    public void setCrzAlt(double altitude) {
        this.crzAlt = altitude;
    }

    /**
     * This method returns the cruise altitude of the flight plan
     * @return  Altitude in meters
     */
    public double getCrzAlt() {
        return this.crzAlt;
    }

    /**
     * This method sets the maximum vertical speed of the flight plan
     * @param vertSpd  Vertical speed in meters per second
     */
    public void setMaxVertSpd(double vertSpd) {
        this.maxVertSpd = vertSpd;
    }

    /**
     * This method returns the maximum vertical speed of the flight plan
     * @return  Altitude in meters
     */
    public double getMaxVertSpd() {
        return this.maxVertSpd;
    }

    /**
     * This method returns the lateral plan assigned to the flight plan
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
        this.velEvents.add(velEvent);
    }

    /**
     * This method returns a velocity event of a specific event number
     *
     * @param velEventNum   Number of the change velocity event, starts with 0
     * @return The change velocity event
     */
    public EventChgAirspeed getVelEvent(int velEventNum){
        return ListUtil.getListItem(velEventNum, this.velEvents);
    }

    /**
     * @param velEvent The change velocity event
     * @return The number of the change velocity event in the list, index starts with 0
     */
    public int getVelEventPos(EventChgAirspeed velEvent){
        return ListUtil.getListItemNum(velEvent, this.velEvents);
    }

    /**
     * @return The total number of change velocity events
     */
    public int getVelEventCount(){
        return this.velEvents.size();
    }
    
    /**
     * This method adds a change altitude event to the altitude event list
     *
     * @param vertEvent  Change altitude event to be added.
     */
    public void addVertEvent(EventChgAltitude vertEvent){
        this.vertEvents.add(vertEvent);
    }

    /**
     * This method returns a altitude event of a specific event number
     *
     * @param vertEventNum   Number of the change altitude event, starts with 0
     * @return The change altitude event
     */
    public EventChgAltitude getVertEvent(int vertEventNum){
        return ListUtil.getListItem(vertEventNum, this.vertEvents);
    }

    /**
     * @param vertEvent The change altitude event
     * @return The number of the change altitude event in the list, index starts with 0
     */
    public int getVertEventPos(EventChgAltitude vertEvent){
        return ListUtil.getListItemNum(vertEvent, this.vertEvents);
    }

    /**
     * @return The total number of change altitude events
     */
    public int getVertEventCount(){
        return this.vertEvents.size();
    }

    /**
     * This method adds a change weather event to the weather event list
     *
     * @param wxEvent  Change weather event to be added.
     */
    public void addWxEvent(EventChgWeather wxEvent){
        this.wxEvents.add(wxEvent);
    }

    /**
     * This method returns a weather event of a specific event number
     *
     * @param wxEventNum   Number of the change weather event, starts with 0
     * @return The change weather event
     */
    public EventChgWeather getWxEvent(int wxEventNum){
        return ListUtil.getListItem(wxEventNum, this.wxEvents);
    }

    /**
     * @param wxEvent The change weather event
     * @return The number of the change weather event in the list, index starts with 0
     */
    public int getWxEventPos(EventChgWeather wxEvent){
        return ListUtil.getListItemNum(wxEvent, this.wxEvents);
    }

    /**
     * @return The total number of change weather events
     */
    public int getWxEventCount(){
        return this.wxEvents.size();
    }

    /**
     * This method loads the lateral plan and additional variables from an external master plan xml file.
     *
     * @param fileName  The complete path and file name of the external master plan xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
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
                    this.depAlt = PerfCalc.convertFt(ALT_DEP,"ft");
                    this.destAlt = PerfCalc.convertFt(ALT_DEST,"ft");
                    this.initCrzAlt = PerfCalc.convertFt(ALT_INIT_CRUISE,"ft");
                    this.midCrzAlt = PerfCalc.convertFt(ALT_MID_CRUISE,"ft");
                    this.crzAlt = PerfCalc.convertFt(ALT_FINAL_CRUISE,"ft");

                    // Get additional waypoint data
                    Node plan = findNode(findNode(masterPlanXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID)).getChildNodes(),"Waypoints");
                    List<Node> waypoints = getChildElementsByTagName(plan, "Waypoint");

                    for(int w = 0; w < waypoints.size(); w++) {
                        // Get hold time at start of takeoff point
                        if(w == 0)
                            this.timeOffset = Double.parseDouble(getAttributeValue(waypoints.get(w), "timeOffset"));

                        // Get velocity at start of takeoff  and end of landing points
                        if(w == 0 || w == waypoints.size() - 1)
                            velocity.add(PerfCalc.convertKts(Double.parseDouble(getAttributeValue(waypoints.get(w), "spd")), "kts"));
                    }
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the set of waypoints and waypoint data into a set of consecutive velocity, vertical and
     * weather events. Each event type is processed in a separate method. Ref. prepareVelocityEvents(),
     * prepareVerticalEvents(), prepareWeatherEvents() methods.
     *
     */
    public void transform(){
        try{
            // Prepare vertical events
            this.prepareVerticalEvents();

            // Prepare velocity events
            this.prepareVelocityEvents();

            // Prepare weather events
            prepareWeatherEvents();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void prepareVerticalEvents(){
        try{
            EventChgAltitude vertEvent;
            Waypoint startWpt, endWpt;
            double alpha, vertGrndDist, fafAlt, flareAlt, retardAlt, transAlt, initClimbAlpha, climbTransAlpha, climbAlpha, descAlpha, fafAlpha, retardAlpha, flareAlpha, fafDist, retardDist, flareDist;

            // Initialize
            transAlt = ALT_TRANSITION;

            initClimbAlpha = ALPHA_TAKEOFF_DEG;
            climbTransAlpha = ALPHA_CLIMB_TRANS_DEG;
            climbAlpha = ALPHA_CLIMB_DEF_DEG;

            descAlpha = ALPHA_DESC_DEF_DEG;

            fafAlpha = ALPHA_FINAL_APP_DEG;
            fafDist = FINAL_APP_FIX_DIST;

            retardAlpha = ALPHA_RETARD_DEG;
            retardAlt = ALT_RETARD;

            flareAlpha = ALPHA_FLARE_DEG;
            flareAlt = ALT_FLARE;

            //////////////////////////////////////////////////////////////////////
            // 01 Climb to climbout fix waypoint
            //////////////////////////////////////////////////////////////////////
            startWpt = latPlan.getWpt(1);
            alpha = initClimbAlpha;

            vertGrndDist = this.climboutFixAlt / sin(toRadians(alpha));
            this.climboutFix = latPlan.getItmWpt(startWpt,vertGrndDist);

            vertEvent = new EventChgAltitude();
            vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            vertEvent.setEndPt(this.climboutFix.getLat(), this.climboutFix.getLon());
            vertEvent.setAlti(this.depAlt);
            vertEvent.setAltf(this.depAlt + this.climboutFixAlt);
            vertEvents.add(vertEvent);

            if(this.crzAlt > PerfCalc.convertFt(transAlt,"ft")){
                //////////////////////////////////////////////////////////////////////
                // 02 Climb to transition altitude
                //////////////////////////////////////////////////////////////////////
                startWpt = this.climboutFix;
                alpha = climbTransAlpha;

                vertGrndDist = ((PerfCalc.convertFt(transAlt,"ft") - this.depAlt - this.climboutFixAlt) / sin(toRadians(alpha)));
                endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                vertEvent = new EventChgAltitude();
                vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                vertEvent.setAlti(this.depAlt + this.climboutFixAlt);
                vertEvent.setAltf(PerfCalc.convertFt(transAlt, "ft"));
                vertEvents.add(vertEvent);

                //////////////////////////////////////////////////////////////////////
                // 03 Climb to cruise altitude (> 10k feet)
                //////////////////////////////////////////////////////////////////////
                if(ALT_INIT_CRUISE == ALT_FINAL_CRUISE){
                    /////////////////////////////////////////////////////////////////
                    // A Climb straight to cruise altitude
                    /////////////////////////////////////////////////////////////////
                    startWpt = vertEvents.get(vertEvents.size()-1).getEndPt();
                    alpha = climbAlpha;

                    vertGrndDist = (this.crzAlt - (PerfCalc.convertFt(transAlt,"ft"))) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(PerfCalc.convertFt(transAlt, "ft"));
                    vertEvent.setAltf(this.crzAlt);
                    vertEvents.add(vertEvent);

                } else if(ALT_MID_CRUISE == ALT_FINAL_CRUISE){
                    /////////////////////////////////////////////////////////////////
                    // B One step climb
                    /////////////////////////////////////////////////////////////////

                    // Climb to initial cruise altitude
                    startWpt = vertEvents.get(vertEvents.size()-1).getEndPt();
                    alpha = climbAlpha;

                    vertGrndDist = (this.initCrzAlt - (PerfCalc.convertFt(transAlt,"ft"))) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(PerfCalc.convertFt(transAlt, "ft"));
                    vertEvent.setAltf(this.initCrzAlt);
                    vertEvents.add(vertEvent);

                    // Climb to final cruise altitude
                    startWpt = latPlan.getItmWpt(vertEvents.get(vertEvents.size()-2).getEndPt(), ((ALT_FINAL_STEP_TIME*60)*PerfCalc.convertKts(SPD_CRUISE,"kts")));

                    vertGrndDist = (this.crzAlt - this.initCrzAlt) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(this.initCrzAlt);
                    vertEvent.setAltf(this.crzAlt);
                    vertEvents.add(vertEvent);

                } else {
                    /////////////////////////////////////////////////////////////////
                    // C Two step climb
                    /////////////////////////////////////////////////////////////////

                    // Climb to initial cruise altitude
                    startWpt = vertEvents.get(vertEvents.size()-1).getEndPt();
                    alpha = climbAlpha;

                    vertGrndDist = (this.initCrzAlt - (PerfCalc.convertFt(transAlt,"ft"))) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(PerfCalc.convertFt(transAlt, "ft"));
                    vertEvent.setAltf(this.initCrzAlt);
                    vertEvents.add(vertEvent);

                    // Climb to mid cruise altitude
                    startWpt = latPlan.getItmWpt(vertEvents.get(vertEvents.size()-2).getEndPt(), ((ALT_MID_STEP_TIME*60)*PerfCalc.convertKts(SPD_CRUISE,"kts")));

                    vertGrndDist = (this.midCrzAlt - this.initCrzAlt) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(this.initCrzAlt);
                    vertEvent.setAltf(this.midCrzAlt);
                    vertEvents.add(vertEvent);

                    // Climb to final cruise altitude
                    startWpt = latPlan.getItmWpt(vertEvents.get(vertEvents.size()-3).getEndPt(), ((ALT_FINAL_STEP_TIME*60)*PerfCalc.convertKts(SPD_CRUISE,"kts")));

                    vertGrndDist = (this.crzAlt - this.initCrzAlt) / sin(toRadians(alpha));
                    endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                    vertEvent = new EventChgAltitude();
                    vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                    vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                    vertEvent.setAlti(this.midCrzAlt);
                    vertEvent.setAltf(this.crzAlt);
                    vertEvents.add(vertEvent);
                }
            } else{
                //////////////////////////////////////////////////////////////////////
                // 03 Climb to crz alt (<=10k feet)
                //////////////////////////////////////////////////////////////////////
                startWpt = this.climboutFix;
                alpha = climbTransAlpha;

                vertGrndDist = (this.crzAlt - this.depAlt - this.climboutFixAlt) / sin(toRadians(alpha));
                endWpt = latPlan.getItmWpt(startWpt,vertGrndDist);

                vertEvent = new EventChgAltitude();
                vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                vertEvent.setAlti(this.depAlt + this.climboutFixAlt);
                vertEvent.setAltf(this.crzAlt);
                vertEvents.add(vertEvent);
            }

            //////////////////////////////////////////////////////////////////////
            // 04 Descent from cruise altitude to final approach fix altitude
            //////////////////////////////////////////////////////////////////////

            // Calculate descent distances and altitudes
            flareDist = PerfCalc.convertFt(flareAlt,"ft") / tan(toRadians(flareAlpha));
            retardDist = ((PerfCalc.convertFt(retardAlt,"ft") - PerfCalc.convertFt(flareAlt,"ft")) / tan(toRadians(retardAlpha))) + flareDist;
            fafAlt = (tan(toRadians(fafAlpha)) * (fafDist - retardDist)) + PerfCalc.convertFt(retardAlt,"ft") + PerfCalc.convertFt(flareAlt,"ft");
            this.finalAppFix = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), fafDist * -1);

            endWpt = latPlan.getItmWpt(this.finalAppFix, FINAL_APP_FIX_DESC_DIST * -1);

            vertGrndDist = (this.crzAlt - PerfCalc.convertFt(fafAlt, "ft") - this.destAlt) / sin(toRadians(descAlpha));
            startWpt = latPlan.getItmWpt(endWpt,vertGrndDist * -1);

            vertEvent = new EventChgAltitude();
            vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            vertEvent.setAlti(this.crzAlt);
            vertEvent.setAltf(fafAlt + this.destAlt);
            vertEvents.add(vertEvent);

            //////////////////////////////////////////////////////////////////////
            // 05 Descent from event final approach fix altitude to retard altitude
            //////////////////////////////////////////////////////////////////////
            startWpt = this.finalAppFix;
            endWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), retardDist * -1);

            vertEvent = new EventChgAltitude();
            vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            vertEvent.setAlti(fafAlt + this.destAlt);
            vertEvent.setAltf(PerfCalc.convertFt(retardAlt, "ft") + this.destAlt);
            vertEvents.add(vertEvent);

            //////////////////////////////////////////////////////////////////////
            // 06 Descent from retard altitude to flare altitude
            //////////////////////////////////////////////////////////////////////
            startWpt = vertEvents.get(vertEvents.size() - 1).getEndPt();
            endWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), flareDist * -1);

            vertEvent = new EventChgAltitude();
            vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            vertEvent.setAlti(PerfCalc.convertFt(retardAlt, "ft") + this.destAlt);
            vertEvent.setAltf(PerfCalc.convertFt(flareAlt,"ft") + this.destAlt);
            vertEvents.add(vertEvent);

            //////////////////////////////////////////////////////////////////////
            // 07 Descent from flare altitude to runway altitude
            //////////////////////////////////////////////////////////////////////
            startWpt = vertEvents.get(vertEvents.size() - 1).getEndPt();
            endWpt = latPlan.getWpt(latPlan.getWptSize() - 2);

            vertEvent = new EventChgAltitude();
            vertEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            vertEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            vertEvent.setAlti(PerfCalc.convertFt(flareAlt,"ft") + this.destAlt);
            vertEvent.setAltf(this.destAlt);
            vertEvents.add(vertEvent);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void prepareVelocityEvents(){
        try{
            EventChgAirspeed velEvent;
            Waypoint startWpt, endWpt;
            double acc, velDist, vertGrndDist, d, t;

            //////////////////////////////////////////////////////////////////////
            // 01 Create event acceleration to takeoff speed
            //////////////////////////////////////////////////////////////////////
            startWpt = latPlan.getWpt(0);
            endWpt = latPlan.getWpt(1);

            velEvent = new EventChgAirspeed();
            velEvent.setStartPt(startWpt.getLat(),startWpt.getLon());
            velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            velEvent.setvAsi(velocity.get(0));
            velEvent.setvAsf(PerfCalc.convertKts(SPD_LIFTOFF, "kts"));
            velEvent.setOffset(this.timeOffset);
            velEvents.add(velEvent);

            //////////////////////////////////////////////////////////////////////
            // 02 Create event acceleration to initial climb speed
            //////////////////////////////////////////////////////////////////////
            acc = 0.66;
            //t = (PerfCalc.convertKts(SPD_CLIMBOUT, "kts") - PerfCalc.convertKts(SPD_LIFTOFF, "kts"))/acc;
            //d = (PerfCalc.convertKts(SPD_LIFTOFF, "kts") * t) + ((acc / 2) * pow(t,2));

            startWpt = latPlan.getWpt(1);
            //endWpt = latPlan.getItmWpt(startWpt,d);

            velEvent = new EventChgAirspeed();
            velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            //velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(SPD_LIFTOFF, "kts"));
            velEvent.setvAsf(PerfCalc.convertKts(SPD_CLIMBOUT, "kts"));
            velEvent.setAcc(acc);
            velEvents.add(velEvent);

            //////////////////////////////////////////////////////////////////////
            // 03 Create event acceleration to transition speed
            //////////////////////////////////////////////////////////////////////
            //startWpt = latPlan.getItmWpt(velEvents.get(1).getEndPt(),2500);
            startWpt = this.climboutFix;
            acc = 0.50;

            velEvent = new EventChgAirspeed();
            velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(SPD_CLIMBOUT, "kts"));
            velEvent.setvAsf(PerfCalc.convertKts(SPD_TRANS, "kts"));
            velEvent.setAcc(acc);
            velEvents.add(velEvent);

            if(crzAlt > PerfCalc.convertFt(ALT_TRANSITION,"ft")){
                //////////////////////////////////////////////////////////////////////
                // 04 Create event acceleration to climb speed
                //////////////////////////////////////////////////////////////////////

                // Calculate distances
                acc = 0.35;
                t = (PerfCalc.convertKts(SPD_CLIMB_CRUISE, "kts") - PerfCalc.convertKts(SPD_TRANS, "kts"))/acc;
                d = (PerfCalc.convertKts(SPD_TRANS, "kts") * t) + ((acc / 2) * pow(t,2));

                startWpt = vertEvents.get(2).getStartPt();
                endWpt = latPlan.getItmWpt(startWpt,d);

                velEvent = new EventChgAirspeed();
                velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                velEvent.setvAsi(PerfCalc.convertKts(SPD_TRANS, "kts"));
                velEvent.setvAsf(PerfCalc.convertKts(SPD_CLIMB_CRUISE, "kts"));
                //velEvent.setAcc(acc);
                velEvents.add(velEvent);

                //////////////////////////////////////////////////////////////////////
                // 05 Create event acceleration to cruise speed
                //////////////////////////////////////////////////////////////////////
                //startWpt = vertEvents.get(2).getStartPt();
                startWpt = endWpt;
                endWpt = vertEvents.get(2).getEndPt();

                velEvent = new EventChgAirspeed();
                velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
                velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                velEvent.setvAsi(PerfCalc.convertKts(SPD_CLIMB_CRUISE, "kts"));
                velEvent.setvAsf(PerfCalc.convertKts(SPD_CRUISE, "kts"));
                velEvent.setAcc(Double.NaN);
                velEvents.add(velEvent);
            }

            //////////////////////////////////////////////////////////////////////
            // Create event deceleration to transition speed
            //////////////////////////////////////////////////////////////////////
            if(crzAlt > PerfCalc.convertFt(ALT_TRANSITION,"ft")){
                // Calc distance backwards from final approach fix on default descent slope
                velDist = (PerfCalc.convertFt(ALT_TRANSITION,"ft") - this.destAlt) / sin(toRadians(ALPHA_DESC_DEF_DEG));
                endWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), velDist * -1);

                vertGrndDist = (this.crzAlt - PerfCalc.convertFt(ALT_TRANSITION,"ft")) / sin(toRadians(ALPHA_DESC_DEF_DEG));
                startWpt = latPlan.getItmWpt(endWpt,vertGrndDist * -1);

                velEvent = new EventChgAirspeed();
                velEvent.setStartPt(startWpt.getLat(),startWpt.getLon());
                velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
                velEvent.setvAsi(PerfCalc.convertKts(SPD_CRUISE, "kts"));
                velEvent.setvAsf(PerfCalc.convertKts(SPD_TRANS, "kts"));
                velEvent.setAcc(Double.NaN);
                velEvents.add(velEvent);
            }

            //////////////////////////////////////////////////////////////////////
            // Create event deceleration to initial approach speed
            //////////////////////////////////////////////////////////////////////
            // latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), fafDist * -1)
            // endWpt = latPlan.getItmWpt(this.finalAppFix, (DECEL_APP_FIX_INIT_APP_SPEED_DIST * -1));
            endWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), ((DECEL_APP_FIX_INIT_APP_SPEED_DIST + FINAL_APP_FIX_DIST) * -1));
            acc = -0.35;

            velEvent = new EventChgAirspeed();
            velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(SPD_TRANS, "kts"));
            velEvent.setvAsf(PerfCalc.convertKts(FLAPS_1_SPD - 5, "kts"));
            velEvent.setAcc(acc);
            velEvents.add(velEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event deceleration to final approach fix speed
            //////////////////////////////////////////////////////////////////////
            //endWpt = latPlan.getItmWpt(this.finalAppFix, (DECEL_APP_FIX_FINAL_APP_SPEED_DIST * -1));
            endWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), ((DECEL_APP_FIX_FINAL_APP_SPEED_DIST + FINAL_APP_FIX_DIST) * -1));
            acc = -0.40;

            velEvent = new EventChgAirspeed();
            velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(FLAPS_1_SPD - 5, "kts"));
            velEvent.setvAsf(PerfCalc.convertKts(SPD_FINAL_APP_FIX, "kts"));
            velEvent.setAcc(acc);
            velEvents.add(velEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event deceleration to final approach speed
            //////////////////////////////////////////////////////////////////////
            //startWpt = this.finalAppFix;
            startWpt = latPlan.getItmWpt(latPlan.getWpt(latPlan.getWptSize() - 2), (FINAL_APP_FIX_DIST * -1));
            acc = -0.45;

            velEvent = new EventChgAirspeed();
            velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(SPD_FINAL_APP_FIX, "kts"));
            velEvent.setvAsf(PerfCalc.convertKts(SPD_APP, "kts"));
            velEvent.setAcc(acc);
            velEvents.add(velEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event deceleration to taxi speed
            //////////////////////////////////////////////////////////////////////
            startWpt = latPlan.getWpt(latPlan.getWptSize() - 2); // vertEvents.get(vertEvents.size() - 2).getEndPt();
            endWpt = latPlan.getWpt(latPlan.getWptSize() - 1);

            velEvent = new EventChgAirspeed();
            velEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            velEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            velEvent.setvAsi(PerfCalc.convertKts(SPD_APP, "kts"));
            velEvent.setvAsf(velocity.get(1));
            velEvents.add(velEvent);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    private void prepareWeatherEvents(){
        try{
            EventChgWeather wxEvent;
            Waypoint startWpt, endWpt;

            //////////////////////////////////////////////////////////////////////
            // Create event turbulence takeoff
            //////////////////////////////////////////////////////////////////////
            startWpt = vertEvents.get(0).getStartPt();
            endWpt = latPlan.getItmWpt(startWpt, ALT_TRANSITION);

            wxEvent = new EventChgWeather();
            wxEvent.setStartPt(startWpt.getLat(), startWpt.getLon());
            wxEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            wxEvent.setTurbulence(CAT_MAGN_TAKEOFF, CAT_FREQ_TAKEOFF);
            wxEvent.setWind(0, 0);
            wxEvents.add(wxEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event turbulence cruise
            //////////////////////////////////////////////////////////////////////
            startWpt = latPlan.getItmWpt(startWpt, ALT_TRANSITION);
            endWpt = latPlan.getItmWpt(this.finalAppFix, ALT_TRANSITION * -1);

            wxEvent = new EventChgWeather();
            wxEvent.setStartPt(startWpt.getLat(),startWpt.getLon());
            wxEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            wxEvent.setTurbulence(CAT_MAGN_CRUISE, CAT_FREQ_CRUISE);
            wxEvent.setWind(0, 0);
            wxEvents.add(wxEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event turbulence initial approach
            //////////////////////////////////////////////////////////////////////
            startWpt = latPlan.getItmWpt(this.finalAppFix, ALT_TRANSITION * -1);
            endWpt = this.finalAppFix;

            wxEvent = new EventChgWeather();
            wxEvent.setStartPt(startWpt.getLat(),startWpt.getLon());
            wxEvent.setEndPt(endWpt.getLat(), endWpt.getLon());
            wxEvent.setTurbulence(CAT_MAGN_INIT_APP, CAT_FREQ_INIT_APP);
            wxEvent.setWind(0, 0);
            wxEvents.add(wxEvent);

            //////////////////////////////////////////////////////////////////////
            // Create event turbulence final approach
            //////////////////////////////////////////////////////////////////////
            startWpt = this.finalAppFix;
            endWpt = vertEvents.get(vertEvents.size()-2).getEndPt();

            wxEvent = new EventChgWeather();
            wxEvent.setStartPt(startWpt.getLat(),startWpt.getLon());
            wxEvent.setEndPt(endWpt.getLat(),endWpt.getLon());
            wxEvent.setTurbulence(CAT_MAGN_FINAL_APP, CAT_FREQ_FINAL_APP);
            wxEvent.setWind(0,0);
            wxEvents.add(wxEvent);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the set of vertical, velocity and weather events created by the transform method. The
     * method specifically checks for overlaps and mismatches between consecutive segments.
     *
     */
    public void validate(){
        try{
            // Check for negative distances between start and end points of consecutive events

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

}
