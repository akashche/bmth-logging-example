package com.redhat.byteman.thermostat.helper;

import java.util.LinkedHashMap;

/**
 * User: alexkasko
 * Date: 5/18/15
 */
public class ChartRecord {

    private long timestamp;
    private String marker;
    private LinkedHashMap<String, Object> data;

    // constructor for Gson
    private ChartRecord() {
    }

    ChartRecord(long timestamp, String marker, LinkedHashMap<String, Object> data) {
        this.timestamp = timestamp;
        this.marker = marker;
        this.data = data;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getMarker() {
        return marker;
    }

    public LinkedHashMap<String, Object> getData() {
        return data;
    }

    @Override
    public String toString() {
        return "ChartRecord{" +
                "timestamp=" + timestamp +
                ", marker='" + marker + '\'' +
                ", data=" + data +
                '}';
    }
}
