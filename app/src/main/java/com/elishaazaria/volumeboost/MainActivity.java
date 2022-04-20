package com.elishaazaria.volumeboost;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.elishaazaria.volumeboost.messages.QueryReplyPing;
import com.elishaazaria.volumeboost.messages.ServiceCommand;
import com.elishaazaria.volumeboost.messages.ServiceQueryPing;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private SwitchMaterial enableService;
    private boolean enableServiceManualCheck = false;

    private boolean serviceRunning = false;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        NotificationChannelHelper.createNotificationChannel(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        enableService = findViewById(R.id.enableServiceSwitch);

        enableService.setChecked(false);

        enableService.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (enableServiceManualCheck) return;
            if (isChecked) ServiceDispatcher.startService(this);
            else ServiceDispatcher.stopService(this);
        });

        LoudnessEnhancer enhancer = new LoudnessEnhancer(0);

        EditText editTextNumber = findViewById(R.id.editTextNumber);
        Button setLoudnessButton = findViewById(R.id.setLoudnessButton);

        editTextNumber.setText(Objects.toString(sharedPreferences.getInt(GlobalVars.SP_LOUDNESS, 0) / 10.0));

        setLoudnessButton.setOnClickListener(v -> {
            try {

                double loudness = Double.parseDouble(editTextNumber.getText().toString());
                int intLoudness = (int) (loudness * 10);
                sharedPreferences.edit().putInt(GlobalVars.SP_LOUDNESS, intLoudness).commit();
                EventBus.getDefault().post(ServiceCommand.UPDATE);
            } catch (NumberFormatException ignored) {
                Toast.makeText(this, "Invalid loudness number", Toast.LENGTH_SHORT).show();
            }
        });
        enhancer.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

        EventBus.getDefault().post(new ServiceQueryPing());
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void handlePingReply(QueryReplyPing reply) {
        serviceRunning = true;
        enableServiceManualCheck = true;
        enableService.setChecked(true);
        enableServiceManualCheck = false;
        if (GlobalVars.DEBUG_TOAST)
            Toast.makeText(this, "Ping reply", Toast.LENGTH_SHORT).show();
    }
}