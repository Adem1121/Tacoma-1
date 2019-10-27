package ftdis.fdpu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static ftdis.fdpu.DOMUtil.*;
import static java.lang.Math.*;

/**
 * The Weather Plan class represents the planned weather along the flight plan as per the Change Weather
 * events defined in the external xml file.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherPlan implements Weather{
    public int id;
    public boolean dataValid = false;
    protected Lateral lateral;
    protected Velocity velocity;
    private List<EventChgWeather> events;
    protected List<WeatherSegment> weatherSegments;

    /**
     * Constructor(s)
     */

    WeatherPlan(){
        this.events = new ArrayList<EventChgWeather>();
        this.weatherSegments = new ArrayList<WeatherSegment>();
    }

    WeatherPlan(int planID){
        this();
        this.id = planID;
    }

    /**
     * This method assigns a lateral plan/track to the vertical plan.
     *
     * @param latPlan   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral latPlan){
        this.lateral = latPlan;
    }

    /**
     * This method assigns a velocity plan/track to the vertical plan.
     *
     * @param velPlan   Reference to a lateral plan/track.
     */
    public void assignVel(Velocity velPlan){
        this.velocity = velPlan;
    }

    /**
     * This method adds a weather segment to the weather plan's segment list
     *
     * @param sgmt Weather segment segment to be added.
     */
    public void addSgmt(WeatherSegment sgmt){
        this.weatherSegments.add(sgmt);
    }

    /**
     * This method returns the weather plan's segment of a specific segment number
     *
     * @param sgmtNum Number of the weather segment, index starts at 0
     * @return The weather segment
     */
    public WeatherSegment getSgmt(int sgmtNum){
        return ListUtil.getListItem(sgmtNum, this.weatherSegments);
    }

    /**
     * This method finds and returns the corresponding weather segment of a waypoint positioned
     * on the lateral plan/track, i.e. plan/track error must be zero.
     *
     * @param wpt   Waypoint
     * @return      Corresponding weather segment of the waypoint
     */
    public WeatherSegment getWptSgmt(Waypoint wpt){
        try{
            // loop through each direct segment and calculate track error
            int sgmt, i = 0;
            double startDist,endDist,smlst;
            double[] planError = new double[this.weatherSegments.size()];

            for(WeatherSegment wxSgmt : this.weatherSegments){
                //startDist = abs(lateral.getDist(wxSgmt.getStartPt(),wpt));
                //endDist = abs(lateral.getDist(wpt,wxSgmt.getEndPt()));
                startDist = lateral.getDist(wxSgmt.getStartPt(), wpt);
                endDist = lateral.getDist(wpt, wxSgmt.getEndPt());

                if(startDist < 0 || (endDist <= 0 && i < this.weatherSegments.size() - 1))
                    planError[i] = 9999;
                else
                    planError[i] = (startDist + endDist) / wxSgmt.getDist();

                i++;
            }

            // find index of segment with smallest track error
            smlst = planError[0];
            sgmt = 0;
            for(i = 0; i < planError.length; i++) {
                if(smlst > planError[i]) {
                    smlst = planError[i];
                    sgmt = i;
                }
            }
            // return segment
            return this.weatherSegments.get(sgmt);
        }catch(Exception e){
            System.out.println(e.getMessage());
            return null;
        }
    }

    /**
     * This method returns the position of a specific weather segment in the list of segments
     *
     * @param sgmt      The weather segment
     * @return          The position of the weather segment in the list, index starts at 0
     */
    public int getSgmtPos(WeatherSegment sgmt){
        return ListUtil.getListItemNum(sgmt, weatherSegments);
    }

    /**
     * This method returns the total number of segments in the list of weather segments
     *
     * @return The total number of weather segments
     */
    public int getSgmtCount(){
        return this.weatherSegments.size();
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
            return 0;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the target value of the CAT (Clear Air Turbulence) at
     * a specific waypoint along the flight plan/track.
     *
     * @param wpt   Waypoint along the flight plan/track.
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    public double getCatTargetValueAtWpt(Waypoint wpt, String type){
        try{
            return 0;
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
            return 0;
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
            return 0;
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
            return 0;
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
     * This method loads the weather events from an external xml file, creating and adding corresponding
     * Change Weather events to the weather plan
     *
     * @param fileName  The complete path and file name of the external event collection xml file.
     * @param planID    ID of the plan to be loaded
     */
    public void load(String fileName, int planID){
        try{
            Waypoint waypoint;
            EventChgWeather chgWeather;

            if(fileName != null && !fileName.isEmpty()){
                // create Velocity Plan document and parse xml file
                DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                Document eventCollectionXML = dBuilder.parse(new File(fileName));

                // normalize
                eventCollectionXML.getDocumentElement().normalize();

                // Get flight plan and loop through corresponding weather events
                Node plan = findNode(eventCollectionXML.getDocumentElement().getChildNodes(), "Plan", "ID", Integer.toString(planID));
                NodeList xmlEvents = plan.getChildNodes();

                for (int n = 0; n < xmlEvents.getLength(); n++) {
                    Node node = xmlEvents.item(n);
                    if (node.getNodeName().equalsIgnoreCase("Event")) {
                        //check for attribute name and value
                        if (getAttributeValue(node, "type").equalsIgnoreCase("chgWeather")) {
                            chgWeather = new EventChgWeather();
                            chgWeather.assignLat(lateral);

                            // Get Waypoints
                            Node waypoints = findNode(node.getChildNodes(),"Waypoints");
                            List<Node> wptList = getChildElementsByTagName(waypoints, "Waypoint");

                            for(Node wptNode : wptList){
                                // get reference to new waypoint and set latitude and longitude coordinates
                                waypoint = new Waypoint((Integer.parseInt(getAttributeValue(wptNode,"ID"))));

                                waypoint.setLat(Double.parseDouble(
                                        getElementValue(
                                                findNode(wptNode.getChildNodes(), "Latitude", "unit", "dec"))));

                                waypoint.setLon(Double.parseDouble(
                                        getElementValue(
                                                findNode(wptNode.getChildNodes(), "Longitude", "unit", "dec"))));

                                if(waypoint.id == 1)
                                    chgWeather.setStartPt(waypoint.getLat(),waypoint.getLon());
                                else if(waypoint.id ==2)
                                    chgWeather.setEndPt(waypoint.getLat(), waypoint.getLon());
                            }

                            // Get Variables
                            Node variables = findNode(node.getChildNodes(),"Variables");

                            chgWeather.setTurbulence(
                                    Double.parseDouble(
                                            getElementValue(
                                                    findNode(variables.getChildNodes(), "Var", "type", "magn"))),
                                    Double.parseDouble(
                                            getElementValue(
                                                    findNode(variables.getChildNodes(), "Var", "type", "freq"))));

                            chgWeather.setWind(
                                    Double.parseDouble(
                                            getElementValue(
                                                    findNode(variables.getChildNodes(), "Var", "type", "windDir"))),
                                    Double.parseDouble(
                                            getElementValue(
                                                    findNode(variables.getChildNodes(), "Var", "type", "windSpd"))));

                            // Validate event and add to velocity plan
                            chgWeather.validate();
                            this.events.add(chgWeather);
                        }
                    }
                }

            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method transforms the set of change weather events into a consecutive set of weather segments.
     */
    public void transform(){
        WeatherSegment wxSgmt,itmSgmt;
        int s = 0, i = 1;

        // Loop through change weather events and create consecutive set of weather segments
        for(EventChgWeather event : this.events){
            // Transform change weather events
            wxSgmt = new WeatherSegment();
            wxSgmt.setStartPt(event.getStartPt().getLat(), event.getStartPt().getLon());
            wxSgmt.setEndPt(event.getEndPt().getLat(), event.getEndPt().getLon());
            wxSgmt.setDist(lateral.getDist(wxSgmt.getStartPt(), wxSgmt.getEndPt()));
            wxSgmt.setCat(event.getTurbulenceMagn(), event.getTurbulenceFreq());
            wxSgmt.setWind(event.getWindDir(), event.getWindSpd());
            this.weatherSegments.add(wxSgmt);

            // Build intermediate segment
            if(s < (this.events.size() - 1) && lateral.getDist(this.events.get(s).getEndPt(),this.events.get(s +1).getStartPt()) > 0){
                itmSgmt = new WeatherSegment();
                itmSgmt.setStartPt(this.events.get(s).getEndPt().getLat(), this.events.get(s).getEndPt().getLon());
                itmSgmt.setEndPt(this.events.get(s + 1).getStartPt().getLat(), this.events.get(s + 1).getStartPt().getLon());
                itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
                itmSgmt.setCat(0, 0);
                itmSgmt.setWind(0, 0);
                this.weatherSegments.add(itmSgmt);
            }
            s++;
        }

        // Check for gap between start of lateral plan/track and first weather segment
        if(lateral.getDist(lateral.getStartWpt(),this.weatherSegments.get(0).getStartPt()) > 0){
            itmSgmt = new WeatherSegment();
            itmSgmt.setStartPt(lateral.getStartWpt().getLat(), lateral.getStartWpt().getLon());
            itmSgmt.setEndPt(this.weatherSegments.get(0).getStartPt().getLat(), this.weatherSegments.get(0).getStartPt().getLon());
            itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
            itmSgmt.setCat(0, 0);
            itmSgmt.setWind(0, 0);
            this.weatherSegments.add(0,itmSgmt);
        }

        // Check for gap between final weather segment and end of lateral plan/track
        s = this.weatherSegments.size() - 1;
        if(lateral.getDist(this.weatherSegments.get(s).getEndPt(),lateral.getEndWpt()) > 0){
            itmSgmt = new WeatherSegment();
            itmSgmt.setStartPt(this.weatherSegments.get(s).getEndPt().getLat(), this.weatherSegments.get(s).getEndPt().getLon());
            itmSgmt.setEndPt(lateral.getEndWpt().getLat(),lateral.getEndWpt().getLon());
            itmSgmt.setDist(lateral.getDist(itmSgmt.getStartPt(), itmSgmt.getEndPt()));
            itmSgmt.setCat(0, 0);
            itmSgmt.setWind(0, 0);
            this.weatherSegments.add(itmSgmt);
        }

        // Set ids of vertical segments
        for(WeatherSegment sgmt : this.weatherSegments) {
            sgmt.id = i++;
        }
    }

    /*
     * This method validates the integrity of the weather segment set. In the case of an overlap between two
     * separate segments, the data set is marked as invalid.
     */
    public void validate(){
        try{
            dataValid = true;

            for(int s = 0; s < this.weatherSegments.size() - 1; s++){
                // Check whether connecting segment between two weather segments runs in opposite direction of lateral plan
                if(this.weatherSegments.get(s).getDist() < 0){
                    dataValid = false;
                }
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }
}
