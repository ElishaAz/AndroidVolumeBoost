package com.elishaazaria.volumeboost;

import android.media.audiofx.LoudnessEnhancer;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SessionList {

    private static final int defaultGain = 10;

    private static final Map<AudioSessionInfo, LoudnessEnhancer> sessions = new HashMap<>();

    public SessionList() {

    }

    public synchronized void add(AudioSessionInfo info) {
        LoudnessEnhancer enhancer = new LoudnessEnhancer(info.sessionId);
        enhancer.setTargetGain(defaultGain);

        enhancer.setEnabled(true);

        sessions.put(info, enhancer);
    }

    public synchronized void remove(AudioSessionInfo info) {
        if (sessions.containsKey(info)) {
            LoudnessEnhancer enhancer = sessions.remove(info);

            if (enhancer != null) {
                enhancer.release();
            }
        }
    }

    public synchronized void clear() {
        for (LoudnessEnhancer enhancer :
                sessions.values()) {
            enhancer.release();
        }
        sessions.clear();
    }

    @NonNull
    @Override
    public String toString() {
        return sessions.keySet().toString();
    }
}
