package graph;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;
import org.jfree.ui.*;
import java.util.ArrayList;


/**
 * Created by thomas on 09/12/16.
 */
public class Graph extends ApplicationFrame {

    /* TO LAUNCH
    final XYSeriesDemo demo = new XYSeriesDemo("XY Series Demo");
    demo.pack();
    RefineryUtilities.centerFrameOnScreen(demo);
    demo.setVisible(true);
     */

    public Graph(String title, ArrayList<ArrayList<Double>> originalData){
        super(title);
        final XYSeries series = new XYSeries("Accuracy function of Recall");
        for (ArrayList<Double> table : originalData){
            series.add(table.get(0), table.get(1));
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Information Search",
                "Recall",
                "Accuracy",
                data,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);

    }

}
