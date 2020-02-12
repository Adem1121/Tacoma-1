package ftdis.fdpu;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Monitor implements Serializable {
    private LocalDateTime startDateTime;
    private double progrPerc;

    public Monitor(LocalDateTime startDateTime, double progrPerc) {
        this.startDateTime = startDateTime;
        this.progrPerc = progrPerc;
    }

    public LocalDateTime getStartDateTime(){
        try{
            return startDateTime;
        }catch (Exception e){
            return null;
        }
    }

    public double getProgress(){
        try{
            return progrPerc;
        }catch (Exception e){
            return 0;
        }
    }

}
