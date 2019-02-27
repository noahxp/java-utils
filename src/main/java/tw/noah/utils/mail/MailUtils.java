package tw.noah.utils.mail;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;

@Log4j2
/**
 * email utils, email specification : https://tools.ietf.org/html/rfc4021#section-2.1.6
 */
public class MailUtils {

  /**
   * send mail using smtp server
   * @param host mail server domain or ip address
   * @param from Specifies the author of the message;<br> that is, the mailbox of the person or system responsible for the writing of the message
   * @param to Primary recipient mailbox;<br>Contains the address of the primary recipient of the message.
   * @param subject Topic of message;<br> Contains a short string identifying the topic of the message.
   * @param body content of mail body
   */
  public static void sendSmtpHtmlMail(String host,String from,String to,String subject,String body){
    MailUtils.sendSmtpHtmlMail(host, from, to, null, null, subject, body);
  }

  /**
   * send mail using smtp server (with sender,replyTo)
   * @param host mail server domain or ip address
   * @param from Specifies the author of the message;<br> that is, the mailbox of the person or system responsible for the writing of the message
   * @param to Primary recipient mailbox;<br>Contains the address of the primary recipient of the message.
   * @param sender Specifies the mailbox of the agent responsible for the actual transmission of the message
   * @param replyTo Mailbox for replies to message;<br> It indicates the mailbox to which the author of the message suggests that replies be sent
   * @param subject Topic of message;<br> Contains a short string identifying the topic of the message.
   * @param body content of mail body
   */
  public static void sendSmtpHtmlMail(String host,String from,String to,String sender,String replyTo,String subject,String body){
    MailUtils.sendSmtpHtmlMail(host, from, to, sender, replyTo, subject, body,null);
  }

  /**
   * single attachment mail using smtp server
   * @param host mail server domain or ip address
   * @param from Specifies the author of the message;<br> that is, the mailbox of the person or system responsible for the writing of the message
   * @param to Primary recipient mailbox;<br>Contains the address of the primary recipient of the message.
   * @param sender Specifies the mailbox of the agent responsible for the actual transmission of the message
   * @param replyTo Mailbox for replies to message;<br> It indicates the mailbox( to which the author of the message suggests that replies be sent
   * @param subject Topic of message;<br> Contains a short string identifying the topic of the message.
   * @param body content of mail body
   * @param attachFile the mail attach file
   */
  public static void sendSmtpHtmlMail(String host,String from,String to,String sender,String replyTo,String subject,String body,File attachFile){
    List<File> files = new ArrayList<>();
    if (attachFile!=null)
      files.add(attachFile);

    MailUtils.sendSmtpHtmlMailFiles(host, from, to, sender, replyTo, subject, body,files);

  }

  /**
   * multiple attachment mail using smtp server
   * @param host mail server domain or ip address
   * @param from Specifies the author of the message;<br> that is, the mailbox of the person or system responsible for the writing of the message
   * @param to Primary recipient mailbox;<br>Contains the address of the primary recipient of the message.
   * @param sender Specifies the mailbox of the agent responsible for the actual transmission of the message
   * @param replyTo Mailbox for replies to message;<br> It indicates the mailbox to which the author of the message suggests that replies be sent
   * @param subject Topic of message;<br> Contains a short string identifying the topic of the message.
   * @param body content of mail body
   * @param attachFiles the mail attach file
   */
  public static void sendSmtpHtmlMailFiles(String host,String from,String to,String sender,String replyTo,String subject,String body,List<File> attachFiles){
    MailModel mail = new MailModel();
    mail.setHost(host);
    mail.setFrom(from);
    mail.setTo(to);
    mail.setSender(sender);
    mail.setReplyTo(replyTo);
    mail.setSubject(subject);
    mail.setBody(body);

    mail.setHtml(true);

    List<MailAttachment> att = new ArrayList<>();
    for (File f:attachFiles) {
      try {
        att.add(new MailAttachment(f.getName(), Files.readAllBytes(f.toPath())));
      } catch (IOException e) {
        log.error(e,e);
      }
    }

//    List<MailAttachment> att = new ArrayList<>();
//    try {
//      att.add(new MailAttachment("a.pdf", Files.readAllBytes(new File("~/1234.pdf").toPath())));
//      att.add(new MailAttachment("a.log", Files.readAllBytes(new File("~/abc.log").toPath())));
//    } catch (IOException e) {
//      e.printStackTrace();
//    }
    mail.setMailAttachments(att);

    MailUtils.sendSmtpMail(mail);

  }

