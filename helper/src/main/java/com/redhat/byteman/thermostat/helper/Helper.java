package com.redhat.byteman.thermostat.helper;

import com.google.gson.Gson;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * User: alexkasko
 * Date: 5/18/15
 */
public class Helper {
    private static final Gson GSON = new Gson();

    private static final Queue<LogEntry> cache = new ConcurrentLinkedQueue<LogEntry>();

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

    public static void deactivated() throws IOException {
        System.out.println("Dumping data for Byteman logging helper...");
        File file = dumpToFile();
        System.out.println("Data dumped to file: [" + file.getAbsolutePath() + "]");
        System.out.println("Byteman logging helper deactivated");
    }

    // varargs are not supported by Byteman
    public boolean log(String message, List<Object> state) {
        long timestamp = System.currentTimeMillis();
        String msg = defaultString(message);
        StackTraceElement[] elems = Thread.currentThread().getStackTrace();
        String stackTrace = formatStackTrace(elems);
        String threadName = Thread.currentThread().getName();
        // todo: fixme
        String className = elems[elems.length - 1].getClassName();
        String methodName = elems[elems.length - 1].getMethodName();
        LinkedHashMap<String, Object> stateMap = toMap(state);
        LogEntry en = new LogEntry(timestamp, msg, stackTrace, threadName, className, methodName, stateMap);
        cache.add(en);
        return true;
    }

    private static LinkedHashMap<String, Object> toMap(List<Object> queryParams){
        LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
        if (0 != queryParams.size() % 2) throw new BmThHelperException("Invalid odd parameters count");
        for (int i = 0; i < queryParams.size(); i += 2) {
            Object objKey = queryParams.get(i);
            if (!(objKey instanceof String)) throw new BmThHelperException("Invalid key: [" + objKey + "]");
            map.put((String) objKey, queryParams.get(i+1));
        }
        return map;
    }

    private static File dumpToFile() throws IOException {
        File file = new File(System.currentTimeMillis() + ".json");
        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            Writer writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
            GSON.toJson(cache, writer);
            writer.close();
            return file;
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

    private static String formatStackTrace(StackTraceElement[] elems) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement el : elems) {
            printFrame(sb, el);
        }
        return sb.toString();
    }

    private static void printFrame(StringBuilder buffer, StackTraceElement frame)
    {
        buffer.append(frame.getClassName());
        buffer.append(".");
        buffer.append(frame.getMethodName());
        String fileName = frame.getFileName();
        if (fileName != null) {
            buffer.append("(");
            buffer.append(fileName);
            buffer.append(":");
            buffer.append(frame.getLineNumber());
            buffer.append(")");
        } else {
            buffer.append(" (Unknown Source)");
        }
        buffer.append('\n');
    }
}
