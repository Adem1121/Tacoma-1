package ftdis.fdpu;

import java.util.List;
import static java.lang.Math.*;
import static ftdis.fdpu.Config.*;

/**
 * The PerfCalc class contains a set of methods that perform performance calculations. Since many of these
 * methods can be shared between various processes and classes they must be maintained in a a central repository.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class PerfCalc {

    /**
     * This method realigns a given linear value to a custom sigmoid curve. The curve is characterized by an
     * initiation segment which spans across a set interval of the overall value range at the start of the
     * curve to increase the curve's slope from 0 to the linear slope of the curve and a termination segment,
     * which spans across a set interval of the overall range at the end of the curve to decrease the curve's
     * slope back to 0.
     *
     * @param rngSize   Size of total value range
     * @param value     Linear value
     * @param slope     Curve 5,10,15,20,25,30,35 or 40 percent
     * @return          Value realigned to the sigmoid curve
     */
    public static double getSigmoidVal(double rngSize, double value, int slope){
        try{
            double x, x1, x2, y, y1, y2, a, m, n, crvAdjustFct;

            if(value == 0)
                return 0;

            // Set variables for exponential function
            switch (slope) {
                case 925:
                    a = 2.931568512;
                    break;
                case 950:
                    a =2.0411743572;
                    break;
                case 1:
                    a = 1.7753639511;
                    break;
                case 2:
                    a = 1.6214832573;
                    break;
                case 5:
                    a = 1.3312550618;
                    break;
                case 10:
                    a = 1.2060383272;
                    break;
                case 15:
                    a = 1.1567306549;
                    break;
                case 20:
                    a = 1.1297507209;
                    break;
                case 22:
                    a = 1.1220784817;
                    break;
                case 25:
                    a = 1.1127066511;
                    break;
                case 30:
                    a = 1.1011005683;
                    break;
                case 35:
                    a = 1.0929162338;
                    break;
                case 40:
                    a = 1.0871679816;
                    break;
                case 45:
                    a = 1.0834337842;
                    break;
                case 50:
                    a = 1.0818420047;
                    break;
                default:
                    return value;
            }

            // Calculate slope m and y-intercept n of linear function
            x1 = slope;
            y1 = pow(a, x1) - 1.0000;
            x2 = 100 - slope;
            y2 = 100 - (1 / pow(a,(x2 - 100)) - 1.0000);

            m = (y2 - y1)/(x2 - x1);
            n = y1 - m * x1;

            // Calculate percent of x value in value range
            x = abs(value) / abs(rngSize) * 100;

            // Calculate curve adjustment factor
            if(x <= slope){
                y = pow(a,x) - 1.0000;
            }else if(x >= 100 - slope){
                y = 100 - (1 / pow(a,(x - 100)) - 1.0000);
            }else{
                y = m * x + n;
            }

            crvAdjustFct = y/x;

            return value * crvAdjustFct;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }

    }

    /**
     * This method realigns a given linear value to a custom acceleration curve. The curve is characterized by an
     * initiation segment which spans across a set interval of the overall value range at the start of the
     * curve to increase the curve's slope exponentially from 0 to the slope of the linear segment of the curve.
     *
     * @param rngSize   Size of total value range
     * @param value     Linear value
     * @param slope     40,45,50,55 or 60  percent
     * @return          Value realigned to the acceleration curve
     */
    public static double getAccVal(double rngSize, double value, int slope){
        try{
            double x, x1, x2, y, y1, y2, a, m, n, crvAdjustFct;

            if(value == 0)
                return 0;

            switch (slope) {
                case 2:
                    a = 1.6197354377;
                    break;
                case 5:
                    a = 1.3279252240;
                    break;
                case 10:
                    a = 1.2015381168;
                    break;
                case 15:
                    a = 1.1514091555;
                    break;
                case 20:
                    a = 1.1236789900;
                    break;
                case 25:
                    a = 1.1057903460;
                    break;
                case 35:
                    a = 1.0838607950;
                    break;
                case 45:
                    a = 1.0718542203;
                    break;
                case 50:
                    a = 1.0669279229;
                    break;
                case 55:
                    a = 1.0629480360;
                    break;
                case 60:
                    a = 1.0596560229;
                    break;
                default:
                    return value;
            }

            // Calculate slope m and y-intercept n of linear function
            x1 = slope - 1;
            y1 = pow(a, x1) - 1.0000;
            x2 = 100;
            y2 = 100;

            m = (y2 - y1)/(x2 - x1);
            n = y1 - m * x1;

            // Calculate percent of x value in value range
            x = abs(value) / abs(rngSize) * 100;

            // Calculate curve adjustment factor
            if(x <= slope){
                y = pow(a,x) - 1.0000;
            }else{
                y = m * x + n;
            }

            crvAdjustFct = y/x;

            return value * crvAdjustFct;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method realigns a given linear value to a custom deceleration curve. The curve is characterized by an
     * initiation segment which spans across a set interval of the overall value range at the start of the
     * curve to increase the curve's slope exponentially from 0 to the slope of the linear segment of the curve.
     *
     * @param rngSize   Size of total value range
     * @param value     Linear value
     * @param slope     40,45,50,55 or 60  percent
     * @return          Value realigned to the acceleration curve
     */
    public static double getDecVal(double rngSize, double value, int slope){
        try{
            double x, x1, x2, y, y1, y2, a, m, n, crvAdjustFct;

            if(value == 0)
                return 0;

            switch (slope) {
                case 2:
                    a= 1.6197353593;
                    break;
                case 5:
                    a = 1.3277650267;
                    break;
                case 10:
                    a = 1.2015668098;
                    break;
                case 15:
                    a = 1.1514057149;
                    break;
                case 20:
                    a = 1.1235881177;
                    break;
                case 25:
                    a = 1.1057500002;
                    break;
                case 35:
                    a = 1.0838598369;
                    break;
                default:
                    return value;
            }

            // Calculate slope m and y-intercept n of linear function
            x1 = 0;
            y1 = 0;
            x2 = 100 - slope;
            y2 = 100 - (1 / pow(a,(x2 - 100)) - 1.0000);

            m = (y2 - y1)/(x2 - x1);
            n = y1 - m * x1;

            // Calculate percent of x value in value range
            x = abs(value) / abs(rngSize) * 100;

            // Calculate curve adjustment factor
            if(x >= 100 - slope)
                y = 100 - (1 / pow(a,(x - 100)) - 1.0000);
            else
                y = m * x + n;

            crvAdjustFct = y/x;

            return value * crvAdjustFct;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return 0;
        }
    }


    /**
     * This method adjusts a linear input value within a given range based on a sinus curve,
     * to reflect the natural distribution of the parameters during the lifecycle of an event.
     *
     * @param rngSize   The total size of the value range
     * @param value     The value
     * @param waveLn    Controls the wavelength of the sinus curve.
     * @return Value realigned to sigmoid curve
     */
    public static double getSinVal(double rngSize, double value, double waveLn) {
        try {
            return 0;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return 0;
        }
    }

    /**
     * This method returns bank angles for corresponding ranges of airspeed. In general, the higher the airspeed
     * the lower the bank angle, to minimize stress for the airframe and pax.
     *
     * @param vAs   Airspeed in m/s
     * @return      Bank angle in degrees
     */
    public static double getBankAngleVas(double vAs){
        try {
            if (vAs <= convertKts(190,"kts"))
                return BANK_ANGLE_0_190;
            else if (vAs > convertKts(190,"kts") && vAs <= convertKts(210,"kts"))
                return BANK_ANGLE_190_210;
            else if (vAs > convertKts(210,"kts") && vAs <= convertKts(250,"kts"))
                return BANK_ANGLE_210_250;
            else
                return BANK_ANGLE_250_999;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns roll rates for corresponding ranges of airspeed. In general, the higher the airspeed
     * the lower the roll rate, to minimize stress for the airframe and pax.
     *
     * @param vAs   Airspeed in m/s
     * @return      Roll rate in degrees per second
     */
    public static double getRollRateVas(double vAs){
        try {
            if (vAs <= convertKts(190,"kts"))
                return ROLL_RATE_0_190;
            else if (vAs > convertKts(190,"kts") && vAs <= convertKts(210,"kts"))
                return ROLL_RATE_190_210;
            else if (vAs > convertKts(210,"kts") && vAs <= convertKts(250,"kts"))
                return ROLL_RATE_210_250;
            else
                return ROLL_RATE_250_999;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method returns the target aileron positions for corresponding ranges of airspeed. In general, the
     * higher the airspeed, the lower the target position
     *
     * @param vAs   Airspeed in m/s
     * @return      Target aileron position in percent
     */
    public static double getAileronTargetVas(double vAs){
        try {
            if (vAs <= convertKts(190,"kts"))
                return AILERON_RATE_0_190;
            else if (vAs > convertKts(190,"kts") && vAs < convertKts(250,"kts"))
                return AILERON_RATE_190_210;
            else if (vAs >= convertKts(250,"kts") && vAs < convertKts(310,"kts"))
                return AILERON_RATE_210_250;
            else
                return AILERON_RATE_250_999;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    public static double getAngleOfAttack(double vAs, double liftCoeff){
        try{
            double aoa;

            // Basic/Simple implementation --> Update by incorporating full and proper lift equation
            if (vAs <= convertKts(SPD_ROTATE,"kts"))
                // Takeoff roll
                aoa = 0;
            else if (vAs > convertKts(SPD_ROTATE,"kts") && vAs <= convertKts(SPD_LIFTOFF,"kts"))
                // Rotate
                aoa = ((convertKts(vAs,"ms") - SPD_ROTATE) * (PITCH_CLIMB / (SPD_LIFTOFF - SPD_ROTATE)));
            else if (vAs > convertKts(SPD_LIFTOFF,"kts") && vAs <= convertKts(SPD_CRUISE,"kts"))
                aoa = PITCH_CLIMB - ((convertKts(vAs,"ms") - SPD_LIFTOFF) * (PITCH_CLIMB - PITCH_CRUISE)/(SPD_CRUISE - SPD_LIFTOFF));
            else
                aoa = PITCH_CRUISE;

            // Adjust angle of attack as per lift coefficient, i.e. changed wing shape and changed surface area
            aoa *= liftCoeff;

            // Validate
            if(aoa < 0)
                aoa = 0;

            return aoa;
        }catch (Exception e){
            return Double.NaN;
        }
    }

    /**
     * This method returns pitch rates for corresponding ranges of airspeed. In general, the higher the airspeed
     * the lower the pitch rate, to minimize stress for the airframe and pax.
     *
     * @param vAs   Airspeed in m/s
     * @return      Pitch rate in degrees per second
     */
    public static double getPitchRateVas(double vAs){
        try {
            if (vAs <= convertKts(150,"kts"))
                return PITCH_RATE_0_150;
            else if (vAs > convertKts(150,"kts") && vAs <= convertKts(190,"kts"))
                return PITCH_RATE_150_190;
            else if (vAs > convertKts(190,"kts") && vAs <= convertKts(250,"kts"))
                return PITCH_RATE_190_250;
            else if (vAs > convertKts(250,"kts") && vAs < convertKts(310,"kts"))
                return PITCH_RATE_250_310;
            else
                return PITCH_RATE_310_999;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }



    /**
     * This method converts values in kts to m/s and vice versa.
     *
     * @param value     The input value
     * @param inputUnit Specifying the unit of the input value, i.e. "kts", or "ms"
     * @return          The converted value
     */
    public static double convertKts(double value, String inputUnit) {
        try {
            double conversionFact = 1.94384449;
            if (inputUnit.equalsIgnoreCase("kts"))
                return value / conversionFact;
            else if (inputUnit.equalsIgnoreCase("ms"))
                return value * conversionFact;
            else
                return Double.NaN;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }

    /**
     * This method converts values in meters to feet and vice versa.
     *
     * @param value     The input value
     * @param inputUnit Specifying the unit of the input value, i.e. "ft", or "m"
     * @return          The converted value
     */
    public static double convertFt(double value, String inputUnit) {
        try {
            double conversionFact = 3.28084;
            if (inputUnit.equalsIgnoreCase("ft"))
                return value / conversionFact;
            else if (inputUnit.equalsIgnoreCase("m"))
                return value * conversionFact;
            else
                return Double.NaN;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return Double.NaN;
        }
    }
}
