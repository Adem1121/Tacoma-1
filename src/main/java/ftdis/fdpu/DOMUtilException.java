package ftdis.fdpu;

/**
 * The DOMUtilException class represents a custom, generic exception that is thrown when encountering
 * specific exceptions that can occur during the execution of the DOMUtil methods.
 *
 * @author windowSeatFSX@gmail.com
 * @version 0.1
 */
public class DOMUtilException extends Exception{
    String desc;

    /**
     * Default constructor
     *
     * @param exceptionTxt  Description of exception.
     */
    DOMUtilException(String exceptionTxt){
        super();
        desc = exceptionTxt;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(){
        return "DOMUtil Exception: " + desc;
    }
}
