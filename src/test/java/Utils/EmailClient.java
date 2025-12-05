package Functions.Utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

public class EmailClient {

    public static void sendEmail(String testTeamName, String testType, String from, String subject, String to, String cc, String emailBodyRelativePath, String attachmentLocation,Boolean Attach) {
        System.out.println("Attachment Location is: " + attachmentLocation);
        String host, emailBody = null, finalEmailBody = null;

        // Locating body to use on email
        try {
            emailBody = new String(Files.readAllBytes(Paths.get(emailBodyRelativePath))).trim();
            System.out.println("Locating email body template: " + emailBodyRelativePath);
        } catch (IOException e) {
            //throw new GenericEx("Error in retrieving email body from file\n" + e);
        }

        // Replacing Email Template Values
        try {
            finalEmailBody = emailBody.replaceAll("~testTeamName~", testTeamName.toUpperCase()).replace("~testType~", testType).replace("~executedBy~", System.getProperty("user.name"));
            finalEmailBody =finalEmailBody.replaceAll("~Path~",attachmentLocation);
        } catch (Exception e) {
           // throw new GenericEx("Error in replacing placeholders on email body template\n" + e);
        }


        //Setting email server information
        host = "";
        Properties properties = System.getProperties();
        properties.setProperty("mail.smtp.host", host);
        Session session = Session.getDefaultInstance(properties);

        try {
            MimeMessage message = new MimeMessage(session);
            Multipart multipart = new MimeMultipart();

            // Constructing 'from' email address
            message.setFrom(new InternetAddress(from));
            // Constructing 'to' email address
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));

            // Constructing 'cc' email address if available
            if (cc != null) {
                message.addRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));


            }
            // Constructing subject of email
            message.setSubject(subject);

            // Constructing body of email
            BodyPart messageBodyPart = new MimeBodyPart();
            messageBodyPart.setContent(finalEmailBody, "text/html");
            multipart.addBodyPart(messageBodyPart);
            messageBodyPart = new MimeBodyPart();

            // Constructing attachments for email
            if(Attach.toString().equalsIgnoreCase("false")){
                System.out.println("Email has no attachment");
            }
            else {
                FileDataSource source = new FileDataSource(new File(attachmentLocation));
                messageBodyPart.setDataHandler(new DataHandler(source));
                messageBodyPart.setFileName(source.getFile().getName());
                multipart.addBodyPart(messageBodyPart);
            }
            message.setContent(multipart);
            Transport.send(message);
            if (cc != null) {
                System.out.println("Email message has been sent successfully to: " + to + " and " + cc);
            } else {
                System.out.println("Email message has been sent successfully to: " + to);
            }

        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
