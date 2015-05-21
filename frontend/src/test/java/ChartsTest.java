import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.redhat.byteman.thermostat.frontend.FrontendException;
import com.redhat.byteman.thermostat.helper.ChartRecord;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.LineRenderer3D;
import org.jfree.chart.renderer.category.StackedBarRenderer3D;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.junit.Test;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * User: alexkasko
 * Date: 5/21/15
 */
public class ChartsTest {
    private static final String EMPTY = "";

    private static final Gson GSON = new Gson();
    private static final Type CHART_RECORD_LIST_TYPE = new TypeToken<ArrayList<ChartRecord>>(){}.getType();
    private static final List<ChartRecord> DATA = readData();

    @Test
    public void testPlainValuesOverTimestamps() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1; int i = 0; long start = DATA.get(0).getTimestamp();
        for (ChartRecord cr : DATA) {
            if (!"reportRenderTime".equals(cr.getMarker())) continue;
            Object valObj = cr.getData().get("value");
            if (null !=  valObj && valObj instanceof Number) {
                Number valNum = (Number) valObj;
                long val = valNum.longValue();
                if (val < min) min = val;
                if (val > max) max = val;
                ds.addValue(val, "", Long.toString(cr.getTimestamp() - start));
            }
            if (i++ > 20) break;
        }
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Render time ms");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("PlainValuesOverTimestamps (elapsed time ms)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "PlainValuesOverTimestamps");
    }

    @Test
    public void testPlainTypedValuesOverTimestampsWithCPU() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1; int i = 0; long start = DATA.get(0).getTimestamp();
        for (ChartRecord cr : DATA) {
            if (!"reportRenderTime".equals(cr.getMarker())) continue;
            Object valObj = cr.getData().get("value");
            if (null !=  valObj && valObj instanceof Number) {
                Number valNum = (Number) valObj;
                long val = valNum.longValue();
                if (val < min) min = val;
                if (val > max) max = val;
                ds.addValue(val, cr.getData().get("reportId").toString(), Long.toString(cr.getTimestamp() - start));
            }
            if (i++ > 20) break;
        }
        JFreeChart chart = ChartFactory.createStackedBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Render time ms");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("PlainTypedValuesOverTimestampsWithCPU (elapsed time ms)");
        BarRenderer3D barrenderer = new StackedBarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB669900"));
        barrenderer.setSeriesPaint(1, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        // line
        Random random = new Random(42);
        DefaultCategoryDataset dsline = new DefaultCategoryDataset();
        int j = 0;
        for (ChartRecord cr : DATA) {
            if (!"reportRenderTime".equals(cr.getMarker())) continue;
            dsline.addValue(60 + random.nextInt(30), "", Long.toString(cr.getTimestamp() - start));
            if (j++ > 20) break;
        }
        NumberAxis rightYAxis = new NumberAxis("CPU load in percent (not real)");
        colorAxis(rightYAxis);
        rightYAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rightYAxis.setRange(new Range(0, 100));
//        rightYAxis.setTickLabelFont(conf.getTickLabelFont());
        plot.setDataset(1, dsline);
        plot.setRangeAxis(1, rightYAxis);
        plot.mapDatasetToRangeAxis(1, 1);
        LineRenderer3D linerenderer = new LineRenderer3D();
        linerenderer.setSeriesPaint(0, toColor("#BB0099CC"));
        linerenderer.setXOffset(16.0d);
        linerenderer.setYOffset(12.0d);
        plot.setRenderer(1, linerenderer);
        plot.setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        chartToSvg(chart, 1024, 480, "PlainTypedValuesOverTimestampsWithCPU");
    }

    @Test
    public void testDeltaValuesOverTimestamps() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1; int i = 0; long start = DATA.get(0).getTimestamp();
        for (ChartRecord cr : DATA) {
            if (!"reportRenderTime".equals(cr.getMarker())) continue;
            Number valNumCur = (Number) cr.getData().get("value");
            long valCur = valNumCur.longValue();
            Number valNumNext = (Number) DATA.get(i + 1).getData().get("value");
            long valNext = valNumNext.longValue();
            long val = valNext - valCur;
            if (val < min) min = val;
            if (val > max) max = val;
            ds.addValue(val, "", Long.toString(cr.getTimestamp() - start));
            if (i++ > 20) break;
        }
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Render time delta ms");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("DeltaValuesOverTimestamps (elapsed time ms)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB433D8B"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "DeltaValuesOverTimestamps");
    }

    @Test
    public void testDividedValuesOverTimestamps() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        double min = 0; double max = 0; int i = 0; long start = DATA.get(0).getTimestamp();
        for (ChartRecord cr : DATA) {
            if (!"reportRenderTime".equals(cr.getMarker())) continue;
            if (i > 0) {
                long timeDelta = cr.getTimestamp() - DATA.get(i - 1).getTimestamp();
                Object valObj = cr.getData().get("value");
                Number valNum = (Number) valObj;
                double val = valNum.doubleValue()/timeDelta;
                if (val < min) min = val;
                if (val > max) max = val;
                ds.addValue(val, "", Long.toString(cr.getTimestamp() - start));
            } else {
                ds.addValue(0, "", Long.toString(0));
            }
            if (i++ > 20) break;
        }
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Render time divided by time interval");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("DividedValuesOverTimestamps (elapsed time ms)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB669900"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "DividedValuesOverTimestamps");
    }

    @Test
    public void testAccumulatedValuesOverTimestampIntervals() {
        ArrayList<ChartRecord> renderTimes = new ArrayList<ChartRecord>();
        for (ChartRecord cr : DATA) {
            if ("reportRenderTime".equals(cr.getMarker())) {
                renderTimes.add(cr);
            }
        }
        long start = renderTimes.get(0).getTimestamp();
        long end = renderTimes.get(renderTimes.size() - 1).getTimestamp();
        long[] accumulated = new long[(int)((end - start)/10000) + 1];
        for (ChartRecord cr : renderTimes) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) ((cr.getTimestamp() - start)/10000);
            accumulated[idx] += val;
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1; int i = 0;
        for (long val : accumulated) {
            if (val < min) min = val;
            if (val > max) max = val;
            ds.addValue(val, "", Long.toString(i*10) + "-" + Long.toString((i+1)*10));
            i = i + 1;
        }
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Time in ms spent in rendering during the 10 seconds intervals");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("AccumulatedValuesOverTimestampIntervals (10 second intervals)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BBA52A2A"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "AccumulatedValuesOverTimestampIntervals");
    }

    @Test
    public void testAccumulatedSideBySideValuesOverTimestampIntervals() {
        ArrayList<ChartRecord> renderTimes1 = new ArrayList<ChartRecord>();
        ArrayList<ChartRecord> renderTimes2 = new ArrayList<ChartRecord>();
        long longest1 = 0;
        long longest2 = 0;
        for (ChartRecord cr : DATA) {
            if ("reportRenderTime".equals(cr.getMarker())) {
                Number valNum = (Number) cr.getData().get("value");
                long val = valNum.longValue();
                if (552275 == ((Number) cr.getData().get("reportId")).intValue()) {
                    renderTimes1.add(cr);
                    if (val > longest1) longest1 = val;
                } else {
                    renderTimes2.add(cr);
                    if (val > longest2) longest2 = val;
                }
            }
        }
        long start = Math.min(renderTimes1.get(0).getTimestamp(), renderTimes2.get(0).getTimestamp());
        long end = Math.max(renderTimes1.get(renderTimes1.size() - 1).getTimestamp(), renderTimes2.get(renderTimes2.size() - 1).getTimestamp());
        int len = (int)((end - start)/10000) + 1;
        long[] accumulated1 = new long[len];
        long[] accumulated2 = new long[len];
        for (ChartRecord cr : renderTimes1) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) ((cr.getTimestamp() - start)/10000);
            accumulated1[idx] += val;
        }
        for (ChartRecord cr : renderTimes2) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) ((cr.getTimestamp() - start)/10000);
            accumulated2[idx] += val;
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long max1 = 0; int i1 = 0;
        for (long val : accumulated1) {
            if (val > max1) max1 = val;
            ds.addValue(val, "1", Long.toString(i1*10) + "-" + Long.toString((i1+1)*10));
            i1 = i1 + 1;
        }
        long max2 = 0; int i2 = 0;
        for (long val : accumulated2) {
            if (val > max2) max2 = val;
            ds.addValue(val, "2", Long.toString(i2*10) + "-" + Long.toString((i2+1)*10));
            i2 = i2 + 1;
        }
        long min = 0; long max = Math.max(max1, max2);
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Time in ms spent in rendering during the 10 seconds intervals");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("AccumulatedSideBySideValuesOverTimestampIntervals (10 second intervals)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB669900"));
        barrenderer.setSeriesPaint(1, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "AccumulatedSideBySideValuesOverTimestampIntervals");
    }

    @Test
    public void testBucketedCountDistribution() {
        ArrayList<ChartRecord> renderTimes = new ArrayList<ChartRecord>();
        long longest = 0;
        for (ChartRecord cr : DATA) {
            if ("reportRenderTime".equals(cr.getMarker())) {
                renderTimes.add(cr);
                Number valNum = (Number) cr.getData().get("value");
                long val = valNum.longValue();
                if (val > longest) longest = val;
            }
        }
        long[] counted = new long[(int)(longest/1000)+1];
        for (ChartRecord cr : renderTimes) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) (val/1000);
            counted[idx] += 1;
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1; int i = 0;
        for (long val : counted) {
            if (val < min) min = val;
            if (val > max) max = val;
            ds.addValue(val, "", Long.toString(i) + "-" + Long.toString(i+1));
            i = i + 1;
        }
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Number of reports rendered in 1 sec intervals");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
//        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("BucketedCountDistribution (1 sec intervals of rendering time)");
        BarRenderer3D barrenderer = new BarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB0099CC"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "BucketedCountDistribution");
    }

    @Test
    public void testBucketedCountStackedDistribution() {
        ArrayList<ChartRecord> renderTimes1 = new ArrayList<ChartRecord>();
        ArrayList<ChartRecord> renderTimes2 = new ArrayList<ChartRecord>();
        long longest1 = 0;
        long longest2 = 0;
        for (ChartRecord cr : DATA) {
            if ("reportRenderTime".equals(cr.getMarker())) {
                Number valNum = (Number) cr.getData().get("value");
                long val = valNum.longValue();
                if (552275 == ((Number) cr.getData().get("reportId")).intValue()) {
                    renderTimes1.add(cr);
                    if (val > longest1) longest1 = val;
                } else {
                    renderTimes2.add(cr);
                    if (val > longest2) longest2 = val;
                }
            }
        }
        int len = (int) (Math.max(longest1, longest2) / 1000) + 1;
        long[] counted1 = new long[len];
        for (ChartRecord cr : renderTimes1) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) (val/1000);
            counted1[idx] += 1;
        }
        long[] counted2 = new long[len];
        for (ChartRecord cr : renderTimes2) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) (val/1000);
            counted2[idx] += 1;
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long max1 = 1; int i1 = 0;
        for (long val : counted1) {
            if (val > max1) max1 = val;
            ds.addValue(val, "1", Long.toString(i1) + "-" + Long.toString(i1 + 1));
            i1 = i1 + 1;
        }
        long max2 = 1; int i2 = 0;
        for (long val : counted2) {
            if (val > max2) max2 = val;
            ds.addValue(val, "2", Long.toString(i2) + "-" + Long.toString(i2 + 1));
            i2 = i2 + 1;
        }
        long min = 0; long max = max1 + max2;
        JFreeChart chart = ChartFactory.createStackedBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Number of reports of different type rendered in 1 sec intervals");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
