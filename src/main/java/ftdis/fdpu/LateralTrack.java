package ftdis.fdpu;

import java.util.*;
import static java.lang.Math.*;
import static ftdis.fdpu.Config.*;

/**
 * The Lateral Track class is a subclass which represent the lateral flight track. It is the key
 * component for the event enrichment and validation processes, as well as the flight data processing.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.2
 */
public class LateralTrack implements Lateral {
    public Integer id;
    public boolean dataValid = false;
    private Lateral lateralPlan;
    private Velocity velocityPlan;
    private List<LateralSegment> latSegments;
    private List<DirectSegment> directSegments;
    private List<TurnSegment> turnSegments;

    /**
     * Constructors
     */
    LateralTrack(){
        this.latSegments = new ArrayList<LateralSegment>();
        this.directSegments = new ArrayList<DirectSegment>();
        this.turnSegments = new ArrayList<TurnSegment>();
    }

    LateralTrack(Integer flightTrackID){
        this();
        this.id = flightTrackID;
    }

    /**
     * This method assigns the lateral track to a velocity plan.
     *
     * @param velPlan   Reference to the velocity plan
     */
    public void assignVel(Velocity velPlan){
        this.velocityPlan = velPlan;
    }

    /**
     * This method adds a segment to the flight plan's direct segment list
     *
     * @param sgm DirectSegment segment to be added.
     */
    public void addSgmt(LateralSegment sgm){
        this.latSegments.add(sgm);
    }

    /**
     * This method returns the lateral plan assigned to the lateral track
     * @return  Reference to lateral plan
     */
    public Lateral getLateralPlan(){
        return this.lateralPlan;
    }

    /**
     * This method returns the flight plan's segment of a specific segment number
     *
     * @param sgmNum Number of the direct segment, starts with 0
     * @return The direct segment
     */
    public LateralSegment getSgmt(int sgmNum){
        return ListUtil.getListItem(sgmNum, this.latSegments);
    }

    /**
     * @param sgm The direct segment
     * @return The number of the lateral segment in the list, index starts with 0
     */
    public int getSgmtPos(LateralSegment sgm){
        return ListUtil.getListItemNum(sgm, this.latSegments);
    }

    /**
     * @return The total number of direct segments
     */
    public int getSgmtCount(){
        return this.latSegments.size();
    }

    /**
     * This method returns the length of the lateral plan/track in meters.
     * The length is calculated on the basis of the great circle distance between
     * the start and end waypoints of the segment.
     *
     * @return The length of the lateral segment in meters.
     */
    public double getLength(){
        try{
            //return ListUtil.getListLength(this.latSegments);
            return this.getDist(this.getStartWpt(),this.getEndWpt());
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method calculates the direct distance between two waypoints that are positioned
     * on the flight track.
     *
     * @param wpt1  1st waypoint
     * @param wpt2  2nd waypoint
     * @return      The direct distance between two waypoints in m.
     */
    public double getDist(Waypoint wpt1, Waypoint wpt2){
        try{
            LateralSegment strtSgmt, thisSgmt, endSgmt;
            boolean endPointReached = false;
            int s;
            double dist = 0.0;

            // find segments for start and end waypoints
            strtSgmt = getWptSgmt(wpt1);
            endSgmt = getWptSgmt(wpt2);
            s = getSgmtPos(strtSgmt);

            // in case start and end waypoint are in the same segment, get direct distance
            if(strtSgmt == endSgmt){
                dist = strtSgmt.getDist(wpt1, wpt2);
                endPointReached = true;
            }

            // in case start and end waypoints are in different segments, loop through each segment and accumulate distance
            while(!endPointReached && ListUtil.inBound(s,this.latSegments)){
                thisSgmt = this.latSegments.get(s);
                // get distance between w1 and start/end of start segment
                if(thisSgmt == strtSgmt){
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt)){
                        // moving forwards: Dist = w1 -> end of start segment
                        dist += thisSgmt.getDist(wpt1, thisSgmt.getEndPt());
                        s++;
                    } else {
                        // moving backwards: Dist = start of start segment <- w1
                        dist += thisSgmt.getDist(wpt1, thisSgmt.getStartPt());
                        s--;
                    }
                // get distance between w2 and start/end of end segment
                }else if(thisSgmt == endSgmt){
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt))
                        // moving forwards: Dist = start of end segment -> w2
                        dist += thisSgmt.getDist(thisSgmt.getStartPt(), wpt2);
                    else
                        // moving backwards: Dist = w2 <- end of end segment
                        dist += thisSgmt.getDist(thisSgmt.getEndPt(), wpt2);
                    endPointReached = true;
                // get distance of all segments inbetween start and end segments
                }else {
                    if(getSgmtPos(strtSgmt) < getSgmtPos(endSgmt)){
                        // moving forward
                        dist += thisSgmt.getDist(thisSgmt.getStartPt(), thisSgmt.getEndPt());
                        s++;
                    }
                    else {
                        // moving backwards
                        dist -= thisSgmt.getDist(thisSgmt.getStartPt(), thisSgmt.getEndPt());
                        s--;
                    }
                }
            }

