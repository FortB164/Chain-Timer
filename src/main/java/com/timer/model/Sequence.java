package com.timer.model;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.util.UUID;

/**
 * Represents a sequence of phases that execute in order
 */
public class Sequence {
    private final String id;
    private final StringProperty name;
    private final ObservableList<Phase> phases;
    private final BooleanProperty isRunning;
    private final BooleanProperty isCompleted;
    private final IntegerProperty currentPhaseIndex;
    private final IntegerProperty totalDuration;

    public Sequence(String name) {
        this.id = UUID.randomUUID().toString();
        this.name = new SimpleStringProperty(name);
        this.phases = FXCollections.observableArrayList();
        this.isRunning = new SimpleBooleanProperty(false);
        this.isCompleted = new SimpleBooleanProperty(false);
        this.currentPhaseIndex = new SimpleIntegerProperty(-1);
        this.totalDuration = new SimpleIntegerProperty(0);
    }

    public void addPhase(Phase phase) {
        phases.add(phase);
        updateTotalDuration();
    }

    public void removePhase(Phase phase) {
        phases.remove(phase);
        updateTotalDuration();
    }

    public void removePhase(int index) {
        if (index >= 0 && index < phases.size()) {
            phases.remove(index);
            updateTotalDuration();
        }
    }

    public Phase getCurrentPhase() {
        int index = currentPhaseIndex.get();
        if (index >= 0 && index < phases.size()) {
            return phases.get(index);
        }
        return null;
    }

    public void start() {
        if (!phases.isEmpty()) {
            isRunning.set(true);
            isCompleted.set(false);
            if (currentPhaseIndex.get() < 0) {
                // Starting fresh - reset everything
                currentPhaseIndex.set(0);
                phases.forEach(Phase::reset);
            }
            // Set current phase as active
            Phase current = getCurrentPhase();
            if (current != null) {
                current.setActive(true);
            }
        }
    }
    
    public void startFromPhase(int phaseIndex) {
        if (!phases.isEmpty() && phaseIndex >= 0 && phaseIndex < phases.size()) {
            isRunning.set(true);
            isCompleted.set(false);
            currentPhaseIndex.set(phaseIndex);
            
            // Reset all phases first
            phases.forEach(Phase::reset);
            
            // Set the starting phase as active
            phases.get(phaseIndex).setActive(true);
        }
    }
    
    public void prepareFromPhase(int phaseIndex) {
        if (!phases.isEmpty() && phaseIndex >= 0 && phaseIndex < phases.size()) {
            isRunning.set(false);
            isCompleted.set(false);
            currentPhaseIndex.set(phaseIndex);
            
            // Reset all phases first
            phases.forEach(Phase::reset);
            
            // Don't set any phase as active - just prepare the sequence
        }
    }

    public void pause() {
        isRunning.set(false);
        Phase current = getCurrentPhase();
        if (current != null) {
            current.setActive(false);
        }
    }

    public void reset() {
        isRunning.set(false);
        isCompleted.set(false);
        currentPhaseIndex.set(-1);
        phases.forEach(Phase::reset);
    }

    public void nextPhase() {
        int currentIndex = currentPhaseIndex.get();
        if (currentIndex >= 0 && currentIndex < phases.size()) {
            // Mark current phase as completed
            phases.get(currentIndex).setCompleted(true);
            phases.get(currentIndex).setActive(false);
        }

        int nextIndex = currentIndex + 1;
        if (nextIndex < phases.size()) {
            currentPhaseIndex.set(nextIndex);
            phases.get(nextIndex).setActive(true);
        } else {
            // All phases completed
            isRunning.set(false);
            isCompleted.set(true);
            currentPhaseIndex.set(-1);
        }
    }


    private void updateTotalDuration() {
        int total = phases.stream().mapToInt(Phase::getDurationMinutes).sum();
        totalDuration.set(total);
    }

    // Getters and setters
    public String getId() { return id; }

    public String getName() { return name.get(); }
    public StringProperty nameProperty() { return name; }
    public void setName(String name) { this.name.set(name); }

    public ObservableList<Phase> getPhases() { return phases; }

    public boolean isRunning() { return isRunning.get(); }
    public BooleanProperty isRunningProperty() { return isRunning; }

    public boolean isCompleted() { return isCompleted.get(); }
    public BooleanProperty isCompletedProperty() { return isCompleted; }

    public int getCurrentPhaseIndex() { return currentPhaseIndex.get(); }
    public IntegerProperty currentPhaseIndexProperty() { return currentPhaseIndex; }

    public int getTotalDuration() { return totalDuration.get(); }
    public IntegerProperty totalDurationProperty() { return totalDuration; }

    @Override
    public String toString() {
        return name.get() + " (" + phases.size() + " phases, " + totalDuration.get() + " min)";
    }
}
