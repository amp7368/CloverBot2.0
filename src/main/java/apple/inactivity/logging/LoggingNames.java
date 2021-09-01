package apple.inactivity.logging;

import apple.utilities.logging.AppleLoggerName;

public enum LoggingNames implements AppleLoggerName {
    ALL("all"),
    CLOVER("clover"),
    WYNN("wynn"),
    DISCORD("discord"),
    DAEMON("daemon"),
    FAILURE("failure");

    private String name;

    LoggingNames(String name) {
        this.name = name;
    }

    @Override
    public String getLoggerName() {
        return name;
    }
}
