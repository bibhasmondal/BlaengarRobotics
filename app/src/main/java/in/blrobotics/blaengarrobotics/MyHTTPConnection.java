package in.blrobotics.blaengarrobotics;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by bibha on 17-01-2018.
 */

public class MyHTTPConnection {

    MyHTTPConnection(){
    }

    private String getInputStream(InputStream stream) throws IOException {
        BufferedReader bufferedReader =new BufferedReader(new InputStreamReader(stream));
        String s=bufferedReader.readLine();
        if (stream!=null){
            stream.close();
        }
        return s;
    }

    public String getHttpConnection(String urlString) throws IOException {
        //long length = 0;
        InputStream inputStream=null;
        URL url = new URL(urlString);
        URLConnection connection = url.openConnection();
        try {
            HttpURLConnection httpConnection = (HttpURLConnection) connection;
            httpConnection.setRequestMethod("GET");
            httpConnection.connect();
            //length=httpConnection.getContentLength();
            if (httpConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                inputStream = httpConnection.getInputStream();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return getInputStream(inputStream);
    }
}
