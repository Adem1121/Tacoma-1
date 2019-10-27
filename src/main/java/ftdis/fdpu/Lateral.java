package ftdis.fdpu;

/**
 * The Lateral interface defines the group of methods for the validation, calculation and transformation
 * of the flight plan and track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface Lateral {

    /**
     * This method adds a direct segment to a plan's/track's segment list
     *
     * @param sgmt LateralSegment to be added.
     */
    void addSgmt(LateralSegment sgmt);

    /**
     * This method returns the plan's/track's segment of a specific segment number
     *
     * @param sgmtNum   Number of the segment, starts at 0
     * @return          The segment
     */
    LateralSegment getSgmt(int sgmtNum);

    /**
     * This method finds and returns the corresponding segment of a waypoint. As per the definition,
     * the corresponding segment of a waypoint is the segment which has a track error of zero /
     * the smallest track error among all segments.
     *
     * @param wpt   Waypoint
     * @return      Corresponding segment of the waypoint
     */
    LateralSegment getWptSgmt(Waypoint wpt);

    /**
     * This method returns the position of a specific segment in a list of segments
     *
     * @param sgmt      The segment
     * @return          The position of the segment in the list, index starts at 0
     */
    int getSgmtPos(LateralSegment sgmt);

    /**
     * This method returns the total number of segments in a list of segments
     *
     * @return The total number of direct segments
     */
    int getSgmtCount();

    /**
     * This method calculates and returns the distance between two waypoints on the lateral plan/track.
     * The order of the waypoints is considered, i.e. a movement in the direction of the lateral plan/track
     * returns a positive a distance and a movement in the opposite direction will return a negative distance.
     *
     * @param wpt1    Start waypoint on the flight plan/track.
     * @param wpt2    End waypoint on the flight plan/track.
     * @return        The distance between the start and end waypoint in meters.
     */
    double getDist(Waypoint wpt1, Waypoint wpt2);

    /**
     * This method returns the course of a waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      The course at the waypoint in degrees
     */
    double getCourseAtWpt(Waypoint wpt);

    /**
     * This method returns the initial start waypoint of the lateral plan/track
     *
     * @return  Start waypoint of the lateral plan/track
     */
    Waypoint getStartWpt();

    /**
     * This method returns the final end waypoint of the lateral plan/track
     *
     * @return  End waypoint of the lateral plan/track
     */
    Waypoint getEndWpt();

    /**
     * This method returns an intermediate waypoint on a flight plan/track, based on the distance
     * from another waypoint positioned on the plan/track. Movement can be either forward or backwards. In
     * case the distance is positive the method assumes that the object is heading towards the end point. In case
     * the distance is negative it assume that the object is heading towards the start point of the flight plan/track.
     *
     * @param wpt   Waypoint positioned on the plan/track
     * @param dist  Distance from the waypoint in meters
     * @return      A waypoint positioned on the flight plan/track
     */
    Waypoint getItmWpt(Waypoint wpt, double dist);

    /**
     * This method calculates and returns the error between a random waypoint and the lateral flight plan/track.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error, i.e. the variation between the direct distance
     *              of the segment and the distance via the waypoint, expressed in percent.
     */
    double getWptError(Waypoint wpt);

    /**
     * This method realigns a waypoint to the flight plan/track.
     *
     * @param wpt  Waypoint that isn't positioned on the plan
     */
    void alignWpt(Waypoint wpt);
}
