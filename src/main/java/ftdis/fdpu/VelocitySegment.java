package ftdis.fdpu;

import static java.lang.Math.*;

/**
 * The Velocity Segment class is a key component for the Velocity Plan and Velocity Track classes.
 * It holds all of the data concerning an object's velocity, i.e. start and end air speeds as well
 * as acceleration, of a specific segment along the lateral plan/track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocitySegment {
    public int id;
    private Waypoint[] waypoints;
    private double vAsi, vAsf, vAsu, a, offSet, dist;

    /**
     * Simple constructor
     */
    VelocitySegment(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        vAsi = Double.NaN;
        vAsf = Double.NaN;
        a = Double.NaN;
        offSet = 0;
    }

    /**
     * Default constructor
     *
     * @param sgmtId The segment's numeric ID
     */
    VelocitySegment(int sgmtId){
        this();
        this.id = sgmtId;
    }

    /**
     * Replicate constructor
     *
     * @param obj   Velocity segment object reference
     */
    VelocitySegment(VelocitySegment obj){
        this();
        this.id = obj.id;
        this.setStartPt(obj.getStartPt().getLat(),obj.getStartPt().getLon());
        this.setEndPt(obj.getEndPt().getLat(),obj.getEndPt().getLon());
        this.setVasi(obj.getVasi());
        this.setVasf(obj.getVasf());
        this.setAcc(obj.getAcc());
        this.setDist(obj.getDist());
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
     * This method sets the total distance of the segment along the lateral plan/track.
     *
     * @param dist  Distance of segment in meters.
     */
    public void setDist(double dist){
        this.dist = dist;
    }

    /**
     * This method sets the change airspeed event's initial airspeed
     *
     * @param vAsi  Airspeed in m/s
     */
    public void setVasi(double vAsi){
        this.vAsi = vAsi;
    }

    /**
     * This method sets the change airspeed event's final airspeed
     *
     * @param vAsf  Airspeed in m/s
     */
    public void setVasf(double vAsf){
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
     * This method sets the change airspeed event's offset
     *
     * @param offsetTime  Offset in seconds
     */
    public void setOffset(double offsetTime){
        this.offSet = offsetTime;
    }

    public double getOffset(){
        return this.offSet;
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
     * This method returns the distance of the velocity segment in meters
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
     * start point. Distance cannot exceed the total length of the segment
     *
     * @return  The airspeed in m/s
     */
    public double getVas(double dist){
        try{
            double accEq;
            if(dist > this.dist)
                dist = this.dist;

            if(dist > 0) {
                accEq = pow(this.vAsi, 2) + (2 * this.a * dist);
                if (accEq > 0)
                    return sqrt(accEq);
                else
                    return 0;
            }else
                return this.vAsi;
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
    public double getVasi(){
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
    public double getVasf(){
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
}
