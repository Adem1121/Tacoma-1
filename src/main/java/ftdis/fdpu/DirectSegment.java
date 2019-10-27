package ftdis.fdpu;

import java.io.Serializable;

import static java.lang.Math.*;

/**
 * The DirectSegment LateralSegment class is a key component of the Lateral Plan and Lateral Track classes.
 * It represents a direct, straight segment which is described by an ID, as well as a start
 * and end waypoint in latitude and longitude coordinates.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class DirectSegment implements LateralSegment, Serializable {
    public int id;
    private Waypoint[] waypoints;

    /**
     * Default constructor
     *
     * @param directSegmentId  The segment's numeric ID
     */
    DirectSegment(int directSegmentId){
        this.id = directSegmentId;
        this.waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint(1);
        this.waypoints[1] = new Waypoint(2);
    }

    /**
     * Creates a new "copy" of an existing DirectSegment reference
     *
     * @param dirSgmt   Reference object to copy
     */
    DirectSegment(DirectSegment dirSgmt){
        this.id = dirSgmt.id;
        this.waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint(1);
        this.waypoints[1] = new Waypoint(2);

        this.waypoints[0].setLat(dirSgmt.getStartPt().getLat());
        this.waypoints[0].setLon(dirSgmt.getStartPt().getLon());

        this.waypoints[1].setLat(dirSgmt.getEndPt().getLat());
        this.waypoints[1].setLon(dirSgmt.getEndPt().getLon());
    }

    /**
     * Simple constructor
     */
    public DirectSegment(){
        this.id = 0;
        this.waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint(1);
        this.waypoints[1] = new Waypoint(2);
    }

    /**
     * This method sets the latitude and longitude coordinates of the segment's start waypoint.
     *
     * @param latitude  The latitude at the segment's start waypoint in decimal format
     * @param longitude The longitude at the segment's start waypoint in decimal format
     */
    public void setStartPt(double latitude, double longitude){
        this.waypoints[0].setLat(latitude);
        this.waypoints[0].setLon(longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the segment's end waypoint.
     *
     * @param latitude  The latitude at the segment's end waypoint in decimal format
     * @param longitude The longitude at the segment's end waypoint in decimal format
     */
    public void setEndPt(double latitude, double longitude){
        this.waypoints[1].setLat(latitude);
        this.waypoints[1].setLon(longitude);
    }

    /**
     * @return The segments start waypoint
     */
    public Waypoint getStartPt(){
        return this.waypoints[0];
    }

    /**
     * @return The segment's end waypoint
     */
    public Waypoint getEndPt(){
        return this.waypoints[1];
    }

    /**
     * This method returns the length of the lateral segment's track in meters.
     * The length is calculated on the basis of the great circle distance between
     * the start and end waypoints of the segment.
     *
     * @return The length of the lateral segment in meters.
     */
    public double getLength(){
        try{
            return NavCalc.getDirectDist(this.getStartPt(), this.getEndPt());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method calculates the direct distance between two waypoints that are positioned within
     * the segment.
     *
     * @param w1    1st waypoint
     * @param w2    2nd waypoint
     * @return      The direct distance between two waypoints in m.
     */
    public double getDist(Waypoint w1, Waypoint w2){
        try{
            double startDist, endDist, dist;

            startDist = NavCalc.getDirectDist(this.getStartPt(),w1);
            endDist = NavCalc.getDirectDist(this.getStartPt(),w2);
            dist = NavCalc.getDirectDist(w1, w2);

            if (startDist > endDist)
                dist = dist * -1;

            return dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    public double getDist(){
        try{
            return this.getDist(this.getStartPt(),this.getEndPt());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the heading of the object at the start of the direct segment.
     *
     * @return Heading of the object in degrees
     */
    public double getCourseStart(){
        try{
            return NavCalc.getInitBearing(this.getStartPt(), this.getItmWpt(this.getStartPt(),1));
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the heading of the object at the end of the direct segment
     *
     * @return Heading of the object in degrees
     */
    public double getCourseEnd(){
        try{
            return NavCalc.getInitBearing(this.getItmWpt(this.getEndPt(),-1),this.getEndPt());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns an object's course at a specific waypoint that is positioned
     * along the lateral segment's flight track. The method assumes that the object
     * is heading directly towards the end waypoint of the segment.
     *
     * @param wpt The position of the object along the lateral segment's track
     * @return The object's course at the position of the waypoint along the lateral segment's track.
     */
    public double getCourseAtWpt(Waypoint wpt){
        try{
            double courseAtWpt;

            if(this.getDist(this.getStartPt(),wpt) < 0.01)
                courseAtWpt = this.getCourseStart();
            else if(this.getDist(wpt,this.getEndPt()) < 0.01)
                courseAtWpt = this.getCourseEnd();
            else
                courseAtWpt = NavCalc.getInitBearing(wpt, this.getEndPt());

            return courseAtWpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

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
    public Waypoint getItmWpt(Waypoint wpt, double dist){
        try{
            double itmDist, f;
            Waypoint tmpWpt,itmWpt,endWpt;

            if(dist != 0){
                // evaluate object's movement, i.e. forwards, or backwards, and set end waypoint accordingly
                if(dist < 0)
                    endWpt = this.getStartPt();
                else
                    endWpt = this.getEndPt();

                // check whether dist exceeds distance between intermediary and end waypoint
                itmDist = abs(this.getDist(wpt, endWpt));

                if(!(abs(dist) > itmDist))
                    f = abs(dist) / itmDist;
                else
                    f = 1;

                // get and return intermediary waypoint
                itmWpt = NavCalc.getItmWpt(wpt, endWpt, f);

                return itmWpt;
            }else
                return wpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method calculates and returns the error between a random waypoint and the lateral flight plan
     * of the direct segment.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's  error, i.e. the variation between the direct distance of the segment
     *              along it's flight plan and the distance via the waypoint, expressed in percent.
     */
    public double getPlanError(Waypoint wpt){
        try {
            Waypoint startPt, endPt;
            double a, b, c, alpha, offsetDist;

            // define reference waypoint, i.e. start/or end point with shortest distance to wpt
            if (NavCalc.getDirectDist(this.getStartPt(), wpt) <= NavCalc.getDirectDist(wpt, this.getEndPt())) {
                startPt = this.getStartPt();
                endPt = this.getEndPt();
            }else{
                startPt = this.getEndPt();
                endPt = this.getStartPt();
            }

            // calculate alpha of triangle start point, end point, waypoint
            a = NavCalc.getDirectDist(endPt, wpt);
            b = this.getLength();
            c = NavCalc.getDirectDist(startPt, wpt);

            if(b != 0 && c != 0){
                alpha = (pow(b, 2) + pow(c, 2) - pow(a, 2)) / (2 * b * c);
                alpha = acos(min(max(alpha, -1.0), 1.0));

                if(toDegrees(alpha) > 90)
                    offsetDist = c;
                else
                    offsetDist = sin(alpha) * c;
            }else
                offsetDist = 0;

            return offsetDist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method calculates and returns the error between a random waypoint and the lateral flight track
     * of the direct segment. Since the direct segment represents represents a direct, straight segment between
     * two waypoints, the flight plan and track are congruent.
     *
     * @param wpt Random waypoint
     * @return  The waypoint's  error, i.e. the variation between the direct distance of the segment
     *          along it's flight track and the distance via the waypoint, expressed in percent.
     */
    public double getTrackError(Waypoint wpt){
        try {
             return this.getPlanError(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method relocates a waypoint that isn't positioned on the lateral segment's flight plan,
     * i.e. waypoint error, to the flight plan.
     *
     * @param wpt  Waypoint that isn't positioned on the flight plan
     */
    public void alignToPlan(Waypoint wpt){
        try{
            Waypoint itmWpt;
            double a,b,c,alpha,dist;

            // calculate alpha of triangle start point, end point, waypoint
            a = NavCalc.getDirectDist(this.getStartPt(), wpt);
            b = this.getLength();
            c = NavCalc.getDirectDist(this.getEndPt(), wpt);

            if(b != 0 && c != 0){
                alpha = (pow(b, 2) + pow(c, 2) - pow(a, 2)) / (2 * b * c);
                alpha = acos(min(max(alpha,-1.0),1.0));

                if(toDegrees(alpha) > 90)
                    alpha = toRadians(90);

                dist = c * cos(alpha);
            }else
                dist = 0;

            // use distance  to find corresponding position on direct segment's track
            itmWpt = this.getItmWpt(this.getEndPt(), dist * -1);

            wpt.setLat(itmWpt.getLat());
            wpt.setLon(itmWpt.getLon());
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method relocates a waypoint that isn't positioned on the track, i.e. waypoint error, to the
     * flight segment's track. Since the direct segment represents represents a direct, straight segment between
     * two waypoints, the flight plan and track are congruent.
     *
     * @param wpt  Waypoint that isn't positioned on the track
     */
    public void alignToTrack(Waypoint wpt){
        try{
            this.alignToPlan(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
