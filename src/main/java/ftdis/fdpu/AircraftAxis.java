package ftdis.fdpu;

//import sun.misc.Perf;

import java.util.ArrayList;

import static java.lang.Math.*;
import static ftdis.fdpu.PerfCalc.*;
import static ftdis.fdpu.Config.*;

/**
 * The AircraftAxis class represents the aircraft's axis, i.e. roll/bank, pitch and heading along and contains
 * a set of methods to calculate the axis' positions along the flight track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class AircraftAxis {
    public int id;
    private Lateral lateral;
    private Velocity velocity;
    private Vertical vertical;
    private Weather weather;
    private AircraftControl control;
    private double startPitch;
    private double targetPitch;
    private double targetPitchDist;
    private double pitchRate;
    private boolean pitchChangeComplete;
    private Waypoint pitchEndWpt;

    /**
     * Constructor(s)
     */
    AircraftAxis(){
        this.id = 0;
        this.pitchRate = 0;
        this.startPitch = 0;
        this.targetPitch = 0;
        this.targetPitchDist = 0;
        this.pitchChangeComplete = true;
        this.pitchEndWpt = new Waypoint();
    }

    AircraftAxis(Integer aircraftID){
        this();
        this.id = aircraftID;
    }

    /**
     * This method assigns a lateral plan/track to the aircraft axis.
     *
     * @param lateral   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral lateral){
        this.lateral = lateral;
    }

    /**
     * This method assigns a velocity plan/track to the aircraft axis.
     *
     * @param velocity   Reference to a lateral plan/track.
     */
    public void assignVel(Velocity velocity){
        this.velocity = velocity;
    }

    /**
     * This method assigns a vertical plan/track to the aircraft axis.
     *
     * @param vertical   Reference to a lateral plan/track.
     */
    public void assignVert(Vertical vertical){
        this.vertical = vertical;
    }

    /**
     * This method assigns a weather plan/track to the aircraft axis.
     *
     * @param wx   Reference to a weather plan/track.
     */
    public void assignWx(Weather wx){
        this.weather = wx;
    }

    /**
     * This method assigns the aircraft's control to the aircraft axis.
     *
     * @param ctrl   Reference to aircraft control.
     */
    public void assignControl(AircraftControl ctrl){
        this.control = ctrl;
    }

    /**
     * This method returns the aircraft's bank angle at a given waypoint along the lateral track. The data provided by the method
     * is leveraged to adjust the aircraft's corresponding aileron position.
     *
     * @param   wpt Waypoint along the lateral track
     * @return  Bank angle in degrees
     */
    public double getBankAngleAtWpt(Waypoint wpt) {
        try{
            int sigmoidSlope = BANK_SIGM_SLOPE;
            double vAsu, rollRate, rollT, wptT, wptDist, sgmtT, sgmtDist, sgmtAngle, sgmtWx, wptAngle, wptAileron, wptWx, scale;
            LateralSegment refSgmt;

            refSgmt = lateral.getWptSgmt(wpt);
            wptAngle = 0;
            control.setAileron(0);

            // Calculate basic turn segment performance values
            vAsu = velocity.getVasu(refSgmt.getStartPt(), refSgmt.getEndPt());
            sgmtDist = refSgmt.getDist();
            sgmtT = sgmtDist/vAsu;
            rollRate = PerfCalc.getRollRateVas(vAsu);

            // Calculate wpt specific performance values
            wptDist = lateral.getDist(refSgmt.getStartPt(), wpt);
            wptT = wptDist/vAsu;

            if(refSgmt instanceof TurnSegment){

                // Calculate target bank angle
                sgmtAngle = rollRate * sgmtT / 2;
                if(sgmtAngle > PerfCalc.getBankAngleVas(vAsu))
                    sgmtAngle = PerfCalc.getBankAngleVas(vAsu);

                rollT = sgmtAngle/rollRate;
                scale = sgmtAngle/PerfCalc.getBankAngleVas(vAsu);

                // Check whether aircraft is in turn initiation, turn termination phase
                if(wptT <= rollT) {
                    // Calculate bank angle
                    wptAngle = rollRate * wptT;

                    // Calculate aileron pos
                    wptAileron = sin((wptT / rollT) * PI) * PerfCalc.getAileronTargetVas(vAsu) * scale;
                    wptAileron = PerfCalc.getSigmoidVal(PerfCalc.getAileronTargetVas(vAsu),wptAileron,sigmoidSlope);
                    control.setAileron(wptAileron);

                }else if(wptT >= sgmtT - rollT){
                    // Calculate bank angle
                    wptAngle = rollRate * (sgmtT - wptT);

                    // Calculate aileron position
                    wptAileron = sin((sgmtT - wptT) / rollT * PI) * PerfCalc.getAileronTargetVas(vAsu) * scale;
                    wptAileron = PerfCalc.getSigmoidVal(PerfCalc.getAileronTargetVas(vAsu),wptAileron,sigmoidSlope);
                    control.setAileron(wptAileron * -1);
                }else{
                    wptAngle = sgmtAngle;
                    control.setAileron(0);
                }

                // To increase smoothness of start/end roll, realign wptAngle to Sigmoid function
                wptAngle = PerfCalc.getSigmoidVal(sgmtAngle, wptAngle, sigmoidSlope);

                // Adjust bank angle according to direction of turn, i.e. left turn = negative, right turn = positive angle
                if(((TurnSegment) refSgmt).getCourseChange() < 0) {
                    wptAngle *= -1;
                    control.setAileron(control.getAileron() * -1);
                }
            }

            // Adjust bank angle by CATs (Clear Air Turbulence) effect
            wptWx = weather.getBankDeviationAtWpt(wpt);

            if(wptWx != 0){
                sgmtWx = weather.getCatTargetValueAtWpt(wpt, "bank");
                wptAngle += PerfCalc.getSigmoidVal(sgmtWx,wptWx,sigmoidSlope);

                // Adjust controls by CATS (Clear Air Turbulence) effect
                //scale = sgmtWx/8;
                scale = sgmtWx/PerfCalc.getBankAngleVas(vAsu);

                if(weather.getCatDistAtWpt(wpt, "bank") <= 0.5)
                    wptAileron = sin(weather.getCatDistAtWpt(wpt, "bank") * 2 * PI) * PerfCalc.getAileronTargetVas(vAsu) * scale;
                else
                    wptAileron = sin((weather.getCatDistAtWpt(wpt, "bank") - 0.5) * 2 * PI) * PerfCalc.getAileronTargetVas(vAsu) * scale * -1;

                wptAileron = PerfCalc.getSigmoidVal(PerfCalc.getAileronTargetVas(vAsu), wptAileron, sigmoidSlope);
                control.setAileron(control.getAileron() + wptAileron);
            }

            return wptAngle;
        }catch (Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the aircraft's heading at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @return  Heading in degrees
     */
    public double getHeadingAtWpt(Waypoint wpt) {
        try{
            int sigmoidSlope;
            double wptCrs,wptCrsChg,sgmtCrsChg, vAs;
            LateralSegment refSgmt;

            refSgmt = lateral.getWptSgmt(wpt);
            wptCrs = lateral.getCourseAtWpt(wpt);
            vAs = velocity.getVasAtWpt(wpt);

            if (vAs <= convertKts(25,"kts"))
                sigmoidSlope = TURN_SIGM_SLOPE_0_25;
            else if (vAs > convertKts(25,"kts") && vAs <= convertKts(210,"kts"))
                sigmoidSlope = TURN_SIGM_SLOPE_25_210;
            else if (vAs > convertKts(210,"kts") && vAs <= convertKts(250,"kts"))
                sigmoidSlope = TURN_SIGM_SLOPE_210_250;
            else
                sigmoidSlope = TURN_SIGM_SLOPE_250_999;

            // To increase smoothness of start/end turn, realign wpt course to Sigmoid function
            if(refSgmt instanceof TurnSegment){
                sgmtCrsChg = ((TurnSegment) refSgmt).getCourseChange();
                wptCrsChg = NavCalc.getCourseChange(((TurnSegment) refSgmt).getCourseStart(), wptCrs);
                wptCrs = PerfCalc.getSigmoidVal(abs(sgmtCrsChg), abs(wptCrsChg), sigmoidSlope);

                // Adjust course according to direction of turn, i.e. left turn = negative, right turn = positive course change
                if(wptCrsChg < 0)
                    wptCrs *= -1;

                wptCrs = NavCalc.getNewCourse(((TurnSegment) refSgmt).getCourseStart(),wptCrs);
            }
            return wptCrs;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the aircraft's pitch at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @return  Pitch in degrees
     */
    public double getPitchAngleAtWpt(Waypoint wpt){
        try{
            int s, l, sigmoidSlope = PITCH_SIGM_SLOPE;
            double startPitch, targetPitch, targetDist, wptDist, pitchChg, vAsu, wptPitch = 0, pitchDelayRatio = 0, refAlpha = 0;
            VerticalSegment thisSgmt, nextSgmt, sgmtStart, sgmtEnd;
            Waypoint endWpt;

            // Identify vertical segments
            thisSgmt = vertical.getWptSgmt(wpt);
            l = vertical.getSgmtCount();
            s = vertical.getSgmtPos(thisSgmt);


            // The pitch transition starts at the end of segment n (sgmtStart) and finishes in the succeeding segment
            // n+1 (sgmtEnd). Hence, the method must consider that one transition is executed across two segments.
            //
            //  Segment n         Start Trans.      Segment n+1     End Transition
            // X------------------S================X================E------------------X
            //                    |----------------|----------------|
            //                         sgmtStart        sgmtEnd
            //                  pitch   0 deg     >>>      10 deg
            //
            // The pitchDelayRatio defines the distribution of the pitch transition across the two segments, i.e.
            //
            // pitchDelayRatio 1.0 : Pitch transition starts at start point of sgmtEnd
            // pitchDelayRatio 0.5 : Pitch transition start in sgmtStart and ends in sgmtEnd
            // pitchDelayRatio 0.0 : Pitch transition ends at end point of sgmtStart
            //

            if((lateral.getDist(thisSgmt.getStartPt(),wpt) < lateral.getDist(wpt,thisSgmt.getEndPt()) && s != 0) || s == (l-1))
                s -= 1;

            sgmtStart = vertical.getSgmt(s);
            sgmtEnd = vertical.getSgmt(s+1);

            if(s < l-1){
                //nextSgmt = vertical.getSgmt(s+1);

                // Define start and target pitch
                //if(this.pitchChangeComplete){
                    if(s == 0){
                        // Takeoff
                        this.pitchRate = PITCH_RATE_TAKEOFF;
                        this.startPitch = PITCH_GROUND;
                        this.targetPitch = toDegrees(sgmtEnd.getAlpha()) + PerfCalc.getAngleOfAttack(velocity.getVasAtWpt(sgmtEnd.getStartPt()),1.0);
                        pitchDelayRatio = 0.65;
                    } else if(s > 0 && s < l-6) {
                        // FlightS
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = toDegrees(sgmtStart.getAlpha()) + PerfCalc.getAngleOfAttack(velocity.getVasAtWpt(sgmtStart.getStartPt()),1.0);
                        this.targetPitch = toDegrees(sgmtEnd.getAlpha()) + PerfCalc.getAngleOfAttack(velocity.getVasAtWpt(sgmtEnd.getStartPt()),1.0);
                        pitchDelayRatio = 0.5;
                    } else if(s == l-6) {
                        // Level Off Final Approach
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = toDegrees(sgmtStart.getAlpha()) + PerfCalc.getAngleOfAttack(velocity.getVasAtWpt(sgmtStart.getStartPt()), 1.0);
                        this.targetPitch = PITCH_CRUISE;
                        pitchDelayRatio = 0.5;
                    } else if(s == l-5) {
                        // Descent Final Approach
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = PITCH_CRUISE;
                        this.targetPitch = PITCH_FINAL_APP;
                        pitchDelayRatio = 0.5;
                    } else if(s == l-4) {
                        // Retard
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = PITCH_FINAL_APP;
                        this.targetPitch = PITCH_RETARD;
                        pitchDelayRatio = 0.25;
                    } else if(s == l-3) {
                        // Flare
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = PITCH_RETARD;
                        this.targetPitch = PITCH_FLARE;
                        pitchDelayRatio = 0.25;
                    } else if(s == l-2) {
                        // Nose Down after landing
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(sgmtEnd.getStartPt()));
                        this.startPitch = PITCH_FLARE;
                        this.targetPitch = PITCH_GROUND;
                        pitchDelayRatio = 1.0;
                    } else {
                        // Ground
                        this.pitchRate = PerfCalc.getPitchRateVas(velocity.getVasAtWpt(wpt));
                        this.startPitch = PITCH_GROUND;
                        this.targetPitch = PITCH_GROUND;
                    }

                    // Calc dist required to complete pitch change
                    this.targetPitchDist = abs(velocity.getDist(sgmtStart.getEndPt(), abs(this.targetPitch - this.startPitch) / this.pitchRate));

                    // Move end waypoint forward by delay ratio
                    this.pitchEndWpt = lateral.getItmWpt(sgmtStart.getEndPt(), this.targetPitchDist * pitchDelayRatio);

                    //this.pitchChangeComplete = false;
                //}
            }

            wptDist = lateral.getDist(wpt,this.pitchEndWpt);

            // Check if wpt is in pitch transition section
            if(wptDist > 0 && wptDist <= this.targetPitchDist){

                wptDist = this.targetPitchDist - wptDist;

                pitchChg = abs(this.targetPitch - this.startPitch) * (wptDist / this.targetPitchDist);
                pitchChg = PerfCalc.getSigmoidVal(abs(this.targetPitch - this.startPitch), pitchChg, sigmoidSlope);

                // Adjust for negative pitch and catch "over pitch"
                if (this.targetPitch - this.startPitch < 0){
                    pitchChg *= -1;

                    if(this.startPitch + pitchChg < this.targetPitch)
                        wptPitch = this.targetPitch;
                    else
                        wptPitch = this.startPitch + pitchChg;
                } else {
                    if(this.startPitch + pitchChg > this.targetPitch)
                        wptPitch = this.targetPitch;
                    else
                        wptPitch = this.startPitch + pitchChg;
                }

            } else if (wptDist > this.targetPitchDist)
                wptPitch = this.startPitch;
            else if (wptDist <= 0)
                wptPitch = this.targetPitch;




            /******************************************************************************************************************
             *  25/08/19    Revised code for multi threading. Kept old code for testing / revision.
             ******************************************************************************************************************
            wptPitch = this.startPitch;

            if(this.startPitch != this.targetPitch){

                wptDist = this.targetPitchDist - lateral.getDist(wpt, this.pitchEndWpt);

                if(wptDist > 0){
                    // Calculate pitch at waypoint
                    pitchChg = abs(this.targetPitch - this.startPitch) * (wptDist / this.targetPitchDist);
                    pitchChg = PerfCalc.getSigmoidVal(abs(this.targetPitch - this.startPitch), pitchChg, sigmoidSlope);

                    // Adjust for negative pitch and catch "over pitch"
                    if (this.targetPitch - this.startPitch < 0){
                        pitchChg *= -1;

                        if(this.startPitch + pitchChg < this.targetPitch)
                            wptPitch = this.targetPitch;
                        else
                            wptPitch = this.startPitch + pitchChg;
                    } else {
                        if(this.startPitch + pitchChg > this.targetPitch)
                            wptPitch = this.targetPitch;
                        else
                            wptPitch = this.startPitch + pitchChg;
                    }

                    //if(lateral.getDist(wpt, this.pitchEndWpt) <= 0.01)
                    //    this.pitchChangeComplete = true;
                }
            } else
                wptPitch = this.startPitch;
            */

            // *********************************************
            // Revised 02/11/18 Angle of attack too extreme at most cases
            // Adjust pitch by angle of attack
            // wptPitch += PerfCalc.getAngleOfAttack(velocity.getVasAtWpt(wpt),1.0);
            // *********************************************

            // Adjust pitch angle by CATs (Clear Air Turbulence) effect
            wptPitch += weather.getPitchDeviationAtWpt(wpt);

            return wptPitch;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the aircraft's altitude at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @return  Altitude in meters
     */
    public double getAltAtWpt(Waypoint wpt){
        try{
            int s, l;
            double wptAltChg = 0, sgmtAltChg = 0, wptAlt;
            VerticalSegment thisSgmt;

            // Get segment, position and altitude at waypoint
            thisSgmt = vertical.getWptSgmt(wpt);
            s = vertical.getSgmtPos(thisSgmt);
            l = vertical.getSgmtCount();
            wptAlt = vertical.getAltAtWpt(wpt);

            if(thisSgmt.getVs() != 0){
                // Adjust slope for takeoff / rotate segment
                if(s == 1) {
                    sgmtAltChg = thisSgmt.getAltf() - thisSgmt.getAlti();
                    wptAltChg = wptAlt - thisSgmt.getAlti();
                    wptAlt = PerfCalc.getAccVal(sgmtAltChg, wptAltChg, ALT_SIGM_SLOPE_TAKEOFF);

                // Adjust slope for climb segments
                } else if(s > 1 && s < l-2 && thisSgmt.getVs() > 0){
                    sgmtAltChg = thisSgmt.getAltf() - thisSgmt.getAlti();
                    wptAltChg = wptAlt - thisSgmt.getAlti();

                    // Check for end of climb segments
                    if(vertical.getSgmt(s+1).getVs() == 0){
                        wptAlt = PerfCalc.getDecVal(sgmtAltChg,wptAltChg,ALT_SIGM_SLOPE_DEF);
                    } else
                        wptAlt = wptAltChg;

                // Adjust slope for descent segments
                } else if(s > 1 && s < l-2 && thisSgmt.getVs() < 0) {
                    sgmtAltChg = thisSgmt.getAlti() - thisSgmt.getAltf();
                    wptAltChg = thisSgmt.getAlti() - wptAlt;

                    // Check for start of descent segments
                    if(vertical.getSgmt(s-1).getVs() == 0){
                        wptAlt = PerfCalc.getAccVal(sgmtAltChg,wptAltChg,ALT_SIGM_SLOPE_DEF);
                    } else
                        wptAlt = wptAltChg;

                // Addjust slope for flare segment
                }else if (s == l-2) {
                    sgmtAltChg = thisSgmt.getAlti() - thisSgmt.getAltf();
                    wptAltChg = thisSgmt.getAlti() - wptAlt;
                    wptAlt = PerfCalc.getDecVal(sgmtAltChg, wptAltChg, ALT_SIGM_SLOPE_FLARE);
                }

                // Adjust altitude according to type of vertical change, i.e. descent = negative, climb = positive angle
                if(thisSgmt.getVs() < 0)
                    wptAlt *= -1;

                wptAlt = thisSgmt.getAlti() + wptAlt;

            }

            // Adjust altitude by CATs (Clear Air Turbulence) effect
            wptAlt += weather.getAltDeviationAtWpt(wpt);

            return wptAlt;

        }catch(Exception e){
            return Double.NaN;
        }

    }
}
