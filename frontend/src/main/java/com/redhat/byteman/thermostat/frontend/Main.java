package com.redhat.byteman.thermostat.frontend;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redhat.byteman.thermostat.helper.ChartRecord;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * User: alexkasko
 * Date: 5/19/15
 */
public class Main {
    private static final Gson GSON = new Gson();
    public static final Type LOG_ENTRY_LIST_TYPE = new TypeToken<ArrayList<ChartRecord>>() {}.getType();

    public static void main(String[] args) throws FileNotFoundException {
        if (1 != args.length) throw new RuntimeException(
                "Invalid arguments: JSON file as first and only argument is required");
        List<ChartRecord> list = readJson(args[0]);
        Charter charter = new Charter();
        charter.createChart(list, "time", new File("time_chart.svg"));
        charter.createChart(list, "compileCount", new File("compile_count_chart.svg"));
    }

    private static List<ChartRecord> readJson(String file) throws FileNotFoundException {
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
            return GSON.fromJson(reader, LOG_ENTRY_LIST_TYPE);
        } finally {
            closeQuietly(is);
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
}
