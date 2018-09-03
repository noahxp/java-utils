package tw.noah.utils.log4j2;

import java.io.Serializable;
import java.net.URL;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.config.plugins.validation.constraints.Required;

/**
 * reference : org.apache.logging.log4j.core.appender.HttpAppender
 */
@Plugin(name = "SlackAppender", category = Core.CATEGORY_NAME, elementType = Appender.ELEMENT_TYPE, printObject = true)
public class SlackAppender extends AbstractAppender {

  public static class Builder<B extends Builder<B>> extends AbstractAppender.Builder<B> implements
      org.apache.logging.log4j.core.util.Builder<SlackAppender> {

    @PluginBuilderAttribute
    @Required(message = "No webHook provided for SlackAppender")
    private URL webHook;

    @PluginBuilderAttribute
    @Required(message = "No appName provided for SlackAppender")
    private String appName;

    @PluginBuilderAttribute
    @Required(message = "No errorChannel provided for SlackAppender")
    private String errorChannel;

    @PluginBuilderAttribute
    @Required(message = "No warnChannel provided for SlackAppender")
    private String warnChannel;

    @PluginBuilderAttribute
    @Required(message = "No infoChannel provided for SlackAppender")
    private String infoChannel;

    @PluginBuilderAttribute
    @Required(message = "No environment provided for SlackAppender")
    private String environment;

    @PluginBuilderAttribute
    private int connectTimeoutSeconds = 3;

    @PluginBuilderAttribute
    private int frequency;


    @Override
    public SlackAppender build() {
      SlackManager slackManager = new SlackManager(getConfiguration(), getConfiguration().getLoggerContext(), getName(), webHook, errorChannel,
                                                   warnChannel, infoChannel, appName, environment, connectTimeoutSeconds, frequency);

      return new SlackAppender(getName(), getFilter(), getLayout(), slackManager);
    }

  }

  protected SlackAppender(String name, Filter filter, Layout<? extends Serializable> layout, SlackManager manager) {
    super(name, filter, layout);
    this.manager = manager;
  }

  private SlackManager manager;

  /**
   * @return a builder for a SlackAppender.
   */
  @PluginBuilderFactory
  public static <B extends Builder<B>> B newBuilder() {
    return new Builder<B>().asBuilder();
  }

  @Override
  public void append(LogEvent event) {
    manager.sendMessage(getLayout(), event);
  }

}
