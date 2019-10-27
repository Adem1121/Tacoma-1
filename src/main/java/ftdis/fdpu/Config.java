package ftdis.fdpu;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static ftdis.fdpu.DOMUtil.*;

/**
 * The FDPU config class loads and sets all of the aircraft/flight specific variables required by
 * the flight data processing and flight planning unit.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class Config {

    // Static variables Flight Data Processing Unit (FDPU)
    public static int      BANK_SIGM_SLOPE, TURN_SIGM_SLOPE_0_25, TURN_SIGM_SLOPE_25_210, TURN_SIGM_SLOPE_210_250, TURN_SIGM_SLOPE_250_999,
                    PITCH_SIGM_SLOPE, ALT_SIGM_SLOPE_DEF, ALT_SIGM_SLOPE_TAKEOFF, ALT_SIGM_SLOPE_FINAL_APP, ALT_SIGM_SLOPE_RETARD,
                    ALT_SIGM_SLOPE_FLARE,  ALT_MID_STEP_TIME, ALT_FINAL_STEP_TIME;

    public static double   PITCH_GROUND, PITCH_FINAL_APP, PITCH_RETARD, PITCH_FLARE, FSX_AILERON_RNG, OUTBOUND_SECTION_DIST,
                    INBOUND_SECTION_DIST, FLAPS_TAKEOFF_SPD, FLAPS_UP_INBOUND_DIST, FLAPS_0_SPD, FLAPS_1_SPD, FLAPS_5_SPD, FLAPS_10_SPD,
                    FLAPS_15_SPD, FLAPS_25_SPD, FLAPS_30_SPD, FSX_SPOILER_RNG, ALT_THRUST_RED, ALT_TRANSITION, ALT_GEAR_UP, ALT_GEAR_DOWN,
                    ENG_VAR_APP, ENG_IDLE_N1, ENG_CLIMB_N1, ENG_DESC_N1, ENG_TAXI_N1, ENG_TAKEOFF_40_N1_DIST, ENG_TAXI_ACC_N1, ENG_TAXI_INERTIA_DELAY, ENG_CRUISE_N1,
                    ENG_TAKEOFF_40_N1, ENG_TAKEOFF_THRUST_N1, ENG_FINAL_APP_N1, ENG_PRE_APP_N1, ENG_REVERSE_N1, ENG_RATE_N1, TAXI_TURN_RAD_12_999,
                    TAXI_TURN_RAD_8_12, TAXI_TURN_RAD_0_8, FLT_PROC_CYCLE_LN, LIGHTS_NAV_ON_OUTBOUND_DIST, LIGHTS_TAXI_ON_OUTBOUND_DIST,
                    LIGHTS_TAXI_OFF_INBOUND_DIST, LIGHTS_LANDING_ON_OUTBOUND_DIST, LIGHTS_LANDING_OFF_INBOUND_DIST, LIGHTS_LANDING_ALT,
                    CONTROL_TEST_TIME, CONTROL_TEST_DUR, CONTROL_TEST_BREAK, BANK_ANGLE_0_190, BANK_ANGLE_190_210, BANK_ANGLE_210_250,
                    BANK_ANGLE_250_999, ROLL_RATE_0_190, ROLL_RATE_190_210, ROLL_RATE_210_250, ROLL_RATE_250_999, AILERON_RATE_0_190,
                    AILERON_RATE_190_210, AILERON_RATE_210_250, AILERON_RATE_250_999, PITCH_CLIMB, PITCH_CRUISE, PITCH_RATE_TAKEOFF, PITCH_RATE_0_150,
                    PITCH_RATE_150_190, PITCH_RATE_190_250, PITCH_RATE_250_310, PITCH_RATE_310_999, CAT_MIN_DUR, CAT_MAX_DUR, CAT_MIN_PITCH,
                    CAT_MAX_PITCH, CAT_MIN_BANK, CAT_MAX_BANK, CAT_MIN_ALT, CAT_MAX_ALT;

    public static String AIRCRAFT_TYPE, SMTP_SERVER, SMTP_USER, SMTP_PW, SMTP_PORT;

    // Static variables FLight Planning Unit (FPLU)
    public static double   ALPHA_TAKEOFF_DEG, ALPHA_CLIMB_TRANS_DEG, ALPHA_CLIMB_DEF_DEG, ALPHA_DESC_DEF_DEG, ALPHA_FINAL_APP_DEG, DECEL_APP_FIX_INIT_APP_SPEED_DIST,
                            DECEL_APP_FIX_FINAL_APP_SPEED_DIST, FINAL_APP_FIX_DIST,FINAL_APP_FIX_DESC_DIST, ALPHA_RETARD_DEG, ALPHA_FLARE_DEG, ALT_DEP, ALT_INIT_CRUISE,
                            ALT_MID_CRUISE, ALT_FINAL_CRUISE, ALT_RETARD, ALT_FLARE, ALT_DEST, SPD_PUSHBACK,SPD_TAXI, SPD_ROTATE, SPD_LIFTOFF, SPD_CLIMBOUT, SPD_CLIMB_CRUISE,
                            SPD_CRUISE, SPD_TRANS, SPD_FINAL_APP_FIX, SPD_APP, CAT_MAGN_TAKEOFF, CAT_FREQ_TAKEOFF, CAT_MAGN_CRUISE, CAT_FREQ_CRUISE, CAT_MAGN_INIT_APP,
                            CAT_FREQ_INIT_APP, CAT_MAGN_FINAL_APP, CAT_FREQ_FINAL_APP, AIRCRAFT_PARK_TIME, ACC_PUSHBACK, ACC_TAXI, TAXI_TURN_BREAK_FRACT;

    public static String   DEP_ICAO, DEST_ICAO;

    static {
        Path localDir;
        String ioDir;

        // Set input directories and load config .xml file
        final String os = System.getProperty("os.name");

        if (os.contains("Windows"))
            ioDir = "\\IO\\";
        else
            ioDir = "/IO/";

        try{
            localDir = Paths.get("").toAbsolutePath().getParent().getParent();
            loadConfig(localDir + ioDir + "config.xml");
        }
        catch(Exception e){
            localDir = Paths.get("").toAbsolutePath();
            loadConfig(localDir + ioDir + "config.xml");
        }
    }

    /**
     * This method loads and assigns all of the config variables stored in the config .xml file.
     *
     * @param filePathName  Path and file name of local config .xml file
     */
    public static void loadConfig(String filePathName){
        try{
            int configId = 1;

            // Parse config .xml file and load variables
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document configXML = dBuilder.parse(new File(filePathName));

            // normalize
            configXML.getDocumentElement().normalize();

            // Get fdpu config vars
            Node fdpu = findNode(configXML.getDocumentElement().getChildNodes(),"fdpu");

            LIGHTS_NAV_ON_OUTBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_NAV_ON_OUTBOUND_DIST"),"val"));
            LIGHTS_TAXI_ON_OUTBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_TAXI_ON_OUTBOUND_DIST"),"val"));
            LIGHTS_TAXI_OFF_INBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_TAXI_OFF_INBOUND_DIST"),"val"));
            LIGHTS_LANDING_ON_OUTBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_LANDING_ON_OUTBOUND_DIST"),"val"));
            LIGHTS_LANDING_OFF_INBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_LANDING_OFF_INBOUND_DIST"),"val"));
            LIGHTS_LANDING_ALT = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"LIGHTS_LANDING_ALT"),"val"));

            CONTROL_TEST_TIME = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CONTROL_TEST_TIME"),"val"));
            CONTROL_TEST_DUR = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CONTROL_TEST_DUR"),"val"));
            CONTROL_TEST_BREAK = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CONTROL_TEST_BREAK"),"val"));

            BANK_ANGLE_0_190 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"BANK_ANGLE_0_190"),"val"));
            BANK_ANGLE_190_210 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"BANK_ANGLE_190_210"),"val"));
            BANK_ANGLE_210_250 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"BANK_ANGLE_210_250"),"val"));
            BANK_ANGLE_250_999 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"BANK_ANGLE_250_999"),"val"));

            ROLL_RATE_0_190 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ROLL_RATE_0_190"),"val"));
            ROLL_RATE_190_210 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ROLL_RATE_190_210"),"val"));
            ROLL_RATE_210_250 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ROLL_RATE_210_250"),"val"));
            ROLL_RATE_250_999 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ROLL_RATE_250_999"),"val"));

            AILERON_RATE_0_190 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"AILERON_RATE_0_190"),"val"));
            AILERON_RATE_190_210 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"AILERON_RATE_190_210"),"val"));
            AILERON_RATE_210_250 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"AILERON_RATE_210_250"),"val"));
            AILERON_RATE_250_999 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"AILERON_RATE_250_999"),"val"));

            PITCH_CLIMB = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_CLIMB"),"val"));
            PITCH_CRUISE = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_CRUISE"),"val"));
            PITCH_RATE_TAKEOFF = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_TAKEOFF"),"val"));
            PITCH_RATE_0_150 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_0_150"),"val"));
            PITCH_RATE_150_190 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_150_190"),"val"));
            PITCH_RATE_190_250 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_190_250"),"val"));
            PITCH_RATE_250_310 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_250_310"),"val"));
            PITCH_RATE_310_999 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RATE_310_999"),"val"));

            CAT_MIN_DUR = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MIN_DUR"),"val"));
            CAT_MAX_DUR = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MAX_DUR"),"val"));
            CAT_MIN_PITCH = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MIN_PITCH"),"val"));
            CAT_MAX_PITCH = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MAX_PITCH"),"val"));
            CAT_MIN_BANK = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MIN_BANK"),"val"));
            CAT_MAX_BANK = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MAX_BANK"),"val"));
            CAT_MIN_ALT = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MIN_ALT"),"val"));
            CAT_MAX_ALT = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"CAT_MAX_ALT"),"val"));

            BANK_SIGM_SLOPE = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"BANK_SIGM_SLOPE"),"val"));
            TURN_SIGM_SLOPE_0_25 = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"TURN_SIGM_SLOPE_0_25"),"val"));
            TURN_SIGM_SLOPE_25_210 = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"TURN_SIGM_SLOPE_25_210"),"val"));
            TURN_SIGM_SLOPE_210_250 = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"TURN_SIGM_SLOPE_210_250"),"val"));
            TURN_SIGM_SLOPE_250_999 = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"TURN_SIGM_SLOPE_250_999"),"val"));

            ALT_SIGM_SLOPE_DEF = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_SIGM_SLOPE_DEF"),"val"));
            ALT_SIGM_SLOPE_TAKEOFF = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_SIGM_SLOPE_TAKEOFF"),"val"));
            ALT_SIGM_SLOPE_FINAL_APP = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_SIGM_SLOPE_FINAL_APP"),"val"));
            ALT_SIGM_SLOPE_RETARD = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_SIGM_SLOPE_RETARD"),"val"));
            ALT_SIGM_SLOPE_FLARE = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_SIGM_SLOPE_FLARE"),"val"));

            PITCH_SIGM_SLOPE = Integer.parseInt(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_SIGM_SLOPE"),"val"));
            PITCH_GROUND = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_GROUND"),"val"));
            PITCH_FINAL_APP = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_FINAL_APP"),"val"));
            PITCH_RETARD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_RETARD"),"val"));
            PITCH_FLARE = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"PITCH_FLARE"),"val"));

            ALPHA_TAKEOFF_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_TAKEOFF_DEG"),"val"));
            ALPHA_CLIMB_TRANS_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_CLIMB_TRANS_DEG"),"val"));
            ALPHA_CLIMB_DEF_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_CLIMB_DEF_DEG"),"val"));
            ALPHA_DESC_DEF_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_DESC_DEF_DEG"),"val"));
            ALPHA_FINAL_APP_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_FINAL_APP_DEG"),"val"));
            ALPHA_RETARD_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_RETARD_DEG"),"val"));
            ALPHA_FLARE_DEG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALPHA_FLARE_DEG"),"val"));

            FSX_AILERON_RNG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FSX_AILERON_RNG"),"val"));
            FSX_SPOILER_RNG = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FSX_SPOILER_RNG"),"val"));

            OUTBOUND_SECTION_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"OUTBOUND_SECTION_DIST"),"val"));
            INBOUND_SECTION_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"INBOUND_SECTION_DIST"),"val"));
            FLAPS_TAKEOFF_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_TAKEOFF_SPD"),"val"));
            FLAPS_UP_INBOUND_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_UP_INBOUND_DIST"),"val"));
            FLAPS_0_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_0_SPD"),"val"));
            FLAPS_1_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_1_SPD"),"val"));
            FLAPS_5_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_5_SPD"),"val"));
            FLAPS_10_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_10_SPD"),"val"));
            FLAPS_15_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_15_SPD"),"val"));
            FLAPS_25_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_25_SPD"),"val"));
            FLAPS_30_SPD = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLAPS_30_SPD"),"val"));

            ALT_THRUST_RED = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_THRUST_RED"),"val"));
            ALT_TRANSITION = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_TRANSITION"),"val"));
            ALT_GEAR_UP = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_GEAR_UP"),"val"));
            ALT_GEAR_DOWN = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ALT_GEAR_DOWN"),"val"));

            ENG_VAR_APP = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_VAR_APP"),"val"));
            ENG_IDLE_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_IDLE_N1"),"val"));
            ENG_CLIMB_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_CLIMB_N1"),"val"));
            ENG_DESC_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_DESC_N1"),"val"));
            ENG_TAXI_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAXI_N1"),"val"));
            ENG_TAXI_ACC_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAXI_ACC_N1"),"val"));
            ENG_TAXI_INERTIA_DELAY = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAXI_INERTIA_DELAY"),"val"));
            ENG_CRUISE_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_CRUISE_N1"),"val"));
            ENG_TAKEOFF_40_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAKEOFF_40_N1"),"val"));
            ENG_TAKEOFF_40_N1_DIST = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAKEOFF_40_N1_DIST"),"val"));
            ENG_TAKEOFF_THRUST_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_TAKEOFF_THRUST_N1"),"val"));
            ENG_FINAL_APP_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_FINAL_APP_N1"),"val"));
            ENG_PRE_APP_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_PRE_APP_N1"),"val"));
            ENG_REVERSE_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_REVERSE_N1"),"val"));
            ENG_RATE_N1 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"ENG_RATE_N1"),"val"));

            //TAXI_TURN_RAD_12_999 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"TAXI_TURN_RAD_12_999"),"val"));
            //TAXI_TURN_RAD_8_12 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"TAXI_TURN_RAD_8_12"),"val"));
            //TAXI_TURN_RAD_0_8 = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"TAXI_TURN_RAD_0_8"),"val"));

            FLT_PROC_CYCLE_LN = Double.parseDouble(getAttributeValue(findNode(fdpu.getChildNodes(),"FLT_PROC_CYCLE_LN"),"val"));

            // Get fplu config vars
            Node fplu = findNode(configXML.getDocumentElement().getChildNodes(),"fplu");

            AIRCRAFT_TYPE = getAttributeValue(findNode(fplu.getChildNodes(),"AIRCRAFT_TYPE"),"val");
            DEP_ICAO = getAttributeValue(findNode(fplu.getChildNodes(),"DEP_ICAO"),"val");
            DEST_ICAO = getAttributeValue(findNode(fplu.getChildNodes(),"DEST_ICAO"),"val");

            DECEL_APP_FIX_INIT_APP_SPEED_DIST = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"DECEL_APP_FIX_INIT_APP_SPEED_DIST"),"val"));
            DECEL_APP_FIX_FINAL_APP_SPEED_DIST = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"DECEL_APP_FIX_FINAL_APP_SPEED_DIST"),"val"));
            FINAL_APP_FIX_DIST = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"FINAL_APP_FIX_DIST"),"val"));
            FINAL_APP_FIX_DESC_DIST = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"FINAL_APP_FIX_DESC_DIST"),"val"));

            ALT_DEP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_DEP"),"val"));
            ALT_INIT_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_INIT_CRUISE"),"val"));
            ALT_MID_STEP_TIME = Integer.parseInt(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_MID_CRUISE"),"minutes"));
            ALT_MID_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_MID_CRUISE"),"val"));
            ALT_FINAL_STEP_TIME = Integer.parseInt(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_FINAL_CRUISE"),"minutes"));
            ALT_FINAL_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_FINAL_CRUISE"),"val"));
            ALT_RETARD = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_RETARD"),"val"));
            ALT_FLARE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_FLARE"),"val"));
            ALT_DEST = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ALT_DEST"),"val"));

            SPD_PUSHBACK = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_PUSHBACK"),"val"));
            SPD_TAXI = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_TAXI"),"val"));
            SPD_ROTATE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_ROTATE"),"val"));
            SPD_LIFTOFF = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_LIFTOFF"),"val"));
            SPD_CLIMBOUT = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_CLIMBOUT"),"val"));
            SPD_CLIMB_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_CLIMB_CRUISE"),"val"));
            SPD_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_CRUISE"),"val"));
            SPD_TRANS = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_TRANS"),"val"));
            SPD_FINAL_APP_FIX = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_FINAL_APP_FIX"),"val"));
            SPD_APP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"SPD_APP"),"val"));

            CAT_MAGN_TAKEOFF = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_MAGN_TAKEOFF"),"val"));
            CAT_FREQ_TAKEOFF = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_FREQ_TAKEOFF"),"val"));
            CAT_MAGN_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_MAGN_CRUISE"),"val"));
            CAT_FREQ_CRUISE = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_FREQ_CRUISE"),"val"));
            CAT_MAGN_INIT_APP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_MAGN_INIT_APP"),"val"));
            CAT_FREQ_INIT_APP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_FREQ_INIT_APP"),"val"));
            CAT_MAGN_FINAL_APP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_MAGN_FINAL_APP"),"val"));
            CAT_FREQ_FINAL_APP = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"CAT_FREQ_FINAL_APP"),"val"));

            ACC_PUSHBACK = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ACC_PUSHBACK"),"val"));
            ACC_TAXI = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"ACC_TAXI"),"val"));
            TAXI_TURN_BREAK_FRACT = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"TAXI_TURN_BREAK_FRACT"),"val"));

            AIRCRAFT_PARK_TIME = Double.parseDouble(getAttributeValue(findNode(fplu.getChildNodes(),"AIRCRAFT_PARK_TIME"),"val"));

            // Get util config vars
            Node util = findNode(configXML.getDocumentElement().getChildNodes(),"util");

            SMTP_SERVER = getAttributeValue(findNode(util.getChildNodes(),"SMTP_SERVER"),"val");
            SMTP_USER = getAttributeValue(findNode(util.getChildNodes(),"SMTP_USER"),"val");
            SMTP_PW = getAttributeValue(findNode(util.getChildNodes(),"SMTP_PW"),"val");
            SMTP_PORT = SMTP_PORT = getAttributeValue(findNode(util.getChildNodes(),"SMTP_PORT"),"val");

        }catch(Exception e){
            //System.out.println(e.getMessage());
            throw new IllegalArgumentException(e.getMessage());
        }
    }
}
