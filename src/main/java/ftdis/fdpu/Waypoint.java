package ftdis.fdpu;

import java.io.Serializable;
import java.util.Comparator;

/**
 * The Waypoint class is a crucial component for the Lateral LateralSegment, TurnSegment LateralSegment and Event classes.
 * The class represents a waypoint, which is composed of a waypoint ID, latitude and longitude coordinates.
 *
 * The class implements the Comparator interface to allow for objects to be ordered on the waypoint's id.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class Waypoint implements Comparator<Waypoint>, Serializable {
    public int id;
    private double latitude;
    private double longitude;

    /**
     * Default constructor
     *
     * @param waypointId    Numeric Id of waypoint id
     */
    public Waypoint(int waypointId){
        this.id = waypointId;
    }

    /**
     * Copy constructor. Creates a 1:1 copy of an existing waypoint
     *
     * @param wpt   The waypoint
     */
    public Waypoint(Waypoint wpt){
        this.id = wpt.id;
        this.latitude = wpt.getLat();
        this.longitude = wpt.getLon();
    }

    /**
     * Simple constructor
     */
    public Waypoint(){
        this.id = 0;
        this.latitude = Double.NaN;
        this.longitude = Double.NaN;
    }

    /**
     * @param latitude The latitude of the waypoint's position in decimal degrees
     */
    public void setLat(double latitude){
        this.latitude = latitude;
    }

    /**
     * @return The latitude of the waypoint's position in decimal degrees
     */
    public double getLat(){
        return this.latitude;
    }

    /**
     * @param longitude The longitude of the waypoint's position in decimal degrees
     */
    public void setLon(double longitude){
        this.longitude = longitude;
    }

    /**
     * @return The longitude of the waypoints position in decimal degrees
     */
    public double getLon(){
        return this.longitude;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compare(Waypoint w1, Waypoint w2){
        return w1.id - w2.id;
    }
}
