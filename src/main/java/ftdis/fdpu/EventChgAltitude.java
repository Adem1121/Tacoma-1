package ftdis.fdpu;

import java.util.EnumSet;

import static java.lang.Math.*;

/**
 * The Change Altitude class represents the change altitude events that form the basis
 * for the calculation of the airframe's vertical movement along the flight track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventChgAltitude {

    public int id;
    private Lateral latPlan;
    private Velocity velPlan;
    private Waypoint[] waypoints;
    private double alti, altf, vs, dist, alpha;

    enum Parameter{
        STRTWPT,ENDWPT,ALTI,ALTF,VS;

        private static final EnumSet<Parameter> calcStartWpt = EnumSet.of(ENDWPT,ALTI,ALTF,VS);
        private static final EnumSet<Parameter> calcEndWpt = EnumSet.of(STRTWPT,ALTI,ALTF,VS);
        private static final EnumSet<Parameter> calcAlti = EnumSet.of(STRTWPT,ENDWPT,ALTF,VS);
        private static final EnumSet<Parameter> calcAltf = EnumSet.of(STRTWPT,ENDWPT,ALTI,VS);
        private static final EnumSet<Parameter> calcVs = EnumSet.of(STRTWPT,ENDWPT,ALTI,ALTF);
    }
    public EnumSet<Parameter> isDefined = EnumSet.noneOf(Parameter.class);
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    public EventChgAltitude(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        alti = Double.NaN;
        altf = Double.NaN;
        vs = Double.NaN;
        alpha = Double.NaN;
    }

    public EventChgAltitude(int eventId){
        this();
        this.id = eventId;
    }

    public EventChgAltitude(EventChgAltitude obj){
        this();
        this.id = obj.id;
        this.setStartPt(obj.getStartPt().getLat(),obj.getStartPt().getLon());
        this.setEndPt(obj.getEndPt().getLat(),obj.getEndPt().getLon());
        this.setAlti(obj.getAlti());
        this.setAltf(obj.getAltf());
        this.setVs(obj.getVs());

    }

    /**
     * This method assigns the event to a lateral plan. This reference is a key component for further
     * Change Altitude methods, such as getDist.
     *
     * @param pln   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral pln){
        this.latPlan = pln;
    }

    /**
     * This method assigns the event to a velocity plan. This reference is a key component for further
     * Change Altitude methods, such as getDist.
     *
     * @param velPln   Reference to a velocity plan.
     */
    public void assignVel(Velocity velPln){
        this.velPlan = velPln;
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
     * This method sets the change altitude event's initial altitude
     *
     * @param alti  Altitude in m
     */
    public void setAlti(double alti){
        this.alti = alti;
    }

    /**
     * This method sets the change altitude event's final altitude
     *
     * @param altf  Altitude in m
     */
    public void setAltf(double altf){
        this.altf = altf;
    }

    /**
     * This method sets the change altitude event's vertical speed
     *
     * @param vs  Vertical speed in m/s
     */
    public void setVs(double vs){
        this.vs = vs;
    }

    /**
     * This method sets the change altitude event's alpha
     *
     * @param alpha  Alpha in degrees
     */
    public void setAlpha(double alpha){
        this.alpha = alpha;
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
     * This method returns the altitude of the change altitude event at a given distance from the event's
     * start point
     *
     * @return  The altitude in m
     */
    public double getAlt(double dist){
        try{
            return tan(this.alpha) * dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the initial altitude of the change altitude event.
     *
     * @return  The altitude in m
     */
    public double getAlti(){
        try{
            return this.alti;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the altitude at the end  of the change altitude event.
     *
     * @return  The acceleration in m/s
     */
    public double getAltf(){
        try{
            return this.altf;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the vertical speed of the change airspeed event.
     *
     * @return  The vertical speed in m/s
     */
    public double getVs(){
        try{
            return this.vs;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the alpha of the change airspeed event.
     *
     * @return  Alpha in degrees
     */
    public double getAlpha(){
        try{
            return this.alpha;
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
            double t,vAsu;

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

            if(!Double.isNaN(alti))
                isDefined.add(Parameter.ALTI);

            if(!Double.isNaN(altf))
                isDefined.add(Parameter.ALTF);

            if(!Double.isNaN(vs))
                isDefined.add(Parameter.VS);

            if(isDefined.size() >= 4)
                dataValid = true;

            if(isDefined.size() == 5) {
                this.dist = latPlan.getDist(this.getStartPt(),this.getEndPt());
                this.alpha = atan((this.getAltf() - this.getAlti()) / this.dist);
                return;
            }

            // Calculate any missing parameters
            if(dataValid && isDefined.containsAll(Parameter.calcStartWpt)){
                t = (this.getAltf() - this.getAlti())/this.getVs();
                this.dist = velPlan.getDist(this.getEndPt(),t * -1);
                Waypoint itmWpt = latPlan.getItmWpt(this.getEndPt(), this.dist * -1);
                this.setStartPt(itmWpt.getLat(), itmWpt.getLon());
            }

            if(dataValid && isDefined.containsAll(Parameter.calcEndWpt)){
                t = (this.getAltf() - this.getAlti())/this.getVs();
                this.dist = velPlan.getDist(this.getStartPt(),t);
                Waypoint itmWpt = latPlan.getItmWpt(this.getStartPt(), this.dist);
                this.setEndPt(itmWpt.getLat(), itmWpt.getLon());
            }

            if(dataValid && isDefined.containsAll(Parameter.calcAlti)) {
                this.dist = latPlan.getDist(this.getStartPt(), this.getEndPt());
                vAsu = velPlan.getVasu(this.getStartPt(),this.getEndPt());
                t = this.dist/vAsu;
                this.setAlti(this.getAltf() - (t * this.getVs()));
            }

            if(dataValid && isDefined.containsAll(Parameter.calcAltf)){
                this.dist = latPlan.getDist(this.getStartPt(), this.getEndPt());
                vAsu = velPlan.getVasu(this.getStartPt(),this.getEndPt());
                t = this.dist/vAsu;
                this.setAltf(this.getAlti() + (t * this.getVs()));
            }

            if(dataValid && isDefined.containsAll(Parameter.calcVs)){
                this.dist = latPlan.getDist(this.getStartPt(), this.getEndPt());
                vAsu = velPlan.getVasu(this.getStartPt(),this.getEndPt());
                t = this.dist/vAsu;
                this.setVs((this.getAltf()-this.getAlti())/t);
            }

            // Calculate and set alpha
            this.alpha = atan((this.getAltf() - this.getAlti())/this.dist);

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
