package in.blrobotics.blaengarrobotics;

import android.os.AsyncTask;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

/**
 * Created by bibha on 10-01-2018.
 */

public class MyAsyncTask extends AsyncTask<String, Void, String> {
    MyHTTPConnection myHTTPConnection=new MyHTTPConnection();
    TextView[] textView=null;
    MyAsyncTask(TextView[] textView){
        this.textView=textView;
    }

    @Override
    protected String doInBackground(String... url) {
        // NO CHANGES TO UI TO BE DONE HERE
        String data=null;
        try {
            data=myHTTPConnection.getHttpConnection(url[0]);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    protected void onPostExecute(String data) {
        //This is where we update the UI with the acquired data
        try {
            JSONObject jsonObject =new JSONObject(data);
            //JSONArray jsonArray=jsonObject.getJSONArray("table4");
            textView[0].setText(jsonObject.getString("gps_coordinate"));//jsonArray.getJSONObject(0).optString("gps_coordinate"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }
}