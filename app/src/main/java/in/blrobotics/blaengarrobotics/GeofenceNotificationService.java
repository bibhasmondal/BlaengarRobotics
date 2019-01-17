package in.blrobotics.blaengarrobotics;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v7.preference.PreferenceManager;
import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.PolyUtil;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Type;
import java.util.*;

public class GeofenceNotificationService extends Service {

    private Timer timer;
    private TimerTask timerTask;
    private MySQLConnection conn;
    Gson gson = new Gson();
    NotificationCompat.Builder mBuilder;
    NotificationManager notificationManager;
    private SharedPreferences sharedPreferences;
    private int geofenceSynchronizationTime = 60000;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public GeofenceNotificationService(Context applicationContext) {
    }

    public GeofenceNotificationService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // connecting to database
        conn = new MySQLConnection(getApplicationContext(), getString(R.string.dbConnAddress), getString(R.string.dbName), getString(R.string.dbUserName), getString(R.string.dbPassword));
        conn.open();
        // getSharedPreferences
        sharedPreferences = getSharedPreferences(getString(R.string.shared_preference_name), Context.MODE_PRIVATE);
        // Build notification
        mBuilder = new NotificationCompat.Builder(getApplicationContext());
        //Setting statusbar icon
        mBuilder.setSmallIcon(R.drawable.ic_notifiaction_small);
        // Setting notification bitmap icon
        Drawable myDrawable = getResources().getDrawable(R.mipmap.splash_icon_round);
        Bitmap anImage      = ((BitmapDrawable) myDrawable).getBitmap();
        mBuilder.setLargeIcon(anImage);
        //Define sound URI
        Uri soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        mBuilder.setSound(soundUri);
        // Gets an instance of the NotificationManager service
        notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);

        // Setting up Geofence checking interval
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(this);
        geofenceSynchronizationTime = Integer.parseInt(preference.getString("geo_interval","60000"));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        startTimer();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Intent broadcastIntent = new Intent(this, RestarterBroadcastReceiver.class);
//        Intent broadcastIntent = new Intent("RestartGeofenceNotificationService");
        sendBroadcast(broadcastIntent);
        stopTimer();
    }

    public void startTimer() {
        //set a new Timer
        timer = new Timer();
        //initialize the TimerTask's job
        initializeTimerTask();
        //schedule the timer, to wake up every 10 min
        timer.schedule(timerTask, 1000, geofenceSynchronizationTime);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                if (sharedPreferences.contains("userId")) {
                    int userId = sharedPreferences.getInt("userId", 0);
                    checkGeofenceByUserId(userId);
                }
            }
        };
    }

    public void stopTimer() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
    public void showNotification(int no,String deviceSN){
        mBuilder.setContentTitle("Device Serial Number "+deviceSN);
        mBuilder.setContentText("Out of geofence");
        // To post your notification to the notification bar
        notificationManager.notify(no,mBuilder.build());
    }

    public void checkGeofenceByUserId(int userId){
        String query = "SELECT * FROM `Devices` WHERE `id` IN (SELECT `device` FROM `Owners` WHERE `user`=" + userId + ")";
        AsyncTask asyncTask = conn.execute(query);
        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
            @Override
            public void getResult(Object dataObject) throws Exception {
                JSONArray deviceDetails = (JSONArray)dataObject;
                if (!deviceDetails.isNull(0)) {
                    final int[] condition = new int[2];
                    condition[0] = deviceDetails.length();
                    condition[1] = 0;

                    for (int i = 0; i < deviceDetails.length(); i++) {
                        int deviceID = (int)deviceDetails.getJSONObject(i).remove("id");
                        final String deviceSN = deviceDetails.getJSONObject(i).getString("serial_no");
                        // Getting device geofence
                        final List<LatLng>[] points = new List[]{new ArrayList<>()};
                        String queryGeofence = "SELECT `coordinates` FROM `Geofences` WHERE `device` =" + deviceID;
                        AsyncTask asyncTaskGeofence = conn.execute(queryGeofence);
                        conn.setOnResult(new MySQLConnection.OnResult(asyncTaskGeofence) {
                            @Override
                            void getResult(Object dataObject) throws Exception {
                                JSONArray deviceGeofence = (JSONArray)dataObject;
                                if (!deviceGeofence.isNull(0)) {
                                    String pointList = deviceGeofence.getJSONObject(0).getString("coordinates");
                                    Type type = new TypeToken<List<LatLng>>() {}.getType();
                                    points[0] = gson.fromJson(pointList, type);
                                }
                            }
                        });
                        // Getting device data
                        String query = "SELECT `id`,`lat`,`lon` FROM `Data` WHERE `device` =" + deviceID + " ORDER BY `id` DESC LIMIT 1";
                        AsyncTask asyncTask = conn.execute(query);
                        final int notificationNo = i;
                        conn.setOnResult(new MySQLConnection.OnResult(asyncTask) {
                            @Override
                            public void getResult(Object dataObject) throws Exception {
                                JSONArray deviceData = (JSONArray)dataObject;
                                if (!deviceData.isNull(0)) {
                                    JSONObject items = deviceData.getJSONObject(0);
                                    LatLng currPos = new LatLng(items.getDouble("lat"),items.getDouble("lon"));
                                    while (true){
                                        if (!points[0].isEmpty()){
                                            if (!PolyUtil.containsLocation(currPos,points[0],false)){
                                                // Notification will shown here
                                                showNotification(notificationNo,deviceSN);
                                            }
                                            return;
                                        }
                                    }
                                }
                            }
                        });

                    }
                }
            }
        });
    }
}
