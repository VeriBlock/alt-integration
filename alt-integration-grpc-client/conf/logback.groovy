import ch.qos.logback.classic.encoder.PatternLayoutEncoder

import static ch.qos.logback.classic.Level.INFO

def logRootPath = System.getenv('ALT_CLIENT_LOG_PATH') ?: ''

appender("STDOUT", ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        pattern = "%date{YYYY-MM-dd HH:mm:ss.SSSXX} [%thread] %-5level %logger{36} - %msg%n"
    }
}
appender("FILE", RollingFileAppender) {
    file = "${logRootPath}alt-client.log"
    rollingPolicy(SizeAndTimeBasedRollingPolicy) {
        fileNamePattern = "${logRootPath}alt-client.%d{yyyy-MM-dd}.%i.log"
        maxHistory = 30
        maxFileSize = "10MB"
        totalSizeCap = "1GB"
    }
    encoder(PatternLayoutEncoder) {
        pattern = "%date{YYYY-MM-dd HH:mm:ss.SSSXX} %level [%thread] %logger{10} [%file:%line] %msg%n"
    }
}

logger("org.veriblock", INFO, ["STDOUT"])

root(INFO, ["FILE"])