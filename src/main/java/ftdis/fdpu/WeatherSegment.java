package ftdis.fdpu;

import java.util.*;
import static ftdis.fdpu.Config.*;

/**
 * The Weather Segment class is a key component for the Weather Plan and Weather Track classes.
 * It holds all of the data concerning the weather conditions, such as wind direction, or turbulence,
 * as well as their magnitude and frequency, of a specific segment along the lateral plan/track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherSegment {
    public int id;
    private Waypoint[] waypoints;
    private double dist, catMagn, catFreq, windDir, windSpd, minDur, maxDur, minPitch, maxPitch, minBank, maxBank, minAlt, maxAlt;
    private List<Cat> pitchEvents;
    private List<Cat> bankEvents;
    private List<Cat> altEvents;
    private List<Cat> headingEvents;

    /**
     * Constructor(s)
     */
    WeatherSegment(){
        waypoints = new Waypoint[2];
        this.waypoints[0] = new Waypoint();
        this.waypoints[1] = new Waypoint();
        this.pitchEvents = new ArrayList<Cat>();
        this.bankEvents = new ArrayList<Cat>();
        this.altEvents = new ArrayList<Cat>();
        this.headingEvents = new ArrayList<Cat>();

        catMagn = Double.NaN;
        catFreq = Double.NaN;
        windDir = Double.NaN;
        windSpd = Double.NaN;

        // Min and max duration fields define the minimum and maximum duration for each dimension during a CAT
        minDur = CAT_MIN_DUR;
        maxDur = CAT_MAX_DUR;
        minPitch = CAT_MIN_PITCH;
        maxPitch = CAT_MAX_PITCH;
        minBank = CAT_MIN_BANK;
        maxBank = CAT_MAX_BANK;
        minAlt = CAT_MIN_ALT;
        maxAlt = CAT_MAX_ALT;
    }

    WeatherSegment(int eventID){
        this();
        this.id = eventID;
    }

    WeatherSegment(WeatherSegment obj){
        this();
        this.id = obj.id;
        this.setStartPt(obj.getStartPt().getLat(),obj.getStartPt().getLon());
        this.setEndPt(obj.getEndPt().getLat(),obj.getEndPt().getLon());
        this.setCat(obj.getCatMagn(), obj.getCatFreq());
        this.setWind(obj.getWindDir(), obj.getWindSpd());
        this.setDist(obj.getDist());
    }

    /**
     * This method sets the latitude and longitude coordinates of the event's start waypoint.
     *
     * @param latitude  The latitude at the event's start waypoint in decimal format
     * @param longitude The longitude at the event's start waypoint in decimal format
     */
    public void setStartPt(double latitude, double longitude){
        this.waypoints[0].setLat(latitude);
        this.waypoints[0].setLon(longitude);
    }

    /**
     * This method sets the latitude and longitude coordinates of the event's end waypoint.
     *
     * @param latitude  The latitude at the event's end waypoint in decimal format
     * @param longitude The longitude at the event's end waypoint in decimal format
     */
    public void setEndPt(double latitude, double longitude){
        this.waypoints[1].setLat(latitude);
        this.waypoints[1].setLon(longitude);
    }

    /**
     * This method sets the total distance of the segment along the lateral plan/track.
     *
     * @param dist  Distance of segment in meters.
     */
    public void setDist(double dist){
        this.dist = dist;
    }

    /**
     * This method set the change weather event's clear air turbulence (CAT)
     *
     * @param magn  Magnitude, i.e. severance, of the weather event
     * @param freq  Frequency of occurrence, of the weather event
     */
    public void setCat(double magn, double freq){
        this.catMagn = magn;
        this.catFreq = freq;
    }

    /**
     * This method set the change weather event's clear air turbulence (CAT)
     *
     * @param dir  Wind direction in degrees
     * @param spd  Wind speed in m/s
     */
    public void setWind(double dir, double spd){
        this.windDir = dir;
        this.windSpd = spd;
    }

    /**
     * This method returns the clear air turbulence's (CAT) magnitude of the change weather event.
     *
     * @return  Magnitude, i.e. severance, of the weather event
     */
    public double getCatMagn() {
        try {
            return this.catMagn;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the clear air turbulence's (CAT) frequency of the change weather event.
     *
     * @return  Frequency of the weather event
     */
    public double getCatFreq() {
        try {
            return this.catFreq;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the wind direction of the change weather event.
     *
     * @return  Wind direction in degrees
     */
    public double getWindDir() {
        try {
            return this.windDir;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the wind speed of the change weather event.
     *
     * @return  Wind speed in m/s
     */
    public double getWindSpd() {
        try {
            return this.windSpd;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the start waypoint of the event.
     *
     * @return The event's start waypoint
     */
    public Waypoint getStartPt(){
        return this.waypoints[0];
    }

    /**
     * This method returns the end waypoint of the event.
     *
     * @return The event's end waypoint
     */
    public Waypoint getEndPt(){
        return this.waypoints[1];
    }

    /**
     * This method returns the distance of the lateral segment required to complete the
     * event in meters
     *
     * @return The length of the lateral segment in meters.
     */
    public double getDist(){
        try{
            return this.dist;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the distance between the start point of a CAT (Clear Air Turbulence) and
     * a specific distance from the start of the segment.
     *
     * @param dist  Distance from start point of segment in meters
     * @param type  Type of CAT i.e. pitch, bank, or alt
     * @return      Distance between start of CAT and wpt in percent
     */
    public double getCatDistAtDist(double dist, String type){
        try{
            Cat refCat;
            double targetDist = 0;

            if(type.equalsIgnoreCase("pitch")){
                refCat = this.getCatDist(dist,this.pitchEvents);
                targetDist = (dist - refCat.startDist) / refCat.dist;
            }else if(type.equalsIgnoreCase("bank")){
                refCat = this.getCatDist(dist,this.bankEvents);
                targetDist = (dist - refCat.startDist) / refCat.dist;
            }else if(type.equalsIgnoreCase("alt")){
                refCat = this.getCatDist(dist,this.altEvents);
                targetDist = (dist - refCat.startDist) / refCat.dist;
            }

            return targetDist;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns the target value of the CAT (Clear Air Turbulence) at
     * a specific distance from the start of the segment.
     *
     * @param dist  Distance from start point of segment in meters
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    public double getCatTargetValueAtDist(double dist, String type){
        try{
            Cat refCat;
            double targetVal = 0;

            if(type.equalsIgnoreCase("pitch")){
                refCat = this.getCatDist(dist,this.pitchEvents);
                targetVal = refCat.value;
            }else if(type.equalsIgnoreCase("bank")){
                refCat = this.getCatDist(dist,this.bankEvents);
                targetVal = refCat.value;
            }else if(type.equalsIgnoreCase("alt")){
                refCat = this.getCatDist(dist,this.altEvents);
                targetVal = refCat.value;
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
     * @param dist  Distance from start point of segment in meters
     * @return      Effect on pitch angle in degrees, expressed as a deviation in degrees
     */
    public double getPitchDeviationAtDist(double dist){
        try{
            double catDist, catVal;
            // Get CAT event
            Cat pitchCat = this.getCatDist(dist,this.pitchEvents);

            // Calculate value
            if((dist - pitchCat.startDist) <= (pitchCat.dist / 2)){
                // UPDATE MAKE SURE MINIMUM TIME FRAME IS AVAILABLE ELSE 0
                catDist = (dist - pitchCat.startDist) / (pitchCat.dist / 2);
                catVal = pitchCat.value * catDist;
            }else{
                catDist = ((dist - pitchCat.startDist) - (pitchCat.dist / 2)) / (pitchCat.dist / 2);
                catVal = pitchCat.value * (1 - catDist);
            }

            // Catch null return values
            if(Double.isNaN(catVal))
                catVal = 0;

            return catVal;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the weather's effect on the bank angle of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param dist  Distance from start point of segment in meters
     * @return      Effect on bank angle in degrees, expressed as a deviation in degrees
     */
    public double getBankDeviationAtDist(double dist){
        try{
            double catDist, catVal;
            // Get CAT event
            Cat bankCat = this.getCatDist(dist,this.bankEvents);

            // Calculate value
            if((dist - bankCat.startDist) <= (bankCat.dist / 2)){
                catDist = (dist - bankCat.startDist) / (bankCat.dist / 2);
                catVal = bankCat.value * catDist;
            }else{
                catDist = ((dist - bankCat.startDist) - (bankCat.dist / 2)) / (bankCat.dist / 2);
                catVal = bankCat.value * (1 - catDist);
            }

            // Catch null return values
            if(Double.isNaN(catVal))
                catVal = 0;

            return catVal;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the weather's effect on the altitude of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * @param dist  Distance from start point of segment in meters
     * @return      Effect on the altitude, expressed as a deviation in meters
     */
    public double getAltDeviationAtDist(double dist){
        try{
            double catDist = 0, catVal = 0;
            // Get CAT event
            Cat altCat = this.getCatDist(dist,this.altEvents);

            // Calculate value
            if((dist - altCat.startDist) <= (altCat.dist / 2)){
                catDist = (dist - altCat.startDist) / (altCat.dist / 2);
                catVal = altCat.value * catDist;
            }else{
                catDist = ((dist - altCat.startDist) - (altCat.dist / 2)) / (altCat.dist / 2);
                catVal = altCat.value * (1 - catDist);
            }

            // Catch null return values
            if(Double.isNaN(catVal))
                catVal = 0;

            return catVal;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the weather's effect on the heading of the airframe at
     * a specific waypoint along the flight plan/track.
     *
     * NOTE: Not implemented at the moment. Returns null only.
     *
     * @param dist  Distance from start point of segment in meters
     * @return      Effect on the altitude, expressed as a deviation in meters
     */
    public double getHeadingDeviationAtDist(double dist){
        try{
            return 0;
        }catch(Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method initializes the weather segment by creating random pitch, bank, altitude and heading events
     * based on / within the boundaries specified in the event definition.
     *
     * NOTE: The initialization process can only be started, after the segment's have been aligned to the lateral
     * flight track. Otherwise, there is a risk of knock-on effects on the event data, when aligning the segments
     * to the lateral track.
     *
     * @param vAsu  The uniform airspeed of the segment in m/s
     * @param type  Type of event, i.e. pitch, bank, alt
     */
    public void init(double vAsu, String type){
        try{
            Cat cat;
            List<Cat> catList;
            double runDist, sgmDist, catDist, catVal, minVal, maxVal, minDur, maxDur, magn, freq;
            boolean endPointReached = false;

            sgmDist = this.getDist();
            runDist = 0;

            if(type.equalsIgnoreCase("pitch")){
                catList = pitchEvents;
                magn = this.getCatMagn();
                freq = this.getCatFreq();
                minVal = this.minPitch;
                maxVal = this.maxPitch;
                minDur = this.minDur;
                maxDur = this.maxDur;
            } else if(type.equalsIgnoreCase("bank")){
                catList = bankEvents;
                magn = this.getCatMagn();
                freq = this.getCatFreq();
                minVal = this.minBank;
                maxVal = this.maxBank;
                minDur = this.minDur;
                maxDur = this.maxDur;
            } else if(type.equalsIgnoreCase("alt")){
                catList = altEvents;
                magn = this.getCatMagn();
                freq = this.getCatFreq();
                minVal = this.minAlt;
                maxVal = this.maxAlt;
                minDur = this.minDur;
                maxDur = this.maxDur;
            } else {
                catList = new ArrayList<Cat>();
                magn = Double.NaN;
                freq = Double.NaN;
                minVal = Double.NaN;
                maxVal = Double.NaN;
                minDur = Double.NaN;
                maxDur = Double.NaN;
            }

            // Initialize CAT event
            while(!endPointReached){
                catVal = randDbl(minVal * magn, maxVal * magn);
                catDist = randDbl(minDur, maxDur) * vAsu;

                if((runDist + catDist) <= (sgmDist * freq)){
                    runDist += catDist;
                }else {
                    endPointReached = true;

                    // Adjust to remaining distance
                    catDist = (sgmDist * freq) - runDist;

                    // If remaining distance doesn't meet minimum duration, set value of CAT event to 0
                    if(catDist/vAsu < minDur)
                        catVal = 0;
                }
                // Create new CAT, assign random values and add to list
                cat = new Cat();
                cat.dist = catDist;
                cat.value = catVal;
                catList.add(cat);
            }

            runDist = 0;
            catDist = (sgmDist * (1 - freq)) / catList.size();
            for(int s = 0; s < catList.size(); s++){
                if ((s & 1) == 0 ) {
                    // Even, i.e. create connecting segment
                    cat = new Cat();
                    cat.id = s + 1;
                    cat.dist = catDist;
                    cat.startDist = runDist;
                    cat.endDist = cat.startDist + cat.dist;
                    cat.value = 0;
                    catList.add(s,cat);

                } else {
                    // Odd, i.e. update CAT segment
                    cat = catList.get(s);
                    cat.id = s + 1;
                    cat.startDist = runDist;
                    cat.endDist = cat.startDist + cat.dist;
                }
                runDist = cat.endDist;
            }
            System.out.println();
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method finds and returns a Weather Segment's CAT, based on the distance from the segment's
     * start point.
     *
     * @param dist      Distance from the Weather Segment's start point in meters
     * @param catList   Reference to Weather Segment's specific list of CATs, i.e. pitch, bank, or altitude.
     * @return          CAT
     */
    private Cat getCatDist(double dist, List<Cat> catList){
        try{
            Cat refCat = new Cat();
            for(Cat thisCat : catList){
                if(dist <= thisCat.endDist && dist >= thisCat.startDist){
                    refCat = thisCat;
                    break;
                }
            }
            return refCat;
        } catch (Exception e){
            System.out.println(e.getMessage());
            return new Cat();
        }
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     *
     * @param min   Minimum value
     * @param max   Maximum value.  Must be greater than min.
     * @return      Double value between min and max, inclusive.
     */
    public static double randDbl(double min, double max) {
        return min + Math.random() * (max - min);
    }

    /**
     * The nested Cat class represents a random clear air turbulence (CAT) occurring within a weather segment.
     * Since this class is only used by the WeatherSegment class, it is embedded to keep the two classes and
     * the code of their implementation together making the package more streamlined.
     */
    class Cat{
        public int id;
        public double startDist;
        public double endDist;
        public double dist;
        public double value;

        /**
         * Constructor(s)
         */
        Cat(){
            this.id = 0;
            this.startDist = Double.NaN;
            this.endDist = Double.NaN;
            this.value = Double.NaN;
        }

        Cat(int id){
            this();
            this.id = id;
        }
    }
}
