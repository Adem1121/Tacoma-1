package ftdis.fdpu;

import java.util.*;
import static java.lang.Math.*;

/**
 * The Change Airspeed class represents the change airspeed events that form the basis
 * for the calculation of the airframe's lateral movement along the flight track as well as
 * the calculation of many calculations further downstream
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventChgAirspeed implements Event{
    public int id;
    private Lateral latPlan;
    private Waypoint[] waypoints;
    private double vAsi, vAsf, vAsu, a, tOffset, dist;

    enum Parameter{
        STRTWPT,ENDWPT,VASI,VASF,A;

        private static final EnumSet<Parameter> calcStartWpt = EnumSet.of(ENDWPT,VASI,VASF,A);
        private static final EnumSet<Parameter> calcEndWpt = EnumSet.of(STRTWPT,VASI,VASF,A);
        private static final EnumSet<Parameter> calcVasi = EnumSet.of(STRTWPT,ENDWPT,VASF,A);
        private static final EnumSet<Parameter> calcVasf = EnumSet.of(STRTWPT,ENDWPT,VASI,A);
        private static final EnumSet<Parameter> calcA = EnumSet.of(STRTWPT,ENDWPT,VASI,VASF);
    }
    public EnumSet<Parameter> isDefined = EnumSet.noneOf(Parameter.class);
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    public EventChgAirspeed(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        vAsi = Double.NaN;
        vAsf = Double.NaN;
        a = Double.NaN;
        tOffset = Double.NaN;
    }

    public EventChgAirspeed(int eventId){
        this();
        this.id = eventId;
    }

    /**
     * Replicate constructor
     *
     * @param obj   Change Airspeed object reference
     */
    public EventChgAirspeed(EventChgAirspeed obj){
        this();
        this.id = obj.id;
        this.setStartPt(obj.getStartPt().getLat(),obj.getStartPt().getLon());
        this.setEndPt(obj.getEndPt().getLat(),obj.getEndPt().getLon());
        this.setvAsi(obj.getVAsi());
        this.setvAsf(obj.getVAsf());
        this.setAcc(obj.getAcc());
        this.setOffset(obj.getOffset());
    }

    /**
     * This method assigns the event to a lateral plan.
     *
     * @param pln   Reference to a lateral plan.
     */
    public void assign(Lateral pln){
        this.latPlan = pln;
    }

    /**
     * This method sets the latitude and longitude coordinates of the event's start waypoint.
     *
     * @param latitude  The latitude at the event's start waypoint in decimal format
     * @param longitude The longitude at the event's start waypoint in decimal format
     */
    public void setStartPt(double latitude, double longitude){
        this.waypoints[0].setLat(latitude);
        this.waypoints[0].setLon(longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the event's end waypoint.
     *
     * @param latitude  The latitude at the event's end waypoint in decimal format
     * @param longitude The longitude at the event's end waypoint in decimal format
     */
    public void setEndPt(double latitude, double longitude){
        this.waypoints[1].setLat(latitude);
        this.waypoints[1].setLon(longitude);
    }

    /**
     * This method sets the change airspeed event's initial airspeed
     *
     * @param vAsi  Airspeed in m/s
     */
    public void setvAsi(double vAsi){
        this.vAsi = vAsi;
    }

    /**
     * This method sets the change airspeed event's final airspeed
     *
     * @param vAsf  Airspeed in m/s
     */
    public void setvAsf(double vAsf){
        this.vAsf = vAsf;
    }

    /**
     * This method sets the change airspeed event's acceleration
     *
     * @param acc  Airspeed in m/s^2
     */
    public void setAcc(double acc){
        this.a = acc;
    }

    /**
     * This method sets the change airspeed event's time offset
     *
     * @param t  Time offset in s
     */
    public void setOffset(double t){
            this.tOffset = t;
    }

    /**
     * This method returns the start waypoint of the event.
     *
     * @return The event's start waypoint
     */
    public Waypoint getStartPt(){
        return this.waypoints[0];
    }

    /**
     * This method returns the end waypoint of the event.
     *
     * @return The event's end waypoint
     */
    public Waypoint getEndPt(){
        return this.waypoints[1];
    }

    /**
     * This method returns the distance of the lateral segment required to complete the
     * event in meters
     *
     * @return The length of the lateral segment in meters.
     */
    public double getDist(){
        try{
            return this.dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the airspeed of the change airspeed event at a given distance from the event's
     * start point
     *
     * @return  The acceleration in m/s
     */
    public double getVAs(double dst){
        try{
            return sqrt(pow(this.vAsi,2) + (2 * a * dst));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the initial airspeed of the change airspeed event. In case the parameter isn't set,
     * the method calculates and sets the parameter at run time.
     *
     * @return  The acceleration in m/s
     */
    public double getVAsi(){
        try{
            return this.vAsi;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the final airspeed  of the change airspeed event. In case the parameter isn't set,
     * the method calculates and sets the parameter at run time.
     *
     * @return  The acceleration in m/s
     */
    public double getVAsf(){
        try{
            return this.vAsf;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the acceleration parameter of the change airspeed event. In case the parameter isn't set,
     * the method calculates and sets the parameter at run time.
     *
     * @return  The acceleration in m/s
     */
     public double getAcc(){
         try{
             return this.a;
         }catch(Exception e){
             System.out.println(e.getMessage());
             return Double.NaN;
         }
     }

    /**
     * This method returns the time offset parameter of the change airspeed event.
     *
     * @return  The time offset in s
     */
    public double getOffset(){
        try{
            return this.tOffset;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method validates the completeness and integrity of an event's data set. In case certain parameters
     * of the event haven't been specified, the method attempts to calculate and set the data based on the
     * available parameters.
     *
     */
    public void validate(){
        try{
            // Walk through each parameter and check whether they are defined
            isDefined.clear();

            if(!Double.isNaN(this.getStartPt().getLat()) && !Double.isNaN(this.getStartPt().getLon())) {
                latPlan.alignWpt(this.getStartPt());
                isDefined.add(Parameter.STRTWPT);
            }

            if(!Double.isNaN(this.getEndPt().getLat()) && !Double.isNaN(this.getEndPt().getLon()))
            {
                latPlan.alignWpt(this.getEndPt());
                isDefined.add(Parameter.ENDWPT);
            }

            if(!Double.isNaN(vAsi))
                isDefined.add(Parameter.VASI);

            if(!Double.isNaN(vAsf))
                isDefined.add(Parameter.VASF);

            if(!Double.isNaN(a))
                isDefined.add(Parameter.A);

            if(isDefined.size() >= 4)
                dataValid = true;

            if(isDefined.size() == 5) {
                dist = (pow(vAsf, 2) - pow(vAsi, 2)) / (2 * a);
                return;
            }

            // Calculate any missing parameters
            if(dataValid && isDefined.containsAll(Parameter.calcStartWpt)){
                dist = (pow(vAsf,2) - pow(vAsi,2))/(2 * a);
                Waypoint itmWpt = latPlan.getItmWpt(this.getEndPt(), dist * -1);
                this.setStartPt(itmWpt.getLat(), itmWpt.getLon());
            }

            if(dataValid && isDefined.containsAll(Parameter.calcEndWpt)){
                dist = (pow(vAsf,2) - pow(vAsi,2))/(2 * a);
                Waypoint itmWpt = latPlan.getItmWpt(this.getStartPt(), dist);
                this.setEndPt(itmWpt.getLat(), itmWpt.getLon());
            }

            if(dataValid && isDefined.containsAll(Parameter.calcVasi)) {
                dist = latPlan.getDist(this.getStartPt(),this.getEndPt());
                this.vAsi = sqrt(pow(this.vAsf,2) - (2 * a * dist));
            }

            if(dataValid && isDefined.containsAll(Parameter.calcVasf)){
                dist = latPlan.getDist(this.getStartPt(),this.getEndPt());
                this.vAsf = sqrt(pow(this.vAsi,2) + (2 * a * dist));
            }

            if(dataValid && isDefined.containsAll(Parameter.calcA)){
                dist = latPlan.getDist(this.getStartPt(),this.getEndPt());
                this.a = (pow(vAsf,2) - pow(vAsi,2))/(2 * dist);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * {@inheritDoc}
     */
     public int compare(EventChgAirspeed w1, EventChgAirspeed w2){
        return w1.id - w2.id;
    }

}
