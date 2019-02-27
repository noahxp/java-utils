package tw.noah.utils.mail;

import java.io.Serializable;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@AllArgsConstructor
@NoArgsConstructor
@Data
public class MailModel implements Serializable {


  /**
   * smtp mail server address or ip
   */
  private String host;


  /**
   * smtp mail port , defautl port is 25
   */
  private int port = 25;

  /**
   * Specifies the mailbox of the agent responsible for the actual transmission of the message
   */
  private String sender;

  /**
   * Specifies the author of the message;<br> that is, the mailbox of the person or system responsible for the writing of the message.<br> the format is: Name&lt;e-mail&gt; or e-mail
   */
  private String from;

  /**
   * Primary recipient mailbox;<br>Contains the address of the primary recipient of the message.<br> the format is: Name&lt;e-mail&gt; or e-mail
   */
  private String to;

  /**
   * Carbon-copy recipient mailbox;<br> Contains the addresses of others who are to receive the message, though the content of the message may not be directed at them.<br> the format is: Name&lt;e-mail&gt; or e-mail
   */
  private String cc;

  /**
   * Blind-carbon-copy recipient mailbox;<br> Contains addresses of recipients of the message whose addresses are not to be revealed to other recipients of the message.<br> the format is: Name&lt;e-mail&gt; or e-mail
   */
  private String bcc;

  /**
   * @param replyTo Mailbox for replies to message;<br> It indicates the mailbox to which the author of the message suggests that replies be sent.<br>the format is: Name&lt;e-mail&gt; or e-mail
   */
  private String replyTo;

//  /** 目前找不到支援 returnPath 的方法 ，所以暫時拿掉
//   * 退件信箱，無設定時，會以「from」替代」，格式: Name&lt;e-mail&gt; 或 e-mail ，
//   */
//  private String returnPath;

  /**
   * Topic of message;<br> Contains a short string identifying the topic of the message.
   */
  private String subject;

  /**
   * content of mail body
   */
  private String body;

  /**
   * set the mail is html mail or text mail
   */
  private boolean html;

  /**
   *
   */
  private List<MailAttachment> mailAttachments;

  /**
   * set the mail is bulk mail ( "Precedence: bulk" on e-mail header) https://support.google.com/mail/answer/81126?hl=en
   */
  private boolean bulk;

}
