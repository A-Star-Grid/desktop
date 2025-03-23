package org.example.core.models;

public enum VirtualMachineState {
    POWEROFF("poweroff"), // Машина выключена
    RUNNING("running"),   // Машина запущена
    PAUSED("paused"),     // Машина приостановлена
    SAVED("saved"),       // Состояние машины сохранено
    ABORTED("aborted"),   // Машина аварийно завершена
    STARTING("starting"), // Машина запускается
    STOPPING("stopping"), // Машина останавливается
    SAVING("saving"),     // Машина сохраняет состояние
    RESTORING("restoring"), // Машина восстанавливается из сохраненного состояния
    UNKNOWN("unknown");   // Состояние машины неизвестно

    private final String stateName;

    VirtualMachineState(String stateName) {
        this.stateName = stateName;
    }

    public String getStateName() {
        return stateName;
    }

    public static VirtualMachineState fromStateName(String stateName) {
        for (VirtualMachineState state : values()) {
            if (state.stateName.equalsIgnoreCase(stateName)) {
                return state;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return stateName;
    }
}
