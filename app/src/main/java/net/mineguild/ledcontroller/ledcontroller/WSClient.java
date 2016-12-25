package net.mineguild.ledcontroller.ledcontroller;

import android.app.Activity;
import android.graphics.Color;
import android.os.Looper;
import android.util.JsonReader;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RunnableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WSClient extends WebSocketClient {

    public static final int GET_PROTOCOL = 0;
    public static final int SET_PROTOCOL = 1;

    private int protocol;
    private MainActivity act;

    WSClient(URI serverURI, Draft draft, int protocol, MainActivity act) {
        super(serverURI, draft);
        this.protocol = protocol;
        this.act = act;

    }

    @Override
    public void onOpen(ServerHandshake handshakedata) {
        send(act.getUUID());
    }

    @Override
    public void onMessage(String message) {
        if (protocol == GET_PROTOCOL) {
            try {
                JSONObject json = new JSONObject(message);
                JSONArray colors = json.getJSONArray("color");
                final int color = Color.argb(255, colors.getInt(0), colors.getInt(1), colors.getInt(2));
                act.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        act.setColor(color);

                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {

    }

    @Override
    public void onError(Exception ex) {
        ex.printStackTrace();
    }
}
