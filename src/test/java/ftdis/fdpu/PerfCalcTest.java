package ftdis.fdpu;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test performance calculation  methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class PerfCalcTest {

    @Test
    public void testGetSigmoidVal() throws Exception {
        // 5 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 5),0.0001);
        assertEquals(3.18125,PerfCalc.getSigmoidVal(100, 5, 5),0.00001);
        assertEquals(23.98958, PerfCalc.getSigmoidVal(100, 25, 5),0.00001);
        assertEquals(55.20208,PerfCalc.getSigmoidVal(100, 55, 5),0.00001);
        assertEquals(86.41458,PerfCalc.getSigmoidVal(100, 85, 5),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 5),0.00001);

        // 10 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 10),0.00001);
        assertEquals(1.55155,PerfCalc.getSigmoidVal(100, 5, 10),0.00001);
        assertEquals(22.19403, PerfCalc.getSigmoidVal(100, 25, 10),0.00001);
        assertEquals(55.56119,PerfCalc.getSigmoidVal(100, 55, 10),0.00001);
        assertEquals(88.92835,PerfCalc.getSigmoidVal(100, 85, 10),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 10),0.00001);

        // 15 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 15),0.00001);
        assertEquals(1.07091,PerfCalc.getSigmoidVal(100, 5, 15),0.00001);
        assertEquals(19.91531, PerfCalc.getSigmoidVal(100, 25, 15),0.00001);
        assertEquals(56.01693,PerfCalc.getSigmoidVal(100, 55, 15),0.00001);
        assertEquals(92.11855,PerfCalc.getSigmoidVal(100, 85, 15),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 15),0.00001);

        // 20 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 20),0.00001);
        assertEquals(0.84040,PerfCalc.getSigmoidVal(100, 5, 20),0.00001);
        assertEquals(17.06029, PerfCalc.getSigmoidVal(100, 25, 20),0.00001);
        assertEquals(56.58794,PerfCalc.getSigmoidVal(100, 55, 20),0.00001);
        assertEquals(94.76639,PerfCalc.getSigmoidVal(100, 85, 20),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 20),0.00001);

        // 25 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 25),0.00001);
        assertEquals(0.70570,PerfCalc.getSigmoidVal(100, 5, 25),0.00001);
        assertEquals(13.43833, PerfCalc.getSigmoidVal(100, 25, 25),0.00001);
        assertEquals(57.31233,PerfCalc.getSigmoidVal(100, 55, 25),0.00001);
        assertEquals(96.03738,PerfCalc.getSigmoidVal(100, 85, 25),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 25),0.00001);

        // 30 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 30),0.00001);
        assertEquals(0.61858,PerfCalc.getSigmoidVal(100, 5, 30),0.00001);
        assertEquals(10.10899, PerfCalc.getSigmoidVal(100, 25, 30),0.00001);
        assertEquals(58.25479,PerfCalc.getSigmoidVal(100, 55, 30),0.00001);
        assertEquals(96.75961,PerfCalc.getSigmoidVal(100, 85, 30),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 30),0.00001);

        // 35 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 35),0.00001);
        assertEquals(0.55931,PerfCalc.getSigmoidVal(100, 5, 35),0.00001);
        assertEquals(8.21874, PerfCalc.getSigmoidVal(100, 25, 35),0.00001);
        assertEquals(59.52829,PerfCalc.getSigmoidVal(100, 55, 35),0.00001);
        assertEquals(97.20856,PerfCalc.getSigmoidVal(100, 85, 35),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 35),0.00001);

        // 40 percent curve
        assertEquals(0,PerfCalc.getSigmoidVal(100, 0, 40),0.00001);
        assertEquals(0.51873,PerfCalc.getSigmoidVal(100, 5, 40),0.00001);
        assertEquals(7.08009, PerfCalc.getSigmoidVal(100, 25, 40),0.00001);
        assertEquals(61.34740,PerfCalc.getSigmoidVal(100, 55, 40),0.00001);
        assertEquals(97.49692,PerfCalc.getSigmoidVal(100, 85, 40),0.00001);
        assertEquals(100,PerfCalc.getSigmoidVal(100, 100, 40),0.00001);
    }

    @Test
    public void testGetAccVal() throws Exception{
        // 50 percent curve
        assertEquals(0,PerfCalc.getAccVal(100, 0, 50),0.00001);
        assertEquals(4.05099,PerfCalc.getAccVal(100, 25, 50),0.00001);
        assertEquals(24.51256, PerfCalc.getAccVal(100, 50, 50),0.00001);
        assertEquals(62.21184,PerfCalc.getAccVal(100, 75, 50),0.00001);
        assertEquals(100,PerfCalc.getAccVal(100, 100, 50),0.00001);
    }

    @Test
    public void testGetDecVal() throws Exception{
        // 5 percent curve
        assertEquals(0,PerfCalc.getDecVal(100, 0, 5),0.00001);
        assertEquals(25.49297,PerfCalc.getDecVal(100, 25, 5),0.00001);
        assertEquals(50.98593, PerfCalc.getDecVal(100, 50, 5),0.00001);
        assertEquals(76.47890,PerfCalc.getDecVal(100, 75, 5),0.00001);
        assertEquals(98.65920,PerfCalc.getDecVal(100, 97, 5),0.00001);
        assertEquals(100,PerfCalc.getDecVal(100, 100, 5),0.00001);
    }

    @Test
    public void testGetSinVal() throws Exception {

    }
}