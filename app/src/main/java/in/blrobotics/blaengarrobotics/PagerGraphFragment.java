package in.blrobotics.blaengarrobotics;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.jjoe64.graphview.series.DataPoint;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.*;

public class PagerGraphFragment extends Fragment {

    MySQLConnection conn;
    Activity activity;
    GraphFragment[] graph = new GraphFragment[2];
    private int lastReceivedId = 0;
    private double lastX = 0d;
    private int deviceId;
    List<String> color = Arrays.asList("red","green","blue");
    String selectPharms = "";
    private final Handler handler = new Handler();
    private Runnable runnable;
    private int syncTime = 10000;

    public PagerGraphFragment(){
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstateState) {
        if (getArguments().containsKey("deviceId")){
            deviceId = getArguments().getInt("deviceId");
            getArguments().remove("deviceId");
        }
        super.onCreate(savedInstateState);

        // Setting up Sync time
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(getContext());
        syncTime = Integer.parseInt(preference.getString("data_sync","10000"));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_pager_graph, container, false);

        lastReceivedId = 0;
        graph[0] = (GraphFragment) getChildFragmentManager().findFragmentById(R.id.graph1);
        graph[1] = (GraphFragment) getChildFragmentManager().findFragmentById(R.id.graph2);

        Iterator<String> keys = getArguments().keySet().iterator();
        for (int i=0;i<2;i++){
            if (keys.hasNext()){
                String key = keys.next();
                graph[i].setPlotTitle(key);
                graph[i].setAxisTitle("",key);
                String[] attrs = getArguments().getStringArray(key);
                graph[i].addLines(attrs.length);
                graph[i].setLinesColor(color);
                graph[i].setLinesTitle(Arrays.asList(attrs));
                for(String attr:attrs){
                    selectPharms += ", `"+attr+"`";
                }
            }
        }

        getData(deviceId,false);

        return rootView;
    }
    @Override
    public void onAttach(Activity activity) {
        this.activity = activity;
        /* database connection */
        conn = new MySQLConnection(activity);
        super.onAttach(activity);
    }

    @Override
    public void onResume() {
        super.onResume();
        runnable = new Runnable() {
            @Override
            public void run() {
                // TODO //synchronization time have to be set from setting
                getData(deviceId,true);
                // Works like infinite loop
                handler.postDelayed(this, syncTime);
            }
        };
        handler.postDelayed(runnable, syncTime);
    }

    @Override
    public void onPause() {
        handler.removeCallbacks(runnable);
        super.onPause();
    }

    public void getData(int deviceId, final boolean async){
        final List<DataPoint[]>[] dataList = new List[]{new ArrayList(),new ArrayList()};
        String query = "SELECT `id`"+selectPharms+" FROM `Data` WHERE `device` = "+deviceId+" AND `id` > "+lastReceivedId+" ORDER BY `id` DESC LIMIT 100";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray result = (JSONArray)dataObject;
                int index = 0;
                int length = result.length();
                DataPoint[][][] data = new DataPoint[2][3][length];
                for (int i=result.length()-1;i>=0;i--) {
                    JSONObject items = result.getJSONObject(i);
                    lastReceivedId = items.getInt("id");
                    Iterator<String> keys = getArguments().keySet().iterator();
                    for (int j = 0; j < 2; j++) {
                        if (keys.hasNext()) {
                            String key = keys.next();
                            int k = 0;
                            for (String attr : getArguments().getStringArray(key)) {
                                data[j][k++][index] = new DataPoint(lastX, Double.parseDouble(items.getString(attr).replaceAll("[^0-9.]", "")));
                            }
                        }
                    }
                    ++index;++lastX;
                }
                dataList[0].addAll(Arrays.asList(data[0]));
                dataList[1].addAll(Arrays.asList(data[1]));
                while (true){
                    if (index == length){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (graph[0] != null && graph[1] !=null) {
                                    if (async){
                                        graph[0].appendData(dataList[0]);
                                        graph[1].appendData(dataList[1]);
                                    }
                                    else {
                                        graph[0].setData(dataList[0]);
                                        graph[1].setData(dataList[1]);
                                    }
                                }
                            }
                        });
                        return;
                    }
                }
            }

        });
    }
}
