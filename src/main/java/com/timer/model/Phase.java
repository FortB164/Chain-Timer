package com.timer.model;

import javafx.beans.property.*;

/**
 * Represents a single phase (timer) in a sequence
 */
public class Phase {
    private final StringProperty name;
    private final IntegerProperty durationMinutes;
    private final IntegerProperty remainingSeconds;
    private final IntegerProperty originalDurationSeconds;
    private final BooleanProperty isActive;
    private final BooleanProperty isCompleted;
    private final StringProperty endSound;

    public Phase(String name, int durationMinutes) {
        this.name = new SimpleStringProperty(name);
        this.durationMinutes = new SimpleIntegerProperty(durationMinutes);
        this.remainingSeconds = new SimpleIntegerProperty(durationMinutes * 60);
        this.originalDurationSeconds = new SimpleIntegerProperty(durationMinutes * 60);
        this.isActive = new SimpleBooleanProperty(false);
        this.isCompleted = new SimpleBooleanProperty(false);
        this.endSound = new SimpleStringProperty(""); // Empty string means no sound
    }

    // Getters and setters
    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public int getDurationMinutes() { return durationMinutes.get(); }
    public IntegerProperty durationMinutesProperty() { return durationMinutes; }
    public void setDurationMinutes(int durationMinutes) { 
        this.durationMinutes.set(durationMinutes);
        this.remainingSeconds.set(durationMinutes * 60);
    }
    
    public void setDurationSeconds(int totalSeconds) {
        this.durationMinutes.set(totalSeconds / 60);
        this.remainingSeconds.set(totalSeconds);
        this.originalDurationSeconds.set(totalSeconds);
    }
    
    public int getTotalDurationSeconds() {
        return remainingSeconds.get();
    }

    public int getRemainingSeconds() { return remainingSeconds.get(); }
    public IntegerProperty remainingSecondsProperty() { return remainingSeconds; }
    public void setRemainingSeconds(int remainingSeconds) { this.remainingSeconds.set(remainingSeconds); }

    public boolean isActive() { return isActive.get(); }
    public BooleanProperty isActiveProperty() { return isActive; }
    public void setActive(boolean active) { this.isActive.set(active); }

    public boolean isCompleted() { return isCompleted.get(); }
    public BooleanProperty isCompletedProperty() { return isCompleted; }
    public void setCompleted(boolean completed) { this.isCompleted.set(completed); }

    public String getEndSound() { return endSound.get(); }
    public StringProperty endSoundProperty() { return endSound; }
    public void setEndSound(String endSound) { this.endSound.set(endSound); }

    public void reset() {
        // Reset to the original total duration
        remainingSeconds.set(originalDurationSeconds.get());
        isActive.set(false);
        isCompleted.set(false);
    }

    public String getFormattedTime() {
        int minutes = remainingSeconds.get() / 60;
        int seconds = remainingSeconds.get() % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return name.get() + " (" + durationMinutes.get() + " min)";
    }
}
