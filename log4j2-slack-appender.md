## log4j2 slack appender

log4j2.xml example(1) : default on info level , special class path on warn level , and error level send to slack

    <?xml version="1.0" encoding="UTF-8"?>
    <Configuration packages="tw.noah.utils.log4j2">
      <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
          <PatternLayout pattern="%d{HH:mm:ss.SSS} %-5level [%logger{1.}.%t] - %msg%n"/>
        </Console>
 
        <!-- with -D VM Options , will use sys: variables -->    
        <!-- -Dslack.webHook=https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX -->
        <!--<SlackAppender name="slack" webhook="${sys:slack.webHook}"-->
 
        <SlackAppender name="slack" webhook="https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX"
          appName="SlackMainApp"
          errorChannel="#channel1"
          warnChannel="#channel2"
          infoChannel="#channel3"
          environment="dev"
          frequency="3"
        >
          <ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY" />
          <PatternLayout pattern="%d{HH:mm:ss.SSS} %-5level [%logger{1.}.%t] - %msg%n"/>
        </SlackAppender>
      </Appenders>
      <Loggers>
    
        <Logger name="test.main" level="warn" additivity="true">
          <AppenderRef ref="slack"/>
        </Logger>
    
        <Root level="info">
          <AppenderRef ref="Console"/>
        </Root>
      </Loggers>
    </Configuration>
    
log4j2.xml example(2) : default on info level , and error level send to slack

    <?xml version="1.0" encoding="UTF-8"?>
    <Configuration packages="tw.noah.utils.log4j2">
      <Appenders>
        <Console name="Console" target="SYSTEM_OUT">
          <PatternLayout pattern="%d{HH:mm:ss.SSS} %-5level [%logger{1.}.%t] - %msg%n"/>
        </Console>
    
        <!-- -Dslack.webHook=https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX -->
        <!--<SlackAppender name="slack" webhook="${sys:slack.webHook}"-->
    
        <SlackAppender name="slack" webhook="https://hooks.slack.com/services/XXXXXXXX/XXXXXXXX/XXXXXXXXXXXX"
          appName="SlackMainApp"
          errorChannel="#channel1"
          warnChannel="#channel2"
          infoChannel="#channel3"
          environment="dev"
          frequency="3"
        >
          <ThresholdFilter level="error" onMatch="ACCEPT" onMismatch="DENY" />
          <PatternLayout pattern="%d{HH:mm:ss.SSS} %-5level [%logger{1.}.%t] - %msg%n"/>
        </SlackAppender>
      </Appenders>
      <Loggers>
    
        <Root level="info">
          <AppenderRef ref="Console"/>
          <AppenderRef ref="slack"/>
        </Root>
      </Loggers>
    </Configuration>


- slack appender variables :
  - Required
    - webhook : The Slack incoming webHook url
    - appName : The Application Name , show on message title.
    - errorChannel : The Slack channel where an error occurred.
    - warnChannel : The Slack channel where a warn occurred.
    - infoChannel : The Slack channel where a info occurred.
    - environment : the environment identification word.
  - Optional
    - connectTimeoutSeconds : Send to slack timeout seconds.
    - frequency : The frequency sent to slack per second
    

- result example :

  ![](doc/log4j2-slack-example.png)