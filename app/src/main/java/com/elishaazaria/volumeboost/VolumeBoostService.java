package com.elishaazaria.volumeboost;

import static com.elishaazaria.volumeboost.BuildConfig.DEBUG;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.elishaazaria.volumeboost.messages.QueryReplyOn;
import com.elishaazaria.volumeboost.messages.ServiceCommand;
import com.elishaazaria.volumeboost.messages.ServiceQueryPing;
import com.elishaazaria.volumeboost.messages.QueryReplyPing;
import com.elishaazaria.volumeboost.messages.ServiceQueryOn;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class VolumeBoostService extends Service {

    private static final String TAG = "VolumeBoostService";

    private static final int FOREGROUND_NOTIFICATION = 1;

    public static final String EXTRA_COMMAND = "com.elishazaria.volumeboost.EXTRA_COMMAND";

    private static final int CODE_STOP = 1;

    private LoudnessEnhancer enhancer;
//    private final SessionList audioSessions = new SessionList();

    private SharedPreferences sharedPreferences;

    private boolean on = true;

    public VolumeBoostService() {
    }

    private int getLoudness() {
        return sharedPreferences.getInt(GlobalVars.SP_LOUDNESS, 0);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        enhancer = new LoudnessEnhancer(0);
    }

    @Override
    public void onDestroy() {
        enhancer.release();
        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    @Subscribe
    public void handleCommands(ServiceCommand command) {
        switch (command) {
            case STOP:
                stopSelf();
                break;
            case UPDATE:
                enhancer.setTargetGain(getLoudness());
                updateNotification();
                break;
            case PLAY:
                enhancer.setEnabled(true);
                on = true;
                updateNotification();
                break;
            case PAUSE:
                enhancer.setEnabled(false);
                on = false;
                updateNotification();
                break;
        }
        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(this, "Received command: " + command, Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void handlePingQuery(ServiceQueryPing event) {
        EventBus.getDefault().post(new QueryReplyPing());
        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(this, "Ping Query", Toast.LENGTH_SHORT).show();
    }

    @Subscribe
    public void handleOnQuery(ServiceQueryOn query) {
        EventBus.getDefault().post(new QueryReplyOn(on));
    }

//    @Subscribe
//    public void handleAudioSessionEvent(AudioSessionEvent event) {
//        if (event.open)
//            audioSessions.add(event.session);
//        else
//            audioSessions.remove(event.session);
//
//        updateNotification();
//    }

    private void startForeground() {

        int flag = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        }
        Notification notification = getNotification(flag);
        NotificationChannelHelper.createNotificationChannel(this);
        startForeground(FOREGROUND_NOTIFICATION, notification);
    }

    @NonNull
    private Notification getNotification(int flag) {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, flag);
        return new NotificationCompat.Builder(this, NotificationChannelHelper.SERVICE_CHANNEL)
                .setContentTitle("Volume Boost Service")
                .setContentText(on ? "Loudness Boost: " + (getLoudness() / 10.0) + "db" : "Paused")
                .setSmallIcon(R.drawable.ic_icon
                )
                .setContentIntent(pendingIntent)
                .build();
    }

    private void updateNotification() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            Notification notification = getNotification(PendingIntent.FLAG_IMMUTABLE);

            NotificationManager notificationManager = this.getSystemService(NotificationManager.class);

            notificationManager.notify(FOREGROUND_NOTIFICATION, notification);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        startForeground();

        if (DEBUG) {
            Log.i(TAG, "onStartCommand() called with " + "intent = [" + intent + "], flags = ["
                    + flags + "], startId = [" + startId + "]");
        }
        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(this, "onStartCommand() called with " + "intent = [" + intent + "], flags = ["
                    + flags + "], startId = [" + startId + "]", Toast.LENGTH_LONG).show();

        return super.onStartCommand(intent, flags, startId);//START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}