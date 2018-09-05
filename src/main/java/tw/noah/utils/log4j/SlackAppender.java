package tw.noah.utils.log4j;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import tw.noah.utils.log4j2.model.slack.Slack;
import tw.noah.utils.log4j2.model.slack.Slack.color;
import tw.noah.utils.log4j2.model.slack.Slack.icon;
import tw.noah.utils.log4j2.model.slack.SlackModel;
import tw.noah.utils.log4j2.model.slack.SlackAttachment;
import tw.noah.utils.log4j2.model.slack.SlackField;

public class SlackAppender extends AppenderSkeleton {

  /* param from log4j.xml */
  private String webHook;
  private URL webHookUrl;
  private int connectTimeoutSeconds;
  private int connectTimeoutMillis = 10_000;
  private String errorChannel; //channel
  private String warnChannel;  //channel
  private String infoChannel;  //channel
  private String appName;  // username
  private String environment;  // ex : production/staging/dev/lab/aws/idc....
  private int frequency;
  private long frequencyMillis;
  private String proxyUrl;
  private int proxyPort;

  /* Priority define */
  private final static SlackField errorFields = new SlackField("Priority", "High");
  private final SlackField warnFields = new SlackField("Priority", "Medium");
  private final SlackField infoFields = new SlackField("Priority", "Low");

  private long lastPostTime = 0;
  private List<SlackAttachment> atts = new ArrayList<>(); // pending object - Slack Attachment
  private boolean hasWaitThreads = false; // waiting thread flag


  @Override
  protected void append(LoggingEvent event) {

    if (!checkEntryConditions()) {
      return;
    }

    String channel;
    color color;
    icon icon;  // Variable pollution problem, but because of color recognition, the problem is no longer processed
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
    if (channel == null) {  // no channel defined , ignore message
      LogLog.warn("No slack channel provided for SlackAppender");
      return;
    }

    StringBuffer message = new StringBuffer();
    message.append(layout.format(event));
    if (layout.ignoresThrowable()) {
      String[] ss = event.getThrowableStrRep();
      if (ss != null) {
        for (String s : ss) {
          message.append(s).append("\n");
        }
      }
    }

    SlackAttachment att = new SlackAttachment();
    att.setColor(color);
    att.setPreText("(" + environment + ")");
    //    att.setAuthorName("Thread:" + event.getThreadId() + "\t\t" + event.getThreadName());
    //    att.setTitle(event.getMessage().toString());
    att.setText(message.toString());
    att.setSlackFields(new SlackField[]{slackField});
    att.setFooter("Thread:" + event.getThreadName() + "\t\t");
    att.setTimestamp(event.getTimeStamp() / 1000);

    atts.add(att);
    if (hasWaitThreads) {
      return;
    }

    // thread simulation async
    Runnable runnable = () -> {
      hasWaitThreads = true;

      long timeRange = (System.currentTimeMillis() - lastPostTime);
      if (frequencyMillis > timeRange) {  // inner frequency
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
      model.setSlackAttachments(atts.toArray(new SlackAttachment[atts.size()])); // old syntax
      //      model.setSlackAttachments(atts.stream().distinct().toArray(SlackAttachment[]::new)); // jdk 1.8 syntax

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

        if (proxyUrl != null && proxyUrl.trim().length() > 0 && proxyPort > 0) {
          Proxy proxy = new Proxy(Type.HTTP, new InetSocketAddress(proxyUrl, proxyPort));
          conn = (HttpURLConnection) webHookUrl.openConnection(proxy);
        } else {
          conn = (HttpURLConnection) webHookUrl.openConnection();
        }
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
          LogLog.error("Got a non-200 status: " + responseCode + "\nHttp-Body=" + conn.getResponseMessage() + "\nPayload=" + json);
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

  public void close() {
  }

  public boolean requiresLayout() {
    return true;
  }

  protected boolean checkEntryConditions() {
    if (webHook == null || webHook.trim().length() == 0) {
      LogLog.error("No webHook provided for SlackAppender");
      return false;
    }
    if (errorChannel == null || errorChannel.trim().length() == 0) {
      LogLog.error("No errorChannel provided for SlackAppender");
      return false;
    }
    if (appName == null || appName.trim().length() == 0) {
      LogLog.error("No appName provided for SlackAppender");
      return false;
    }
    if (environment == null || environment.trim().length() == 0) {
      LogLog.error("No appName provided for SlackAppender");
      return false;
    }

    return true;
  }


  public void setWebHook(String webHook) {
    try {
      this.webHookUrl = new URL(webHook);
    } catch (MalformedURLException e) {
      LogLog.error(e.getMessage(), e);
    }
    this.webHook = webHook;
  }

  public void setConnectTimeoutSeconds(int connectTimeoutSeconds) {
    this.connectTimeoutSeconds = connectTimeoutSeconds;
    this.connectTimeoutMillis = connectTimeoutSeconds * 1000;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public void setErrorChannel(String errorChannel) {
    this.errorChannel = errorChannel;
  }

  public void setWarnChannel(String warnChannel) {
    this.warnChannel = warnChannel;
  }

  public void setInfoChannel(String infoChannel) {
    this.infoChannel = infoChannel;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public void setFrequency(int frequency) {
    this.frequency = frequency;
    this.frequencyMillis = frequency * 1000;
  }

  public void setProxyUrl(String proxyUrl) {
    this.proxyUrl = proxyUrl;
  }

  public void setProxyPort(int proxyPort) {
    this.proxyPort = proxyPort;
  }
}
