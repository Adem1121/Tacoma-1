package ftdis.fdpu;

public class kmlWaypoint extends Waypoint {

    private double timeStamp;
    private double altitude;

    /**
     * @param timeStamp The time stamp at which the waypoint is reached
     */
    public void setTime(double timeStamp){
        this.timeStamp = timeStamp;
    }

    /**
     * @return The time stamp at which the waypoint is reached
     */
    public double getTime(){
        return this.timeStamp;
    }

    /**
     * @param altitude The waypoint's altitude
     */
    public void setAlt(double altitude){
        this.altitude = altitude;
    }

    /**
     * @return The waypoint's altitude
     */
    public double getAlt(){
        return this.altitude;
    }
}
