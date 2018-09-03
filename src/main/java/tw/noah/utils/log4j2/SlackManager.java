package tw.noah.utils.log4j2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractManager;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;
import org.apache.logging.log4j.core.config.Configuration;
import tw.noah.utils.log4j2.model.slack.Slack;
import tw.noah.utils.log4j2.model.slack.SlackAttachment;
import tw.noah.utils.log4j2.model.slack.SlackField;
import tw.noah.utils.log4j2.model.slack.SlackModel;

public class SlackManager extends AbstractManager {

  private Configuration configuration;


  /* param from log4j2.xml */
  private URL webHook;
  private int connectTimeoutMillis = 10_000;
  private String errorChannel; //channel
  private String warnChannel;  //channel
  private String infoChannel;  //channel
  private String appName;  // username
  private String environment;  // ex : production/staging/dev/lab/aws/idc....
  private long frequencyMillis;

  /* Priority define */
  private SlackField errorFields = new SlackField("Priority", "High");
  private SlackField warnFields = new SlackField("Priority", "Medium");
  private SlackField infoFields = new SlackField("Priority", "Low");

  private long lastPostTime = 0;
  private List<SlackAttachment> atts = new ArrayList<>(); // pending object - Slack Attachment
  private boolean hasWaitThreads = false; // waiting thread flag


  public SlackManager(final Configuration configuration, final LoggerContext loggerContext, final String appenderName, final URL webHook,
      final String errorChannel, final String warnChannel, final String infoChannel, final String appName, String environment,
      final int connectTimeoutSeconds, final int frequency) {

    super(loggerContext, appenderName);

    this.configuration = configuration;
    this.webHook = webHook;
    this.errorChannel = errorChannel;
    this.warnChannel = warnChannel;
    this.infoChannel = infoChannel;
    this.appName = appName;
    this.environment = environment;
    if (connectTimeoutSeconds > 0) {
      this.connectTimeoutMillis = connectTimeoutSeconds * 1000;
    }
    this.frequencyMillis = frequency * 1000;

  }

  public Configuration getConfiguration() {
    return configuration;
  }


  public void sendMessage(final Layout<?> layout, final LogEvent event) {

    String message = (String) layout.toSerializable(event);

    String channel;
    Slack.color color;
    Slack.icon icon;  // Variable pollution problem, but because of color recognition, the problem is no longer processed
    SlackField slackField;

    if (event.getLevel() == Level.FATAL | event.getLevel() == Level.ERROR) {
      channel = errorChannel;
      color = Slack.color.DANGER;
      icon = Slack.icon.DANGER;
      slackField = errorFields;
    } else if (event.getLevel() == Level.WARN) {
      channel = warnChannel;
      color = Slack.color.WARNING;
      icon = Slack.icon.WARNING;
      slackField = warnFields;
    } else {
      channel = infoChannel;
      color = Slack.color.GOOD;
      icon = Slack.icon.GOOD;
      slackField = infoFields;
    }

    SlackAttachment att = new SlackAttachment();
    att.setColor(color);
    att.setPreText("(" + environment + ")");
//    att.setAuthorName("Thread:" + event.getThreadId() + "\t\t" + event.getThreadName());
    att.setTitle(event.getSource().toString());
    att.setText(message);
    att.setSlackFields(new SlackField[]{slackField});
    att.setFooter("Thread:" + event.getThreadId() + "\t" + event.getThreadName() + "\t\t");
    att.setTimestamp(event.getTimeMillis() / 1000);

    atts.add(att);
    if (hasWaitThreads){
      return;
    }

    // thread simulation async
    Runnable runnable = () -> {
      hasWaitThreads = true;

      long timeRange = (System.currentTimeMillis() - lastPostTime);
      if (frequencyMillis > timeRange  ) {  // inner frequency
        try {
          Thread.sleep(frequencyMillis);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }
      }


      SlackModel model = new SlackModel();
      model.setChannel(channel);
      model.setUsername(appName);
      model.setIconEmoji(icon);
//      model.setSlackAttachments(atts.toArray(new SlackAttachment[atts.size()])); // old syntax
      model.setSlackAttachments(atts.stream().distinct().toArray(SlackAttachment[]::new)); // jdk 1.8 syntax

      atts.clear();
      lastPostTime = System.currentTimeMillis();
      hasWaitThreads = false;



      HttpURLConnection conn;
      try {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        String json = ow.writeValueAsString(model);
        if (event.getLevel() == Level.DEBUG) {
          System.out.println(json);
        }

        conn = (HttpURLConnection) webHook.openConnection();
        conn.setAllowUserInteraction(false);
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("content-type", "application/json; charset=utf-8");
        conn.setConnectTimeout(connectTimeoutMillis);
        conn.setReadTimeout(connectTimeoutMillis);
        conn.connect();

        OutputStream out = conn.getOutputStream();
        out.write(json.getBytes());
        out.flush();
        out.close();

        int responseCode = conn.getResponseCode();
        if (responseCode != HttpURLConnection.HTTP_OK) {
          throw new AppenderLoggingException("Got a non-200 status: " + responseCode + "\nHttp-Body=" + conn.getResponseMessage() + "\nPayload=" + json );

        }
        conn.disconnect();

      } catch (IOException e) {
        throw new UncheckedIOException(e);
      } finally {
        conn = null;
      }
    };

    Thread thread = new Thread(runnable);
    thread.start();
  }


}
