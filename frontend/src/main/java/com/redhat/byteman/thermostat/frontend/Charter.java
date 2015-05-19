package com.redhat.byteman.thermostat.frontend;

import com.redhat.byteman.thermostat.helper.LogEntry;
import org.apache.batik.dom.GenericDOMImplementation;
import org.apache.batik.svggen.SVGGraphics2D;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.Axis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.data.Range;
import org.jfree.data.category.DefaultCategoryDataset;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;

import java.awt.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

/**
 * User: alexkasko
 * Date: 5/19/15
 */
public class Charter {
    private static final String EMPTY = "";

    public void createChart(List<LogEntry> list, String key, File out) {
        ChartDatasetWithBounds barset = convertToDataset(list, key);
        JFreeChart chart = ChartFactory.createBarChart(EMPTY, EMPTY, EMPTY, barset.getDataset(), PlotOrientation.VERTICAL, false, true, false);
        CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(toColor("#FFFFFFFF"));
        plot.setBackgroundImageAlpha(0.0f);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(toColor("#FFAAAAAA"));
        plot.getRangeAxis().setRange(new Range(barset.getMin(), barset.getMax() * 1.1));
        plot.getRangeAxis().setStandardTickUnits(NumberAxis.createIntegerTickUnits());
//        plot.getRangeAxis().setTickLabelFont(conf.getTickLabelFont());
        colorAxis(plot.getRangeAxis());
        colorAxis(plot.getDomainAxis());
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI * 0.12d));
        plot.getDomainAxis().setLowerMargin(0.05d);
        plot.getDomainAxis().setUpperMargin(0.01d);
//        plot.getDomainAxis().setTickLabelFont(conf.getTickLabelFont());

        BarRenderer3D barrenderer = new BarRenderer3D(24.0d, 16.0d);
        barrenderer.setSeriesPaint(0, toColor("#BBFF8800"));
        barrenderer.setWallPaint(toColor("#FFEEEEEE"));
        barrenderer.setBaseItemLabelsVisible(false);
        barrenderer.setShadowVisible(false);
        barrenderer.setItemMargin(0.0d);
        plot.setRenderer(barrenderer);
        plot.setOutlineVisible(false);
        chartToSvg(chart, (int) (800 * 1.5d), (int) (600 * 1.5d), out);
    }

    private static void colorAxis(Axis ax) {
        ax.setAxisLinePaint(toColor("#FFAAAAAA"));
        ax.setTickMarkPaint(toColor("#FFAAAAAA"));
        ax.setTickLabelPaint(toColor("#FF222222"));
    }

    private static ChartDatasetWithBounds convertToDataset(List<LogEntry> list, String key) {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        long min = 0; long max = 1;
        for (LogEntry le : list) {
            Object valObj = le.getState().get(key);
            if (null !=  valObj && valObj instanceof Number) {
                Number valNum = (Number) valObj;
                long val = valNum.longValue();
                if (val < min) min = val;
                if (val > max) max = val;
                ds.addValue(val, "", Long.toString(le.getTimestamp()));
            }
        }
        return new ChartDatasetWithBounds(ds, min, max);
    }

    private static void chartToSvg(JFreeChart chart, int width, int height, File out) {
        Writer writer = null;
        try {
            DOMImplementation domImpl = GenericDOMImplementation.getDOMImplementation();
            Document document = domImpl.createDocument(null, "svg", null);
            SVGGraphics2D svgGenerator = new SVGGraphics2D(document);
            svgGenerator.setSVGCanvasSize(new Dimension(width, height));
            chart.draw(svgGenerator, new Rectangle(width, height));
            OutputStream os = new FileOutputStream(out);
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

    private static class ChartDatasetWithBounds {
        private final DefaultCategoryDataset dataset;
        private final double min;
        private final double max;

        ChartDatasetWithBounds(DefaultCategoryDataset dataset, double min, double max) {
            this.dataset = dataset;
            this.min = min;
            this.max = max;
        }

        public DefaultCategoryDataset getDataset() {
            return dataset;
        }

        public double getMin() {
            return min;
        }

        public double getMax() {
            return max;
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
