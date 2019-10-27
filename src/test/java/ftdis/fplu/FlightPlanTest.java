package ftdis.fplu;

import ftdis.fdpu.EventChgAirspeed;
import ftdis.fdpu.Lateral;
import ftdis.fdpu.PerfCalc;
import org.junit.Test;

/**
 * Unit test TaxiPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class FlightPlanTest {
    FlightPlan testPlan = new FlightPlan();

    @Test
    public void testLoad() throws Exception {
        testPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 10/IO/F10 Master Plan.xml",3);
    }

    @Test
    public void testTransform() throws Exception {
        testPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 10/IO/F10 Master Plan.xml",3);
        testPlan.transform();
    }

    @Test
    public void testValidate() throws Exception {
        Lateral latPlan;
        EventChgAirspeed thisEvent, nextEvent;
        double nextDist;

        testPlan.load("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 10/IO/F10 Master Plan.xml", 2);
        testPlan.transform();
        //testPlan.validate();

        latPlan = testPlan.getLateralPlan();

        for(int e = 0; e < testPlan.getVelEventCount(); e++){
            thisEvent = testPlan.getVelEvent(e);

            if(e < (testPlan.getVelEventCount() - 1)){
                nextEvent = testPlan.getVelEvent(e + 1);
                nextDist = latPlan.getDist(thisEvent.getEndPt(),nextEvent.getStartPt());
            }else
                nextDist = 0;

            System.out.println("Event: " + (e + 1));
            System.out.println("Vasi : " + PerfCalc.convertKts(thisEvent.getVAsi(), "ms"));
            System.out.println("Vasf : " + PerfCalc.convertKts(thisEvent.getVAsf(), "ms"));
            System.out.println("Acc  : " + thisEvent.getAcc());
            System.out.println("Dist : " + latPlan.getDist(thisEvent.getStartPt(), thisEvent.getEndPt()));
            System.out.println("Next : " + nextDist);
            System.out.println();
        }
    }

}