package ftdis.fdpu;

/**
 * The LateralSegment interface defines the group of methods for the validation, calculation and transformation
 * of the lateral data.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface LateralSegment {

    /**
     * This method sets the latitude and longitude coordinates of the segment's start waypoint.
     *
     * @param latitude  The latitude at the segment's start waypoint in decimal format
     * @param longitude The longitude at the segment's start waypoint in decimal format
     */
    void setStartPt(double latitude, double longitude);

    /**
     * This method sets the latitude and longitude coordinates of the segment's end waypoint.
     *
     * @param latitude  The latitude at the segment's end waypoint in decimal format
     * @param longitude The longitude at the segment's end waypoint in decimal format
     */
    void setEndPt(double latitude, double longitude);

    /**
     * This method returns the start waypoint of the lateral segment.
     *
     * @return The segments start waypoint
     */
    Waypoint getStartPt();

    /**
     * This method returns the end waypoint of the lateral segment.
     *
     * @return The segment's end waypoint
     */
    Waypoint getEndPt();

    /**
     * This method calculates the direct distance between two waypoints that are positioned within
     * the segment. The order of the waypoints is important, i.e. a movement in the direction of the flight plan/track
     * returns a positive a distance and a movement in the opposite direction will return a negative distance.
     *
     * @param wpt1  1st waypoint
     * @param wpt2  2nd waypoint
     * @return      The direct distance between two waypoints in m.
     */
    double getDist(Waypoint wpt1, Waypoint wpt2);

    /**
     * This method returns the length of the lateral plan/track in meters.
     * The length is calculated on the basis of the great circle distance between
     * the start and end waypoints of the segment.
     *
     * @return The length of the lateral segment in meters.
     */
    double getDist();

    /**
     * This method returns the bearing at a specific waypoint that is positioned
     * along the lateral segment's flight track. The method assumes that the object
     * is heading directly towards the end waypoint of the segment.
     *
     * @param wpt Waypoint along the lateral segment's track
     * @return The bearing at the position of the waypoint along the lateral segment's track.
     */
    double getCourseAtWpt(Waypoint wpt);

    /**
     * This method returns an intermediate waypoint on a direct segment's track, based on the distance
     * from another waypoint positioned on the segment's track. Movement can be either forward or backwards. In case the
     * distance is positive the method assumes that the object is heading towards the end point. In case
     * the distance is negative it assume that the object is heading towards the start point.
     *
     *
     * @param wpt  Waypoint positioned on the direct segment's track
     * @param dist      Distance from the waypoint in meters
     * @return A waypoint positioned on the direct segment's track
     */
    Waypoint getItmWpt(Waypoint wpt, double dist);

    /**
     * This method calculates and returns the variation between a random waypoint
     * and the flight plan of the lateral segment.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error, i.e. the variation between the direct distance
     *              of the segment and the distance via the waypoint, expressed in percent.
     */
    double getPlanError(Waypoint wpt);

    /**
     * This method relocates a waypoint that isn't positioned on the flight plan, i.e. waypoint error,
     * to the lateral segment's flight plan.
     *
     * @param wpt  Waypoint that isn't positioned on the flight plan
     */
    void alignToPlan(Waypoint wpt);

    /**
     * This method calculates and returns the error between a random waypoint
     * and the actual flight track of the lateral segment.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error, i.e. the variation between the direct distance
     *              of the segment and the distance via the waypoint, expressed in percent.
     */
    double getTrackError(Waypoint wpt);

    /**
     * This method relocates a waypoint that isn't positioned on the flight track, i.e. waypoint error,
     * to the lateral segment's flight track.
     *
     * @param wpt  Waypoint that isn't positioned on the flight track
     */
    void alignToTrack(Waypoint wpt);


}
