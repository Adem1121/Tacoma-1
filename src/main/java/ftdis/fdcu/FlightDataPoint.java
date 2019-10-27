package ftdis.fdcu;

/**
 * The FlightDataPoint class represents a data point of a real world flight captured from an external source
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class FlightDataPoint {

    public int id;
    public String sCode;
    public double latitude;
    public double longitude;
    public int heading;
    public int altitude;
    public int airSpd;
    public String radarCode;
    public String acType;
    public String regCode;
    public int timeStamp;
    public String depCode;
    public String destCode;
    public int outsideTemp;
    public int vertSpd;
    public String flightNum;
    public int squawkCode;

    /**
     * Constructor(s)
     */
    FlightDataPoint(){
        this.id = 0;
        this.sCode ="NA";
        this.longitude = 0.0;
        this.latitude = 0.0;
        this.heading = 0;
        this.altitude = 0;
        this.airSpd = 0;
        this.radarCode = "NA";
        this.acType = "NA";
        this.regCode = "NA";
        this.timeStamp = 0;
        this.depCode = "NA";
        this.destCode = "NA";
        this.outsideTemp = 0;
        this.vertSpd = 0;
        this.flightNum ="NA";
        this.squawkCode = 0;
    }
}