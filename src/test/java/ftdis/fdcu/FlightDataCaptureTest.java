package ftdis.fdcu;

import org.junit.Before;
import org.junit.Test;
import java.text.ParseException;
import static org.junit.Assert.*;

/**
 * Unit test FlightDataCapture methods
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */
public class FlightDataCaptureTest {
    @Before
    public void setUp(){

    }

    @Test
    public void testIsDateTimeBetweenTwoDateTime(){
        boolean result;
        String argStartDate, argStartTime, argEndDate, argEndTime, argCurrentDate, argCurrentTime;

        argStartDate = "10/10/2018";
        argStartTime = "09:30:00";
        argEndDate = "11/10/2018";
        argEndTime = "22:00:00";

        try{

            argCurrentDate = "10/10/2017";
            argCurrentTime = "10:00:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "09/10/2018";
            argCurrentTime = "18:00:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "10/10/2018";
            argCurrentTime = "09:29:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "10/10/2018";
            argCurrentTime = "09:31:00";
            assertTrue(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "10/10/2018";
            argCurrentTime = "23:30:00";
            assertTrue(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "11/10/2018";
            argCurrentTime = "09:00:00";
            assertTrue(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "11/10/2018";
            argCurrentTime = "21:59:00";
            assertTrue(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "11/10/2018";
            argCurrentTime = "22:01:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "21/11/2018";
            argCurrentTime = "18:00:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

            argCurrentDate = "11/10/2019";
            argCurrentTime = "09:00:00";
            assertFalse(FlightDataCapture.isDateTimeBetweenTwoDateTime(argStartDate,argStartTime,argEndDate,argEndTime,argCurrentDate,argCurrentTime));

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }

}
