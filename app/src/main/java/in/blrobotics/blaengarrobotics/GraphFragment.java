package in.blrobotics.blaengarrobotics;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.LegendRenderer;
import com.jjoe64.graphview.series.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GraphFragment extends Fragment {
    private List<LineGraphSeries<DataPoint>> lineGraphSeriesList;
    private GraphView graph;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        lineGraphSeriesList = new ArrayList<>();

        View rootView = inflater.inflate(R.layout.fragment_graph, container, false);

        graph = (GraphView) rootView.findViewById(R.id.graph);
        // Setting up graph
        graph.setCameraDistance((float) 0.05);   //control the view camera
        // set manual X bounds
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(20);

//        graph.getViewport().setYAxisBoundsManual(true);
//        graph.getViewport().setMinY(-5);
//        graph.getViewport().setMaxY(15);

        // enable scaling and scrolling
        graph.getViewport().setScrollable(true); // enables horizontal scrolling
        graph.getViewport().setScrollableY(true); // enables vertical scrolling
        graph.getViewport().setScalable(true); // enables horizontal zooming and scrolling
        graph.getViewport().setScalableY(true); // enables vertical zooming and scrolling

        // optional styles
        graph.setTitleTextSize(30);
//        graph.setTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setVerticalAxisTitleTextSize(15);
//        graph.getGridLabelRenderer().setVerticalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setHorizontalAxisTitleTextSize(15);
//        graph.getGridLabelRenderer().setHorizontalAxisTitleColor(Color.BLACK);
        graph.getGridLabelRenderer().setHighlightZeroLines(true);    //highlight the axis

        // legend
        graph.getLegendRenderer().setVisible(false);
        graph.getLegendRenderer().setAlign(LegendRenderer.LegendAlign.TOP);

        return rootView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    public void setPlotTitle(String title){
        graph.setTitle(title);
    }

    public void setAxisTitle(String hTitle,String vTitle){
        graph.getGridLabelRenderer().setHorizontalAxisTitle(hTitle);
        graph.getGridLabelRenderer().setVerticalAxisTitle(vTitle);
    }

    public void addLines(int no){
        for (int i=0;i<no;i++){
            // Setting legend
            LineGraphSeries<DataPoint> lineGraphSeries = new LineGraphSeries<>();
            lineGraphSeries.setDrawBackground(true);
            lineGraphSeries.setDrawDataPoints(true);
            lineGraphSeries.setDataPointsRadius((float) 2.5); //point radius
            lineGraphSeries.setThickness(2); //line thickness
            // setting listener
            lineGraphSeries.setOnDataPointTapListener(new OnDataPointTapListener() {
                @Override
                public void onTap(Series series, DataPointInterface dataPoint) {
                    Toast.makeText(getActivity(), "Data Point clicked: "+dataPoint, Toast.LENGTH_SHORT).show();
                }
            });
            lineGraphSeriesList.add(lineGraphSeries);
            // adding series to the graph
            graph.addSeries(lineGraphSeries);

        }
        if (no>1){
            // legend
            graph.getLegendRenderer().setVisible(true);
        }
    }

    public void setLinesColor(List<String> colors){
        Iterator<String> color = colors.iterator();
        for (LineGraphSeries<DataPoint> lineGraphSeries:lineGraphSeriesList) {
            if (color.hasNext()) {
                switch (color.next()){
                    case "red":
                        lineGraphSeries.setColor(Color.argb(255, 255, 60, 60));
                        lineGraphSeries.setBackgroundColor(Color.argb(100, 204, 119, 119));
                        break;
                    case "green":
                        lineGraphSeries.setColor(Color.argb(255, 60, 255, 60));
                        lineGraphSeries.setBackgroundColor(Color.argb(100, 119, 204, 119));
                        break;
                    case "blue":
                        lineGraphSeries.setColor(Color.argb(255, 60, 60, 255));
                        lineGraphSeries.setBackgroundColor(Color.argb(100, 119, 119, 204));
                        break;
                }

            }
        }
    }

    public void setLinesTitle(List<String> titleList){
        Iterator<String> title = titleList.iterator();
        for (LineGraphSeries<DataPoint> lineGraphSeries:lineGraphSeriesList) {
            if (title.hasNext()) {
                // setting up title
                lineGraphSeries.setTitle(title.next());
            }
        }
    }

    public void setData(List<DataPoint[]> dataList){
        Iterator<DataPoint[]> data = dataList.iterator();
        for (LineGraphSeries<DataPoint> lineGraphSeries:lineGraphSeriesList){
            if (data.hasNext()){
                lineGraphSeries.resetData(data.next());
            }
        }

    }

    public void appendData(List<DataPoint[]> dataList) {
        Iterator<DataPoint[]> data = dataList.iterator();
        for (LineGraphSeries<DataPoint> lineGraphSeries:lineGraphSeriesList) {
            if (data.hasNext()){
                for(DataPoint point:data.next()){
                    lineGraphSeries.appendData(point, true, 40);
                }
            }
        }
    }
}
