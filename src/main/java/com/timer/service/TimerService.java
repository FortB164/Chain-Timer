package com.timer.service;

import com.timer.model.Phase;
import com.timer.model.Sequence;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.media.AudioClip;
import javafx.util.Duration;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Service that manages timer countdown and phase transitions
 * Uses Observer pattern to notify listeners of timer events
 */
public class TimerService {
    private final Timeline timeline;
    private final ObjectProperty<Sequence> currentSequence;
    private final List<TimerListener> listeners;
    private final BooleanProperty isRunning;

    public TimerService() {
        this.timeline = new Timeline();
        this.listeners = new ArrayList<>();
        this.isRunning = new SimpleBooleanProperty(false);
        this.currentSequence = new SimpleObjectProperty<>();
        
        // Set up the timeline to tick every second
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), event -> tick()));
    }

    public void startSequence(Sequence sequence) {
        if (isRunning.get()) {
            stopSequence();
        }
        
        currentSequence.set(sequence);
        sequence.start();
        isRunning.set(true);
        timeline.play();
        
        notifySequenceStarted(sequence);
    }
    
    public void startSequenceFromPhase(Sequence sequence, int phaseIndex) {
        if (isRunning.get()) {
            stopSequence();
        }
        
        currentSequence.set(sequence);
        sequence.startFromPhase(phaseIndex);
        isRunning.set(true);
        timeline.play();
        
        notifySequenceStarted(sequence);
    }
    
    public void prepareSequenceFromPhase(Sequence sequence, int phaseIndex) {
        if (isRunning.get()) {
            stopSequence();
        }
        
        currentSequence.set(sequence);
        sequence.prepareFromPhase(phaseIndex);
        isRunning.set(false);
        
        // Notify that sequence is prepared but not started
        notifySequencePrepared(sequence);
    }

    public void pauseSequence() {
        if (currentSequence.get() != null && isRunning.get()) {
            currentSequence.get().pause();
            timeline.pause();
            isRunning.set(false);
            notifySequencePaused(currentSequence.get());
        }
    }


    public void stopSequence() {
        if (currentSequence.get() != null) {
            currentSequence.get().reset();
            timeline.stop();
            isRunning.set(false);
            notifySequenceStopped(currentSequence.get());
        }
    }
    
    public void clearCurrentSequence() {
        if (currentSequence.get() != null) {
            currentSequence.get().reset();
            timeline.stop();
            isRunning.set(false);
            currentSequence.set(null);
        }
    }

    private void tick() {
        if (currentSequence.get() == null || !isRunning.get()) {
            return;
        }

        Phase currentPhase = currentSequence.get().getCurrentPhase();
        if (currentPhase == null) {
            return;
        }

        // Decrease remaining time
        int remaining = currentPhase.getRemainingSeconds();
        if (remaining > 0) {
            currentPhase.setRemainingSeconds(remaining - 1);
            notifyPhaseTick(currentPhase);
        } else {
            // Phase completed
            currentPhase.setCompleted(true);
            currentPhase.setActive(false);
            notifyPhaseCompleted(currentPhase);
            
            // Pause timer and wait for sound to finish
            timeline.pause();
            isRunning.set(false);
            
            // Play sound and wait for it to finish before moving to next phase
            playPhaseEndSoundAndContinue(currentPhase);
        }
    }
    
    private void playPhaseEndSound(Phase phase) {
        String endSound = phase.getEndSound();
        if (endSound == null || endSound.isEmpty()) {
            return; // No sound configured
        }
        
        try {
            // Try to load MP3 sound from assets folder
            String soundPath = "assets/" + endSound.toLowerCase() + ".mp3";
            File soundFile = new File(soundPath);
            
            if (soundFile.exists()) {
                AudioClip audioClip = new AudioClip(soundFile.toURI().toString());
                audioClip.setVolume(0.7); // Set volume to 70%
                audioClip.play();
            } else {
                // Fallback: use system beep for now
                System.out.println("Sound file not found: " + soundPath + ", using system beep");
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
            // Fallback to system beep
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }
    
    private void playPhaseEndSoundAndContinue(Phase phase) {
        String endSound = phase.getEndSound();
        if (endSound == null || endSound.isEmpty()) {
            // No sound configured, move to next phase immediately
            moveToNextPhase();
            return;
        }
        
        try {
            // Try to load MP3 sound from assets folder
            String soundPath = "assets/" + endSound.toLowerCase() + ".mp3";
            File soundFile = new File(soundPath);
            
            if (soundFile.exists()) {
                AudioClip audioClip = new AudioClip(soundFile.toURI().toString());
                audioClip.setVolume(0.7); // Set volume to 70%
                
                // Play the sound
                audioClip.play();
                
                // Since AudioClip doesn't have onEndOfMedia, we'll estimate the duration
                // and use a timer to continue after the sound should finish
                // For now, use a reasonable delay (3 seconds) for most alarm sounds
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(3));
                delay.setOnFinished(event -> moveToNextPhase());
                delay.play();
            } else {
                // Fallback: use system beep and continue after short delay
                System.out.println("Sound file not found: " + soundPath + ", using system beep");
                java.awt.Toolkit.getDefaultToolkit().beep();
                
                // Wait a bit for the beep to finish
                javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(0.5));
                delay.setOnFinished(event -> moveToNextPhase());
                delay.play();
            }
        } catch (Exception e) {
            System.err.println("Error playing sound: " + e.getMessage());
            // Fallback to system beep and continue
            java.awt.Toolkit.getDefaultToolkit().beep();
            
            javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(Duration.seconds(0.5));
            delay.setOnFinished(event -> moveToNextPhase());
            delay.play();
        }
    }
    
    private void moveToNextPhase() {
        // Move to next phase
        currentSequence.get().nextPhase();

        if (currentSequence.get().isCompleted()) {
            // All phases completed
            stopSequence();
            notifySequenceCompleted(currentSequence.get());
        } else {
            // Start next phase
            Phase nextPhase = currentSequence.get().getCurrentPhase();
            if (nextPhase != null) {
                isRunning.set(true);
                timeline.play();
                notifyPhaseStarted(nextPhase);
            }
        }
    }

    // Observer pattern implementation
    public void addListener(TimerListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimerListener listener) {
        listeners.remove(listener);
    }

    private void notifySequenceStarted(Sequence sequence) {
        listeners.forEach(listener -> listener.onSequenceStarted(sequence));
    }

    private void notifySequencePaused(Sequence sequence) {
        listeners.forEach(listener -> listener.onSequencePaused(sequence));
    }

    private void notifySequencePrepared(Sequence sequence) {
        listeners.forEach(listener -> listener.onSequencePrepared(sequence));
    }

    private void notifySequenceStopped(Sequence sequence) {
        listeners.forEach(listener -> listener.onSequenceStopped(sequence));
    }

    private void notifySequenceCompleted(Sequence sequence) {
        listeners.forEach(listener -> listener.onSequenceCompleted(sequence));
    }

    private void notifyPhaseStarted(Phase phase) {
        listeners.forEach(listener -> listener.onPhaseStarted(phase));
    }

    private void notifyPhaseTick(Phase phase) {
        listeners.forEach(listener -> listener.onPhaseTick(phase));
    }

    private void notifyPhaseCompleted(Phase phase) {
        listeners.forEach(listener -> listener.onPhaseCompleted(phase));
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    public BooleanProperty isRunningProperty() {
        return isRunning;
    }

    public Sequence getCurrentSequence() {
        return currentSequence.get();
    }

    public ObjectProperty<Sequence> currentSequenceProperty() {
        return currentSequence;
    }

    public void dispose() {
        stopSequence();
        timeline.stop();
        listeners.clear();
    }
}
