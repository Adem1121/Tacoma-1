package ftdis.fdpu;

import java.nio.file.attribute.UserDefinedFileAttributeView;

import static java.lang.Math.*;

/**
 * The TurnSegment LateralSegment class is a key component of the Lateral Track class. It represents a turn
 * along a circular path in a uniform motion with a constant angular rate of ration. It is composed
 * of an ID, initiation, turn and termination waypoints and holds data specifying the turn radius
 * and the magnitude of the course change.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class TurnSegment implements LateralSegment {
    public int id;
    private Waypoint[] waypoints;
    private DirectSegment initSgmt = new DirectSegment(1), termSgmt = new DirectSegment(2);
    private double turnRadius, courseStart, courseEnd;

    /**
     * Constructors
     */

    TurnSegment(){
        this.id = 0;
        waypoints = new Waypoint[3];
        this.waypoints[0] = new Waypoint(1);
        this.waypoints[1] = new Waypoint(2);
        this.waypoints[2] = new Waypoint(3);
        turnRadius = 0;
        courseStart = 0;
        courseEnd = 0;
    }

    TurnSegment(int turnSegmentId){
        this();
        this.id = turnSegmentId;
    }

    /**
     * This method sets the latitude and longitude coordinates of the turn initiation waypoint.
     *
     * @param latitude  The latitude at the turn initiation waypoint in decimal degrees
     * @param longitude The longitude at the turn initiation waypoint in decimal degrees
     */
    public void setStartPt(double latitude, double longitude){
        this.waypoints[0].setLat(latitude);
        this.waypoints[0].setLon(longitude);

        this.initSgmt.setStartPt(latitude,longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the turn waypoint.
     *
     * @param latitude  The latitude at the segment's turn waypoint in decimal degrees
     * @param longitude The longitude at the segment's turn waypoint in decimal degrees
     */
    public void setTurnPt(double latitude, double longitude){
        this.waypoints[1].setLat(latitude);
        this.waypoints[1].setLon(longitude);

        this.initSgmt.setEndPt(latitude, longitude);
        this.termSgmt.setStartPt(latitude, longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the turn termination waypoint.
     *
     * @param latitude  The latitude at the turn termination waypoint in decimal degrees
     * @param longitude The longitude at the turn termination waypoint in decimal degrees
     */
    public void setEndPt(double latitude, double longitude){
        this.waypoints[2].setLat(latitude);
        this.waypoints[2].setLon(longitude);

        this.termSgmt.setEndPt(latitude, longitude);
    }

    /**
     * This method sets the turn radius of the turn segment.
     *
     * @param radius The turn radius in degrees
     */
    public void setRadius(double radius){
        this.turnRadius = radius;
    }

    /**
     * @return The turn initiation waypoint
     */
    public Waypoint getStartPt(){
        return this.waypoints[0];
    }

    /**
     * @return The turn waypoint
     */
    public Waypoint getTurnPt(){
        return this.waypoints[1];
    }

    /**
     * @return The turn termination waypoint
     */
    public Waypoint getEndPt(){
        return this.waypoints[2];
    }

    /**
     * @return The radius of the turn segment in meters
     */
    public double getRadius(){
        try{
            //double radius, bearing, crsChg;

            //bearing = NavCalc.getInitBearing(this.getStartPt(), this.getEndPt());
            //crsChg = NavCalc.getCourseChange(this.getCourseStart(),bearing) + NavCalc.getCourseChange(bearing,this.getCourseEnd());

            //radius = NavCalc.getDirectDist(this.getStartPt(),this.getEndPt())/(2 * sin(toRadians(abs(crsChg)/2)));

            //return radius;

            return NavCalc.getDirectDist(this.getStartPt(),this.getEndPt())/(2 * sin(toRadians(abs(this.getCourseChange())/2)));
            //return abs(this.turnRadius);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
      }

    /**
     * This method sets the course at the start of the turn segment
     *
     * @param degrees The course in degrees
     */
    public void setCourseStart(double degrees){
        this.courseStart = degrees;
    }

    /**
     * This method returns the heading of the object at the turn initiation point.
     *
     * @return Heading of the object in degrees
     */
    public double getCourseStart(){
        try{
            if(this.courseStart == 0)
                // This logic will only work for turn segments with a course change < 180 degrees
                return NavCalc.getInitBearing(this.getStartPt(), this.getTurnPt());
            else
                // For turn segments with a course change > 180 degrees, the course is calculated and set in the LateralTrack.Transform method
                return this.courseStart;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method sets the course at the end of the turn segment
     *
     * @param degrees The course in degrees
     */
    public void setCourseEnd(double degrees){
        this.courseEnd = degrees;
    }

    /**
     * This method returns the heading of the object at the end of the turn, i.e. the turn
     * termination point
     *
     * @return Heading of the object in degrees
     */
    public double getCourseEnd(){
        try{
            if(this.courseEnd == 0)
                // This logic will only work for turn segments with a course change < 180 degrees
                return NavCalc.getNewCourse(NavCalc.getInitBearing(this.getEndPt(), this.getTurnPt()), 180);
            else
                // For turn segments with a course change > 180 degrees, the course is calculated and set in the LateralTrack.Transform method
                return this.courseEnd;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the magnitude of the object's course change along the turn.
     *
     * @return Total course change in degrees.
     */
    public double getCourseChange(){
        try{
            double bearing, crsChg;

            bearing = NavCalc.getInitBearing(this.getStartPt(), this.getEndPt());
            crsChg = NavCalc.getCourseChange(this.getCourseStart(),bearing) + NavCalc.getCourseChange(bearing,this.getCourseEnd());

            return crsChg;
            //return NavCalc.getCourseChange(this.getCourseStart(), this.getCourseEnd());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the distance between two waypoints along the radial track
     * of a turn segment.
     *
     * The order of the waypoints is considered, i.e. a movement in the direction of the lateral plan/track
     * returns a positive a distance and a movement in the opposite direction will return a negative distance.
     *
     * @param w1    The start waypoint on the radial track
     * @param w2    The end waypoint on the radial track
     * @return      The distance between the start and end waypoints in meters.
     */
    public double getDist(Waypoint w1, Waypoint w2){
        try{

/*
           double startDist, endDist, startDeg, endDeg, a, chordLen, startBearing, endBearing;

            // get distances and degrees
            startDist = NavCalc.getDirectDist(this.getStartPt(),w1);
            startDeg = toDegrees(acos((2 * pow(this.getRadius(),2) - pow(startDist,2))/(2 * pow(this.getRadius(),2))));

            endDist = NavCalc.getDirectDist(this.getEndPt(),w2);
            endDeg = toDegrees(acos((2 * pow(this.getRadius(),2) - pow(endDist,2))/(2 * pow(this.getRadius(),2))));

            if(Double.isNaN(startDeg) )
                startDeg = 0;

            if(Double.isNaN(endDeg))
                endDeg = 0;

            a = abs(this.getCourseChange()) - startDeg - endDeg;

            return toRadians(a) * this.getRadius();
*/
            double startDist, endDist, chordLen;

            // get distances
            startDist = NavCalc.getDirectDist(this.getStartPt(),w1);
            endDist = NavCalc.getDirectDist(this.getStartPt(),w2);
            chordLen = NavCalc.getDirectDist(w1,w2);

            // get beta and return distance in meters
            double beta = 2 * asin(chordLen/(2 * this.getRadius()));

            if(startDist > endDist)
                beta *= -1;

            return beta * this.getRadius();


        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public double getDist(){
        try{
            //return this.getDist(this.getStartPt(), this.getEndPt());
            return toRadians(abs(this.getCourseChange())) * this.getRadius();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the bearing of an object at a specific waypoint along the radial track
     * of a turn segment.
     *
     * @param waypoint The waypoint
     * @return Bearing of the object in decimal degrees.
     */
    public double getCourseAtWpt(Waypoint waypoint){
        try{
            double courseAtWpt;

            // get beta based on distance between turn initiation point and waypoint
            double beta = this.getDist(this.getStartPt(),waypoint)/this.getRadius();

            if(this.getCourseChange() < 0)
                beta = beta * -1;

            // add beta to course at start and return bearing at waypoint
            courseAtWpt = NavCalc.getNewCourse(getCourseStart(),toDegrees(beta));

            return courseAtWpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns an intermediate waypoint on a turn segment's track, based on the distance
     * from another waypoint positioned on the segment's track. Movement can be either forward or backwards. In case the
     * distance is positive the method assumes that the object is heading towards the end point. In case
     * the distance is negative it assume that the object is heading towards the start point.
     *
     *
     * @param waypoint  Waypoint positioned on the turn segment's track
     * @param dist      Distance from the waypoint in meters
     * @return waypoint A waypoint positioned on the turn segment's track
     */
    public Waypoint getItmWpt(Waypoint waypoint, double dist){
        try{
            int courseOffset;
            double courseChange, wptCourse, xOffset, yOffset, xTi, yTi, xWpt, yWpt, latitude, longitude, testRad;
            Waypoint itmWpt = new Waypoint(1);

            // check if +/- distance exceeds remaining distance, if yes adjust accordingly
            if(dist >= 0 && dist >= this.getDist(waypoint,this.getEndPt()))
                return this.getEndPt();
            else if(dist < 0 && dist <= this.getDist(waypoint,this.getStartPt()))
                return this.getStartPt();
            else
                dist = this.getDist(this.getStartPt(),waypoint) + dist;

            // check direction of turn and set flags accordingly
            if(this.getCourseChange() > 0){
                courseOffset = -90;
            } else{
                courseOffset = 90;
            }

            // calculate course at intermediate waypoint and determine x,y coordinates
            courseChange = toDegrees(dist / this.getRadius());

            if(this.getCourseChange() < 0)
                courseChange = courseChange * -1;

            wptCourse = NavCalc.getNewCourse(this.getCourseStart(),courseChange);

            xWpt = sin(toRadians(wptCourse + courseOffset)) * this.getRadius();
            yWpt = cos(toRadians(wptCourse + courseOffset)) * this.getRadius();

            // get course at turn initiation point and determine x,y coordinates
            xTi = sin(toRadians(this.getCourseStart() + courseOffset)) * this.getRadius();
            yTi = cos(toRadians(this.getCourseStart() + courseOffset)) * this.getRadius();

            // calculate x,y offsets between intermediate waypoint and turn initiation point
            xOffset = xWpt - xTi;
            yOffset = yWpt - yTi;

            // transform x,y offsets of intermediate waypoint to lat, long coordinates
            latitude = yOffset / NavCalc.RADIUS_EARTH_FSX_M;
            longitude = xOffset / (NavCalc.RADIUS_EARTH_FSX_M * cos(toRadians(this.getStartPt().getLat())));
            latitude = this.getStartPt().getLat() + toDegrees(latitude);
            longitude = this.getStartPt().getLon() + toDegrees(longitude);

            // return waypoint
            itmWpt.setLat(latitude);
            itmWpt.setLon(longitude);

            return itmWpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return waypoint;
        }
    }

    /**
     * This method calculates and returns the error between a random waypoint and the lateral flight plan
     * of the turn segment.
     *
     * @param wpt Random waypoint
     * @return  The waypoint's  error, i.e. the variation between the direct distance of the segment
     *          along it's flight plan and the distance via the waypoint, expressed in percent.
     */
    public double getPlanError(Waypoint wpt){
        try{
            double initSgmtError,termSgmtError;

            initSgmtError = initSgmt.getPlanError(wpt);
            termSgmtError = termSgmt.getPlanError(wpt);

            if(initSgmtError > termSgmtError)
                return termSgmtError;
            else
                return initSgmtError;

        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method calculates and returns the track error between a random waypoint
     * and the lateral track of the turn segment.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error, i.e. the variation between the direct distance
     *              of the segment and the distance via the waypoint, expressed in percent.
     */
    public double getTrackError(Waypoint wpt){
        try{
            double startDeg, endDeg, wptDeg, crsChg;

            // get bearings between center and start point, end point and waypoing
            startDeg = NavCalc.getInitBearing(this.getTurnCtrPt(),this.getStartPt());
            endDeg = NavCalc.getInitBearing(this.getTurnCtrPt(), this.getEndPt());
            wptDeg = NavCalc.getInitBearing(this.getTurnCtrPt(), wpt);

            // calculate distance between center point of turn and waypoint
            double c = NavCalc.getDirectDist(wpt, this.getTurnCtrPt());

            // check if waypoint is within range of turn
            crsChg = NavCalc.getCourseChange(startDeg, endDeg);

            if(round(abs(crsChg)) == round((abs(NavCalc.getCourseChange(startDeg, wptDeg)) + abs(NavCalc.getCourseChange(endDeg, wptDeg))))){
                // calculate waypoint error, based on total distance and turn radius
                return abs(c - this.getRadius());
            }else{
                return c + this.getRadius();
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method relocates a waypoint that isn't positioned on the turn segment's flight plan,
     * i.e. waypoint error, to the flight plan.
     *
     * @param wpt  Waypoint that isn't positioned on the flight plan
     */
    public void alignToPlan(Waypoint wpt){
        try{

            if(initSgmt.getPlanError(wpt) > termSgmt.getPlanError(wpt))
                termSgmt.alignToPlan(wpt);
            else
                initSgmt.alignToPlan(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method relocates a waypoint that isn't positioned on the track, i.e. waypoint error, to the
     * turn segment's track.
     *
     * @param wpt   The waypoint
     */
    public void alignToTrack(Waypoint wpt){
        try{
            double a, b, c, beta;

            // calc beta of waypoint
            a = NavCalc.getDirectDist(this.getStartPt(),wpt);
            b = this.getRadius();
            c = NavCalc.getDirectDist(wpt,this.getTurnCtrPt());
            beta = acos((pow(b, 2) + pow(c, 2) - pow(a, 2)) / (2 * b * c));

            // Only adjust waypoint if track error is present / can be determined, i.e. b != c and a > 0
            if(!Double.isNaN(beta)) {
                // get waypoint
                Waypoint itmWaypoint = this.getItmWpt(this.getStartPt(), (beta * this.getRadius()));
                wpt.setLat(itmWaypoint.getLat());
                wpt.setLon(itmWaypoint.getLon());
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method aligns a waypoint that is positioned on the turn segment's lateral plan, i.e.
     * on the turn initiation / turn termination segment, to the turn segment's lateral track.
     *
     * @param wpt Waypoint positioned on the turn segment's flight plan.
     */
    public void relocateWpt(Waypoint wpt){
        try{
            Waypoint refWpt;
            double planDist;

            // Determine fraction of plan distance vs. total distance between start/end pt, turn pt and wpt, and find corresponding waypoint on track
            if(NavCalc.getDirectDist(this.getStartPt(),wpt) < NavCalc.getDirectDist(wpt,this.getEndPt())){
                planDist = this.initSgmt.getDist(this.getStartPt(), wpt);
                refWpt = this.getItmWpt(this.getStartPt(), (this.getDist() / 2) * (planDist / this.initSgmt.getDist()));
            }else{
                planDist = this.termSgmt.getDist(wpt, this.getEndPt());
                refWpt = this.getItmWpt(this.getEndPt(),(this.getDist() / 2) * (planDist / this.termSgmt.getDist()) * -1);
            }

            // Assign reference point coordinates to wpt
            wpt.setLat(refWpt.getLat());
            wpt.setLon(refWpt.getLon());

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method determines the latitude and longitude coordinates of the center waypoint
     * of the turn segment.
     *
     * @return  The center waypoint of the turn segment
     */
    public Waypoint getTurnCtrPt(){
        try{
            Waypoint ctrPoint = new Waypoint();
            int courseOffset;
            double xTi, yTi, ctrLat, ctrLon;

            // check direction of turn and set flags accordingly
            if(this.getCourseChange() > 0){
                courseOffset = -90;
            } else{
                courseOffset = 90;
            }

            // get course at turn initiation point and determine x,y coordinates
            xTi = sin(toRadians(this.getCourseStart() + courseOffset)) * this.getRadius();
            yTi = cos(toRadians(this.getCourseStart() + courseOffset)) * this.getRadius();

            // invert x,y offsets for turn initiation point to determine center of circle and lat, long coordinates
            ctrLat = (yTi * -1) / NavCalc.RADIUS_EARTH_FSX_M;
            ctrLon = (xTi * -1) / (NavCalc.RADIUS_EARTH_FSX_M * cos(toRadians(this.getStartPt().getLat())));
            ctrPoint.setLat(this.getStartPt().getLat() + toDegrees(ctrLat));
            ctrPoint.setLon(this.getStartPt().getLon() + toDegrees(ctrLon));

            return ctrPoint;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }

    }
}