  /**
   * send smtp mail with mail model parameters
   * @param mail
   */
  public static void sendSmtpMail(@NonNull MailModel mail) {

    Assert.notNull(mail.getHost(), "mail host can't been null");
    Assert.notNull(mail.getFrom(), "mail from can't been null");
    Assert.notNull(mail.getTo(), "mail to can't been null");
    Assert.notNull(mail.getSubject(), "mail subject can't been null");

    Properties props = System.getProperties();
    props.setProperty("mail.smtp.host", mail.getHost());
    props.setProperty("mail.smtp.timeout", "5000");
    if (mail.getPort() != 25) {
      props.put("mail.smtp.port", mail.getPort());
    }

    /** mail server 需要驗證，目前無需求，先拿掉 **/
//    props.put("mail.smtp.port", "587"); //TLS Port
//    props.put("mail.smtp.auth", "true"); //enable authentication
//    props.put("mail.smtp.starttls.enable", "true"); //enable STARTTLS

//    Authenticator auth = new Authenticator() {
//      //override the getPasswordAuthentication method
//      protected PasswordAuthentication getPasswordAuthentication() {
//        return new PasswordAuthentication(fromEmail, password);
//      }
//    };
//    Session session = Session.getInstance(props, auth);

    Session session = Session.getDefaultInstance(props);

    try {
      MimeMessage message = new MimeMessage(session);

      if (mail.isBulk()) {
        message.addHeader("Precedence", "bulk");
      }

//      if (!StringUtils.isEmpty(mail.getReturnPath())) {
//        message.addHeader("Return-Path", mail.getReturnPath());
//      }

      if (!StringUtils.isEmpty(mail.getSender())) {
        message.setSender(new InternetAddress(mail.getSender()));
      }

      if (!StringUtils.isEmpty(mail.getFrom())) {
        message.setFrom(new InternetAddress(mail.getFrom()));
      }

      if (!StringUtils.isEmpty(mail.getReplyTo())) {
        message.setReplyTo(new InternetAddress[]{new InternetAddress(mail.getReplyTo())});
      }

      if (!StringUtils.isEmpty(mail.getTo())) {
        message.addRecipients(RecipientType.TO, emailToInternetAddress(mail.getTo()));
      }

      if (!StringUtils.isEmpty(mail.getCc())) {
        message.addRecipients(RecipientType.CC, emailToInternetAddress(mail.getCc()));
      }

      if (!StringUtils.isEmpty(mail.getBcc())) {
        message.addRecipients(RecipientType.BCC, emailToInternetAddress(mail.getBcc()));
      }

      if (!StringUtils.isEmpty(mail.getSubject())) {
        message.setSubject(MimeUtility.encodeText(mail.getSubject(), "utf-8", "B"));
      }

      Multipart multipart = new MimeMultipart();

      MimeBodyPart messageBodyPart = new MimeBodyPart();
      if (mail.isHtml()) {
        messageBodyPart.setContent(mail.getBody(), "text/html; charset=utf-8");
      } else {
        messageBodyPart.setText(mail.getBody());
      }
      multipart.addBodyPart(messageBodyPart);
      message.setContent(multipart);

      if (mail.getMailAttachments()!=null) {
        for (MailAttachment v : mail.getMailAttachments()) {
          MimeBodyPart attachPart = new MimeBodyPart();
          attachPart.setFileName(v.getFileName());
          attachPart.setDataHandler(new DataHandler(new ByteArrayDataSource(v.getContent(), "application/octet-stream")));
          multipart.addBodyPart(attachPart);
        }
      }

      Transport.send(message);

      log.trace("send to " + mail.getTo() + "-" + mail.getSubject() + " success.");

    } catch (MessagingException | UnsupportedEncodingException e) {
      log.error(e, e);
    }

  }

  private static InternetAddress[] emailToInternetAddress(String mail) {
    mail = mail.replaceAll(";", ",");
    String[] mails = mail.split(",");

    List<InternetAddress> ia = new ArrayList<>();

    for (String m : mails) {
      try {
        ia.add(new InternetAddress(m));
      } catch (AddressException e) {
        log.trace(m, e);
      }
    }

    return ia.toArray(new InternetAddress[ia.size()]);
  }

}
