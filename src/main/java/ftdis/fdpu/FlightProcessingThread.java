package ftdis.fdpu;

//import com.sun.javaws.exceptions.FailedDownloadingResourceException;

import java.util.Locale;
import java.util.Vector;

import static ftdis.fdpu.Config.*;

/**
 * Process end to end flight data as per specifications.
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.1
 */


/**
 * Main Flight Processing class
 */
public class FlightProcessingThread {

    /**
     * End to end flight data processing
     */
    public static void processFile(int threadId, Vector<FlightProcessingLineItem> lineItems, Vector<FlightProcessingPlanSet> processedPlans, String flightPlanFile, String eventCollectionFile, double timeStart, double timeEnd, double absDuration) {
        try{
            FlightProcessingLineItem lineItem;

            Waypoint pos, prevPos;
            double ailerons, altAtWpt, vasAtWpt, pitchAtWpt, bankAtWpt, headingAtWpt;
            double cycleLn, timeStmp = 0, timeMrk = 0, timeFile = 0, timeProc, trackLn, trackT, prevTrackDist, throttle, lights, varBank, varBankMax = 0.1, varBankRate = 0.005,
                    varPitch, varPitchMax = 0.5, varPitchRate = 0.010, trackDistTMP;
            int flaps, spoilers, gear, signSmk = 0, signSeat = 0, signAtt = 0;
            int[] arcrftLights = new int[8];;
            boolean inProcess;

            // Load config file and set vars
            cycleLn = FLT_PROC_CYCLE_LN;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 01 Process Pushback
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Initialize
            LateralPlan latPlan = new LateralPlan();
            LateralTrack latTrack = new LateralTrack();

            VelocityPlan velPlan = new VelocityPlan();
            VelocityTrack velTrack = new VelocityTrack();

            VerticalPlan vertPlan = new VerticalPlan();
            VerticalTrack vertTrack = new VerticalTrack();

            WeatherPlan wxPlan = new WeatherPlan();
            WeatherTrack wxTrack = new WeatherTrack();

            AircraftAxis arcrftAxis = new AircraftAxis();
            AircraftControl arcrftCtrl = new AircraftControl();
            AircraftEngine arcrftEng = new AircraftEngine();
            AircraftSystem arcrftSyst = new AircraftSystem();

            // 01 Load flight plan and transform to lateral plan
            //latPlan.load(flightPlanFile, 3);
            //latPlan.transform();
            //latPlan.validate();
            latPlan = processedPlans.get(0).latPlan;


            // 02 Load change velocity events and transform to velocity plan
            //velPlan.assignLat(latPlan);
            //velPlan.load(eventCollectionFile, 3);
            //velPlan.transform();
            //velPlan.validate();
            velPlan = processedPlans.get(0).velPlan;

            // 03 Load change altitude events and transform to vertical plan
            //vertPlan.assignLat(latPlan);
            //vertPlan.assignVel(velPlan);
            //vertPlan.load(eventCollectionFile, 3);
            //vertPlan.transform();
            //vertPlan.validate();
            vertPlan = processedPlans.get(0).vertPlan;

            // 04 Load change weather events and transform to weather plan
            //wxPlan.assignLat(latPlan);
            //wxPlan.assignVel(velPlan);
            //wxPlan.load(eventCollectionFile, 3);
            //wxPlan.transform();
            //wxPlan.validate();
            wxPlan = processedPlans.get(0).wxPlan;

            // 05 Transform lateral plan to lateral track and validate
            //latTrack.assignVel(velPlan);
            //latTrack.transform(latPlan);
            //latTrack.validate();
            latTrack = processedPlans.get(0).latTrack;

            // 06 Transform velocity plan to velocity track and validate
            //velTrack.assignLat(latTrack);
            //velTrack.transform(velPlan);
            //velTrack.validate();
            velTrack = processedPlans.get(0).velTrack;

            // 07 Transform vertical plan to vertical track and validate
            //vertTrack.assignLat(latTrack);
            //vertTrack.assignVel(velTrack);
            //vertTrack.transform(vertPlan);
            //vertTrack.validate();
            vertTrack = processedPlans.get(0).vertTrack;

            // 08 Transform weather plan to vertical track and validate
            //wxTrack.assignLat(latTrack);
            //wxTrack.assignVel(velTrack);
            //wxTrack.transform(wxPlan);
            //wxTrack.validate();
            wxTrack = processedPlans.get(0).wxTrack;

            // 09 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);
            arcrftAxis.assignControl(arcrftCtrl);

            // 10 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);
            arcrftCtrl.assignVert(vertTrack);
            arcrftCtrl.assignWx(wxTrack);

            // 11 Prepare Aircraft Propulsion
            arcrftEng.assignLat(latTrack);
            arcrftEng.assignVel(velTrack);
            arcrftEng.assignVert(vertTrack);
            arcrftEng.assignWx(wxTrack);

            // 12 Prepare Aircraft Systems
            arcrftSyst.assignLat(latTrack);
            arcrftSyst.assignVel(velTrack);
            arcrftSyst.assignVert(vertTrack);
            //arcrftSyst.reset();

            // 09 Reset Processing Vars
            varBank = 0;
            varPitch = 0;
            timeProc = 0;

            // Process and Write Data
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            prevPos = null; //latTrack.getStartWpt();
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 01 Calculations
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    //trackDistTMP = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                        trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
                    else
                        trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if ((trackLn - trackDist) <= 1.0E-4) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    // Set gear position
                    gear = 1;

                    // Set throttle
                    throttle = ENG_IDLE_N1;

                    // Set lights
                    if(trackDist < LIGHTS_NAV_ON_OUTBOUND_DIST)
                        lights = arcrftSyst.PARKLIGHT;
                    else
                        lights = arcrftSyst.NAVLIGHT;

                    arcrftLights = arcrftSyst.getLights((int) lights);

                    // Random/variable bank movements during taxi
                    if(PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms") >= 5){
                        varBank += randDbl(varBankRate*-1,varBankRate);
                        if(varBank > varBankMax)
                            varBank = varBankMax;
                        else if (varBank < varBankMax * -1)
                            varBank = varBankMax * -1;

                        varPitch += randDbl(varPitchRate*-1,varPitchRate);
                        if(varPitch > varPitchMax)
                            varPitch = varPitchMax;
                        else if (varPitch < varPitchMax * -1)
                            varPitch = varPitchMax * -1; 
                    }

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 02 Write Data to Vector
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    lineItem = new FlightProcessingLineItem();

                    // Phase & Time Stamp
                    lineItem.timeAtWpt = timeFile;
                    lineItem.fltPhase = 1;

                    // Position Vars: Lat, Long, Alt, Heading, Speed
                    lineItem.latitude = pos.getLat();
                    lineItem.longitude = pos.getLon();

                    lineItem.altAtWpt = arcrftAxis.getAltAtWpt(pos);
                    lineItem.headingAtWpt = NavCalc.getNewCourse(arcrftAxis.getHeadingAtWpt(pos), 180);
                    lineItem.ktsAtWpt = PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms");

                    // Axis Vars: Pitch, Bank
                    lineItem.pitchAtWpt = 0.0;// + varPitch;
                    lineItem.bankAtWpt = 0.0;// varBank;

                    // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                    lineItem.aileronAtWpt = arcrftCtrl.getAileron();
                    lineItem.flapsAtWpt = arcrftCtrl.getFlapsAtWpt(pos,1);
                    lineItem.spoilersAtWpt = arcrftCtrl.getSpoilersAtWpt(pos,1);
                    lineItem.gearAtWpt = gear;
                    lineItem.throttleAtWpt = throttle;
                    lineItem.noseWheelAtWpt = arcrftCtrl.getNoseWheelAtWpt(pos) * -1;

                    // System Vars: Lights, Cabin
                    lineItem.lightsAtWpt = arcrftLights;
                    lineItem.signSeat = arcrftSyst.getSeatSign(pos,1);
                    lineItem.signSmk = arcrftSyst.getSmkSign(pos,1);
                    lineItem.signAtt = arcrftSyst.getAttSign(pos,1);

                    lineItems.add(lineItem);
                    //System.out.println("Progress... " + String.format(Locale.US, "%10.3f", (lineItems.size()/(1/cycleLn))/absDuration * 100) + " %");
                    //System.out.println("Processing " + threadId + " " + String.format(Locale.US, "%10.2f", timeFile));

                    timeProc += cycleLn;
                } else{
                    inProcess = false;
                    timeFile += cycleLn;
                }
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 02 Process Taxi to Runway
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            wxPlan = new WeatherPlan();
            wxTrack = new WeatherTrack();

            arcrftAxis = new AircraftAxis();
            arcrftCtrl = new AircraftControl();

            arcrftEng = new AircraftEngine();

            // 01 Load flight plan and transform to lateral plan
            //latPlan.load(flightPlanFile, 3);
            //latPlan.transform();
            //latPlan.validate();
            latPlan = processedPlans.get(1).latPlan;


            // 02 Load change velocity events and transform to velocity plan
            //velPlan.assignLat(latPlan);
            //velPlan.load(eventCollectionFile, 3);
            //velPlan.transform();
            //velPlan.validate();
            velPlan = processedPlans.get(1).velPlan;

            // 03 Load change altitude events and transform to vertical plan
            //vertPlan.assignLat(latPlan);
            //vertPlan.assignVel(velPlan);
            //vertPlan.load(eventCollectionFile, 3);
            //vertPlan.transform();
            //vertPlan.validate();
            vertPlan = processedPlans.get(1).vertPlan;

            // 04 Load change weather events and transform to weather plan
            //wxPlan.assignLat(latPlan);
            //wxPlan.assignVel(velPlan);
            //wxPlan.load(eventCollectionFile, 3);
            //wxPlan.transform();
            //wxPlan.validate();
            wxPlan = processedPlans.get(1).wxPlan;

            // 05 Transform lateral plan to lateral track and validate
            //latTrack.assignVel(velPlan);
            //latTrack.transform(latPlan);
            //latTrack.validate();
            latTrack = processedPlans.get(1).latTrack;

            // 06 Transform velocity plan to velocity track and validate
            //velTrack.assignLat(latTrack);
            //velTrack.transform(velPlan);
            //velTrack.validate();
            velTrack = processedPlans.get(1).velTrack;

            // 07 Transform vertical plan to vertical track and validate
            //vertTrack.assignLat(latTrack);
            //vertTrack.assignVel(velTrack);
            //vertTrack.transform(vertPlan);
            //vertTrack.validate();
            vertTrack = processedPlans.get(1).vertTrack;

            // 08 Transform weather plan to vertical track and validate
            //wxTrack.assignLat(latTrack);
            //wxTrack.assignVel(velTrack);
            //wxTrack.transform(wxPlan);
            //wxTrack.validate();
            wxTrack = processedPlans.get(1).wxTrack;

            // 09 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);
            arcrftAxis.assignControl(arcrftCtrl);

            // 10 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);
            arcrftCtrl.assignVert(vertTrack);
            arcrftCtrl.assignWx(wxTrack);

            // 11 Prepare Aircraft Propulsion
            arcrftEng.assignLat(latTrack);
            arcrftEng.assignVel(velTrack);
            arcrftEng.assignVert(vertTrack);
            arcrftEng.assignWx(wxTrack);

            // 12 Prepare Aircraft Systems
            arcrftSyst.assignLat(latTrack);
            arcrftSyst.assignVel(velTrack);
            arcrftSyst.assignVert(vertTrack);
            //arcrftSyst.reset();


            // 13 Reset Processing Vars
            varBank = 0;
            varPitch = 0;
            timeProc = 0;

            // Process and Write Data
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            prevPos = null; //latTrack.getStartWpt();
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                inProcess = true;

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 01 Calculations
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Calculate travelled distance on flight track
                    //trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);
                    if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                        trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
                    else
                        trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if ((trackLn - trackDist) <= 1.0E-4) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    // Set gear position
                    gear = 1;

                    // Set throttle. Avoid using getEngineValAtWpt during first 10 seconds to avoid engine rev up inbetween thread breaks
                    throttle = arcrftEng.getEngineValAtWpt(pos,cycleLn);

                    if (timeProc <= 10)
                        throttle = arcrftEng.getEngineTargetValAtWpt(pos);

                    // Set lights
                    if (trackDist < LIGHTS_TAXI_ON_OUTBOUND_DIST)
                        lights = arcrftSyst.NAVLIGHT;
                    else if (trackDist >= LIGHTS_TAXI_ON_OUTBOUND_DIST && latTrack.getDist(pos,latTrack.getEndWpt()) > LIGHTS_LANDING_ON_OUTBOUND_DIST)
                        lights = arcrftSyst.TAXILIGHT;
                    else
                        lights = arcrftSyst.LANDINGLIGHT;

                    arcrftLights = arcrftSyst.getLights((int) lights);


                    // Random/variable bank and pitch movements during taxi
                    if(PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms") >= 5){
                        varBank += randDbl(varBankRate*-1,varBankRate);
                        if(varBank > varBankMax)
                            varBank = varBankMax;
                        else if (varBank < varBankMax * -1)
                            varBank = varBankMax * -1;

                        varPitch += randDbl(varPitchRate*-1,varPitchRate);
                        if(varPitch > varPitchMax)
                            varPitch = varPitchMax;
                        else if (varPitch < varPitchMax * -1)
                            varPitch = varPitchMax * -1;
                    }

                    // Conduct control test before taxi
                    if (timeStmp >= CONTROL_TEST_TIME) {
                        arcrftCtrl.exeControlTest((timeStmp - CONTROL_TEST_TIME), CONTROL_TEST_DUR, CONTROL_TEST_BREAK);
                    }

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 02 Write Data to Vector
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    lineItem = new FlightProcessingLineItem();
                    lineItem.fltPhase = 2;

                    // Phase & Time Stamp
                    lineItem.timeAtWpt = timeFile;

                    // Position Vars: Lat, Long, Alt, Heading, Speed
                    lineItem.latitude = pos.getLat();
                    lineItem.longitude = pos.getLon();
                    lineItem.altAtWpt = arcrftAxis.getAltAtWpt(pos);
                    lineItem.headingAtWpt = arcrftAxis.getHeadingAtWpt(pos);
                    lineItem.ktsAtWpt = PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms");

                    // Axis Vars: Pitch, Bank
                    lineItem.pitchAtWpt = 0.0; // + varPitch;
                    lineItem.bankAtWpt = 0.0 + varBank;

                    // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                    lineItem.aileronAtWpt = arcrftCtrl.getAileron();
                    lineItem.flapsAtWpt = arcrftCtrl.getFlapsAtWpt(pos,2);
                    lineItem.spoilersAtWpt = arcrftCtrl.getSpoilersAtWpt(pos,2);
                    lineItem.gearAtWpt = gear;
                    lineItem.throttleAtWpt = throttle;
                    lineItem.noseWheelAtWpt = arcrftCtrl.getNoseWheelAtWpt(pos);

                    // System Vars: Lights, Cabin
                    lineItem.lightsAtWpt = arcrftLights;
                    lineItem.signSeat = arcrftSyst.getSeatSign(pos,2);
                    lineItem.signSmk = arcrftSyst.getSmkSign(pos,2);
                    lineItem.signAtt = arcrftSyst.getAttSign(pos,2);

                    lineItems.add(lineItem);

                    //System.out.println("Progress... " + String.format(Locale.US, "%10.3f", (lineItems.size()/(1/cycleLn))/absDuration * 100) + " %");

                    timeProc += cycleLn;
                } else{
                    inProcess = false;
                    timeFile += cycleLn;
                }
        }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 03 Process Flight Plan
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            wxPlan = new WeatherPlan();
            wxTrack = new WeatherTrack();

            arcrftAxis = new AircraftAxis();
            arcrftCtrl = new AircraftControl();

            arcrftEng = new AircraftEngine();

            // 01 Load flight plan and transform to lateral plan
            //latPlan.load(flightPlanFile, 3);
            //latPlan.transform();
            //latPlan.validate();
            latPlan = processedPlans.get(2).latPlan;


            // 02 Load change velocity events and transform to velocity plan
            //velPlan.assignLat(latPlan);
            //velPlan.load(eventCollectionFile, 3);
            //velPlan.transform();
            //velPlan.validate();
            velPlan = processedPlans.get(2).velPlan;

            // 03 Load change altitude events and transform to vertical plan
            //vertPlan.assignLat(latPlan);
            //vertPlan.assignVel(velPlan);
            //vertPlan.load(eventCollectionFile, 3);
            //vertPlan.transform();
            //vertPlan.validate();
            vertPlan = processedPlans.get(2).vertPlan;

            // 04 Load change weather events and transform to weather plan
            //wxPlan.assignLat(latPlan);
            //wxPlan.assignVel(velPlan);
            //wxPlan.load(eventCollectionFile, 3);
            //wxPlan.transform();
            //wxPlan.validate();
            wxPlan = processedPlans.get(2).wxPlan;

            // 05 Transform lateral plan to lateral track and validate
            //latTrack.assignVel(velPlan);
            //latTrack.transform(latPlan);
            //latTrack.validate();
            latTrack = processedPlans.get(2).latTrack;

            // 06 Transform velocity plan to velocity track and validate
            //velTrack.assignLat(latTrack);
            //velTrack.transform(velPlan);
            //velTrack.validate();
            velTrack = processedPlans.get(2).velTrack;

            // 07 Transform vertical plan to vertical track and validate
            //vertTrack.assignLat(latTrack);
            //vertTrack.assignVel(velTrack);
            //vertTrack.transform(vertPlan);
            //vertTrack.validate();
            vertTrack = processedPlans.get(2).vertTrack;

            // 08 Transform weather plan to vertical track and validate
            //wxTrack.assignLat(latTrack);
            //wxTrack.assignVel(velTrack);
            //wxTrack.transform(wxPlan);
            //wxTrack.validate();
            wxTrack = processedPlans.get(2).wxTrack;

            // 09 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);
            arcrftAxis.assignControl(arcrftCtrl);

            // 10 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);
            arcrftCtrl.assignVert(vertTrack);
            arcrftCtrl.assignWx(wxTrack);

            // 11 Prepare Aircraft Propulsion
            arcrftEng.assignLat(latTrack);
            arcrftEng.assignVel(velTrack);
            arcrftEng.assignVert(vertTrack);
            arcrftEng.assignWx(wxTrack);

            // 12 Prepare Aircraft Systems
            arcrftSyst.assignLat(latTrack);
            arcrftSyst.assignVel(velTrack);
            arcrftSyst.assignVert(vertTrack);
            //arcrftSyst.reset();

            // 13 Reset Processing Vars
            timeProc = 0;

            // Process and Write Data
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            prevPos = latTrack.getStartWpt();
            prevTrackDist = 0;
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 01 Calculations
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    inProcess = true;

                    // Calculate travelled distance on flight track
                    trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);
                    /*
                    if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                        trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
                    else
                        trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);
                     */

                    // Check if end of track has been reached
                    //if ((trackLn - trackDist) <= 1.0E-4) {
                    if ((trackLn - trackDist) <= 0.01 || (trackDist == prevTrackDist && (trackLn - trackDist) <= 1.0)) {
                        trackDist = trackLn;
                        // Ajdust time stamp to matc  h time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else{
                        timeFile += cycleLn;
                        prevTrackDist = trackDist;
                    }

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    prevPos = pos;

                    altAtWpt  = arcrftAxis.getAltAtWpt(pos);
                    vasAtWpt = velTrack.getVasAtWpt(pos);
                    flaps = arcrftCtrl.getFlapsAtWpt(pos,3);

                    // Set gear position
                    gear = arcrftCtrl.getGearAtWpt(pos);

                    // Set throttle. Avoid using getEngineValAtWpt during first 10 seconds to avoid engine rev up inbetween thread breaks
                    throttle = arcrftEng.getEngineValAtWpt(pos,cycleLn);

                    if (timeProc <= 10)
                        throttle = arcrftEng.getEngineTargetValAtWpt(pos);

                    // Set lights and map to XPlights
                    if (altAtWpt <= LIGHTS_LANDING_ALT)
                        lights = arcrftSyst.LANDINGLIGHT;
                    else
                        lights = arcrftSyst.CRUISELIGHT;

                    arcrftLights = arcrftSyst.getLights((int) lights);


                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 02 Write Data to Vector
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    lineItem = new FlightProcessingLineItem();

                    // Phase & Time Stamp
                    lineItem.timeAtWpt = timeFile;
                    lineItem.fltPhase = 3;

                    // Position Vars: Lat, Long, Alt, Heading, Speed
                    lineItem.latitude = pos.getLat();
                    lineItem.longitude = pos.getLon();
                    lineItem.altAtWpt = altAtWpt;
                    lineItem.headingAtWpt = arcrftAxis.getHeadingAtWpt(pos);
                    lineItem.ktsAtWpt = PerfCalc.convertKts(vasAtWpt,"ms");

                    // Axis Vars: Pitch, Bank
                    lineItem.pitchAtWpt = arcrftAxis.getPitchAngleAtWpt(pos);// + varPitch;
                    lineItem.bankAtWpt = arcrftAxis.getBankAngleAtWpt(pos);// + varBank;

                    // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                    lineItem.aileronAtWpt = arcrftCtrl.getAileron();
                    lineItem.flapsAtWpt = flaps;
                    lineItem.spoilersAtWpt = arcrftCtrl.getSpoilersAtWpt(pos,3);
                    lineItem.gearAtWpt = gear;
                    lineItem.throttleAtWpt = throttle;
                    lineItem.noseWheelAtWpt = 0;

                    // System Vars: Lights, Cabin
                    lineItem.lightsAtWpt = arcrftLights;
                    lineItem.signSeat = arcrftSyst.getSeatSign(pos,3);
                    lineItem.signSmk = arcrftSyst.getSmkSign(pos,3);
                    lineItem.signAtt = arcrftSyst.getAttSign(pos,3);

                    lineItems.add(lineItem);

                    //System.out.println("Progress... " + String.format(Locale.US, "%10.3f", (lineItems.size()/(1/cycleLn))/absDuration * 100) + " %");

                    timeProc += cycleLn;
                } else{
                    inProcess = false;
                    timeFile += cycleLn;
                }
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 04 Process Taxi to the Gate
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Initialize
            latPlan = new LateralPlan();
            latTrack = new LateralTrack();

            velPlan = new VelocityPlan();
            velTrack = new VelocityTrack();

            vertPlan = new VerticalPlan();
            vertTrack = new VerticalTrack();

            wxPlan = new WeatherPlan();
            wxTrack = new WeatherTrack();

            arcrftAxis = new AircraftAxis();
            arcrftCtrl = new AircraftControl();

            arcrftEng = new AircraftEngine();

            // 01 Load flight plan and transform to lateral plan
            //latPlan.load(flightPlanFile, 3);
            //latPlan.transform();
            //latPlan.validate();
            latPlan = processedPlans.get(3).latPlan;


            // 02 Load change velocity events and transform to velocity plan
            //velPlan.assignLat(latPlan);
            //velPlan.load(eventCollectionFile, 3);
            //velPlan.transform();
            //velPlan.validate();
            velPlan = processedPlans.get(3).velPlan;

            // 03 Load change altitude events and transform to vertical plan
            //vertPlan.assignLat(latPlan);
            //vertPlan.assignVel(velPlan);
            //vertPlan.load(eventCollectionFile, 3);
            //vertPlan.transform();
            //vertPlan.validate();
            vertPlan = processedPlans.get(3).vertPlan;

            // 04 Load change weather events and transform to weather plan
            //wxPlan.assignLat(latPlan);
            //wxPlan.assignVel(velPlan);
            //wxPlan.load(eventCollectionFile, 3);
            //wxPlan.transform();
            //wxPlan.validate();
            wxPlan = processedPlans.get(3).wxPlan;

            // 05 Transform lateral plan to lateral track and validate
            //latTrack.assignVel(velPlan);
            //latTrack.transform(latPlan);
            //latTrack.validate();
            latTrack = processedPlans.get(3).latTrack;

            // 06 Transform velocity plan to velocity track and validate
            //velTrack.assignLat(latTrack);
            //velTrack.transform(velPlan);
            //velTrack.validate();
            velTrack = processedPlans.get(3).velTrack;

            // 07 Transform vertical plan to vertical track and validate
            //vertTrack.assignLat(latTrack);
            //vertTrack.assignVel(velTrack);
            //vertTrack.transform(vertPlan);
            //vertTrack.validate();
            vertTrack = processedPlans.get(3).vertTrack;

            // 08 Transform weather plan to vertical track and validate
            //wxTrack.assignLat(latTrack);
            //wxTrack.assignVel(velTrack);
            //wxTrack.transform(wxPlan);
            //wxTrack.validate();
            wxTrack = processedPlans.get(3).wxTrack;

            // 09 Prepare Aircraft Axis
            arcrftAxis.assignLat(latTrack);
            arcrftAxis.assignVel(velTrack);
            arcrftAxis.assignVert(vertTrack);
            arcrftAxis.assignWx(wxTrack);
            arcrftAxis.assignControl(arcrftCtrl);

            // 10 Prepare Aircraft Control
            arcrftCtrl.assignLat(latTrack);
            arcrftCtrl.assignVel(velTrack);
            arcrftCtrl.assignVert(vertTrack);
            arcrftCtrl.assignWx(wxTrack);

            // 11 Prepare Aircraft Propulsion
            arcrftEng.assignLat(latTrack);
            arcrftEng.assignVel(velTrack);
            arcrftEng.assignVert(vertTrack);
            arcrftEng.assignWx(wxTrack);

            // 12 Prepare Aircraft Systems
            arcrftSyst.assignLat(latTrack);
            arcrftSyst.assignVel(velTrack);
            arcrftSyst.assignVert(vertTrack);
            //arcrftSyst.reset();

            // 13 Reset Processing Vars
            varBank = 0;
            varPitch = 0;
            timeProc = 0;

            // Process and Write Data
            trackLn = latTrack.getDist(latTrack.getStartWpt(),latTrack.getEndWpt());
            trackT = trackLn / velTrack.getVasu(latTrack.getStartWpt(),latTrack.getEndWpt());
            prevPos = null; //latTrack.getStartWpt();
            inProcess = false;

            for(double trackDist = 0; trackDist < trackLn && (timeStmp <= trackT || inProcess); timeStmp += cycleLn){

                inProcess = true;

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 01 Calculations
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////

                    // Calculate travelled distance on flight track
                    //trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);
                    if(prevPos != null && latTrack.getDist(latTrack.getStartWpt(),prevPos) > 0)
                        trackDist = latTrack.getDist(latTrack.getStartWpt(),prevPos) + velTrack.getDist(prevPos,cycleLn);
                    else
                        trackDist = velTrack.getDist(latTrack.getStartWpt(), timeStmp);

                    // Check if end of track has been reached
                    if ((trackLn - trackDist) <= 1.0E-4) {
                        trackDist = trackLn;
                        // Ajdust time stamp to match time required to reach end of track
                        timeStmp = (timeStmp - cycleLn) + latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                        timeFile += latTrack.getDist(prevPos, latTrack.getEndWpt()) / velTrack.getVasu(prevPos, latTrack.getEndWpt());
                    }else
                        timeFile += cycleLn;

                    // Get position and save position for calculations in next cycle
                    pos = latTrack.getItmWpt(latTrack.getStartWpt(), trackDist);
                    //pos = latTrack.getItmWpt(prevPos, velTrack.getDist(prevPos, cycleLn));
                    prevPos = pos;

                    // Set gear position
                    gear = 1;

                    // Set throttle. Avoid using getEngineValAtWpt during first 10 seconds to avoid engine rev up inbetween thread breaks
                    throttle = arcrftEng.getEngineValAtWpt(pos,cycleLn);

                    if (timeProc <= 10)
                        throttle = arcrftEng.getEngineTargetValAtWpt(pos);

                    // Set lights and map to XP lights
                    if(trackDist >= LIGHTS_LANDING_OFF_INBOUND_DIST && latTrack.getDist(pos,latTrack.getEndWpt()) > LIGHTS_TAXI_OFF_INBOUND_DIST)
                        lights = arcrftSyst.TAXILIGHT;
                    else if(trackDist >= LIGHTS_LANDING_OFF_INBOUND_DIST && latTrack.getDist(pos,latTrack.getEndWpt()) <= LIGHTS_TAXI_OFF_INBOUND_DIST){
                        if(velTrack.getSgmtPos(velTrack.getWptSgmt(pos)) < (velTrack.getSgmtCount()-1))
                            lights = arcrftSyst.NAVLIGHT;
                        else
                            lights = arcrftSyst.PARKLIGHT;
                    } else
                        lights = arcrftSyst.LANDINGLIGHT;

                    arcrftLights = arcrftSyst.getLights((int) lights);


                    // Random/variable bank movements during taxi
                    if(PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms") >= 5){
                        varBank += randDbl(varBankRate*-1,varBankRate);
                        if(varBank > varBankMax)
                            varBank = varBankMax;
                        else if (varBank < varBankMax * -1)
                            varBank = varBankMax * -1;

                        varPitch += randDbl(varPitchRate*-1,varPitchRate);
                        if(varPitch > varPitchMax)
                            varPitch = varPitchMax;
                        else if (varPitch < varPitchMax * -1)
                            varPitch = varPitchMax * -1;
                    }

                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    // 02 Write Data to Vector
                    ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                    lineItem = new FlightProcessingLineItem();

                    // Phase & Time Stamp
                    lineItem.timeAtWpt = timeFile;
                    lineItem.fltPhase = 4;

                    // Position Vars: Lat, Long, Alt, Heading, Speed
                    lineItem.latitude = pos.getLat();
                    lineItem.longitude = pos.getLon();
                    lineItem.altAtWpt = arcrftAxis.getAltAtWpt(pos);
                    lineItem.headingAtWpt = arcrftAxis.getHeadingAtWpt(pos);
                    lineItem.ktsAtWpt = PerfCalc.convertKts(velTrack.getVasAtWpt(pos),"ms");

                    // Axis Vars: Pitch, Bank
                    lineItem.pitchAtWpt = 0.0;// + varPitch;
                    lineItem.bankAtWpt = 0.0;//varBank;

                    // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                    lineItem.aileronAtWpt = arcrftCtrl.getAileron();
                    lineItem.flapsAtWpt = arcrftCtrl.getFlapsAtWpt(pos,4);
                    lineItem.spoilersAtWpt = arcrftCtrl.getSpoilersAtWpt(pos,4);
                    lineItem.gearAtWpt = gear;
                    lineItem.throttleAtWpt = throttle;
                    lineItem.noseWheelAtWpt = arcrftCtrl.getNoseWheelAtWpt(pos);

                    // System Vars: Lights, Cabin
                    lineItem.lightsAtWpt = arcrftLights;
                    lineItem.signSeat = arcrftSyst.getSeatSign(pos,4);
                    lineItem.signSmk = arcrftSyst.getSmkSign(pos,4);
                    lineItem.signAtt = arcrftSyst.getAttSign(pos,4);

                    lineItems.add(lineItem);

                    //System.out.println("Progress... " + String.format(Locale.US, "%10.3f", (lineItems.size()/(1/cycleLn))/absDuration * 100) + " %");

                    timeProc += cycleLn;
                } else{
                    inProcess = false;
                    timeFile += cycleLn;
                }
            }

            // Save time stamp for next processing step and reset
            timeMrk += timeStmp;
            timeStmp = cycleLn;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // 05  PARK AT GATE ENGINE SHUTDOWN ETC.
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            for(double elapsedTime = 0; elapsedTime <= 60; elapsedTime += cycleLn){

                if((timeStmp + timeMrk) >= timeStart && (timeStmp + timeMrk) <= timeEnd) {

                ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // 01 Calculations
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                timeStmp += cycleLn;
                timeFile += cycleLn;
                pos = latTrack.getEndWpt();

                // Turn engines off after 5 seconds
                if(elapsedTime <= 7)
                    throttle = arcrftEng.getEngineTargetValAtWpt(pos);
                else
                    throttle = -99.0;

                // Turn fasten seatbelt signs and beacon off after 10 seconds
                if(elapsedTime <= 12) {
                    lights = arcrftSyst.NAVLIGHT;
                    signSeat = arcrftSyst.getSeatSign(pos, 5);
                } else {
                    lights = arcrftSyst.PARKLIGHT;
                    signSeat = 0;
                }

                arcrftLights = arcrftSyst.getLights((int) lights);

                ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                // 02 Write Data to Vector
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////
                lineItem = new FlightProcessingLineItem();

                // Phase & Time Stamp
                lineItem.timeAtWpt = timeFile;
                lineItem.fltPhase = 5;

                // Position Vars: Lat, Long, Alt, Heading, Speed
                lineItem.latitude = pos.getLat();
                lineItem.longitude = pos.getLon();
                lineItem.altAtWpt = arcrftAxis.getAltAtWpt(pos);
                lineItem.headingAtWpt = arcrftAxis.getHeadingAtWpt(pos);
                lineItem.ktsAtWpt = 0.0;

                // Axis Vars: Pitch, Bank
                lineItem.pitchAtWpt = 0.0;
                lineItem.bankAtWpt = 0.0;

                // Control Vars: Ailerons, Flaps, Spoilers, Gear, Throttle
                lineItem.aileronAtWpt = arcrftCtrl.getAileron();
                lineItem.flapsAtWpt = arcrftCtrl.getFlapsAtWpt(pos,5);
                lineItem.spoilersAtWpt = arcrftCtrl.getSpoilersAtWpt(pos,5);
                lineItem.gearAtWpt = 1;
                lineItem.throttleAtWpt = throttle;
                lineItem.noseWheelAtWpt = 0;

                // System Vars: Lights, Cabin
                lineItem.lightsAtWpt = arcrftLights;
                lineItem.signSeat = signSeat;
                lineItem.signSmk = arcrftSyst.getSmkSign(pos,5);
                lineItem.signAtt = arcrftSyst.getAttSign(pos,5);

                lineItems.add(lineItem);

                //System.out.println("Progress... " + String.format(Locale.US, "%10.3f", (lineItems.size()/(1/cycleLn))/absDuration * 100) + " %");
                } else {
                    timeFile += cycleLn;
                }
            }

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }

    /**
     * Returns a pseudo-random number between min and max, inclusive.
     *
     * @param min   Minimum value
     * @param max   Maximum value.  Must be greater than min.
     * @return      Double value between min and max, inclusive.
     */
    public static double randDbl(double min, double max) {
        return min + Math.random() * (max - min);
    }
}


