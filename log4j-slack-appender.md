## log4j slack appender

[log4j.xml](src/main/resources/log4j.xml) example(1) : default on info level , special class path on error level , and error level send to slack at the same time

    <?xml version="1.0" encoding="UTF-8"?>
    <!DOCTYPE log4j:configuration SYSTEM "log4j.dtd">
    
    <log4j:configuration xmlns:log4j="http://jakarta.apache.org/log4j/" debug="false">
    
      <appender name="CONSOLE" class="org.apache.log4j.ConsoleAppender">
        <param name="Target" value="System.out" />
    
        <layout class="org.apache.log4j.PatternLayout">
          <param name="ConversionPattern" value="%d{ABSOLUTE} %-5p [%c] %m%n" />
        </layout>
      </appender>
    
      <appender name="SLACK" class="tw.noah.utils.log4j.SlackAppender">
        <param name="webHook" value="https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX" />
        <param name="appName" value="SlackMainApp" />
        <param name="errorChannel" value="#channel1" />
        <param name="warnChannel" value="#channel2" />
        <param name="infoChannel" value="#channle3" />
        <param name="environment" value="dev" />
        <param name="frequency" value="3" />
    
        <param name="Threshold" value="ERROR"/>
    
        <layout class="org.apache.log4j.PatternLayout">
          <param name="ConversionPattern" value="%d{ABSOLUTE} %-5p [%c] %m%n" />
        </layout>
      </appender>
    
    
      <logger name="test.main">
        <level value="error" />
      </logger>
    
      <root>
        <level value="info" />
        <appender-ref ref="CONSOLE" />
        <appender-ref ref="SLACK" />
      </root>
    </log4j:configuration>


- required library:
  - jackson-core-2.x.x.jar
  - jackson-annotations-2.x.x.jar
  - jackson-databind-2.x.x.jar
  - log4j-1.2.x.jar
  
- slack appender variables :
  - Required
    - webHook : The Slack incoming webHook url
    - appName : The Application Name , show on message title.
    - errorChannel : The Slack channel where an error occurred.
    - environment : the environment identification word.
  - Optional
    - warnChannel : The Slack channel where a warn occurred.
    - infoChannel : The Slack channel where a info occurred.
    - connectTimeoutSeconds : Send to slack timeout seconds.
    - frequency : The frequency sent to slack per second
    - proxyUrl : The proxy domain or ip , if need to use proxy to connect
    - proxyPort : The proxy port , if need to use proxy to connect
    
 
 ## log4j.xml use variables (EL)
 
 - example :
 

      System.setProperty("slack.webHook","https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX");
      DOMConfigurator.configure(MainApp.class.getResource("/").getPath().substring(1) + "/log4j.xml");
   

 on log4j.xml

    
      <param name="webHook" value="${slack.webHook}" />
    
      