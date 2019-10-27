package ftdis.fdpu;

import java.util.*;

import static java.lang.Math.*;

/**
 * The Vertical Track class represents the altitude along the lateral flight track as defined in the vertical plan.
 * The class is on of the three key components of the flight track class.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class VerticalTrack extends VerticalPlan implements Vertical{
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    VerticalTrack(){
        this.verticalSegments = new ArrayList<VerticalSegment>();
    }

    VerticalTrack(Integer trackID){
        this();
        this.id = trackID;
    }

    /**
     * This method loads the vertical segments from a vertical plan and aligns the start and end
     * waypoints of each segment to the lateral track.
     *
     * @param vertPlan   Reference to the vertical plan
     */
    public void transform(VerticalPlan vertPlan){
        try{
            VerticalSegment vertSgmt;
            double dist, vAsu, t;

            for(int s =0; s < vertPlan.getSgmtCount(); s++){
                vertSgmt = new VerticalSegment(vertPlan.getSgmt(s));

                // Set start and end waypoints
                if(lateral.getWptSgmt(vertSgmt.getStartPt()) instanceof TurnSegment)
                    ((TurnSegment) lateral.getWptSgmt(vertSgmt.getStartPt())).relocateWpt(vertSgmt.getStartPt());

                if(lateral.getWptSgmt(vertSgmt.getEndPt()) instanceof TurnSegment)
                    ((TurnSegment) lateral.getWptSgmt(vertSgmt.getEndPt())).relocateWpt(vertSgmt.getEndPt());

                // Adjust vertical speed
                dist = lateral.getDist(vertSgmt.getStartPt(), vertSgmt.getEndPt());
                vAsu = velocity.getVasu(vertSgmt.getStartPt(), vertSgmt.getEndPt());
                t = dist/vAsu;
                vertSgmt.setVs((vertSgmt.getAltf()-vertSgmt.getAlti())/t);

                // Adjust alpha parameter
                vertSgmt.setAlpha(atan((vertSgmt.getAltf() - vertSgmt.getAlti())/dist));

                // Adjust distance
                vertSgmt.setDist(dist);

                this.verticalSegments.add(vertSgmt);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the integrity of the vertical track. If segments overlap, i.e. if end and start points
     * of two consecutive waypoints do not match.
     *
     */
    public void validate(){
        try{
            Waypoint startWpt, endWpt;
            dataValid = true;

            for(int s = 0; s < this.verticalSegments.size() - 1; s++){
                endWpt = this.verticalSegments.get(s).getEndPt();
                startWpt = this.verticalSegments.get(s + 1).getStartPt();

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
