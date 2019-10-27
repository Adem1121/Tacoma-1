package ftdis.fdpu;

public class FlightProcessingPlanSet implements Comparable<FlightProcessingPlanSet> {
    public int phase;
    public LateralPlan latPlan = new LateralPlan();
    public LateralTrack latTrack = new LateralTrack();

    public VelocityPlan velPlan = new VelocityPlan();
    public VelocityTrack velTrack = new VelocityTrack();

    public VerticalPlan vertPlan = new VerticalPlan();
    public VerticalTrack vertTrack = new VerticalTrack();

    public WeatherPlan wxPlan = new WeatherPlan();
    public WeatherTrack wxTrack = new WeatherTrack();

    //public AircraftAxis arcrftAxis = new AircraftAxis();

    //public AircraftControl arcrftCtrl = new AircraftControl();

    //public AircraftEngine arcrftEng = new AircraftEngine();

    //public AircraftSystem arcrftSyst = new AircraftSystem();

    // Make vector/collection, sortable based on time stamp, i.e. timeAtWpt
    @Override
    public int compareTo(FlightProcessingPlanSet other) {
        return Double.compare(this.phase, other.phase);
    }

}
