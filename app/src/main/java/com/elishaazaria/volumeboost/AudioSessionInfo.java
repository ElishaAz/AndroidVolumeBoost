package com.elishaazaria.volumeboost;

import java.util.Objects;

public final class AudioSessionInfo {

    public final int sessionId;
    public final String packageName;

    public AudioSessionInfo(int sessionId, String packageName) {
        this.sessionId = sessionId;
        this.packageName = packageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AudioSessionInfo)) return false;
        AudioSessionInfo that = (AudioSessionInfo) o;
        return sessionId == that.sessionId && Objects.equals(packageName, that.packageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, packageName);
    }

    @Override
    public String toString() {
        return "{" + packageName +
                ": " + sessionId +
                '}';
    }
}
