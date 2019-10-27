package ftdis.fdpu;

import java.util.*;

/**
 * The Weather Track class represents the weather along the lateral flight track as defined in the weather plan.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherTrack extends WeatherPlan implements Weather{
    public boolean dataValid = false;

    /**
     * Constructor(s)
     */
    WeatherTrack(){
        this.weatherSegments = new ArrayList<WeatherSegment>();
    }

    WeatherTrack(Integer trackID){
        this();
        this.id = trackID;
    }

    /**
     * This method returns the distance between the start point of a CAT (Clear Air Turbulence) and
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @param type  Type of CAT i.e. pitch, bank, or alt
     * @return      Distance between start of CAT and wpt in percent
     */
    public double getCatDistAtWpt(Waypoint wpt, String type){
        try{
            double dist, targetVal = 0;
            WeatherSegment wxSgm = this.getWptSgmt(wpt);
            dist = lateral.getDist(wxSgm.getStartPt(),wpt);

            if(type.equalsIgnoreCase("pitch")){
                targetVal = wxSgm.getCatDistAtDist(dist,"pitch");
            }else if(type.equalsIgnoreCase("bank")){
                targetVal = wxSgm.getCatDistAtDist(dist, "bank");
            }else if(type.equalsIgnoreCase("alt")){
                targetVal = wxSgm.getCatDistAtDist(dist,"alt");
            }

            return targetVal;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the target value of the CAT (Clear Air Turbulence) at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @param type  Type of CAT i.e. pitch, bank, or alt
     * @return      Target value of specific CAT at waypoint
     */
    public double getCatTargetValueAtWpt(Waypoint wpt, String type){
        try{
            double dist, targetVal = 0;
            WeatherSegment wxSgm = this.getWptSgmt(wpt);
            dist = lateral.getDist(wxSgm.getStartPt(),wpt);

            if(type.equalsIgnoreCase("pitch")){
                targetVal = wxSgm.getCatTargetValueAtDist(dist,"pitch");
            }else if(type.equalsIgnoreCase("bank")){
                targetVal = wxSgm.getCatTargetValueAtDist(dist,"bank");
            }else if(type.equalsIgnoreCase("alt")){
                targetVal = wxSgm.getCatTargetValueAtDist(dist,"alt");
            }

            return targetVal;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the weather's effect on the pitch angle of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on pitch angle in degrees, expressed as a deviation in degrees
     */
    public double getPitchDeviationAtWpt(Waypoint wpt){
        try{
            double dist;
            WeatherSegment wxSgm = this.getWptSgmt(wpt);
            dist = lateral.getDist(wxSgm.getStartPt(),wpt);

            return wxSgm.getPitchDeviationAtDist(dist);
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the weather's effect on the bank angle of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    public double getBankDeviationAtWpt(Waypoint wpt){
        try{
            double dist;
            WeatherSegment wxSgm = this.getWptSgmt(wpt);
            dist = lateral.getDist(wxSgm.getStartPt(),wpt);

            return wxSgm.getBankDeviationAtDist(dist);
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the weather's effect on the altitude of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on the altitude, expressed as a deviation in meters
     */
    public double getAltDeviationAtWpt(Waypoint wpt){
        try{
            double dist;
            WeatherSegment wxSgm = this.getWptSgmt(wpt);
            dist = lateral.getDist(wxSgm.getStartPt(),wpt);

            return wxSgm.getAltDeviationAtDist(dist);
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the weather's effect on the heading of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * NOTE: Not implemented at the moment. Returns null only.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on the altitude, expressed as a deviation in meters
     */
    public double getHeadingDeviationAtWpt(Waypoint wpt){
        try{
            return 0;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method loads the weather segments from a weather plan and aligns the start and end
     * waypoints of each segment to the lateral track.
     *
     * @param wxPlan   Reference to the weather plan
     */
    public void transform(WeatherPlan wxPlan){
        try{
            double vAsu, dist;
            WeatherSegment wxSgm;

            for(int s =0; s < wxPlan.getSgmtCount(); s++){
                wxSgm = new WeatherSegment(wxPlan.getSgmt(s));

                // Set start and end waypoints
                if(lateral.getWptSgmt(wxSgm.getStartPt()) instanceof TurnSegment)
                    ((TurnSegment) lateral.getWptSgmt(wxSgm.getStartPt())).relocateWpt(wxSgm.getStartPt());

                if(lateral.getWptSgmt(wxSgm.getEndPt()) instanceof TurnSegment)
                    ((TurnSegment) lateral.getWptSgmt(wxSgm.getEndPt())).relocateWpt(wxSgm.getEndPt());

                // Adjust distance
                dist = lateral.getDist(wxSgm.getStartPt(), wxSgm.getEndPt());
                wxSgm.setDist(dist);

                // Initialize segment and create random CATs (Clear Air Turbulence) for pitch, bank and altitude axes
                vAsu = velocity.getVasu(wxSgm.getStartPt(), wxSgm.getEndPt());
                wxSgm.init(vAsu,"pitch");
                wxSgm.init(vAsu,"bank");
                wxSgm.init(vAsu,"alt");

                this.weatherSegments.add(wxSgm);
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method validates the integrity of the weather track. If segments overlap, i.e. if end and start points
     * of two consecutive waypoints do not match, the data is marked as invalid.
     *
     */
    public void validate(){
        try{
            Waypoint startWpt, endWpt;
            dataValid = true;

            for(int s = 0; s < this.weatherSegments.size() - 1; s++){
                endWpt = this.weatherSegments.get(s).getEndPt();
                startWpt = this.weatherSegments.get(s + 1).getStartPt();

                if(lateral.getDist(startWpt,endWpt) < 0)
                    dataValid = false;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void load(String filename, int planId){
    }


}
