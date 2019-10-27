package ftdis.fdpu;

public class FlightProcessingLineItem implements Comparable<FlightProcessingLineItem> {
    public double latitude, longitude, altAtWpt, headingAtWpt, ktsAtWpt, pitchAtWpt, bankAtWpt, throttleAtWpt, aileronAtWpt, noseWheelAtWpt, timeAtWpt;
    public int fltPhase, flapsAtWpt, spoilersAtWpt, gearAtWpt, signSmk, signSeat, signAtt;
    public int[] lightsAtWpt = new int[8];

    // Make vector/collection, sortable based on time stamp, i.e. timeAtWpt
    @Override
    public int compareTo(FlightProcessingLineItem other) {
        return Double.compare(this.timeAtWpt, other.timeAtWpt);
    }

}