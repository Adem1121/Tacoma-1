package ftdis.fdpu;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;
import java.util.Date;

import static ftdis.fdpu.Config.*;

public class MonitorUtil {

    public void sendProgressMail(double progPerc){

        String mailServer = SMTP_SERVER;
        String mailServerPort = SMTP_PORT;
        String userName = SMTP_USER;
        String userAuth = SMTP_PW;

        String mailTo = "BobbieERay@gmail.com";
        String mailCc = "";
        String testVar = "123";

        String EMAIL_SUBJECT = "FTDIS - Progress " + progPerc + "%";
        String EMAIL_TEXT = "Hello " + userName + "\n\n FTDIS progress is at " + progPerc + "% \n\n Best regards,\n FTDIS";

        Properties prop = System.getProperties();
        prop.put("mail.smtp.host", mailServer); //optional, defined in SMTPTransport  df
        prop.put("mail.smtp.port", mailServerPort); // default port 25
        prop.put("mail.smtp.auth", "true");
        prop.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(prop,
                new javax.mail.Authenticator() {
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(userName, userAuth);
                    }
                });

        Message msg = new MimeMessage(session);

        try {

            // from
            msg.setFrom(new InternetAddress(userName));

            // to
            msg.setRecipients(Message.RecipientType.TO,
                    InternetAddress.parse(mailTo, false));

            // cc
            msg.setRecipients(Message.RecipientType.CC,
                    InternetAddress.parse(mailCc, false));

            // subject
            msg.setSubject(EMAIL_SUBJECT);

            // content
            msg.setText(EMAIL_TEXT);

            msg.setSentDate(new Date());

            Transport.send(msg);

        }catch(Exception e){
            System.out.println(e.getMessage());

        }
    }
}
