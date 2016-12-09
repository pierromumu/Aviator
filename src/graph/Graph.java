package graph;

import org.jfree.chart.*;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.*;
import org.jfree.ui.*;

import java.util.ArrayList;


public class Graph extends ApplicationFrame {

    // affichage
    public Graph(String title, ArrayList<ArrayList<Double>> originalData, int id){
        super(title);
        final XYSeries series = new XYSeries("Accuracy function of Recall");
        for (ArrayList<Double> table : originalData){
            series.add(table.get(0), table.get(1));
        }
        final XYSeriesCollection data = new XYSeriesCollection(series);
        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Query n°"+id,
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
