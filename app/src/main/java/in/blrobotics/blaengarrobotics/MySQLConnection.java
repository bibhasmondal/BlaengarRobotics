package in.blrobotics.blaengarrobotics;

import java.sql.*;
import java.util.concurrent.TimeUnit;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.os.*;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;

public class MySQLConnection {
    public static String dbConnUrl,dbUserName,dbPassword;
    public static Connection conn = null;
    private ProgressBar progressBar = null;
    private Context context;

    public MySQLConnection(Context context) {
        this.context = context;
        loadDynamicProgressBar(context);
    }

    public MySQLConnection(final Context context, String dbConnAddress, String dbName, String dbUserName, String dbPassword) {
        /* Create database connection url. */
        this.dbConnUrl = "jdbc:mysql://" + dbConnAddress + "/" + dbName;
        this.dbUserName = dbUserName;
        this.dbPassword = dbPassword;
        this.context = context;
    }

    public void open(){
        Thread open = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    /* If connection is closed or null then try to reconnect */
                    try{
                        if (conn == null){
                            connect();
                        }
                        else if (conn.isClosed()){
                            connect();
                        }
                    }
                    catch (SQLException e){
                        e.printStackTrace();
                    }
                }
            }
        });
        open.start();
    }

    private void connect(){
        /* checking internet connection */
        if(isInternetConnection()){
            /* Alternative of AsyncTask */
            if (android.os.Build.VERSION.SDK_INT > 9) {
                /* This is required because network connection does not work on main thread */
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
            }
            try{
                /* Register database jdbc driver class. */
                Class.forName("com.mysql.jdbc.Driver");
                /* Get the Connection object. */
                conn = DriverManager.getConnection(dbConnUrl, dbUserName , dbPassword);

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println(context.getString(R.string.error_driver));
            } catch (SQLException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
            }
        }
        else{
            System.out.println(context.getString(R.string.error_internet_conn));
        }
    }

    public void close(){
        try {
            if (conn != null){
                conn.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        conn = null;
    }

    private void showToastOnUiThread(final String text){
        ((Activity)context).runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(context, text, Toast.LENGTH_LONG).show();
            }
        });
    }


    public Object execute(String query){
        AsyncTask<String,Void,Object> asyncTask = new AsyncTask<String, Void, Object>() {
            @Override
            protected void onPreExecute(){
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            protected Object doInBackground(String... strings) {
                /* null indicates here any error */
                if(isInternetConnection()) {
                    return executeQuery(strings[0]);
                }
                else{
                    showToastOnUiThread(context.getString(R.string.error_internet_conn));
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object result){
                progressBar.setVisibility(View.GONE);
            }
        };
        asyncTask.execute(query);
        try {
            return asyncTask.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            Toast toast = Toast.makeText(context,"Connection Timeout",Toast.LENGTH_SHORT);
            toast.show();
        }
        return null;
    }

    /* All user submitted query will be execute here */
    private Object executeQuery(String query){
        /* null means error here */
        if (conn != null){
            if (!query.isEmpty()) {
                Statement statement = null;
                ResultSet resultSet = null;
                try {
                    statement = conn.createStatement();
                    /* return true for select and false for update or insert */
                    if (statement.execute(query)) {
                        resultSet = statement.getResultSet();
                        return convertToJSON(resultSet);
                    } else {
                        return statement.getUpdateCount();
                    }
                }
                catch (Exception e) {
                    e.printStackTrace();
                    showToastOnUiThread(e.getMessage());
                }
                finally {
                    try {
                        if (resultSet != null){
                            resultSet.close();
                        }
                        if (statement != null){
                            statement.close();
                        }
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        else {
            showToastOnUiThread(context.getString(R.string.error_internet_conn));
        }
        return null;
    }

    /* convert ResultSet to JSONArray */
    public JSONArray convertToJSON(ResultSet resultSet) throws Exception {
        JSONArray jsonArray = new JSONArray();
        while (resultSet.next()) {
            int no_columns = resultSet.getMetaData().getColumnCount();
            JSONObject obj = new JSONObject();
            for (int i = 0; i < no_columns; i++) {
                obj.put(resultSet.getMetaData().getColumnLabel(i + 1).toLowerCase(), resultSet.getObject(i + 1));
            }
            jsonArray.put(obj);
        }
        return jsonArray;
    }
    /* convert ResultSet to XML */
    public String convertToXML(ResultSet resultSet) throws Exception {
        StringBuffer xmlArray = new StringBuffer("<results>");
        while (resultSet.next()) {
            int no_columns = resultSet.getMetaData().getColumnCount();
            xmlArray.append("<result ");
            for (int i = 0; i < no_columns; i++) {
                xmlArray.append(" " + resultSet.getMetaData().getColumnLabel(i + 1)
                        .toLowerCase() + "='" + resultSet.getObject(i + 1) + "'"); }
            xmlArray.append(" />");
        }
        xmlArray.append("</results>");
        return xmlArray.toString();
    }

    public void loadDynamicProgressBar(Context context){
        ViewGroup rootView = (ViewGroup)((Activity) context).getWindow().getDecorView().getRootView();
        progressBar = new ProgressBar(context,null,android.R.attr.progressBarStyleLarge);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(true);
        RelativeLayout layout = new RelativeLayout(context);
        layout.setGravity(Gravity.CENTER);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,   //width
                RelativeLayout.LayoutParams.MATCH_PARENT  //height
        );
        layout.setLayoutParams(layoutParams);
        layout.addView(progressBar);
        rootView.addView(layout);
    }

    public boolean isInternetConnection() {
        // get Connectivity Manager object to check connection
        ConnectivityManager connec = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        // Check for network connections
        if (connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTED ||
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTING ||
                connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.CONNECTED) {
            return true;

        } else if (
                connec.getNetworkInfo(0).getState() == android.net.NetworkInfo.State.DISCONNECTED ||
                        connec.getNetworkInfo(1).getState() == android.net.NetworkInfo.State.DISCONNECTED) {
            return false;
        }
        return false;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show,final View fromView) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = context.getResources().getInteger(android.R.integer.config_shortAnimTime);

            fromView.setVisibility(show ? View.GONE : View.VISIBLE);
            fromView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    fromView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            progressBar.animate().setDuration(shortAnimTime).alpha(show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        }
        else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            fromView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
