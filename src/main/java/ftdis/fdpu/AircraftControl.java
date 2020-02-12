package ftdis.fdpu;

import static java.lang.Math.*;
import static ftdis.fdpu.Config.*;
import static ftdis.fdpu.PerfCalc.*;

/**
 * The AircraftControl class represents the aircraft's controls, i.e. ailerons, spoilers and flaps, and contains
 * a set of methods to calculate the controls' positions along the flight track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class AircraftControl {
    public int id;
    private Lateral lateral;
    private Velocity velocity;
    private Vertical vertical;
    private Weather weather;
    private double aileron;
    private double noseWheel;
    private double flaps, spoiler;

    /**
     * Constructor(s)
     */
    AircraftControl(){
        this.id = 0;
        this.aileron = 0;
        this.spoiler = 0;
        this.flaps = 0;
        this.noseWheel = 0;
    }

    AircraftControl(Integer aircraftID){
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
     * This method sets the aircraft's aileron position
     *
     */
    public void setAileron(double aileronPos) {
        try{
            this.aileron = aileronPos;
        }catch (Exception e){
            this.aileron = 0;
        }
    }

    /**
     * This method returns the aircraft's aileron position.
     *
     * @return  Aileron position in percent.
     */
    public double getAileron() {
        try{
            return this.aileron;
        }catch (Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method sets the aircraft's nose wheel position
     *
     */
    public void setNoseWheel(double noseWheelPos) {
        try{
            this.noseWheel = noseWheelPos;
        }catch (Exception e){
            this.noseWheel = 0;
        }
    }

    /**
     * This method returns the aircraft's nose wheel position.
     *
     * @return  Nose wheel position in degrees.
     */
    public double getNoseWheel() {
        try{
            return this.noseWheel;
        }catch (Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the aircraft's flaps position a given waypoint along the lateral track
     *
     * @param   wpt     Waypoint along the lateral track
     * @param   phase   Identifier for phase of flight, e.g. pushback, taxi, flight
     * @return  Flaps position in degrees
     */
    public int getFlapsAtWpt(Waypoint wpt, int phase){
        try{
            double vAs;
            int flapsDeg;

            vAs = velocity.getVasAtWpt(wpt);

            if(lateral.getDist(lateral.getStartWpt(),wpt) <= OUTBOUND_SECTION_DIST){
                if(vAs > PerfCalc.convertKts(FLAPS_TAKEOFF_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_1_SPD, "kts"))
                    flapsDeg = 1;
                else if(vAs <= PerfCalc.convertKts(FLAPS_TAKEOFF_SPD, "kts"))
                    //if(phase == 2 && lateral.getDist(lateral.getStartWpt(),wpt) < 0.01/4)
                    if(phase == 2 && velocity.getTime(lateral.getStartWpt(),wpt) < 30)
                        flapsDeg = 0;
                    else if(phase == 4 && lateral.getDist(lateral.getStartWpt(),wpt) >= FLAPS_UP_INBOUND_DIST)
                        flapsDeg = 0;
                    else if(phase == 4 && lateral.getDist(lateral.getStartWpt(),wpt) < FLAPS_UP_INBOUND_DIST)
                        flapsDeg = 30;
                    else if(phase == 5 || phase == 1)
                        flapsDeg = 0;
                    else
                        flapsDeg = 5;
                else
                    flapsDeg = 0;
            } else {
                if(vAs > PerfCalc.convertKts(FLAPS_5_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_1_SPD, "kts"))
                    flapsDeg = 1;
                else if(vAs > PerfCalc.convertKts(FLAPS_10_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_5_SPD, "kts"))
                    flapsDeg = 5;
                else if(vAs > PerfCalc.convertKts(FLAPS_15_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_10_SPD, "kts"))
                    flapsDeg = 10;
                else if(vAs > PerfCalc.convertKts(FLAPS_25_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_15_SPD, "kts"))
                    flapsDeg = 15;
                else if(vAs > PerfCalc.convertKts(FLAPS_30_SPD, "kts") && vAs <= PerfCalc.convertKts(FLAPS_25_SPD, "kts"))
                    flapsDeg = 25;
                else if(vAs >= PerfCalc.convertKts(0, "kts") && vAs <= PerfCalc.convertKts(FLAPS_30_SPD, "kts"))
                    flapsDeg = 30;
                else
                    flapsDeg = 0;
            }

            return flapsDeg;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the aircraft's spoiler position at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @param   phase   Identifier for phase of flight, e.g. pushback, taxi, flight
     * @return  Spoiler position in degrees, 0 (down), 1 (flt), or 2 (up)
     */
    public int getSpoilersAtWpt(Waypoint wpt, int phase){
        try{
            int spoilersDeg;

            // Deploy Spoilers for landing
            if(phase == 3 && vertical.getAltAtWpt(wpt) == vertical.getAltAtWpt(lateral.getEndWpt()) && lateral.getDist(wpt, lateral.getEndWpt()) <= INBOUND_SECTION_DIST)
                spoilersDeg = 2;
            else if(phase == 4 && vertical.getAltAtWpt(wpt) == vertical.getAltAtWpt(lateral.getEndWpt()) && lateral.getDist(lateral.getStartWpt(), wpt) <= (LIGHTS_LANDING_OFF_INBOUND_DIST - 5))
                spoilersDeg = 2;
            else
                spoilersDeg = 0;

            return spoilersDeg;
        } catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the aircraft's gear position along the flight track.
     *
     * @param wpt   Position of the aircraft along the flight track
     * @return      Gear position down (1), or up (0)
     */
    public int getGearAtWpt(Waypoint wpt) {
        try {
            double alt, depAlt, destAlt;
            int gearPos;

            alt = vertical.getAltAtWpt(wpt);
            depAlt = vertical.getAltAtWpt(lateral.getStartWpt());
            destAlt = vertical.getAltAtWpt(lateral.getEndWpt());

            if (lateral.getDist(lateral.getStartWpt(), wpt) <= OUTBOUND_SECTION_DIST && alt < (PerfCalc.convertFt(ALT_GEAR_UP, "ft") + depAlt)) {
                gearPos = 1;
            } else if (lateral.getDist(wpt, lateral.getEndWpt()) <= INBOUND_SECTION_DIST && alt < (PerfCalc.convertFt(ALT_GEAR_DOWN, "ft") + destAlt)) {
                gearPos = 1;
            } else
                gearPos = 0;

            return gearPos;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * This method returns the aircraft's nose wheel position along the lateral track.
     *
     * @param wpt   Position of the aircraft along the flight track
     * @return      Postion of nose wheel in degrees (-90 to +90 deg.)
     */
    public double getNoseWheelAtWpt(Waypoint wpt){
        try{
            int sigmoidSlope = TURN_SIGM_SLOPE_0_25;
            double wptNwDeg, wptDist, wptT, sgmtDist, sgmtT, vAsu, degLimit, turnRate, turnT, sgmtAngle, targetAngle, scale, wheelbase;
            LateralSegment refSgmt;

            //Get segment from lateral track and check for turn segment
            refSgmt = lateral.getWptSgmt(wpt);

            if(refSgmt instanceof TurnSegment){

                // Calculate basic turn segment performance values
                vAsu = velocity.getVasu(refSgmt.getStartPt(), refSgmt.getEndPt());
                sgmtDist = refSgmt.getDist();
                sgmtT = sgmtDist/vAsu;
                turnRate = AIRCRAFT_WHEELBASE_TURNRATE;
                wheelbase = AIRCRAFT_WHEELBASE;
                //targetAngle = 45; //+-45 degrees max angle
                targetAngle = toDegrees(asin(wheelbase/((TurnSegment) refSgmt).getRadius()));

                // Calculate wpt specific performance values
                wptDist = lateral.getDist(refSgmt.getStartPt(), wpt);
                wptT = wptDist/vAsu;

                // Calculate nose wheel target angle for turn segment in degrees
                sgmtAngle = turnRate * sgmtT / 2;
                if(sgmtAngle > targetAngle)
                    sgmtAngle = targetAngle;

                turnT = sgmtAngle/turnRate;
                //scale = sgmtAngle/targetAngle;

                // Check whether aircraft is in turn initiation, turn termination phase
                if(wptT <= turnT) {
                    // Calculate nose wheel angle
                    wptNwDeg = turnRate * wptT;
                    //wptNwDeg = sin((wptT / turnT) * PI) * targetAngle * scale;
                    //wptNwDeg = PerfCalc.getSigmoidVal(targetAngle,wptNwDeg,sigmoidSlope);
                    //this.setNoseWheel(wptNwDeg);
                } else if(wptT >= sgmtT - turnT){
                    // Calculate nose wheel angle
                    wptNwDeg = turnRate * (sgmtT - wptT);

                    //wptNwDeg = sin((sgmtT - wptT) / turnT * PI) * targetAngle * scale;
                    //wptNwDeg = PerfCalc.getSigmoidVal(targetAngle,wptNwDeg,sigmoidSlope);
                } else{
                    wptNwDeg = sgmtAngle;
                    //this.setNoseWheel(0);
                }

                // To increase smoothness of start/end roll, realign wptAngle to Sigmoid function
                wptNwDeg = PerfCalc.getSigmoidVal(sgmtAngle, wptNwDeg, sigmoidSlope);

                // Adjust nose wheel angle according to direction of turn, i.e. left turn = negative, right turn = positive angle
                if(((TurnSegment) refSgmt).getCourseChange() < 0) {
                    wptNwDeg *= -1;
                }

            } else
                wptNwDeg = 0;

            this.setNoseWheel(wptNwDeg);

            return this.getNoseWheel();

        } catch (Exception e){
            return 0;
        }
    }

    /**
     * This method conducts a full control test of the aircraft's controls. A control test is usually executed
     * by the flight crew after pushback (after start checklist) and/or right before takeoff.
     *
     * @param timeStamp             Time stamp of point during control test, i.e. 0 - duration, in seconds.
     * @param testDuration          Total length of control test in seconds
     * @param testBreakDuration     Total length of break between test of control test in opposite directions in seconds
     */
    public void exeControlTest(double timeStamp, double testDuration, double testBreakDuration){
        try{
            int sigmoidSlope = 15;
            double ctrlTestDur, wptAileron, aileronTarget = 0.8;

            ctrlTestDur = (testDuration - testBreakDuration) / 2;

            // Check controls positive direction
            if(timeStamp <= ctrlTestDur && timeStamp >= 0){
                wptAileron = sin((timeStamp / ctrlTestDur) * PI) * aileronTarget;
                wptAileron = PerfCalc.getSigmoidVal(aileronTarget,wptAileron,sigmoidSlope);
                this.setAileron(wptAileron);
                // Check controls negative direction
            }else if(timeStamp >= (ctrlTestDur + testBreakDuration) && timeStamp <= testDuration){
                wptAileron = sin(((timeStamp - (ctrlTestDur + testBreakDuration))/ ctrlTestDur) * PI) * aileronTarget;
                wptAileron = PerfCalc.getSigmoidVal(aileronTarget,wptAileron,sigmoidSlope);
                this.setAileron(wptAileron * -1);
            }else
                this.setAileron(0);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}

