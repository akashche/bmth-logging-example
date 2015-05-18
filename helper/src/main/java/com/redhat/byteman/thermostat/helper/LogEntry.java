package com.redhat.byteman.thermostat.helper;

import java.util.LinkedHashMap;

/**
 * User: alexkasko
 * Date: 5/18/15
 */
public class LogEntry {

    private long timestamp;
    private String message;
    private String stackTrace;
    private String threadName;
    private String className;
    private String methodName;
    private LinkedHashMap<String, Object> state;

    // constructor for Gson
    private LogEntry() {
    }

    LogEntry(long timestamp, String message, String stackTrace, String threadName, String className,
                    String methodName, LinkedHashMap<String, Object> state) {
        this.timestamp = timestamp;
        this.message = message;
        this.stackTrace = stackTrace;
        this.threadName = threadName;
        this.className = className;
        this.methodName = methodName;
        this.state = state;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public String getThreadName() {
        return threadName;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public LinkedHashMap<String, Object> getState() {
        return state;
    }

    @Override
    public String toString() {
        return "LogEntry{" +
                "timestamp=" + timestamp +
                ", message='" + message + '\'' +
                ", stackTrace='" + stackTrace + '\'' +
                ", threadName='" + threadName + '\'' +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", state=" + state +
                '}';
    }
}
