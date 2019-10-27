package ftdis.fdpu;

import java.util.EnumSet;

/**
 * The Change Weather class represents the change weather events the form the basis for
 * the calculation of the effects of the defined weather, e.g. turbulence, on the track
 * and axes of the airframe.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventChgWeather {

    public int id;
    private Lateral latPlan;
    private Waypoint[] waypoints;
    private double turbMagn, turbFreq, windDir, windSpd, dist;

    private enum Parameter{STRTWPT,ENDWPT,TURBMAGN,TURBFREQ,WINDDIR,WINDSPD}
    public EnumSet<Parameter> isDefined = EnumSet.noneOf(Parameter.class);
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    public EventChgWeather(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        turbMagn = Double.NaN;
        turbFreq = Double.NaN;
        windDir = Double.NaN;
        windSpd = Double.NaN;
    }

    public EventChgWeather(int eventID){
        this();
        this.id = eventID;
    }

    /**
     * This method assigns the event to a lateral plan. This reference is a key component for further
     * Change Weather processing
     *
     * @param pln   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral pln){
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
     * This method set the change weather event's turbulence
     *
     * @param magn  Magnitude, i.e. severance, of the weather event
     * @param freq  Frequency of occurrence, of the weather event
     */
    public void setTurbulence(double magn, double freq){
        this.turbMagn = magn;
        this.turbFreq = freq;
    }

    /**
     * This method set the change weather event's turbulence
     *
     * @param dir  Wind direction in degrees
     * @param spd  Wind speed in m/s
     */
    public void setWind(double dir, double spd){
        this.windDir = dir;
        this.windSpd = spd;
    }

    /**
     * This method returns the turbulence's magnitude of the change weather event.
     *
     * @return  Magnitude, i.e. severance, of the weather event
     */
    public double getTurbulenceMagn() {
        try {
            return this.turbMagn;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the turbulence's frequency of the change weather event.
     *
     * @return  Frequency of the weather event
     */
    public double getTurbulenceFreq() {
        try {
            return this.turbFreq;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the wind direction of the change weather event.
     *
     * @return  Wind direction in degrees
     */
    public double getWindDir() {
        try {
            return this.windDir;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the wind speed of the change weather event.
     *
     * @return  Wind speed in m/s
     */
    public double getWindSpd() {
        try {
            return this.windSpd;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
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

            if(!Double.isNaN(this.turbMagn))
                isDefined.add(Parameter.TURBMAGN);

            if(!Double.isNaN(this.turbFreq))
                isDefined.add(Parameter.TURBFREQ);

            if(!Double.isNaN(this.windDir))
                isDefined.add(Parameter.WINDDIR);

            if(!Double.isNaN(this.windSpd))
                isDefined.add(Parameter.WINDSPD);

            if(isDefined.size() == 6) {
                this.dist = latPlan.getDist(this.getStartPt(),this.getEndPt());
                dataValid = true;
            }else
                dataValid = false;

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
