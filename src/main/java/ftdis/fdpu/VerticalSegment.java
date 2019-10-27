package ftdis.fdpu;

import static java.lang.Math.*;

/**
 * The Vertical Segment class holds all of the data concerning an object's vertical movement, i.e. start and end
 * altitude, and vertical speed, of a specific segment along the lateral plan/track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VerticalSegment {
    public int id;
    private Waypoint[] waypoints;
    private double alti, altf, vs, alpha, dist;

    /**
     * Constructor(s)
     */
    VerticalSegment(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        alti = Double.NaN;
        altf = Double.NaN;
        vs = Double.NaN;
        alpha = Double.NaN;
    }

    VerticalSegment(int sgmtId){
        this();
        this.id = sgmtId;
    }

    VerticalSegment(VerticalSegment obj){
        this();
        this.id = obj.id;
        this.setStartPt(obj.getStartPt().getLat(),obj.getStartPt().getLon());
        this.setEndPt(obj.getEndPt().getLat(),obj.getEndPt().getLon());
        this.setAlti(obj.getAlti());
        this.setAltf(obj.getAltf());
        this.setVs(obj.getVs());
        this.setAlpha(obj.getAlpha());
        this.setDist(obj.getDist());
    }

    /**
     * This method sets the latitude and longitude coordinates of the vertical segment's start waypoint.
     *
     * @param latitude  The latitude at the event's start waypoint in decimal format
     * @param longitude The longitude at the event's start waypoint in decimal format
     */
    public void setStartPt(double latitude, double longitude){
        this.waypoints[0].setLat(latitude);
        this.waypoints[0].setLon(longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the vertical segment's end waypoint.
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
     * This method sets the vertical segment's initial altitude
     *
     * @param alti  Altitude in m
     */
    public void setAlti(double alti){
        this.alti = alti;
    }

    /**
     * This method sets the event's final altitude
     *
     * @param altf  Airspeed in m
     */
    public void setAltf(double altf){
        this.altf = altf;
    }

    /**
     * This method sets the vertical segment's alpha parameter
     *
     * @param alpha  Alpha in degrees
     */
    public void setAlpha(double alpha){
        this.alpha = alpha;
    }

    /**
     * This method sets the event's vertical speed
     *
     * @param vs  Airspeed in m/s
     */
    public void setVs(double vs){
        this.vs = vs;
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
     * This method returns the lateral distance of the vertical segment in meters
     *
     * @return The lateral length of the vertical segment in meters.
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
     * start point. Distance cannot exceed the total length of the segment
     *
     * @return  The altitude in m
     */
    public double getAlt(double dist){
        try{
            if(dist > this.dist)
                dist = this.dist;

            if(dist > 0)
                return this.getAlti() + (tan(alpha) * dist);
            else
                return this.alti;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the initial altitude of the vertical segment.
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
     * This method returns the final altitude of the vertical segment.
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
     * This method returns the vertical speed of the vertical segment.
     *
     * @return  Vertical speed in m/s
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
     * This method returns the alpha parameter of the vertical segment.
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
}
