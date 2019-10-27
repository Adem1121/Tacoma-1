package ftdis.fdpu;

/**
 * The Vertical interface defines the set of methods for the validation, calculation and transformation
 * of the vertical plan and track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface Vertical {
    /**
     * This method adds a vertical segment to a plan's/track's segment list
     *
     * @param sgmt Vertical segment to be added.
     */
    void addSgmt(VerticalSegment sgmt);

    /**
     * This method returns the plan's/track's segment of a specific segment number
     *
     * @param sgmtNum   Number of the segment, starts at 0
     * @return          The segment
     */
    VerticalSegment getSgmt(int sgmtNum);

    /**
     * This method finds and returns the corresponding segment of a waypoint. As per the definition,
     * the corresponding segment of a waypoint is the segment which has a track error of zero /
     * the smallest track error among all segments.
     *
     * @param wpt   Waypoint
     * @return      Corresponding segment of the waypoint
     */
    VerticalSegment getWptSgmt(Waypoint wpt);

    /**
     * This method returns the position of a specific segment in a list of segments
     *
     * @param sgmt      The segment
     * @return          The position of the segment in the list, index starts at 0
     */
    int getSgmtPos(VerticalSegment sgmt);

    /**
     * This method returns the total number of segments in a list of segments
     *
     * @return The total number of direct segments
     */
    int getSgmtCount();

    /**
     * This method returns the object's altitude at a specific waypoint along the vertical plan/track.
     *
     * @param wpt   Waypoint along the vertical segment's plan/track
     * @return      Altitude at waypoint position in meters
     */
    double getAltAtWpt(Waypoint wpt);

    /**
     * This method returns alpha, i.e. angle of climb/descent, at a given waypoint along the
     * vertical plan/track
     *
     * @param wpt   Waypoint along the velocity segment's plan/track
     * @return      Alpha in degrees
     */
    double getAlphaAtWpt(Waypoint wpt);

}
