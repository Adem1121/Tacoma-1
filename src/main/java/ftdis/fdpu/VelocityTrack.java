package ftdis.fdpu;

import java.util.*;

import static java.lang.Math.*;

/**
 * The Velocity Track class represents the velocity along the lateral flight track as defined in the velocity plan.
 * The class is on of the three key components of the flight track class.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VelocityTrack extends VelocityPlan implements Velocity{
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    VelocityTrack(){
        this.velocitySegments = new ArrayList<VelocitySegment>();
    }

    VelocityTrack(Integer trackID){
        this();
        this.id = trackID;
    }

    /**
     * This method transforms the velocity segments from a velocity plan and aligns the start and end
     * waypoints of each segment to the lateral track.
     *
     * @param velPlan   Reference to Velocity Plan
     */
    public void transform(VelocityPlan velPlan){
        try{
            VelocitySegment velSgmt, refSgmt;
            double dist, a, waitDist, waitSpd = 1E-2, waitTime;
            boolean alignSgmt = false;

            for(int s = 0; s < velPlan.getSgmtCount(); s++){
                velSgmt = new VelocitySegment(velPlan.getSgmt(s));

                // Ensure that waypoints are aligned to flight track
                lateral.alignWpt(velSgmt.getStartPt());
                lateral.alignWpt(velSgmt.getEndPt());

                if(lateral.getWptSgmt(velSgmt.getStartPt()) instanceof TurnSegment){
                    ((TurnSegment) lateral.getWptSgmt(velSgmt.getStartPt())).relocateWpt(velSgmt.getStartPt());
                    alignSgmt = true;
                }

                if(lateral.getWptSgmt(velSgmt.getEndPt()) instanceof TurnSegment){
                    ((TurnSegment) lateral.getWptSgmt(velSgmt.getEndPt())).relocateWpt(velSgmt.getEndPt());
                    alignSgmt = true;
                }


                if(alignSgmt){

                    dist = lateral.getDist(velSgmt.getStartPt(),velSgmt.getEndPt());

                    waitDist = velSgmt.getOffset() * waitSpd;

                    // In case of wait segment, adjust start and end speeds to compensate for potential change in segment's distance
                    if(velSgmt.getDist() <= waitDist && velSgmt.getAcc() == 0) {
                        // Recalculate start and end speeds for wait segment
                        //waitTime = waitDist / velSgmt.getVasi();
                        waitSpd = waitDist/velSgmt.getOffset();
                        velSgmt.setVasi(waitSpd);
                        velSgmt.setVasf(waitSpd);

                        // Adjust start/end speeds of previous/next segment
                        if(s > 0) {
                            refSgmt = this.velocitySegments.get(s - 1);
                            refSgmt.setVasf(waitSpd);
                            refSgmt.setAcc((pow(refSgmt.getVasf(), 2) - pow(refSgmt.getVasi(), 2)) / (2 * lateral.getDist(refSgmt.getStartPt(),refSgmt.getEndPt())));
                        }

                        if(s < (velPlan.getSgmtCount() - 2)){
                            refSgmt = velPlan.getSgmt(s + 1);
                            refSgmt.setVasi(waitSpd);
                            refSgmt.setAcc((pow(refSgmt.getVasf(), 2) - pow(refSgmt.getVasi(), 2)) / (2 * lateral.getDist(refSgmt.getStartPt(),refSgmt.getEndPt())));
                        }
                    }


                    a = (pow(velSgmt.getVasf(),2) - pow(velSgmt.getVasi(),2))/(2 * dist);
                    velSgmt.setAcc(a);
                    velSgmt.setDist(dist);

                    alignSgmt = false;
                }


                this.velocitySegments.add(velSgmt);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the integrity of the velocity track. If segments overlap, i.e. if end and start points
     * of two consecutive waypoints do not match, return false.
     *
     */
    public void validate(){
        try{
            Waypoint startWpt, endWpt;
            dataValid = true;

            for(int s = 0; s < this.velocitySegments.size() - 1; s++){
                endWpt = this.velocitySegments.get(s).getEndPt();
                startWpt = this.velocitySegments.get(s + 1).getStartPt();

                if(lateral.getDist(startWpt,endWpt) < 0)
                    dataValid = false;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void load(String filename, int planID){
    }


}
