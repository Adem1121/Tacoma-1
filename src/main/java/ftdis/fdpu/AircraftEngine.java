package ftdis.fdpu;

import static java.lang.Math.*;
import static ftdis.fdpu.Config.*;

/**
 * The AircraftEngine class represents the aircraft's engines and contains a set of methods to calculate
 * the engines' parameters along the flight track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.2
 */
public class AircraftEngine {
    public int id;
    private Lateral lateral;
    private Velocity velocity;
    private Vertical vertical;
    private Weather weather;
    private double engineVal;

    /**
     * Constructor(s)
     */
    AircraftEngine(){
        this.id = 0;
        this.engineVal = 0.0; //0.20;
    }

    AircraftEngine(Integer aircraftID){
        this();
        this.id = aircraftID;
    }

    /**
     * This method assigns a lateral plan/track to the aircraft control.
     *
     * @param lateral   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral lateral){
        this.lateral = lateral;
    }

    /**
     * This method assigns a velocity plan/track to the aircraft control.
     *
     * @param velocity   Reference to a lateral plan/track.
     */
    public void assignVel(Velocity velocity){
        this.velocity = velocity;
    }

    /**
     * This method assigns a vertical plan/track to the aircraft control.
     *
     * @param vertical   Reference to a lateral plan/track.
     */
    public void assignVert(Vertical vertical){
        this.vertical = vertical;
    }

    /**
     * This method assigns a weather plan/track to the aircraft control.
     *
     * @param wx   Reference to a weather plan/track.
     */
    public void assignWx(Weather wx){
        this.weather = wx;
    }


