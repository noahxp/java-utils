package tw.noah.utils.yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.yaml.snakeyaml.Yaml;

public class YamlUtils {

  /**
   * @param propMap 結果 properties map 物件
   * @param yamlMap 來源 yaml 內容
   * @param parentKey 上層 yaml key
   * @return props
   */
  private static Map<String, Object> convertYaml2Map(Map<String, Object> propMap, Map<String, Object> yamlMap, String parentKey) {
    if (propMap == null) {
      throw new RuntimeException("Props can't been null.");
    }

    yamlMap.forEach((k, v) -> {
      String key = parentKey == null ? k : parentKey + "." + k;
      if (v instanceof HashMap) {
        convertYaml2Map(propMap, (Map<String, Object>) v, key);
      } else {
        if (v != null) {
          propMap.put(key, v);
        }
      }

    });

    return propMap;
  }


  /**
   * read yaml profile , support spring profiles.
   * @param is yaml file input-stream object
   * @return all yaml properties
   */
  public static Properties yamlToProperties(InputStream is) {

    Properties propsRet = new Properties();
    List<Map<String, Object>> propsList = new ArrayList<>();

    new Yaml().loadAll(is).forEach((v) -> {
      Map<String, Object> propMap = new HashMap<>();
      propMap = convertYaml2Map(propMap, (Map<String, Object>) v, null);
      propsList.add(propMap);
    });

    List<String> profileNames = new ArrayList<>();

    if (propsList.size() > 1) { // multi profile check the spring.profiles.active
      propsList.forEach((p) -> {
        if (p.containsKey("spring.profiles.active")) {  // 第一組
          if (p.get("spring.profiles.active").getClass() == ArrayList.class) { // 多個 profile
            profileNames.addAll((ArrayList) p.get("spring.profiles.active"));
          } else {
            profileNames.add(p.get("spring.profiles.active").toString());
          }
        }
      });
    }

    propsList.forEach((p) -> {
      // single yaml doc , main profile , and the active profile
      if (profileNames.size() == 0 || p.containsKey("spring.profiles.active") || (p.containsKey("spring.profiles") && profileNames.contains(p.get("spring.profiles").toString()))) {
        p.forEach((kk, vv) -> {
          propsRet.setProperty(kk, "" + vv);
        });
      }
    });

    return propsRet;
  }
}
