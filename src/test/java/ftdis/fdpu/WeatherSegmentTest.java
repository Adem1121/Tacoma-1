package ftdis.fdpu;

import org.junit.*;

import static org.junit.Assert.*;

/**
 * Unit test VelocitySegment  methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class WeatherSegmentTest {
    WeatherSegment testSgm = new WeatherSegment();

    @Before
    public void setUp() throws Exception {
        testSgm.setDist(5000);
        testSgm.setCat(0.7,0.5);
    }
    /**
     * Since the Weather Segment initialization method creates random events,
     * the method cannot be tested against pre-defined values. Therefore, a print out
     * to the console must be used to review the data manually.
     */
    @Test
    public void testGetPitchDeviationAtWpt() throws Exception {
        testSgm.init(90, "pitch");

        for(int i = 0; i <= testSgm.getDist(); i += 10){
            System.out.println(i + " " + testSgm.getPitchDeviationAtDist(i));
        }
    }

    /**
     * Since the Weather Segment initialization method creates random events,
     * the method cannot be tested against pre-defined values. Therefore, a print out
     * to the console must be used to review the data manually.
     */
    @Test
    public void testGetBankDeviationAtWpt() throws Exception {
        testSgm.init(90, "bank");

        for (int i = 0; i <= testSgm.getDist(); i += 10) {
            System.out.println(i + " " + testSgm.getBankDeviationAtDist(i));

        }
    }

    /**
     * Since the Weather Segment initialization method creates random events,
     * the method cannot be tested against pre-defined values. Therefore, a print out
     * to the console must be used to review the data manually.
     */
    @Test
    public void testGetAltDeviationAtWpt() throws Exception {
            testSgm.init(90,"alt");

            for(int i = 0; i <= testSgm.getDist(); i += 10) {
                System.out.println(i + " " + testSgm.getAltDeviationAtDist(i));
            }
    }

    /**
     * Since the Weather Segment initialization method creates random events,
     * the method cannot be tested against pre-defined values. Therefore, a print out
     * to the console must be used to review the data manually.
     */
    @Test
    public void testGetHeadingDeviationAtWpt() throws Exception {

    }

    @Test
    public void testRandDbl() throws Exception{
        for(int i = 0; i < 100; i++){
            System.out.println(i + ": " + testSgm.randDbl(-8,+8));
        }
    }

    @Ignore
    public void testInit() throws Exception {
        // Tested implicitly by get deviation methods
    }
}