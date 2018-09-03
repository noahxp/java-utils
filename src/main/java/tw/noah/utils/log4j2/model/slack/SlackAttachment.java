package tw.noah.utils.log4j2.model.slack;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class SlackAttachment {

  private Slack.color color;

  @JsonProperty("pretext")
  private String preText;

  @JsonProperty("author_name")
  private String authorName;

  @JsonProperty("author_link")
  private String authorLink;

  private String title;

  @JsonProperty("title_link")
  private String titleLink;

  private String text;

  private String footer;

  @JsonProperty("footer_icon")
  private String footerIcon;

  @JsonProperty("ts")
  private long timestamp;

  @JsonProperty("fields")
  private SlackField[] slackFields;
}
