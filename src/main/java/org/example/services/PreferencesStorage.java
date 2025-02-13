package org.example.services;

import org.springframework.stereotype.Service;

import java.util.prefs.Preferences;

@Service
public class PreferencesStorage {
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private final Preferences preferences;

    public PreferencesStorage() {
        this.preferences = Preferences.userRoot().node("AStarGrid");
    }

    public void saveTokens(String accessToken, String refreshToken) {
        preferences.put(ACCESS_TOKEN_KEY, accessToken);
        preferences.put(REFRESH_TOKEN_KEY, refreshToken);
    }

    public String loadAccessToken() {
        return preferences.get(ACCESS_TOKEN_KEY, null);
    }

    public String loadRefreshToken() {
        return preferences.get(REFRESH_TOKEN_KEY, null);
    }

    public void clearTokens() {
        preferences.remove(ACCESS_TOKEN_KEY);
        preferences.remove(REFRESH_TOKEN_KEY);
    }
}
