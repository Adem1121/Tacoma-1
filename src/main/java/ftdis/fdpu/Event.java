package ftdis.fdpu;

/**
 * The Lateral interface defines the group of methods for the validation, calculation and transformation
 * of the lateral data.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface Event {

    /**
     * This method validates the completeness and integrity of an event's data set. In case certain parameters
     * of a multi dimensional event haven't been specified, the method attempts to calculate and set
     * the data on the available parameters.
     *
     */
    void validate();

    /**
     * This method sets the latitude and longitude coordinates of the event's start waypoint.
     *
     * @param latitude  The latitude at the event's start waypoint in decimal format
     * @param longitude The longitude at the event's start waypoint in decimal format
     */
    void setStartPt(double latitude, double longitude);

    /**
     * This method sets the latitude and longitude coordinates of the event's end waypoint.
     *
     * @param latitude  The latitude at the event's end waypoint in decimal format
     * @param longitude The longitude at the event's end waypoint in decimal format
     */
    void setEndPt(double latitude, double longitude);

    /**
     * This method returns the start waypoint of the event.
     *
     * @return The event's start waypoint
     */
    Waypoint getStartPt();

    /**
     * This method returns the end waypoint of the event.
     *
     * @return The event's end waypoint
     */
    Waypoint getEndPt();

    /**
     * This method returns the length of the lateral segment required to complete the
     * event in meters
     *
     * @return The length of the lateral segment in meters.
     */
    double getDist();

}
