package ftdis.fdpu;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

/**
 * The ListUtil class contains a set of methods that perform actions on list collections
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class ListUtil {

    /**
     * This method adds an element to list.
     *
     * @param element Element to be added.
     */
    public static <T> void addListItem(T element, List<T> list){
        list.add(element);
    }

    /**
     * This method returns an element at a specific position from a list.
     *
     * @param elementNum Number of the waypoint, index starts with 0
     * @return The waypoint.
     */
    public static <T> T getListItem(int elementNum, List<T> list){
        return list.get(elementNum);
    }

    /**
     * This method returns the position of an element in a list.
     *
     * @param element   Element to be found in the list
     * @param <T>       Generic type
     * @return          The position of the element in the list, index starts at 0.
     */
    public static <T,Y> int getListItemNum(T element, List<Y> list){
        return list.indexOf(element);
    }

    /**
    * This method returns the length of a list of lateral segments in meters.
    * The length is calculated on the basis of the great circle distance between
    * the start and end waypoints of each segment.
    *
    * @return The length of the lateral segment in meters.
    */
    public static <T extends LateralSegment> double getListLength(List<T> sgmtList){
       try{
           double totalLength = 0;

           for(LateralSegment sgmt: sgmtList){
               totalLength = totalLength + sgmt.getDist();
           }

           return totalLength;
       }catch(Exception e){
           System.out.println(e.getMessage());
           return 0;
       }
    }

    /**
     * This method checks whether the position of a cursor is within the lower and upper bounds of a list.
     *
     * @param cursor    The position of the cursor
     * @param list      The list
     * @param <T>       Generic parameter of the list
     * @return          Boolean indicating whether the cursor is within the list
     */
    public static <T> boolean inBound(int cursor, List<T> list){
        try{
            if(cursor < list.size() && cursor >= 0)
                return true;
            else
                return false;
        }catch(Exception e){
            System.out.println(e.getMessage());
            return false;
        }
    }


    /**
     * This method makes a "deep clone" of any Java object it is given.
     *
     * @param object    Generic object that is meant to be cloned
     */
    public static Object deepClone(Object object) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream outputStrm = new ObjectOutputStream(outputStream);
            outputStrm.writeObject(object);
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            ObjectInputStream objInputStream = new ObjectInputStream(inputStream);
            return objInputStream.readObject();
        }
        catch (Exception e) {
            return null;
        }
    }
}
