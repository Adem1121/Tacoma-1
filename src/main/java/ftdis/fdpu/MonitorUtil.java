package ftdis.fdpu;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.Message.RecipientType;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class MonitorUtil {
    public MonitorUtil() {
    }

    public void logProgress(LocalDateTime startTimeDate, double progrPerc) {
        String os = System.getProperty("os.name");
        Path localDir = Paths.get("").toAbsolutePath().getParent().getParent();
        String ioDir;
        if (os.contains("Windows")) {
            ioDir = "\\IO\\";
        } else {
            ioDir = "/IO/";
        }

        String logFile = localDir + ioDir + "log.dat";

        try {
            FileOutputStream fos = new FileOutputStream(logFile);

            try {
                ObjectOutputStream oos = new ObjectOutputStream(fos);

                try {
                    Monitor log = new Monitor(startTimeDate, progrPerc);
                    oos.writeObject(log);
                } catch (Throwable var14) {
                    try {
                        oos.close();
                    } catch (Throwable var13) {
                        var14.addSuppressed(var13);
                    }

                    throw var14;
                }

                oos.close();
            } catch (Throwable var15) {
                try {
                    fos.close();
                } catch (Throwable var12) {
                    var15.addSuppressed(var12);
                }

                throw var15;
            }

            fos.close();
        } catch (IOException var16) {
            var16.printStackTrace();
        }

    }

    /*
    public void sendProgressMail(double progPerc) {
        String mailServer = Config.SMTP_SERVER;
        String mailServerPort = Config.SMTP_PORT;
        final String userName = Config.SMTP_USER;
        final String userAuth = Config.SMTP_PW;
        String mailTo = "BobbieERay@gmail.com";
        String mailCc = "";
        String EMAIL_SUBJECT = "FTDIS - Progress " + progPerc + "%";
        String EMAIL_TEXT = "Hello " + userName + "\n\n FTDIS progress is at " + progPerc + "% \n\n Best regards,\n FTDIS";
        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", mailServer);
        prop.put("mail.smtp.port", mailServerPort);
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");
        Session session = Session.getInstance(prop, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(userName, userAuth);
            }
        });
        MimeMessage msg = new MimeMessage(session);

        try {
            msg.setFrom(new InternetAddress(userName));
            msg.setRecipients(RecipientType.TO, InternetAddress.parse(mailTo, false));
            msg.setRecipients(RecipientType.CC, InternetAddress.parse(mailCc, false));
            msg.setSubject(EMAIL_SUBJECT);
            msg.setText(EMAIL_TEXT);
            msg.setSentDate(new Date());
            Transport.send(msg);
        } catch (Exception var15) {
            System.out.println(var15.getMessage());
        }

    }*/
}
