package ftdis.fdpu;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MonitorProgress {
    public static void main(String[] args) {
        String startDateTimeLog, runTimeLog, ioDir, logFile, progrBar = "[..........]";
        double progrLog;
        long runTime;
        int progrLn = 2;
        boolean monitorActive = true;
        LocalDateTime MonitorStartTimeDate;
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        // Set input directories
        final String os = System.getProperty("os.name");

        Path localDir = Paths.get("").toAbsolutePath().getParent().getParent();

        if (os.contains("Windows"))
            ioDir = "\\IO\\";
        else
            ioDir = "/IO/";

        logFile = localDir + ioDir + "log.dat";

        // Initialize monitor
        MonitorStartTimeDate = LocalDateTime.now();

        // Monitor and report progress as per interval
        while(monitorActive){

            if(Duration.between(MonitorStartTimeDate, LocalDateTime.now()).getSeconds() >= progrLn){
                try (FileInputStream fis = new FileInputStream(logFile);
                     ObjectInputStream ois = new ObjectInputStream(fis)) {

                    Monitor log = (Monitor) ois.readObject();

                    startDateTimeLog = dateFormat.format(log.getStartDateTime());

                    runTime = Duration.between(log.getStartDateTime(), LocalDateTime.now()).getSeconds();
                    runTimeLog = String.format("%d:%02d:%02d", runTime / 3600, (runTime % 3600) / 60, (runTime % 60));

                    progrLog = log.getProgress();

                    if(progrLog > 10 && progrLog <= 20)
                        progrBar = "[*.........]";
                    else if(progrLog > 20 && progrLog <= 30)
                        progrBar = "[**........]";
                    else if(progrLog > 30 && progrLog <= 40)
                        progrBar = "[***.......]";
                    else if(progrLog > 40 && progrLog <= 50)
                        progrBar = "[****......]";
                    else if(progrLog > 50 && progrLog <= 60)
                        progrBar = "[*****.....]";
                    else if(progrLog > 60 && progrLog <= 70)
                        progrBar = "[******....]";
                    else if(progrLog > 70 && progrLog <= 80)
                        progrBar = "[*******...]";
                    else if(progrLog > 80 && progrLog <= 90)
                        progrBar = "[********..]";
                    else if(progrLog > 90)
                        progrBar = "[*********.]";

                    // Clear Screen
                    System.out.print("\033[H\033[2J");
                    System.out.flush();

                    // Print progress information
                    System.out.println("Start: " + startDateTimeLog + "     Run Time: " + runTimeLog);
                    System.out.println(progrBar + "  " + String.format(Locale.US, "%10.3f", progrLog)  + " %");

                    MonitorStartTimeDate = LocalDateTime.now();

                } catch (IOException | ClassNotFoundException e) {
                    System.out.println("No Data Found!");
                    monitorActive = false;
                }
            }




        }



    }
}