            // Set non relevant/measurable distances to zero. Otherwise small deviations in calculations
            // can lead to unforeseen knock-on effects during further processing
            if(abs(dist) < 1.0E-5)
                dist = 0;

            return dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the bearing at a specific waypoint that is positioned
     * along the lateral segment's flight track. The method assumes that the object
     * is heading directly towards the end waypoint of the segment.
     *
     * @param wpt Waypoint along the lateral segment's track
     * @return The bearing at the position of the waypoint along the lateral segment's track.
     */
    public double getCourseAtWpt(Waypoint wpt){
        try{
            // find corresponding segment of waypoint
            LateralSegment wptSgm = getWptSgmt(wpt);
            // return course segment at wpt
            return wptSgm.getCourseAtWpt(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns the initial start waypoint of the lateral plan/track
     *
     * @return  Start waypoint of the lateral plan/track
     */
    public Waypoint getStartWpt(){
        try{
            return this.latSegments.get(0).getStartPt();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the final end waypoint of the lateral plan/track
     *
     * @return  End waypoint of the lateral plan/track
     */
    public Waypoint getEndWpt(){
        try{
            return this.latSegments.get(this.getSgmtCount() - 1).getEndPt();
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
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
            Waypoint thisWpt;
            LateralSegment thisSgmt;
            boolean endPointReached = false;
            int s;

            // find segment number of start waypoint and set this waypoint to start waypoint
            s = ListUtil.getListItemNum(getWptSgmt(wpt), this.latSegments);
            thisWpt = wpt;

            // iterate through segments (forwards/backwards) and accumulate distance
            while(!endPointReached && ListUtil.inBound(s, this.latSegments)){
                thisSgmt = this.latSegments.get(s);

                if(dist >= 0){
                    // moving forwards: Check if distance between this  waypoint and end of this segment is larger than remaining distance
                    if(thisSgmt.getDist(thisWpt, thisSgmt.getEndPt()) >= dist){
                        // return intermediary waypoint on direct segment based on remaining distance from this waypoint
                        thisWpt = thisSgmt.getItmWpt(thisWpt, dist);
                        endPointReached = true;
                    }else{
                        // calculate remaining distance between this waypoint and end of segment and move to next segment
                        dist -= thisSgmt.getDist(thisWpt, thisSgmt.getEndPt());
                        thisWpt = thisSgmt.getEndPt();
                        s++;
                    }
                }else{
                    // moving backwards: Check if distance between intermediary waypoint and start of this segment is larger than remaining distance
                    if(abs(thisSgmt.getDist(thisWpt, thisSgmt.getStartPt())) >= abs(dist)){
                        // return intermediary waypoint on direct segment based on remaining distance from this waypoint
                        thisWpt = thisSgmt.getItmWpt(thisWpt, dist);
                        endPointReached = true;
                    }else{
                        // calculate remaining distance between this waypoint and start of segment and move to next segment
                        dist -= thisSgmt.getDist(thisWpt, thisSgmt.getStartPt());
                        thisWpt = thisSgmt.getStartPt();
                        s--;
                    }
                }
            }
            return thisWpt;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method calculates and returns the error between a random waypoint
     * and the lateral flight track.
     *
     * @param wpt   Random waypoint
     * @return      The waypoint's track error expressed in percent.
     */
    public double getWptError(Waypoint wpt){
        try{
            // find corresponding segment of waypoint and return error
            LateralSegment wptSgm = getWptSgmt(wpt);
            return wptSgm.getTrackError(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method relocates a waypoint that isn't positioned on the flight track, i.e. waypoint error,
     * to the flight track.
     *
     * @param wpt  Waypoint that isn't positioned on the track
     */
    public void alignWpt(Waypoint wpt){
        try{
            // find corresponding segment of waypoint
            LateralSegment wptSgm = getWptSgmt(wpt);
            // calibrate waypoint
            wptSgm.alignToTrack(wpt);
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method finds and returns the corresponding lateral segment of a waypoint along the flight track.
     * As per the definition, the corresponding segment of a waypoint is the segment which has
     * a track error of zero / the smallest track error among all segments.
     *
     * Contrary to the direct segments, the plan and track for turn segments does not overlap. As a workaround for
     * finding the correct turn segment, e.g. when adjusting waypoints of a lateral plan, the method returns the smaller
     * error, i.e. either plan or track error, for turn segments.
     *
     * @param wpt Waypoint
     * @return Corresponding direct segment of the waypoint
     */
    public LateralSegment getWptSgmt(Waypoint wpt){
        try{
            // loop through each direct segment and calculate track error
            int sgmt;
            double smlst, trckError, plnError;
            double[] trackError = new double[this.latSegments.size()];

            for(int i = 0; i < this.latSegments.size(); i++){
                if(this.latSegments.get(i) instanceof TurnSegment){
                    trckError = this.latSegments.get(i).getTrackError(wpt);
                    plnError = this.latSegments.get(i).getPlanError(wpt);

                    if(trckError < plnError)
                        trackError[i] = trckError;
                    else
                        trackError[i] = plnError;
                }else
                    trackError[i] = this.latSegments.get(i).getTrackError(wpt);
            }

            // find index of segment with smallest track error
            smlst = trackError[0];
            sgmt = 0;
            for(int i = 0; i < trackError.length; i++) {
                if(smlst > trackError[i]) {
                    smlst = trackError[i];
                    sgmt = i;
                }
            }
            // return segment
            return this.latSegments.get(sgmt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method transforms the the lateral plan into a lateral track for further processing
     *
     */
    public void transform(LateralPlan latPlan){
        try{
            DirectSegment dirSgmt, thisSgmt, nextSgmt, inbSgmt, outbSgmt;
            TurnSegment turnSgmt;
            Waypoint turnStartPt, turnPt, turnEndPt;
            double crsChg, bearing, c, turnRad, vAs, bankAngle, turnDist, turnChkDist, maxTaxSpeed = PerfCalc.convertKts(25,"kts");

            this.lateralPlan = latPlan;

            // Load direct segments from lateral plan
            for(int s =0; s < lateralPlan.getSgmtCount(); s++){
                dirSgmt = new DirectSegment((DirectSegment) lateralPlan.getSgmt(s));
                this.directSegments.add(dirSgmt);
            }

            // Transform direct segments to lateral track
            for(int s = 0; s < (this.directSegments.size() - 1);s++){
                thisSgmt = this.directSegments.get(s);
                nextSgmt = this.directSegments.get(s + 1);

                // Check for course change
                crsChg = NavCalc.getCourseChange(thisSgmt.getCourseEnd(), nextSgmt.getCourseStart());
                if(abs(crsChg) > 0.1){
                    // Initialize new turn segment
                    turnSgmt = new TurnSegment();

                    // Set Vas and bank angle
                    vAs = velocityPlan.getVasAtWpt(thisSgmt.getEndPt());

                    // Calculate turn radius and turn initiation and termination distances
                    if(vAs > maxTaxSpeed) {
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // Flight: Calculate turn radius and turn initiation and termination distances based on velocity and bank angle
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        bankAngle = PerfCalc.getBankAngleVas(vAs);

                        turnRad = pow(vAs, 2) / (9.80665 * tan(toRadians(bankAngle)));
                        turnDist = turnRad/tan(toRadians((180 - abs(crsChg)) / 2));

                        // Confirm that turn distance is sufficient to reach target bank angle
                        if((turnDist / vAs) * PerfCalc.getRollRateVas(vAs) < bankAngle && abs(crsChg) >= 5){
                            // Recalculate bank angle, turn radius and turn distance
                            bankAngle = (turnDist / vAs) * PerfCalc.getRollRateVas(vAs);
                            turnRad = pow(vAs, 2)/(9.80665 * tan(toRadians(bankAngle)));
                            turnDist = turnRad/tan(toRadians((180 - abs(crsChg)) / 2));
                        }

                        // Calculate and turn start, turn and end waypoints
                        turnPt = thisSgmt.getEndPt();
                        turnStartPt = lateralPlan.getItmWpt(turnPt, turnDist * -1);
                        turnEndPt = lateralPlan.getItmWpt(turnPt, turnDist);

                        // Assign vals to turn segment
                        turnSgmt.setRadius(turnRad);
                        turnSgmt.setStartPt(turnStartPt.getLat(),turnStartPt.getLon());
                        turnSgmt.setTurnPt(turnPt.getLat(),turnPt.getLon());
                        turnSgmt.setEndPt(turnEndPt.getLat(),turnEndPt.getLon());

                        // Adjust waypoints of this and next segment to fit in new turn segment
                        thisSgmt.setEndPt(turnStartPt.getLat(),turnStartPt.getLon());
                        nextSgmt.setStartPt(turnEndPt.getLat(), turnEndPt.getLon());
                    }
                    else {
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        // Taxi: Calculate turn radius and turn initiation and termination distances based on turn start / mid / end points
                        ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                        if(s < (this.directSegments.size() - 3)){
                            inbSgmt = thisSgmt;
                            outbSgmt = this.directSegments.get(s + 3);

                            // (Re)Calculate course change
                            bearing = NavCalc.getInitBearing(inbSgmt.getEndPt(), outbSgmt.getStartPt());
                            crsChg = NavCalc.getCourseChange(inbSgmt.getCourseEnd(),bearing) + NavCalc.getCourseChange(bearing,outbSgmt.getCourseStart());
                            //crsChg = abs(NavCalc.getCourseChange(inbSgmt.getCourseEnd(), outbSgmt.getCourseStart()));

                            // Define turn point at intersection of inbound and outbound track
                            turnStartPt = inbSgmt.getEndPt();
                            turnEndPt = outbSgmt.getStartPt();
                            turnPt = NavCalc.getIntersectWpt(inbSgmt.getEndPt(),inbSgmt.getCourseEnd(),outbSgmt.getStartPt(),NavCalc.getNewCourse(outbSgmt.getCourseStart(),180));
                            turnDist = NavCalc.getDirectDist(turnStartPt, turnPt);

                            turnChkDist = NavCalc.getDirectDist(inbSgmt.getStartPt(),outbSgmt.getEndPt()) / cos(toRadians(crsChg/2));

                            // Check for ambiguous results -> turn point must be inbetween turn start and end points
                            if(turnDist > turnChkDist)
                               turnPt = null;

                            // In case intersect point is not inbetween turn start and end points, adjust start/end waypoints and retry

                            if(turnPt == null){
                                // Find alternative intersect waypoint -> after turn end waypoint
                                turnPt = NavCalc.getIntersectWpt(inbSgmt.getEndPt(),inbSgmt.getCourseEnd(),outbSgmt.getStartPt(),outbSgmt.getCourseStart());
                                turnDist = NavCalc.getDirectDist(turnStartPt, turnPt);

                                // Check for ambiguous result -> turn point must be on outbound segment
                                if(turnDist > turnChkDist)
                                    turnPt = null;
                            }

                            if(turnPt == null){
                                // Find alternative intersect waypoint -> in front of turn start waypoint
                                turnPt = NavCalc.getIntersectWpt(inbSgmt.getEndPt(),NavCalc.getNewCourse(inbSgmt.getCourseEnd(),180),outbSgmt.getStartPt(),NavCalc.getNewCourse(outbSgmt.getCourseStart(),180));
                                turnDist = NavCalc.getDirectDist(turnPt, turnEndPt);

                                // Check for ambiguous result -> turn point must be on inbound segment
                                //turnChkDist = NavCalc.getDirectDist(turnPt,turnEndPt) / cos(toRadians(crsChg/2));

                                if(turnPt != null && turnDist < turnChkDist){
                                    inbSgmt.setEndPt(turnPt.getLat(),turnPt.getLon());
                                    turnStartPt = inbSgmt.getItmWpt(inbSgmt.getEndPt(),turnDist*-1);
                                    inbSgmt.setEndPt(turnStartPt.getLat(),turnStartPt.getLon());
                                } else
                                    turnPt = null;

                            }

                            // (Re)define outbound turn point, based on inbound turn segment distance
                            if(turnPt != null && abs(crsChg) < 180) {
                                outbSgmt.setStartPt(turnPt.getLat(), turnPt.getLon());
                                turnEndPt = outbSgmt.getItmWpt(outbSgmt.getStartPt(), turnDist);
                                outbSgmt.setStartPt(turnEndPt.getLat(), turnEndPt.getLon());


                                // Calculate turn radius and bearing for extended turn segment
                                c = NavCalc.getDirectDist(inbSgmt.getEndPt(), outbSgmt.getStartPt());
                                turnRad = c / (2 * sin(toRadians(crsChg / 2)));

                                // Assign vals to turn segment
                                turnSgmt.setRadius(turnRad);
                                turnSgmt.setStartPt(turnStartPt.getLat(), turnStartPt.getLon());
                                turnSgmt.setTurnPt(turnPt.getLat(), turnPt.getLon());
                                turnSgmt.setEndPt(turnEndPt.getLat(), turnEndPt.getLon());

                                // Jump next two, i.e. inbound and outbound, segments and continue
                                s += 2;

                            // Check for "U-Turn" and amend turn data accordingly
                            } else if(abs(crsChg) >= 180){

                                double wptCourse, courseOffset, xWpt, yWpt, xTi, yTi, xOffset, yOffset, latitude, longitude;

                                // Assign turn start and end points
                                turnSgmt.setStartPt(inbSgmt.getEndPt().getLat(), inbSgmt.getEndPt().getLon());
                                turnSgmt.setCourseStart(inbSgmt.getCourseEnd());

                                turnSgmt.setEndPt(outbSgmt.getStartPt().getLat(), outbSgmt.getStartPt().getLon());
                                turnSgmt.setCourseEnd(outbSgmt.getCourseStart());

                                // Calculate turn radius
                                c = NavCalc.getDirectDist(inbSgmt.getEndPt(), outbSgmt.getStartPt());
                                turnRad = c / (2 * sin(toRadians(abs(crsChg) / 2)));
                                turnSgmt.setRadius(turnRad);

                                // TODO Duplication of logic TurnSegment getItmWpt(), centralize in NavCalc utility class

                                // Calculate turn point
                                if(crsChg > 0){
                                    courseOffset = -90;
                                } else{
                                    courseOffset = 90;
                                }

                                wptCourse = NavCalc.getNewCourse(inbSgmt.getCourseEnd(),(crsChg / 2) + courseOffset);

                                xWpt = sin(toRadians(wptCourse + courseOffset)) * turnRad;
                                yWpt = cos(toRadians(wptCourse + courseOffset)) * turnRad;

                                // get course at turn initiation point and determine x,y coordinates
                                xTi = sin(toRadians(inbSgmt.getCourseEnd() + courseOffset)) * turnRad;
                                yTi = cos(toRadians(inbSgmt.getCourseEnd() + courseOffset)) * turnRad;

                                // calculate x,y offsets between intermediate waypoint and turn initiation point
                                xOffset = xWpt - xTi;
                                yOffset = yWpt - yTi;

                                // transform x,y offsets of intermediate waypoint to lat, long coordinates
                                latitude = yOffset / NavCalc.RADIUS_EARTH_FSX_M;
                                longitude = xOffset / (NavCalc.RADIUS_EARTH_FSX_M * cos(toRadians(inbSgmt.getEndPt().getLat())));
                                latitude = inbSgmt.getEndPt().getLat() + toDegrees(latitude);
                                longitude = inbSgmt.getEndPt().getLon() + toDegrees(longitude);

                                turnSgmt.setTurnPt(latitude, longitude);


                                // ALTERNATIVE TURN POINT CALCULATION: Simplified
                                //turnPt = NavCalc.getRadWpt(turnSgmt.getTurnCtrPt(), turnRad, wptCourse);
                                //turnSgmt.setTurnPt(turnPt.getLat(), turnPt.getLon());

                                s += 2;

                            // Raise exception
                            } else
                                throw new IllegalArgumentException();

                        }
                    }

                    // Add segments to list of lateral segments
                    this.latSegments.add(thisSgmt);
                    this.latSegments.add(turnSgmt);

                }else{
                    // Add segment to list of lateral segments
                    this.latSegments.add(thisSgmt);
                }
            }

            // Add final direct segment
            this.latSegments.add(this.directSegments.get(this.directSegments.size() - 1));

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the distance between the end and start points of consecutive segments.
     * A negative distance is an indication that the incorporation of turn segments by the transformation
     * process (method -> transform) had significant knock-on impacts on the lateral plan and the data is invalid.
     *
     * In a case where two consecutive turn segments overlap, the method aims to find matching transition
     * points to readjust the lateral plan. If this is not possible the data is marked as invalid and
     * the processing cannot continue.
     */
    public void validate(){
        try{
            Waypoint prevTrnWpt, prevEndWpt, nextTrnWpt, nextStartWpt, prevCtrWpt, nextCtrWpt;
            DirectSegment tempSgmt = new DirectSegment();
            TurnSegment prevSgmt,nextSgmt;
            double prevCrs, prevRad, nextCrs, nextRad, crsChg, prevInitDist, nextInitDist, theta, beta, tangCrs;
            int s = 0;

            dataValid = true;

            // Loop through direct segments
            for(LateralSegment thisSgmt : latSegments){
                if(thisSgmt instanceof DirectSegment){

                    // Check for difference in course between lateral plan and lateral track
                    if(lateralPlan.getDist(thisSgmt.getStartPt(),thisSgmt.getEndPt()) < 0 && s < (this.latSegments.size() -1)) {
                        dataValid = false;

                        // Get overlapping turn segments
                        prevSgmt = (TurnSegment) this.getSgmt(s - 1);
                        nextSgmt = (TurnSegment) this.getSgmt(s + 1);

                        // If both turn segments change into the same direction, i.e. left/left, right/right, inner tangents
                        // and transition points cannot be found.
                        if ((prevSgmt.getCourseChange() < 0 && nextSgmt.getCourseChange() > 0) || (prevSgmt.getCourseChange() > 0 && nextSgmt.getCourseChange() < 0)) {

                            // Get variables for each turn segment
                            prevCtrWpt = prevSgmt.getTurnCtrPt();
                            prevRad = prevSgmt.getRadius();

                            nextCtrWpt = nextSgmt.getTurnCtrPt();
                            nextRad = nextSgmt.getRadius();

                            // Calculate course of inner tangent
                            theta = toDegrees(asin((prevRad + nextRad) / NavCalc.getDirectDist(prevCtrWpt, nextCtrWpt)));

                            if (prevSgmt.getCourseChange() > 0)
                                tangCrs = NavCalc.getNewCourse(NavCalc.getInitBearing(prevCtrWpt, nextCtrWpt), theta);
                            else
                                tangCrs = NavCalc.getNewCourse(NavCalc.getInitBearing(prevCtrWpt, nextCtrWpt), theta * -1);

                            // Adjust previous turn segment
                            crsChg = NavCalc.getCourseChange(prevSgmt.getCourseStart(), tangCrs);
                            prevInitDist = prevSgmt.getRadius()/tan(toRadians((180 - abs(crsChg)) / 2));

                            prevTrnWpt = lateralPlan.getItmWpt(prevSgmt.getStartPt(), prevInitDist);
                            prevSgmt.setTurnPt(prevTrnWpt.getLat(), prevTrnWpt.getLon());

                            // Adjust next turn segment
                            crsChg = NavCalc.getCourseChange(tangCrs,nextSgmt.getCourseEnd());
                            nextInitDist = nextSgmt.getRadius()/tan(toRadians((180 - abs(crsChg)) / 2));

                            nextTrnWpt = lateralPlan.getItmWpt(nextSgmt.getEndPt(), nextInitDist * -1);
                            nextSgmt.setTurnPt(nextTrnWpt.getLat(), nextTrnWpt.getLon());

                            // Create temp direct segment between the two adjusted turn points and find end and start points of prev/next turn segments
                            tempSgmt.setStartPt(prevTrnWpt.getLat(), prevTrnWpt.getLon());
                            tempSgmt.setEndPt(nextTrnWpt.getLat(), nextTrnWpt.getLon());

                            prevEndWpt = tempSgmt.getItmWpt(tempSgmt.getStartPt(),prevInitDist);
                            prevSgmt.setEndPt(prevEndWpt.getLat(), prevEndWpt.getLon());

                            nextStartWpt = tempSgmt.getItmWpt(tempSgmt.getEndPt(),nextInitDist * -1);
                            nextSgmt.setStartPt(nextStartWpt.getLat(), nextStartWpt.getLon());

                            // Adjust direct segment
                            thisSgmt.setStartPt(prevEndWpt.getLat(), prevEndWpt.getLon());
                            thisSgmt.setEndPt(nextStartWpt.getLat(), nextStartWpt.getLon());

                            dataValid = true;
                        }else {
                            System.out.println("Lateral Track Validation Error! End Segment " + (s-1) + ": " + prevSgmt.getEndPt().getLat() + "/" + prevSgmt.getEndPt().getLon() + " Start Segment " + (s+1) + ": " + nextSgmt.getStartPt().getLat() + "/" + nextSgmt.getStartPt().getLon());
                        }
                    }
                }
                s++;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method checks whether the position of a cursor is within the lower and upper bounds of a list.
     *
     * @param cursor    The position of the cursor
     * @param list      The list
     * @param <T>       Generic parameter of the list
     * @return          Boolean indicating whether the cursor is within the list
     */
    private static <T> boolean inBound(int cursor, List<T> list){
        try{
            if(cursor < list.size() && cursor >= 0)
                return true;
            else
                return false;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }
}
