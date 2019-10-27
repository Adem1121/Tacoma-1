package ftdis.fdpu;

/**
 * ParkingSegment class is a key component of the ParkingPlan class. It represent a parked aircraft and is composed
 * of an ID, latitude and longitude coordinates, as well as heading and altitude of the parking position.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingSegment {
    public int id;
    private Waypoint position;
    private double staticCG, heading, altitude, parkingTime;
    private String aircraftType;

    /**
    * Constructor(s)
    */
    public ParkingSegment(){
        this.id = 0;
        this.staticCG = Double.NaN;
        this.position = new Waypoint();
        this.heading = Double.NaN;
        this.altitude = Double.NaN;
        this.parkingTime = Double.NaN;
    }

    public ParkingSegment(int id){
        this();
        this.id = id;
    }

    public double getStaticCG() {
        return staticCG;
    }

    public void setStaticCG(double staticCG) {
        this.staticCG = staticCG;
    }

    public Waypoint getPosition() {
        return position;
    }

    public void setPosition(Waypoint position) {
        this.position = position;
    }

    public double getHeading() {
        return heading;
    }

    public void setHeading(double heading) {
        this.heading = heading;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }

    public double getParkingTime() {
        return parkingTime;
    }

    public void setParkingTime(double parkingTime) {
        this.parkingTime = parkingTime;
    }

    public String getType() {
        return aircraftType;
    }

    public void setType(String aircraftType) {
        this.aircraftType = aircraftType;
    }
}
