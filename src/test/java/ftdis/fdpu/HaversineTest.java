package ftdis.fdpu;

import static java.lang.Math.*;
import static java.lang.Math.cos;
import static java.lang.Math.toRadians;

/**
 * This is the implementation Haversine Distance Algorithm between two places
 * @author ananth
 *  R = earth’s radius (mean radius = 6,371km)
Δlat = lat2− lat1
Δlong = long2− long1
a = sin²(Δlat/2) + cos(lat1).cos(lat2).sin²(Δlong/2)
c = 2.atan2(√a, √(1−a))
d = R.c
 *
 */

public class HaversineTest{

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        final double R = 6371000.38;; // Radious of the earth
        Double lat1 = 47.476641;
        Double lon1 = -122.307703;
        Double lat2 = 47.485540;
        Double lon2 = -122.320961;

        //Haversine
        Double latDistance = toRad(lat2-lat1);
        Double lonDistance = toRad(lon2-lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
                Math.cos(toRadians(lat1)) * Math.cos(toRadians(lat2)) *
                        Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        Double distance = R * c;

        System.out.println("The distance between two lat and long is::" + distance);

        // laws of cosine
        distance  = acos(sin(toRadians(lat1)) * sin(toRadians(lat2)) +
                cos(toRadians(lat1)) * cos(toRadians(lat2)) * cos(toRadians(lon2-lon1)))
                * R;

        System.out.println("The distance between two lat and long is::" + distance);


    }

    private static Double toRad(Double value) {
        return value * Math.PI / 180;
    }

}
