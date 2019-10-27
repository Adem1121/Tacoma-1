package ftdis.fdpu;

import static ftdis.fdpu.Config.*;

/**
 * The AircraftSystem class represents the aircraft's systems, such as and lights and doors, and contains
 * a set of methods to calculate the systems' states along the flight track.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class AircraftSystem {
    public int id;
    private Lateral lateral;
    private Velocity velocity;
    private Vertical vertical;
    private int seatSign, smkSign, attSign;

    // The state of the aircraft lights is represented by a 10 bit bitfield
    // 1 Nav, 2 Beacon, 3 Landing, 4 Taxi, 5 Strobe, 6 Panel, 7 Recognition, 8 Wing, 9 Logo, 10 Cabin
    public final int PARKLIGHT = 769;    // 1100000001
    public final int NAVLIGHT   = 771;   // 1100000011
    public final int TAXILIGHT = 907;    // 1110001011
    public final int LANDINGLIGHT = 927; // 1110011111
    public final int CRUISELIGHT  = 787; // 1100010011

    //private double strobeTimeStmp, beaconTimeStmp;

    /**
     * Constructor(s)
     */
    AircraftSystem(){
        this.id = 0;
        this.seatSign = 0;
        this.smkSign = 0;
        this.attSign = 0;
        //this.strobeTimeStmp = 0;
        //this.beaconTimeStmp = 0;
    }

    AircraftSystem(Integer aircraftID){
        this();
        this.id = aircraftID;
    }

    /**
     * This methods resets the aircraft systems' vars to their default state
     */
    public void reset(){
        this.seatSign = 0;
        this.smkSign = 0;
        this.attSign = 0;
        //this.strobeTimeStmp = 0;
        //this.beaconTimeStmp = 0;
    }

    /**
     * This method assigns a lateral plan/track to the aircraft control.
     *
     * @param lateral   Reference to a lateral plan/track.
     */
    public void assignLat(Lateral lateral){
        this.lateral = lateral;
    }

    /**
     * This method assigns a velocity plan/track to the aircraft control.
     *
     * @param velocity   Reference to a lateral plan/track.
     */
    public void assignVel(Velocity velocity){
        this.velocity = velocity;
    }

    /**
     * This method assigns a vertical plan/track to the aircraft control.
     *
     * @param vertical   Reference to a lateral plan/track.
     */
    public void assignVert(Vertical vertical){
        this.vertical = vertical;
    }

    /**
     * This method maps the aircraft's light settings to the corresponding standard settings for XP11
     * 0 Nav light
     * 1 Logo light
     * 2 Beacon light
     * 3 Strobe light
     * 4 Taxi light
     * 5 Runway Turnoff light
     * 6 Wing light
     * 7 Landing light
     *
     * @param lights    Constant representing the lights bitfield
     * @return          Array containing parameters for each of standard variables
     */
    public int[] getLights(int lights) {
        try {
            int[] stdLights = new int[8];

            for(int i = 0; i < stdLights.length; i++)
                stdLights[i] = 0;

            // Set variables as per bit field lights definition
            switch(lights){
                case PARKLIGHT:
                    stdLights[0] = 1;
                    stdLights[1] = 1;
                    stdLights[2] = 0;
                    stdLights[3] = 0;
                    stdLights[4] = 0;
                    stdLights[5] = 0;
                    stdLights[6] = 0;
                    stdLights[7] = 0;
                    break;
                case NAVLIGHT:
                    stdLights[0] = 1;
                    stdLights[1] = 1;
                    stdLights[2] = 1;
                    stdLights[3] = 0;
                    stdLights[4] = 0;
                    stdLights[5] = 0;
                    stdLights[6] = 0;
                    stdLights[7] = 0;
                    break;
                case TAXILIGHT:
                    stdLights[0] = 1;
                    stdLights[1] = 1;
                    stdLights[2] = 1;
                    stdLights[3] = 0;
                    stdLights[4] = 1;
                    stdLights[5] = 1;
                    stdLights[6] = 0;
                    stdLights[7] = 0;
                    break;
                case LANDINGLIGHT:
                    stdLights[0] = 1;
                    stdLights[1] = 1;
                    stdLights[2] = 1;
                    stdLights[3] = 1;
                    stdLights[4] = 1;
                    stdLights[5] = 0;
                    stdLights[6] = 1;
                    stdLights[7] = 1;
                    break;
                case CRUISELIGHT:
                    stdLights[0] = 1;
                    stdLights[1] = 1;
                    stdLights[2] = 1;
                    stdLights[3] = 1;
                    stdLights[4] = 0;
                    stdLights[5] = 0;
                    stdLights[6] = 0;
                    stdLights[7] = 0;
                    break;
            }

            return stdLights;
        } catch (Exception e) {
            return new int[4];
        }
    }

    /**
     * This method returns the status of the fasten seatbelt sign at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @param   phase   Identifier for phase of flight, e.g. pushback, taxi, flight
     * @return  Status fasten seatbelt sign 0 (Off), 1 (On)
     */
    public int getSeatSign(Waypoint wpt, int phase){
        try {
            int sign = 0;

            // Pushback & taxi to runway
            if(phase == 1 || phase == 2)
                sign = 1;
            // Flight On/Off below/above transition altitude
            else if (phase == 3 && PerfCalc.convertFt(vertical.getAltAtWpt(wpt),"m") <= (ALT_TRANSITION + 1000))
                sign = 1;
            // Taxi to gate
            else if (phase == 4 || phase == 5)
                sign = 1;

            return sign;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the status of the no smoking sign at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @param   phase   Identifier for phase of flight, e.g. pushback, taxi, flight
     * @return  Status no smoking sign 0 (Off), 1 (On)
     */
    public int getSmkSign(Waypoint wpt, int phase){
        // For the moment on at all times
        return 1;
    }

    /**
     * This method returns the status of the cabin attention sign at a given waypoint along the lateral track
     *
     * @param   wpt Waypoint along the lateral track
     * @param   phase   Identifier for phase of flight, e.g. pushback, taxi, flight
     * @return  Status cabin attention sign
     */
    public int getAttSign(Waypoint wpt, int phase){
        try {
            int sign = 0;

            // Taxi onto Runway
            if(phase == 2 && lateral.getDist(wpt,lateral.getEndWpt()) < LIGHTS_LANDING_ON_OUTBOUND_DIST)
                sign = 1;
            // After takeoff
            else if (phase == 3 && PerfCalc.convertKts(velocity.getVasAtWpt(wpt),"ms") < FLAPS_1_SPD && lateral.getDist(lateral.getStartWpt(),wpt) < OUTBOUND_SECTION_DIST)
                sign = 1;
            else if (phase == 3 && PerfCalc.convertKts(velocity.getVasAtWpt(wpt),"ms") >= FLAPS_1_SPD)
                sign = 2;
            // Approach
            else if (phase == 3 && PerfCalc.convertKts(velocity.getVasAtWpt(wpt),"ms") < FLAPS_1_SPD && lateral.getDist(wpt,lateral.getEndWpt()) < INBOUND_SECTION_DIST)
                sign = 3;
            // After landing
            else if (phase == 4 && lateral.getDist(lateral.getStartWpt(),wpt) > (FLAPS_UP_INBOUND_DIST + 50))
                sign = 4;
            else if (phase == 5)
                sign = 4;

            return sign;
        }catch(Exception e){
            return 0;
        }
    }


}
