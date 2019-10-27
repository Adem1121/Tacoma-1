package ftdis.fdcu;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import static ftdis.fdpu.DOMUtil.*;

/**
 * Capture real world flight data from external data source(s) and store it in a persistent data layer
 *
 * @author  windowSeatFSX@gmail.com
 * @version 0.2
 */
public class FlightDataCapture{

    /**
     * Process flight data capture
     */
    public static void main(String[] args) {
        try {
            // Define and set vars
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
            List<FlightDataPoint> flightDataPoints = new ArrayList<FlightDataPoint>();
            int dataPointId = 0;
            boolean argsValid = false;
            final String os = System.getProperty("os.name");
            String flightNumber = "", startDate="", startTime = "", endDate="", endTime = "", kickOffTime = dateTimeFormat.format(new Date());

            // Validate input arguments
            if(args.length == 5 && isValidDateFormat(args[1],"dd/MM/yyyy") && isValidDateFormat(args[2],"HH:mm:ss") && isValidDateFormat(args[3],"dd/MM/yyyy") && isValidDateFormat(args[4],"HH:mm:ss")){
                argsValid = true;
                flightNumber = args[0];
                startDate = args[1];
                startTime = args[2];
                endDate = args[3];
                endTime = args[4];
            }

            // Start processing if arguments valid
            if(!argsValid){
                System.out.println(kickOffTime + " Arguments invalid! Format LX123 DD/MM/YYYY HH:MM:SS DD/MM/YYYY HH:MM:SS");
            }
            else {
                // Wait to start
                while (!isDateTimeBetweenTwoDateTime(startDate, startTime, endDate, endTime, dateFormat.format(new Date()) , timeFormat.format(new Date()))) {

                    if (os.contains("Windows"))
                        clearScreenWin();
                    else
                        clearScreenOS();

                    System.out.println(kickOffTime + " ==========================================================");
                    System.out.println(kickOffTime + " Data capture started! ");
                    System.out.println(kickOffTime + " ==========================================================");
                    System.out.println(kickOffTime + " Flight No.      " + flightNumber);
                    System.out.println(kickOffTime + " Start Date/Time " + startDate + " " + startTime);
                    System.out.println(kickOffTime + " End Date/Time   " + endDate + " " + endTime);
                    System.out.println(kickOffTime + " ==========================================================");
                    System.out.println(dateTimeFormat.format(new Date()) + " waiting...");
                    Thread.sleep(1000);
                }

                // Call data source in 8 second intervals
                while (isDateTimeBetweenTwoDateTime(startDate, startTime, endDate, endTime, dateFormat.format(new Date()) , timeFormat.format(new Date()))) {
                    // Get flight data point
                    FlightDataPoint thisDataPoint = captureDataPoint(flightNumber);

                    // Check if data has been received successfully
                    if (thisDataPoint.timeStamp > 0) {
                        thisDataPoint.id = dataPointId;
                        flightDataPoints.add(thisDataPoint);

                        System.out.println(dateTimeFormat.format(new Date()) + " Flight Data Point " + dataPointId++ + " captured.");
                    } else
                        System.out.println(dateTimeFormat.format(new Date()) + " No Flight Data Point available.");

                    Thread.sleep(5000);
                }

                // Write result set to KML
                exportFlightDataKML(flightDataPoints);
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * This method captures a flight data point from an external source
     *
     * @param flightNumber  The flight number, e.g. LX87
     * @return Flight data point
     */
    public static FlightDataPoint captureDataPoint(String flightNumber){
        try{
            Date dateTime = new Date();
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            String url = "http://bma.data.fr24.com/zones/fcgi/feed.js?bounds=76.18614167532165,-12.803831062711929,5711.484375,490.078125&faa=1&mlat=1&flarm=1&adsb=1&gnd=1&air=1&vehicles=0&estimated=1&maxage=900&gliders=0&stats=1&flight=";
            String charset = "UTF-8";
            FlightDataPoint thisDataPoint = new FlightDataPoint();

            // Open Http connection and send request to external data source
            HttpURLConnection httpCon = (HttpURLConnection) new URL(url + flightNumber).openConnection();
            httpCon.setRequestProperty("Accept-Charset", charset);
            httpCon.setRequestMethod("GET");

            //TEST OUTPUT
            System.out.println("\n" + dateTimeFormat.format(new Date()) + " Sending 'GET' request to URL : " + url + flightNumber);

            if (httpCon.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // Process Http response
                String inputLine;
                StringBuffer httpContent = new StringBuffer();

                BufferedReader httpResponse = new BufferedReader(new InputStreamReader(httpCon.getInputStream()));

                while ((inputLine = httpResponse.readLine()) != null) {
                    httpContent.append(inputLine);
                }

                httpResponse.close();

                // Process Json
                Object respObject = JSONValue.parse(httpContent.toString());

                if (respObject instanceof JSONObject) {
                    JSONObject srcDataPoint =(JSONObject)respObject;

                    // Loop through Json response object and find flight data point arrays
                    for(Iterator iterator = srcDataPoint.keySet().iterator(); iterator.hasNext();) {
                        String key = (String) iterator.next();

                        if(srcDataPoint.get(key) instanceof JSONArray){
                            JSONArray flightDataArray = (JSONArray) srcDataPoint.get(key);

                            //Check if flight number of data set matches 1:1, as service returns multiple results that match search string
                            if(flightDataArray.get(13).toString().equalsIgnoreCase(flightNumber)){
                                // Assign values from source to flight data point
                                thisDataPoint.id = 0;
                                thisDataPoint.sCode =  flightDataArray.get(0).toString();
                                thisDataPoint.longitude = Double.parseDouble(flightDataArray.get(1).toString());
                                thisDataPoint.latitude =  Double.parseDouble(flightDataArray.get(2).toString());
                                thisDataPoint.heading = Integer.parseInt(flightDataArray.get(3).toString());
                                thisDataPoint.altitude = Integer.parseInt(flightDataArray.get(4).toString());
                                thisDataPoint.airSpd = Integer.parseInt(flightDataArray.get(5).toString());
                                thisDataPoint.radarCode = flightDataArray.get(7).toString();
                                thisDataPoint.acType = flightDataArray.get(8).toString();
                                thisDataPoint.regCode = flightDataArray.get(9).toString();
                                thisDataPoint.timeStamp = Integer.parseInt(flightDataArray.get(10).toString());
                                thisDataPoint.depCode = flightDataArray.get(11).toString();
                                thisDataPoint.destCode = flightDataArray.get(12).toString();
                                thisDataPoint.outsideTemp = Integer.parseInt(flightDataArray.get(14).toString());
                                thisDataPoint.vertSpd = Integer.parseInt(flightDataArray.get(15).toString());
                                thisDataPoint.flightNum = flightDataArray.get(13).toString();
                                thisDataPoint.squawkCode = Integer.parseInt(flightDataArray.get(6).toString());
                            }
                        }
                    }

                }

            }

            return thisDataPoint;
        }catch(Exception e){
            return new FlightDataPoint();
        }
    }

    /**
     * The function exports data of a particular flight from the OnlineFlightData table to a .KML file
     * for a visualization of the flight path in Google Earth
     *
     * @param flightDataPoints  A set of consecutive flight data points
     */
    public static void exportFlightDataKML(List<FlightDataPoint> flightDataPoints){
        try{
            Date dateTime = new Date() ;
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd HHmm");
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm:ss");
            FlightDataPoint thisDataPoint;
            String ioDir, flightTrack = "", latitude, longitude, altitude;

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Configure and prepare kml file and document
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            System.out.println("\n" + dateTimeFormat.format(new Date()) + " ===================================================");
            System.out.println(dateTimeFormat.format(dateTime) + " Start Flight Data Export!");
            System.out.println(dateTimeFormat.format(new Date()) + " ===================================================");

            // Set local folder
            final String os = System.getProperty("os.name");

            Path localDir = Paths.get("").toAbsolutePath();
            if (os.contains("Windows"))
            {
                ioDir = "\\IO\\";
            }
            else
            {
                ioDir = "/IO/";
            }

            // Configure filename and file output stream
            thisDataPoint = flightDataPoints.get(0);

            String fileName = "FR24" + thisDataPoint.flightNum + " " + thisDataPoint.depCode + " " + thisDataPoint.destCode + " " + dateFormat.format(dateTime) + ".kml";
            File fout = new File(localDir + ioDir + fileName);
            FileOutputStream fos = new FileOutputStream(fout);

            // Initialize document builder factory and create new document
            DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
            Document kmlDoc = docBuilder.newDocument();

            // Create KML document root element
            Element kml = createChildElement(kmlDoc, "kml");
            kml.setAttribute("xmlns", "http://www.opengis.net/kml/2.2");

            Element doc = createChildElement(kml,"Document");
            Element docName = createChildElement(doc,"name");
            setElementValue(docName, fileName);

            // Set Flight Track style
            Element trackStyle = createChildElement(doc,"Style");
            trackStyle.setAttribute("id","linePoly");

            Element trackLineStyle = createChildElement(trackStyle,"LineStyle");

            Element trackLineColor = createChildElement(trackLineStyle,"color");
            setElementValue(trackLineColor,"7f00ffff");

            Element trackLineWidth = createChildElement(trackLineStyle,"width");
            setElementValue(trackLineWidth, "4");

            Element trackPolyStyle = createChildElement(trackLineStyle, "PolyStyle");

            Element trackPolyColor = createChildElement(trackPolyStyle,"color");
            setElementValue(trackPolyColor, "7f00ff00");


            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Process data points
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Loop througb data points and append data to flight track
            for(int e = 0; e < (flightDataPoints.size() - 1); e++) {
                thisDataPoint = flightDataPoints.get(e);

                // Get data
                latitude = String.format(Locale.US, "%.6f", thisDataPoint.latitude);
                longitude = String.format(Locale.US, "%.6f",thisDataPoint.longitude);
                altitude = String.format(Locale.US, "%.2f", (double) (thisDataPoint.altitude + 10));

                // Append data to flight track
                flightTrack  += latitude + "," + longitude + "," + altitude + " ";

                System.out.println(dateTimeFormat.format(new Date()) + " Processing Datapoint " + e);
            }

            // Add flight track to document
            Element trackMark = createChildElement(doc,"Placemark");

            Element trackName = createChildElement(trackMark, "name");
            setElementValue(trackName,"Flight Track");

            Element trackStyleUrl = createChildElement(trackMark, "styleUrl");
            setElementValue(trackStyleUrl, "#linePoly");

            // Set linestring configuration elements
            Element trackLineString = createChildElement(trackMark,"LineString");

            Element trackExtrude = createChildElement(trackLineString,"extrude");
            setElementValue(trackExtrude, "1");

            Element trackTessellate = createChildElement(trackLineString,"tessellate");
            setElementValue(trackTessellate,"1");

            Element trackAltMode = createChildElement(trackLineString,"altitudeMode");
            setElementValue(trackAltMode,"absolute");

            Element trackCoordinates = createChildElement(trackLineString,"coordinates");
            setElementValue(trackCoordinates,flightTrack);

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            // Write document to kml file
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            DOMSource source = new DOMSource(kml);
            StreamResult result = new StreamResult(fout);

            transformer.transform(source, result);

            System.out.println(dateTimeFormat.format(new Date()) + " File saved!");
            System.out.println("\n" + dateTimeFormat.format(new Date()) + " ===================================================");
            System.out.println(dateTimeFormat.format(dateTime) + " Data Capture completed!");
            System.out.println(dateTimeFormat.format(new Date()) + " ===================================================");

        }catch(Exception e){
            System.out.println(e.getMessage());
        }
    }


    /**
     * This method checks whether a specific time point is within a given time and date window,
     * defined by start/end times and start/end dates.
     *
     * @param argStartDate      Start date, format dd/MM/yyyy
     * @param argStartTime      Start time, format HH:mm:ss
     * @param argEndDate        End date, format dd/MM/yyyy
     * @param argEndTime        End time point, format HH:mm:ss
     * @param argCurrentDate    Check date, format dd/MM/yyyy
     * @param argCurrentTime    Check time, format HH:mm:ss
     *
     * @return Boolean result
     */
    public static boolean isDateTimeBetweenTwoDateTime(String argStartDate, String argStartTime, String argEndDate, String argEndTime, String argCurrentDate, String argCurrentTime) throws ParseException {

        String timeReg = "^([0-1][0-9]|2[0-3]):([0-5][0-9]):([0-5][0-9])$";
        String dateReg = "^(0[1-9]|[1-2][0-9]|3[0-1])/(0[1-9]|1[0-2])/([0-9][0-9][0-9][0-9])$";

        //Check date and time formats
        if (argStartDate.matches(dateReg) && argStartTime.matches(timeReg) && argEndDate.matches(dateReg) && argEndTime.matches(timeReg) && argCurrentDate.matches(dateReg) && argCurrentTime.matches(timeReg)) {
            boolean valid;

            //Start date and time
            java.util.Date startDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(argStartDate + " " + argStartTime);
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(startDateTime);

            //Current date and time
            java.util.Date currentDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(argCurrentDate + " " + argCurrentTime);
            Calendar currentCalendar = Calendar.getInstance();
            currentCalendar.setTime(currentDateTime);

            //End date and time
            java.util.Date endDateTime = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").parse(argEndDate + " " + argEndTime);
            Calendar endCalendar = Calendar.getInstance();
            endCalendar.setTime(endDateTime);

            if (currentDateTime.before(startDateTime)) {
                valid = false;
            } else {
                if (currentDateTime.before(endDateTime))
                    valid = true;
                else
                    valid = false;
            }

            return valid;
        } else {
            throw new IllegalArgumentException(
                    "Not a valid date time format(s), expecting DD/MM/YYYY HH:MM:SS format");
        }
    }

    /**
     * This method checks whether a given date/time string matches the defined
     * date/time format
     *
     * @param dateTimeString    date/time string to be checked, e.g. "12:00:01"
     * @param dateTimeFormat    date/time format to be checked against, e.g. "HH:mm:ss"
     * @return Boolean result
     */
    public static boolean isValidDateFormat(String dateTimeString, String dateTimeFormat) {
        SimpleDateFormat df = new SimpleDateFormat(dateTimeFormat);
        try {
            df.parse(dateTimeString);
            return true;
        } catch ( ParseException exc ) {
        }
        return false;
    }

    /**
     * This method clears the screen of the Windows console, by using cmd.exe methods
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public static void clearScreenWin() throws IOException, InterruptedException {
        new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
    }

    /**
     * This method clears the screen of a OS console, by adding a predefined number of
     * empty lines.
     */
    public static void clearScreenOS(){
        char c = '\n';
        int length = 25;
        char[] chars = new char[length];
        Arrays.fill(chars, c);
        System.out.print(String.valueOf(chars));
    }
}