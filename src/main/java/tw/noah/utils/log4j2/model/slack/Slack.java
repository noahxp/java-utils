package tw.noah.utils.log4j2.model.slack;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

public class Slack {

  @AllArgsConstructor
  public enum color{
    GOOD("good"),
    WARNING("warning"),
    DANGER("danger");

    @Getter
    @JsonValue
    private String desc;

  }

  @AllArgsConstructor
  public enum icon{
    GOOD(":white_check_mark:"),
    WARNING(":warning:"),
    DANGER(":error:");

    @Getter
    @JsonValue
    private String desc;

  }

}
