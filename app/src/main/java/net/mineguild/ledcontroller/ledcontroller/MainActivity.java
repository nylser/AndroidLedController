package net.mineguild.ledcontroller.ledcontroller;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.java_websocket.WebSocket;
import org.java_websocket.drafts.Draft_17;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    private GradientView mTop;
    private GradientView mBottom;
    private TextView mTextView;
    private Drawable mIcon;
    private Button mOffButton;

    private UUID uuid;

    private WSClient getClient;
    private WSClient setClient;

    private Handler colorHandler;
    private int noChange = 0;

    int mNotificationId;
    Notification.Builder mBuilder;

    public static final String UUID_KEY = "uuid";
    public static final String COLOR_KEY = "lColor";

    public MainActivity() {
        uuid = UUID.randomUUID();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_main);
        mIcon = ResourcesCompat.getDrawable(getResources(), R.mipmap.ic_launcher, null);
        mTextView = (TextView) findViewById(R.id.color);
        mTextView.setCompoundDrawablesWithIntrinsicBounds(mIcon, null, null, null);
        mTop = (GradientView) findViewById(R.id.top);
        mBottom = (GradientView) findViewById(R.id.bottom);
        mTop.setBrightnessGradientView(mBottom);
        mBottom.setOnColorChangedListener(new GradientView.OnColorChangedListener() {
            @Override
            public void onColorChanged(GradientView view, int color) {
                mTextView.setTextColor(color);

                int r = (color & 0xFF0000) >> 16;
                int g = (color & 0xFF00) >> 8;
                int b = (color & 0xFF);
                if (setClient != null && setClient.getReadyState() == WebSocket.READYSTATE.OPEN && noChange == 0) {
                    setClient.send(String.format("%d,%d,%d", r, g, b));
                }
                if (noChange > 0) noChange--;
                mTextView.setText("#" + Integer.toHexString(color));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mIcon.setTint(color);
                }
            }
        });

        mOffButton = (Button) findViewById(R.id.offButton);
        mOffButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchOff();
            }
        });

        mBuilder = new Notification.Builder(this).setSmallIcon(R.drawable.icon_small).setContentTitle("LedController").setContentText("Control your leds!");
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mNotificationId = 001;

        openWebsockets();

    }

    @Override
    protected void onPause() {
        super.onPause();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(mNotificationId, mBuilder.build());
        getClient.close();
        setClient.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(mNotificationId);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.cancel(mNotificationId);
    }

    private void openWebsockets() {
        try {
            System.out.println("Connecting...");
            getClient = new WSClient(new URI("ws://alarmpi:8765/get"), new Draft_17(), WSClient.GET_PROTOCOL, this);
            setClient = new WSClient(new URI("ws://alarmpi:8765/set"), new Draft_17(), WSClient.SET_PROTOCOL, this);
            setClient.connect();
            getClient.connect();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        uuid = UUID.fromString(savedInstanceState.getString(UUID_KEY));
        mTop.setColor(savedInstanceState.getInt(COLOR_KEY));
    }

    // invoked when the activity may be temporarily destroyed, save the instance state here
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(UUID_KEY, uuid.toString());
        outState.putInt(COLOR_KEY, mBottom.getSelectedColor());

        // call superclass to save any view hierarchy
        super.onSaveInstanceState(outState);

    }

    public void switchOff() {
        mBottom.setZero();
    }

    public void setColor(int color) {
        noChange++;
        mTop.setColor(color);
        noChange++;
        mBottom.setColor(color);
    }

    public String getUUID() {
        return uuid.toString();
    }
}
