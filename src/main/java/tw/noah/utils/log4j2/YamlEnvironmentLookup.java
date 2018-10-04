package tw.noah.utils.log4j2;

import java.io.IOException;
import java.util.Properties;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.lookup.AbstractLookup;
import org.apache.logging.log4j.core.lookup.StrLookup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import tw.noah.utils.yaml.YamlUtils;

@Plugin(name = "yaml", category = StrLookup.CATEGORY)
public class YamlEnvironmentLookup extends AbstractLookup {

  private Properties props;

  public YamlEnvironmentLookup() {
    Resource res;
    String configPath = System.getProperty("application.yml.path");

    if (!StringUtils.isEmpty(configPath)) { // config setting found
      if (configPath.startsWith("classpath:")) {  // config path is a classpath
        res = new ClassPathResource(configPath.replaceFirst("classpath:", ""));
      } else {  // config path maybe is a file-path
        res = new FileSystemResource(configPath);
      }
    } else {  // use a default path
      res = new ClassPathResource("application.yml");
      String c = ((ClassPathResource) res).getPath();
      if (!res.exists()) {
        res = new ClassPathResource("application.yaml");
      }
    }
    if (!res.exists()) {
      throw new RuntimeException("config file not found : " + configPath);
    }

    try {
      props = YamlUtils.yamlToProperties(res.getInputStream());
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

  }

  @Override
  public String lookup(LogEvent logEvent, String key) {
    return props.getProperty(key);
  }
}