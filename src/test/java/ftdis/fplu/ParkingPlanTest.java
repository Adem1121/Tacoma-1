package ftdis.fplu;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test ParkingPlan methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ParkingPlanTest {

    @Test
    public void testTransform() throws Exception {
        ParkingPlan parkingPlan = new ParkingPlan();

        parkingPlan.transform("/Users/fross/Development/Flight Tracking Data Integration System/05 Java/Tacoma/Iteration 11/IO/KTPA Traffic Static.txt");
    }
}