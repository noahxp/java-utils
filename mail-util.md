## email utils

[MailUtils](src/main/java/tw/noah/utils/mail/MailUtils.java) send mail using smtp server

example 1:

    MailModel mail = new MailModel();
    mail.setHost("mailserver.com.tw");
    mail.setSender("sender@noah.tw");
    mail.setFrom("Noah<from@noah.tw>");
    mail.setTo("Noah<noah@noah.tw>");
    mail.setReplyTo("noah@noah.tw");
    mail.setBulk(true);
    mail.setBody("<html><body><h1 style='color:red'>this is h1</h1><hr>" + (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")).format(new Date())
        + "</body></html>");
    mail.setSubject("this is mail subject, we support utf-8 charset");
    mail.setHtml(false);
  
    List<MailAttachment> att = new ArrayList<>();
    try {
      att.add(new MailAttachment("a.pdf", Files.readAllBytes(new File("~/1234.pdf").toPath())));
      att.add(new MailAttachment("a.log", Files.readAllBytes(new File("~/abc.log").toPath())));
    } catch (IOException e) {
      log.error(e,e)
    }
    mail.setMailAttachments(att);
  
  
    MailUtils.sendSmtpMail(mail);
    
    
example 2:

    MailUtils.sendSmtpHtmlMail("mailserver.com.tw","From<noah@noah.tw>","To<noah@noah.tw>","oms-agent@noah.tw","Reply<noah@noah.tw>","this is a mail","<html><body><h1>123<h1/></body></html>");
    
    
example 3:

    MailUtils.sendSmtpHtmlMail("mailserver.com.tw","From<noah@noah.tw>","To<noah@noah.tw>","this is a mail","<html><body><h1>123<h1/></body></html>");
    
    
more example see the [source code]((src/main/java/tw/noah/utils/mail/MailUtils.java))