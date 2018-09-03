package tw.noah.utils.log4j2.model.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Data;

@Data
public class SlackModel implements Serializable {

  private String channel;
  private String username;

  @JsonProperty("icon_emoji")
  private Slack.icon iconEmoji;

  @JsonProperty("attachments")
  private SlackAttachment[] slackAttachments;

}