//        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("BucketedCountStackedDistribution (1 sec intervals of rendering time)");
        BarRenderer3D barrenderer = new StackedBarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB669900"));
        barrenderer.setSeriesPaint(1, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "BucketedCountStackedDistribution");
    }

    @Test
    public void testBucketedAccumulatedCountStackedDistribution() {
        ArrayList<ChartRecord> renderTimes1 = new ArrayList<ChartRecord>();
        ArrayList<ChartRecord> renderTimes2 = new ArrayList<ChartRecord>();
        long longest1 = 0;
        long longest2 = 0;
        for (ChartRecord cr : DATA) {
            if ("reportRenderTime".equals(cr.getMarker())) {
                Number valNum = (Number) cr.getData().get("value");
                long val = valNum.longValue();
                if (552275 == ((Number) cr.getData().get("reportId")).intValue()) {
                    renderTimes1.add(cr);
                    if (val > longest1) longest1 = val;
                } else {
                    renderTimes2.add(cr);
                    if (val > longest2) longest2 = val;
                }
            }
        }
        int len = (int) (Math.max(longest1, longest2) / 1000) + 1;
        long[] counted1 = new long[len];
        for (ChartRecord cr : renderTimes1) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) (val/1000);
            for (int i = idx; i < counted1.length; i++) {
                counted1[i] += 1;
            }
        }
        long[] counted2 = new long[len];
        for (ChartRecord cr : renderTimes2) {
            Number valNum = (Number) cr.getData().get("value");
            long val = valNum.longValue();
            int idx = (int) (val/1000);
            for (int i = idx; i < counted2.length; i++) {
                counted2[i] += 1;
            }
        }
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long max1 = 1; int i1 = 0;
        for (long val : counted1) {
            if (val > max1) max1 = val;
            ds.addValue(val, "1", Long.toString(i1) + "-" + Long.toString(i1 + 1));
            i1 = i1 + 1;
        }
        long max2 = 1; int i2 = 0;
        for (long val : counted2) {
            if (val > max2) max2 = val;
            ds.addValue(val, "2", Long.toString(i2) + "-" + Long.toString(i2 + 1));
            i2 = i2 + 1;
        }
        long min = 0; long max = max1 + max2;
        JFreeChart chart = ChartFactory.createStackedBarChart(EMPTY, EMPTY, EMPTY, ds, PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(min, max * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        plot.getRangeAxis().setLabel("Number of reports rendered under time points in 1 sec intervals");
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
//          plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.01d);
        plot.getDomainAxis().setUpperMargin(0.01d);
        plot.getDomainAxis().setLabel("BucketedAccumulatedCountStackedDistribution (1 sec intervals of rendering time)");
        BarRenderer3D barrenderer = new StackedBarRenderer3D(16.0d, 12.0d);
        barrenderer.setSeriesPaint(0, toColor("#BB669900"));
        barrenderer.setSeriesPaint(1, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, 1024, 480, "BucketedAccumulatedCountStackedDistribution");
    }

    private static List<ChartRecord> readData() {
        InputStream is = null;
        try {
            is = ChartsTest.class.getResourceAsStream("/1432223930763.json");
            Reader reader = new InputStreamReader(is, Charset.forName("UTF-8"));
            return GSON.fromJson(reader, CHART_RECORD_LIST_TYPE);
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

    private static void colorAxis(Axis ax) {
        ax.setAxisLinePaint(toColor("#FFAAAAAA"));
        ax.setTickMarkPaint(toColor("#FFAAAAAA"));
        ax.setTickLabelPaint(toColor("#FF222222"));
    }

    private static void chartToSvg(JFreeChart chart, int width, int height, String out) {
        Writer writer = null;
        try {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new Dimension(width, height));
            chart.draw(svgGenerator, new Rectangle(width, height));
            OutputStream os = new FileOutputStream(out + ".svg");
            writer = new BufferedWriter(new OutputStreamWriter(os, Charset.forName("UTF-8")));
            svgGenerator.stream(writer);
        } catch (Exception e) {
            throw new FrontendException("Error rendering SVG with width: [" + width + "]," +
                    " height: [" + height + "]", e);
        } finally {
            closeQuietly(writer);
        }
    }

    private static Color toColor(String rgba) {
        String unprefixed = rgba.startsWith("#") ? rgba.substring(1) : rgba;
        if (!(6 == unprefixed.length() || 8 == unprefixed.length()))
            throw new FrontendException("Invalid color: [" + rgba + "]");
        int hex = (int) Long.parseLong(unprefixed, 16);
        if (6 == unprefixed.length()) {
            return new Color(hex);
        }
        return new Color(hex, true);
    }
}
