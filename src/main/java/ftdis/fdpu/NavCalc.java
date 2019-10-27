package ftdis.fdpu;

import static java.lang.Math.*;

/**
 * The NavCalc class contains a set of methods that perform aviation and navigation calculations.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class NavCalc {
    //static final double RADIUS_EARTH_M = 6370973.27862;
    static final double RADIUS_EARTH_FSX_M = 6371000.38;//6378145.00; //6371000.38; //DOUBLE CHECK!!

    /**
     * This method uses the ‘haversine’ formula to calculate the great-circle distance between two waypoints,
     * i.e. the shortest distance over the earth’s surface.
     *
     * @param w1    1st waypoint
     * @param w2    2nd waypoint
     * @return      The direct great circle distance between two waypoints in m.
     */
    public static double getDirectDist(Waypoint w1, Waypoint w2){
        try{
            double dist;

            // Alternative A: Law of cos
            /*
            dist  = acos(sin(toRadians(w1.getLat())) * sin(toRadians(w2.getLat())) +
                    cos(toRadians(w1.getLat())) * cos(toRadians(w2.getLat())) * cos(toRadians(w2.getLon()-w1.getLon())))
                    * RADIUS_EARTH_FSX_M;
            */

            // Alternative B: Haversine
            Double latDistance = toRadians(w2.getLat() - w1.getLat());
            Double lonDistance = toRadians(w2.getLon() - w1.getLon());
            Double a = sin(latDistance / 2) * sin(latDistance / 2) +
                    cos(toRadians(w1.getLat())) * cos(toRadians(w2.getLat())) *
                            sin(lonDistance / 2) * sin(lonDistance / 2);
            Double c = 2 * atan2(sqrt(a), sqrt(1-a));

            dist = RADIUS_EARTH_FSX_M * c;

            return dist;
        }catch(Exception e){
            return 0;
        }
    }


    /**
     * This method returns an intersection of two tracks given the tracks' lat, lon and bearings
     *
     * @param w1        1st waypoint
     * @param bearing1  Bearing from 1st waypoint
     * @param w2        2nd waypoint
     * @param bearing2  Bearing from 2nd waypoint
     * @return          Waypoint at which both tracks intersect
     */
    public static Waypoint getIntersectWpt(Waypoint w1, double bearing1, Waypoint w2, double bearing2){
        Waypoint intersectWpt = new Waypoint(1);
        try{
            double lat1, lon1, brng13, lat2, lon2, brng23, lat3, lon3, dLat, dLon, dLon13, directDist, dist13, brngA, brngB, brng12, brng21, alpha1, alpha2, alpha3;

            // Convert coordinates and bearings to radians
            lat1 = toRadians(w1.getLat());
            lon1 = toRadians(w1.getLon());
            brng13 = toRadians(bearing1);

            lat2 = toRadians(w2.getLat());
            lon2 = toRadians(w2.getLon());
            brng23 = toRadians(bearing2);

            // Calculate distances
            dLat = lat2-lat1;
            dLon = lon2-lon1;
            directDist = 2* asin( sqrt( sin(dLat/2)* sin(dLat/2) + cos(lat1)* cos(lat2)* sin(dLon/2)* sin(dLon/2)));

            if (directDist == 0) return null;

            // Caclculate initial/final bearings between points
            brngA = acos( ( sin(lat2) - sin(lat1)* cos(directDist) ) / ( sin(directDist)* cos(lat1) ) );
            if (brngA == Double.NaN)
                brngA = 0;  // protect against rounding
            brngB = acos( ( sin(lat1) - sin(lat2)* cos(directDist) ) / ( sin(directDist)* cos(lat2) ) );

            if (sin(lon2-lon1) > 0) {
                brng12 = brngA;
                brng21 = 2* PI - brngB;
            } else {
                brng12 = 2* PI - brngA;
                brng21 = brngB;
            }

            // Calculate angles
            alpha1 = (brng13 - brng12 + PI) % (2* PI) - PI; //2-1-3
            alpha2 = (brng21 - brng23 + PI) % (2* PI) - PI; //1-2-3

            // Catch exceptions
            if (sin(alpha1)==0 && sin(alpha2)==0) return null;  // infinite intersections
            if (sin(alpha1)* sin(alpha2) < 0) return null;       // ambiguous intersection

            // Calculate latitude and longitude of intersection
            alpha3 = acos( -cos(alpha1)* cos(alpha2) + sin(alpha1)* sin(alpha2)* cos(directDist) );
            dist13 = atan2( sin(directDist)* sin(alpha1)* sin(alpha2), cos(alpha2)+ cos(alpha1)* cos(alpha3) );
            lat3 = asin( sin(lat1)* cos(dist13) + cos(lat1)* sin(dist13)* cos(brng13) );
            dLon13 = atan2( sin(brng13)* sin(dist13)* cos(lat1), cos(dist13)- sin(lat1)* sin(lat3) );
            lon3 = lon1+dLon13;
            lon3 = (lon3+3* PI) % (2* PI) - PI;

            // Assign vals and return intersection waypoint
            intersectWpt.setLat(toDegrees(lat3));
            intersectWpt.setLon(toDegrees(lon3));

            return intersectWpt;

        } catch(Exception e){
            return intersectWpt;
        }
    }


    /**
     * This method calculates an object's initial bearing at the first waypoint on a direct great
     * circle route between two waypoints.
     *
     * @param w1    1st waypoint
     * @param w2    2nd waypoint
     * @return      The initial bearing at the first waypoint on a direct great circle route between
     *              two waypoints in degrees.
     */
    public static double getInitBearing(Waypoint w1, Waypoint w2){
        double bearing;
        try{
            bearing = toDegrees(atan2((sin(toRadians(w2.getLon()) - toRadians(w1.getLon())) * cos(toRadians(w2.getLat()))),
                    (cos(toRadians(w1.getLat())) * sin(toRadians(w2.getLat())) - sin(toRadians(w1.getLat())) * cos(toRadians(w2.getLat()))
                            * cos(toRadians(w2.getLon()) - toRadians(w1.getLon())))));

            if(bearing < 0)
                bearing = 360 + bearing;

            return bearing;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns an object's new course after the execution of a course change
     *
     * @param courseAtStart The course at start in degrees
     * @param courseChange  The total course change in degrees
     * @return The new course in degrees
     */
    public static double getNewCourse(double courseAtStart, double courseChange){
        try{
            double newCourse = courseAtStart + courseChange;

            if(newCourse < 0)
                newCourse = newCourse + 360;
            else if(newCourse >= 360)
                newCourse = newCourse - 360;

            return newCourse;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns the magnitude of an object's course change along a turn. The method always returns the smaller of the two possible
     * course changes, e.g. a course change of 0 to 90 degrees, returns +90 and NOT -270
     *
     * @return Total course change in degrees.
     */
    public static double getCourseChange(double getCourseStart, double getCourseEnd){
        try{
            return ((((getCourseStart + 360) - getCourseEnd + 180) % 360) - 180) * -1;
        }catch(Exception e){
            return 0;
        }
    }

    /**
     * This method returns a waypoint on a great circle track between two waypoints, based on it's
     * distance, expressed as a fraction of the track's overall length, from the start point of the track.
     *
     * @param wpt1    Start waypoint
     * @param wpt2    End waypoint
     * @param f     Fraction of distance between start and end waypoint, i.e. w1 = 0, w2 = 1
     * @return      Intermediate waypoint along the great circle track between the start and end waypoint
     */
    public static Waypoint getItmWpt(Waypoint wpt1, Waypoint wpt2, double f){
        Waypoint itmWpt = new Waypoint(1);
        double A,B,x,y,z,distRad;
        try{
            distRad = 2 * asin(sqrt((pow(sin((toRadians(wpt1.getLat()) - toRadians(wpt2.getLat()))/2),2))
                    + cos(toRadians(wpt1.getLat())) * cos(toRadians(wpt2.getLat())) * (pow(sin((toRadians(wpt1.getLon())
                    - toRadians(wpt2.getLon()))/2),2))));

            A = sin((1 - f) * distRad)/sin(distRad);
            B = sin(f * distRad)/sin(distRad);

            x = A * cos(toRadians(wpt1.getLat())) * cos(toRadians(wpt1.getLon())) + B * cos(toRadians(wpt2.getLat())) * cos(toRadians(wpt2.getLon()));
            y = A * cos(toRadians(wpt1.getLat())) * sin(toRadians(wpt1.getLon())) + B * cos(toRadians(wpt2.getLat())) * sin(toRadians(wpt2.getLon()));
            z = A * sin(toRadians(wpt1.getLat())) + B * sin(toRadians(wpt2.getLat()));

            itmWpt.setLat(toDegrees(atan2(z,sqrt(pow(x,2) + pow(y,2)))));
            itmWpt.setLon(toDegrees(atan2(y,x)));

            return itmWpt;
        }catch(Exception e){
            return null;
        }
    }

    /**
     * This method returns a waypoint based on the distance and radial from a reference
     * waypoint.
     *
     * @param startWpt  Start waypoint
     * @param dist      Distance from start waypoint in meters
     * @param radial    Radial from start waypoint in degrees
     * @return          Waypoint based on distance and radial from reference point
     */
    public static Waypoint getRadWpt(Waypoint startWpt, double dist, double radial){
        Waypoint endWpt = new Waypoint();
        double lat, lon, dlon;
        try{
            /** FORMULA
             *  lat =asin(sin(lat1)*cos(d)+cos(lat1)*sin(d)*cos(tc))
             *
             *  dlon=atan2(sin(tc)*sin(d)*cos(lat1),cos(d)-sin(lat1)*sin(lat))
             *  lon=mod( lon1 + dlon +pi,2*pi )-pi
             *
             */

            dist = dist / RADIUS_EARTH_FSX_M;

            lat = asin((sin(toRadians(startWpt.getLat())) * cos(dist)) + (cos(toRadians(startWpt.getLat())) * sin(dist) * cos(toRadians(radial))));

            dlon = atan2(sin(toRadians(radial)) * sin(dist) * cos(toRadians(startWpt.getLat())), cos(dist) - (sin(toRadians(startWpt.getLat())) * sin(lat)));
            lon = ((toRadians(startWpt.getLon()) + dlon + PI) % (2 * PI)) - PI;

            endWpt.setLat(toDegrees(lat));
            endWpt.setLon(toDegrees(lon));

            return endWpt;
        }catch(Exception e){
            return endWpt;
        }
    }
}
