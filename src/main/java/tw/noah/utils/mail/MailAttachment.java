package tw.noah.utils.mail;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MailAttachment implements Serializable {

  private String fileName;

  private byte[] content;
}
