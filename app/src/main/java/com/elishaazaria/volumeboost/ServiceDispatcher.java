/*
 * Copyright (C) 2016 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.elishaazaria.volumeboost;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.audiofx.AudioEffect;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.elishaazaria.volumeboost.messages.AudioSessionEvent;
import com.elishaazaria.volumeboost.messages.ServiceCommand;

import org.greenrobot.eventbus.EventBus;


public class ServiceDispatcher extends BroadcastReceiver {
    private static final String TAG = "ServiceDispatcher";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        boolean open;
        if (action.equals(AudioEffect.ACTION_OPEN_AUDIO_EFFECT_CONTROL_SESSION))
            open = true;
        else if (action.equals(AudioEffect.ACTION_CLOSE_AUDIO_EFFECT_CONTROL_SESSION))
            open = false;
        else
            return;

        int sessionId = intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0);
        String pkg = intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME);

        EventBus.getDefault().post(new AudioSessionEvent(open, new AudioSessionInfo(sessionId, pkg)));

        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(context, intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0) +
                    ", " + intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME), Toast.LENGTH_LONG).show();

        if (BuildConfig.DEBUG)
            Log.d(TAG, intent.getIntExtra(AudioEffect.EXTRA_AUDIO_SESSION, 0) +
                    ", " + intent.getStringExtra(AudioEffect.EXTRA_PACKAGE_NAME));
    }

    public static void startService(Context context) {
        Intent service = new Intent(context.getApplicationContext(), VolumeBoostService.class);

        ContextCompat.startForegroundService(context, service);
    }

    public static void stopService(Context context) {
        EventBus.getDefault().post(ServiceCommand.STOP);
    }
}
