package ftdis.fdpu;

import java.util.ArrayList;
import java.util.List;

/**
 * The Event Collection class represents a central repository for all of the events of a flight plan,
 * The class is a central component for loading, validating and processing the events.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class EventCollection {
    public Integer id;
    private List<Event> events;

    /**
     * Default constructor
     */
    EventCollection(){
        this.events = new ArrayList<Event>();

    }

    EventCollection(int id){
        this();
        this.id = id;
    }

    /**
     * This method adds a event to the event collection's event list
     *
     * @param event DirectSegment segment to be added.
     */
    public void addEvent(Event event){
        this.events.add(event);
    }

    /**
     * This method returns the event collection's event of a specific event number
     *
     * @param eventNum Number of the direct segment, starts with 0
     * @return The direct segment
     */
    public Event getEvent(int eventNum){
        return ListUtil.getListItem(eventNum,this.events);
    }


    /**
     * This method returns the position of a specific event in the event collection's
     * list of events.
     *
     * @param event     The event
     * @return          The position of the event in the event collection list, index starts with 0
     */
    public int getEventPos(Event event){
        return ListUtil.getListItemNum(event, this.events);
    }

    /**
     * @return The total number of events in the event collection
     */
    public int getEventCount(){
        return this.events.size();
    }

}