    /**
     * This method returns the aircraft's engine target value at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @return  Engine target value in percent
     */
    public double getEngineTargetValAtWpt(Waypoint wpt){
        try{
            VelocitySegment thisSgmt, nextSgmt;
            double alt, alpha, engineTargetVal, thrustRedAlt, gearDownAlt, transitionAlt, variationDelta, waitSpd = 1E-2;
            int thisSgmtPos;

            // Define parameters
            thrustRedAlt = PerfCalc.convertFt(ALT_THRUST_RED, "ft");
            transitionAlt = PerfCalc.convertFt(ALT_TRANSITION, "ft");
            gearDownAlt = PerfCalc.convertFt(ALT_GEAR_DOWN, "ft");
            variationDelta = ENG_VAR_APP;

            // Initialize variables
            alt = vertical.getAltAtWpt(wpt);
            alpha = vertical.getAlphaAtWpt(wpt);

            // Determine standard engine settings for climb, descent and cruise
            if(alpha > 0)
                // Climb
                engineTargetVal = ENG_CLIMB_N1;
            else if(alpha < 0)
                // Descent
                engineTargetVal = ENG_DESC_N1;
            else {
                if ((lateral.getDist(lateral.getStartWpt(), wpt) <= OUTBOUND_SECTION_DIST && alt == vertical.getAltAtWpt(lateral.getStartWpt()))
                        || (lateral.getDist(wpt, lateral.getEndWpt()) <= INBOUND_SECTION_DIST && alt == vertical.getAltAtWpt(lateral.getEndWpt()))) {
                    // Taxi
                    engineTargetVal = ENG_TAXI_N1; // RANDOMIZE +/- 5%

                    // Check for acceleration during taxi
                    thisSgmt = velocity.getWptSgmt(wpt);
                    thisSgmtPos = velocity.getSgmtPos(thisSgmt);

                    if(thisSgmtPos < velocity.getSgmtCount() - 1)
                        nextSgmt = velocity.getSgmt(thisSgmtPos + 1);
                    else
                        nextSgmt = null;

                    // Normal acceleration
                    if (velocity.getWptSgmt(wpt).getAcc() > 0)
                        engineTargetVal = ENG_TAXI_ACC_N1;
                    // Acceleration from stand still. Account for inertia (Fixed 3 seconds)
                    else if(nextSgmt != null && thisSgmt.getAcc() == 0 && thisSgmt.getVasf() == waitSpd && nextSgmt.getAcc() > 0) {
                        // Calculate remaining time
                        if(velocity.getTime(wpt,thisSgmt.getEndPt()) < ENG_TAXI_INERTIA_DELAY)
                            engineTargetVal = ENG_TAXI_ACC_N1;
                    }
                } else
                    // Cruise
                    engineTargetVal = ENG_CRUISE_N1;
            }

            // Determine specific engine settings for takeoff, final approach and rollout
            if(lateral.getDist(lateral.getStartWpt(),wpt) <= OUTBOUND_SECTION_DIST && velocity.getWptSgmt(wpt).getVasf() >= PerfCalc.convertKts(80,"kts") && (alt - vertical.getAltAtWpt(lateral.getStartWpt())) <= thrustRedAlt){
                // Takeoff
                if(lateral.getDist(lateral.getStartWpt(),wpt) <= ENG_TAKEOFF_40_N1_DIST)
                    // 40%
                    engineTargetVal = ENG_TAKEOFF_40_N1;
                else
                    // TO thrust
                    engineTargetVal = ENG_TAKEOFF_THRUST_N1;
            } else if(lateral.getDist(wpt,lateral.getEndWpt()) <= INBOUND_SECTION_DIST && velocity.getVasAtWpt(wpt) >= PerfCalc.convertKts(80,"kts") && (alt - vertical.getAltAtWpt(lateral.getEndWpt())) <= gearDownAlt && (alt - vertical.getAltAtWpt(lateral.getEndWpt())) > 0){
                // Final approach
                engineTargetVal = ENG_FINAL_APP_N1;

                // Calculate throttle/thrust variation during approach
                engineTargetVal += variationDelta * randDbl(-1,1);

                // *********************************************
                // Revised 02/11/18 Testing
                //if(vertical.getSgmtPos(vertical.getWptSgmt(wpt)) == (vertical.getSgmtCount() - 3))
                // *********************************************
                //if(vertical.getAltAtWpt(wpt) <= PerfCalc.convertFt(ALT_FLARE,"ft"))
                if ((alt - vertical.getAltAtWpt(lateral.getEndWpt())) < PerfCalc.convertFt(ALT_FLARE,"ft"))
                    // Flare: Idle
                    engineTargetVal = ENG_IDLE_N1;

            } else if(lateral.getDist(wpt, lateral.getEndWpt()) <= INBOUND_SECTION_DIST && velocity.getVasAtWpt(wpt) >= PerfCalc.convertKts(20,"kts") && (alt - vertical.getAltAtWpt(lateral.getEndWpt())) == 0){
                // Rollout: Reverse
                if(lateral.getDist(lateral.getWptSgmt(wpt).getStartPt(), wpt) > 50 && velocity.getVasAtWpt(wpt) >= PerfCalc.convertKts(80,"kts"))
                    engineTargetVal = (1 + ENG_REVERSE_N1) * -1;
                else if(lateral.getDist(lateral.getWptSgmt(wpt).getStartPt(), wpt) > 50 && velocity.getVasAtWpt(wpt) >= PerfCalc.convertKts(20,"kts"))
                    engineTargetVal = -1;
                else
                    engineTargetVal = ENG_IDLE_N1;
            } else if(lateral.getDist(wpt,lateral.getEndWpt()) <= INBOUND_SECTION_DIST && velocity.getVasAtWpt(wpt) >= PerfCalc.convertKts(80,"kts") && (alt - vertical.getAltAtWpt(lateral.getEndWpt())) > gearDownAlt && (alt - vertical.getAltAtWpt(lateral.getEndWpt())) <= transitionAlt && alpha == 0){
                // Approach: Level
                engineTargetVal = ENG_PRE_APP_N1;
            }

            return engineTargetVal;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method sets the aircraft's engine val.
     *
     */
    public void setEngineVal(double engineVal) {
        try{
            this.engineVal = engineVal;
        }catch (Exception e){
            this.engineVal = 0;
        }
    }

    /**
     * @return Engine value in percent
     */
    public double getEngineVal(){
        return this.engineVal;
    }

    /**
     * This method returns the aircraft's engine  value at a given waypoint along the lateral track
     *
     * @return  Engine  value in percent
     */
    public double getEngineValAtWpt(Waypoint wpt, double cycleLen){
        try{
            double engineVal, engineTargetVal, engineRate, thrustDelay;

            // *********************************************
            // Revised 08/12/18 Added logic to include delay between engine thrust
            // and start of aircraft acceleration.
            // Left legacy code as reference until testing is complete.
            // *********************************************

            // Move wpt to include delay between set of thrust and start of acceleration
            thrustDelay = 3; // 3 second delay

            if(vertical.getSgmtPos(vertical.getWptSgmt(wpt)) < vertical.getSgmtCount() - 2)
                wpt = lateral.getItmWpt(wpt,velocity.getDist(wpt,thrustDelay));

            // Define parameters
            engineRate = ENG_RATE_N1;
            engineVal = this.getEngineVal();
            engineTargetVal = this.getEngineTargetValAtWpt(wpt);

            // Bridge gap between engine idle and -1, i.e. start of reverse thrust
            if(engineTargetVal < -1 && engineVal >= 0)
                engineVal = -1;
            else if(abs(engineVal + 1) < 0.1 && engineTargetVal >= 0)
                engineVal = ENG_IDLE_N1;

            // Change engine rate when reverse thrust is engaged
            if(engineVal <= -1)
                engineRate = 0.25;

            if(engineVal != engineTargetVal) {
                // Determine and adjust engine rate
                if(abs(engineTargetVal-engineVal) < engineRate * cycleLen)
                    engineRate = abs(engineTargetVal-engineVal)/cycleLen;

                if(engineTargetVal < engineVal)
                    engineRate *= -1;

                // Determine engine val
                engineVal += engineRate * cycleLen;

                this.setEngineVal(engineVal);
            }
            return engineVal;
        }catch(Exception e){
            return 0;
        }
    }


    //********************************************************************************************************************

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     *
     * @param min   Minimum value
     * @param max   Maximum value.  Must be greater than min.
     * @return      Double value between min and max, inclusive.
     */
    private static double randDbl(double min, double max) {
        return min + Math.random() * (max - min);
    }

}
