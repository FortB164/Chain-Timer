package com.timer.service;

import com.timer.model.Phase;
import com.timer.model.Sequence;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Manages the storage and retrieval of sequences
 * Implements persistence for saving and loading sequences
 */
public class SequenceManager {
    private final ObservableList<Sequence> sequences;
    private static final String SAVE_FILE = "sequences.dat";

    public SequenceManager() {
        this.sequences = FXCollections.observableArrayList();
        loadSequences();
    }

    public void addSequence(Sequence sequence) {
        sequences.add(sequence);
        saveSequences();
    }

    public void removeSequence(Sequence sequence) {
        sequences.remove(sequence);
        saveSequences();
    }

    public void updateSequence(Sequence sequence) {
        // Find and update the sequence
        for (int i = 0; i < sequences.size(); i++) {
            if (sequences.get(i).getId().equals(sequence.getId())) {
                sequences.set(i, sequence);
                break;
            }
        }
        saveSequences();
    }

    public ObservableList<Sequence> getSequences() {
        return sequences;
    }

    public Sequence getSequenceById(String id) {
        return sequences.stream()
                .filter(seq -> seq.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    public Sequence createSequence(String name) {
        Sequence sequence = new Sequence(name);
        addSequence(sequence);
        return sequence;
    }

    public void duplicateSequence(Sequence original) {
        Sequence duplicate = new Sequence(original.getName() + " (Copy)");
        
        // Copy all phases
        for (Phase originalPhase : original.getPhases()) {
            Phase newPhase = new Phase(originalPhase.getName(), originalPhase.getDurationMinutes());
            duplicate.addPhase(newPhase);
        }
        
        addSequence(duplicate);
    }

    private void saveSequences() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(SAVE_FILE))) {
            List<SerializableSequence> serializableSequences = new ArrayList<>();
            
            for (Sequence sequence : sequences) {
                SerializableSequence ss = new SerializableSequence();
                ss.id = sequence.getId();
                ss.name = sequence.getName();
                ss.phases = new ArrayList<>();
                
                for (Phase phase : sequence.getPhases()) {
                    SerializablePhase sp = new SerializablePhase();
                    sp.name = phase.getName();
                    sp.durationMinutes = phase.getDurationMinutes();
                    sp.totalDurationSeconds = phase.getTotalDurationSeconds();
                    sp.endSound = phase.getEndSound() != null ? phase.getEndSound() : "";
                    ss.phases.add(sp);
                }
                
                serializableSequences.add(ss);
            }
            
            oos.writeObject(serializableSequences);
        } catch (IOException e) {
            System.err.println("Failed to save sequences: " + e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void loadSequences() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) {
            return;
        }

        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            List<SerializableSequence> serializableSequences = (List<SerializableSequence>) ois.readObject();
            
            for (SerializableSequence ss : serializableSequences) {
                Sequence sequence = new Sequence(ss.name);
                
                for (SerializablePhase sp : ss.phases) {
                    Phase phase = new Phase(sp.name, sp.durationMinutes);
                    // Set the actual duration in seconds and end sound
                    phase.setDurationSeconds(sp.totalDurationSeconds);
                    phase.setEndSound(sp.endSound != null ? sp.endSound : "");
                    sequence.addPhase(phase);
                }
                
                sequences.add(sequence);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Failed to load sequences: " + e.getMessage());
        }
    }

    // Helper classes for serialization
    private static class SerializableSequence implements Serializable {
        private static final long serialVersionUID = 1L;
        String id;
        String name;
        List<SerializablePhase> phases;
    }

    private static class SerializablePhase implements Serializable {
        private static final long serialVersionUID = 1L;
        String name;
        int durationMinutes;
        int totalDurationSeconds;
        String endSound;
    }
}
