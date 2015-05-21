package com.redhat.byteman.thermostat.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.jboss.byteman.rule.Rule;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: alexkasko
 * Date: 5/18/15
 */
public class Helper extends org.jboss.byteman.rule.helper.Helper {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Queue<ChartRecord> CACHE = new ConcurrentLinkedQueue<ChartRecord>();
    private static final String JVM_ID = ManagementFactory.getRuntimeMXBean().getName();

    protected Helper(Rule rule) {
        super(rule);
    }

    public static void activated() {
        System.out.println("Byteman logging helper activated");
        // todo: check when deactivated actually called
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    deactivated();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }));
    }

    public static void deactivated() {
        System.out.println("Dumping data for Byteman logging helper...");
        File file = dumpToFile();
        System.out.println("Data dumped to file: [" + file.getAbsolutePath() + "]");
        System.out.println("Byteman logging helper deactivated");
    }

    // todo: should be optimized to not create intermediate Object[]
    public boolean addChartRecord(String markerNullable, String key, Object value) {
        return addChartRecord(markerNullable, new Object[]{key, value});
    }

    public boolean addChartRecord(String markerNullable, String key1, Object value1, String key2, Object value2) {
        return addChartRecord(markerNullable, new Object[]{key1, value1, key2, value2});
    }

    public boolean addChartRecord(String markerNullable, String key1, Object value1, String key2, Object value2,
                                  String key3, Object value3) {
        return addChartRecord(markerNullable, new Object[]{key1, value1, key2, value2, key3, value3});
    }

    public boolean addChartRecord(String markerNullable, Object... dataArray) {
        long timestamp = System.currentTimeMillis();
        String marker = defaultString(markerNullable);
        LinkedHashMap<String, Object> data = toMap(dataArray);
        data.put("stackTrace", formatStack());
        data.put("methodName", formatStack(1));
        data.put("threadName", Thread.currentThread().getName());
        data.put("jvmId", JVM_ID);
        ChartRecord en = new ChartRecord(timestamp, marker, data);
        CACHE.add(en);
        return true;
    }

    private static LinkedHashMap<String, Object> toMap(Object[] dataArray){
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        if (0 != dataArray.length % 2) throw new BmThHelperException("Invalid odd elements count");
        for (int i = 0; i < dataArray.length; i += 2) {
            Object objKey = dataArray[i];
            if (!(objKey instanceof String)) throw new BmThHelperException("Invalid key: [" + objKey + "]");
            map.put((String) objKey, dataArray[i + 1]);
        }
        return map;
    }

    private static File dumpToFile() {
        File file = new File(System.currentTimeMillis() + ".json");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Writer writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
            GSON.toJson(CACHE, writer);
            writer.close();
            return file;
        } catch (Exception e) {
            // deactivated method cannot throw
            e.printStackTrace();
            return null;
        } finally {
            closeQuietly(os);
        }
    }

    private static void closeQuietly(Closeable closeable) {
        if (null != closeable) {
            try {
                closeable.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String defaultString(String st) {
        return null != st ? st : "";
    }

}
