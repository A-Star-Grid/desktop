package org.example.server.services;

import org.springframework.stereotype.Service;

import java.io.File;
import java.util.UUID;
import java.util.prefs.Preferences;

@Service
public class PreferencesStorage {
    private static final String ACCESS_TOKEN_KEY = "access_token";
    private static final String REFRESH_TOKEN_KEY = "refresh_token";

    private static final String RAM_LIMIT_KEY = "ram_limit";
    private static final String CPU_LIMIT_KEY = "cpu_limit";
    private static final String DISK_LIMIT_KEY = "disk_limit";
    private static final String VIRTUALBOX_PATH_KEY = "virtualbox_path";
    private static final String COMPUTATION_ACTIVE_KEY = "computation_active";
    private static final String DEVICE_UUID_KEY = "device_uuid";

    private static final double DEFAULT_CPU = 1;
    private static final int DEFAULT_RAM_MB = 2048;
    private static final int DEFAULT_DISK_GB = 25;
    private static final String DEFAULT_VIRTUALBOX_PATH = detectDefaultVirtualBoxPath();

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

    // --- Методы для установки лимитов ---
    public boolean setRamLimit(int ramMB) {
        if (ramMB > 0 && ramMB <= getTotalRAM()) {
            preferences.putInt(RAM_LIMIT_KEY, ramMB);
            return true;
        }
        return false;
    }

    public boolean setCpuLimit(double cpuCount) {
        if (cpuCount > 0 && cpuCount <= getAvailableProcessors()) {
            preferences.putDouble(CPU_LIMIT_KEY, cpuCount);
            return true;
        }
        return false;
    }

    public boolean setDiskLimit(int diskGB) {
        if (diskGB > 0 && diskGB <= getAppDiskSpace()) {
            preferences.putInt(DISK_LIMIT_KEY, diskGB);
            return true;
        }
        return false;
    }

    public boolean setVirtualBoxPath(String path) {
        if (isPathValid(path)) {
            preferences.put(VIRTUALBOX_PATH_KEY, path);
            return true;
        }
        return false;
    }

    public void setComputationActive(boolean isActive) {
        preferences.putBoolean(COMPUTATION_ACTIVE_KEY, isActive);
    }

    public int getRamLimit() {
        return preferences.getInt(RAM_LIMIT_KEY, DEFAULT_RAM_MB);
    }

    public double getCpuLimit() {
        return preferences.getDouble(CPU_LIMIT_KEY, DEFAULT_CPU);
    }

    public int getDiskLimit() {
        return preferences.getInt(DISK_LIMIT_KEY, DEFAULT_DISK_GB);
    }

    public String getVirtualBoxPath() {
        return preferences.get(VIRTUALBOX_PATH_KEY, DEFAULT_VIRTUALBOX_PATH);
    }

    public boolean isComputationActive() {
        return preferences.getBoolean(COMPUTATION_ACTIVE_KEY, false);
    }

    public void resetToDefaults() {
        preferences.putDouble(CPU_LIMIT_KEY, DEFAULT_CPU);
        preferences.putInt(RAM_LIMIT_KEY, DEFAULT_RAM_MB);
        preferences.putInt(DISK_LIMIT_KEY, DEFAULT_DISK_GB);
        preferences.put(VIRTUALBOX_PATH_KEY, DEFAULT_VIRTUALBOX_PATH);
        preferences.putBoolean(COMPUTATION_ACTIVE_KEY, false);
    }

    public UUID getDeviceUUID() {
        var deviceUUID = preferences.get(DEVICE_UUID_KEY, null);

        if (deviceUUID == null) {
            deviceUUID = UUID.randomUUID().toString();
            preferences.put(DEVICE_UUID_KEY, deviceUUID);
        }

        return UUID.fromString(deviceUUID);
    }

    private int getTotalRAM() {
        return (int) (Runtime.getRuntime().maxMemory() / (1024 * 1024));
    }

    private int getAvailableProcessors() {
        return Runtime.getRuntime().availableProcessors();
    }

    private int getAppDiskSpace() {
        File appDir = new File(System.getProperty("user.dir")); // Получаем путь к папке с приложением
        File rootDisk = appDir.toPath().getRoot().toFile(); // Определяем корневой диск
        return (int) (rootDisk.getTotalSpace() / (1024 * 1024 * 1024)); // Переводим в ГБ
    }

    private boolean isPathValid(String path) {
        File file = new File(path);
        return file.exists() && file.isDirectory();
    }

    private static String detectDefaultVirtualBoxPath() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            return "C:\\Program Files\\Oracle\\VirtualBox";
        } else if (os.contains("mac")) {
            return "/Applications/VirtualBox.app";
        } else {
            return "/usr/lib/virtualbox";
        }
    }
}
