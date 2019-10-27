package ftdis.fdpu;

/**
 * The Weather interface defines the group of methods for the validation, calculation and transformation
 * of the weather plan and track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public interface Weather {

    /**
     * This method adds a weather segment to a plan's/track's segment list
     *
     * @param sgmt WeatherSegment to be added.
     */
    void addSgmt(WeatherSegment sgmt);

    /**
     * This method returns the plan's/track's segment of a specific segment number
     *
     * @param sgmtNum   Number of the segment, starts at 0
     * @return          The segment
     */
    WeatherSegment getSgmt(int sgmtNum);

    /**
     * This method finds and returns the corresponding segment of a waypoint. As per the definition,
     * the corresponding segment of a waypoint is the segment which has a track error of zero /
     * the smallest track error among all segments.
     *
     * @param wpt   Waypoint
     * @return      Corresponding segment of the waypoint
     */
    WeatherSegment getWptSgmt(Waypoint wpt);

    /**
     * This method returns the position of a specific segment in a list of segments
     *
     * @param sgmt      The segment
     * @return          The position of the segment in the list, index starts at 0
     */
    int getSgmtPos(WeatherSegment sgmt);

    /**
     * This method returns the total number of segments in a list of segments
     *
     * @return The total number of direct segments
     */
    int getSgmtCount();

    /**
     * This method returns the distance between the start point of a CAT (Clear Air Turbulence) and
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @param type  Type of CAT i.e. pitch, bank, or alt
     * @return      Distance between start of CAT and wpt in percent
     */
    double getCatDistAtWpt(Waypoint wpt, String type);

    /**
     * This method returns the target value of the CAT (Clear Air Turbulence) at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    double getCatTargetValueAtWpt(Waypoint wpt, String type);

    /**
     * This method returns the weather's effect on the bank angle of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    double getBankDeviationAtWpt(Waypoint wpt);

    /**
     * This method returns the weather's effect on the pitch angle of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on pitch angle in degrees, expressed as a deviation in degrees
     */
    double getPitchDeviationAtWpt(Waypoint wpt);

    /**
     * This method returns the weather's effect on the altitude of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on the altitude, expressed as a deviation in meters
     */
    double getAltDeviationAtWpt(Waypoint wpt);

    /**
     * This method returns the weather's effect on the heading of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on the airframe's heading, expressed as a deviation in degrees
     */
    double getHeadingDeviationAtWpt(Waypoint wpt);
}