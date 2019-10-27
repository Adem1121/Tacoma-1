package ftdis.fdpu;

/**
 * The Velocity interface defines the set of methods for the validation, calculation and transformation
 * of the velocity plan and track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface Velocity {
    /**
     * This method adds a velocity segment to a plan's/track's segment list
     *
     * @param sgmt LateralSegment to be added.
     */
    void addSgmt(VelocitySegment sgmt);

    /**
     * This method returns the plan's/track's segment of a specific segment number
     *
     * @param sgmtNum   Number of the segment, starts at 0
     * @return          The segment
     */
    VelocitySegment getSgmt(int sgmtNum);

    /**
     * This method finds and returns the corresponding segment of a waypoint. As per the definition,
     * the corresponding segment of a waypoint is the segment which has a track error of zero /
     * the smallest track error among all segments.
     *
     * @param wpt   Waypoint
     * @return      Corresponding segment of the waypoint
     */
    VelocitySegment getWptSgmt(Waypoint wpt);

    /**
     * This method returns the position of a specific segment in a list of segments
     *
     * @param sgmt      The segment
     * @return          The position of the segment in the list, index starts at 0
     */
    int getSgmtPos(VelocitySegment sgmt);

    /**
     * This method returns the total number of segments in a list of segments
     *
     * @return The total number of direct segments
     */
    int getSgmtCount();

    /**
     * This method calculates and returns the total distance an object has travelled along
     * the lateral plan/track in a given time
     *
     * @param wpt     Start waypoint on the flight plan/track
     * @param time    Total time of travel in seconds
     * @return        The distance between the start and end waypoint in meters
     */
    double getDist(Waypoint wpt, double time);

    /**
     * This method calculates the time required to cover the distance between two waypoints
     * that are positioned on the velocity/plan track.
     *
     * @param wpt1  1st waypoint
     * @param wpt2  2nd waypoint
     * @return      The time required to cover the distance between two waypoints in seconds
     */
    double getTime(Waypoint wpt1, Waypoint wpt2);

    /**
     * This method returns the object's airspeed at a specific waypoint along the velocity plan/track.
     *
     * @param wpt   Waypoint along the velocity segment's plan/track
     * @return      Airspeed at waypoint position in m/s
     */
    double getVasAtWpt(Waypoint wpt);

    /**
     * This method returns the uniform airspeed between two waypoints along the velocity plan/track.
     *
     * @param wpt1  Start waypoint on the flight plan/track
     * @param wpt2  End waypoint on the flight plan/track
     * @return      Uniform airspeed in m/s
     */
    double getVasu(Waypoint wpt1, Waypoint wpt2);
}
