package in.blrobotics.blaengarrobotics;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.TextView;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

public class WebSocket extends WebSocketClient {
    TextView[] textViews;
    Activity activity;
    public WebSocket(URI serverUri, Draft draft) {
        super(serverUri, draft);
    }

    public WebSocket(URI serverURI,Activity activity, TextView[] textViews) {
        super(serverURI);
        this.textViews=textViews;
        this.activity=activity;
    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        textViews[1].setText("OnOpen");
        send("E:/Docoments/Bibhas/Python/Websocket");
        //System.out.println("new connection opened");
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        textViews[1].setText(reason);
        //System.out.println("closed with exit code " + code + " additional info: " + reason);
    }

    @Override
    public void onMessage(String message) {
        final String s=message;
        Log.i("message",s)
;        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textViews[2].setText(s);
            }
        });
        //textViews[2].setText(message);
        //System.out.println("received message: " + message);
    }

    @Override
    public void onMessage(ByteBuffer message) {
        System.out.println("received ByteBuffer");
    }

    @Override
    public void onError(Exception ex) {
        System.err.println("an error occurred:" + ex);
    }

    //public static void main(String[] args) throws URISyntaxException {
        //WebSocketClient client = new WebSocket(new URI("ws://localhost:8887"));
        //client.connect();
    //}
}