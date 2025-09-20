package com.timer.service;

import com.timer.model.Phase;
import com.timer.model.Sequence;

/**
 * Interface for objects that want to be notified of timer events
 * Part of the Observer pattern implementation
 */
public interface TimerListener {
    void onSequenceStarted(Sequence sequence);
    void onSequencePaused(Sequence sequence);
    void onSequencePrepared(Sequence sequence);
    void onSequenceStopped(Sequence sequence);
    void onSequenceCompleted(Sequence sequence);
    void onPhaseStarted(Phase phase);
    void onPhaseTick(Phase phase);
    void onPhaseCompleted(Phase phase);
}
