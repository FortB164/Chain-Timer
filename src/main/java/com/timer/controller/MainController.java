package com.timer.controller;

import com.timer.model.Phase;
import com.timer.model.Sequence;
import com.timer.service.*;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.scene.input.*;
import javafx.scene.input.ClipboardContent;
import javafx.application.Platform;
import java.io.IOException;
import java.util.Optional;

/**
 * Main controller for the Chain Timer application
 * Coordinates between UI, services, and models
 */
public class MainController implements TimerListener {
    
    @FXML private VBox sequenceContainer;
    
    @FXML private VBox phaseListContainer;
    @FXML private VBox currentPhaseContainer;
    
    @FXML private Label currentSequenceTitleLabel;
    @FXML private Label currentPhaseLabel;
    @FXML private Label timeRemainingLabel;
    @FXML private Label sequenceProgressLabel;
    
    @FXML private Button startPauseButton;
    @FXML private Button resetButton;
    
    @FXML private Button newSequenceButton;
    @FXML private Button deleteSequenceButton;
    
    @FXML private Button addPhaseButton;
    @FXML private Button editPhaseButton;
    @FXML private Button removePhaseButton;
    
    private final SequenceManager sequenceManager;
    private final TimerService timerService;
    private final SoundService soundService;
    private final ObjectProperty<Sequence> selectedSequence;
    private final ObjectProperty<Phase> selectedPhase;
    private Sequence copiedSequence;

    public MainController() {
        this.sequenceManager = new SequenceManager();
        this.timerService = new TimerService();
        this.soundService = new SoundService();
        this.selectedSequence = new SimpleObjectProperty<>();
        this.selectedPhase = new SimpleObjectProperty<>();
        
        // Register as a timer listener
        timerService.addListener(this);
    }

    @FXML
    public void initialize() {
        setupSequenceBubbles();
        setupBindings();
        setupEventHandlers();
        refreshUI();
        
        // Setup keyboard shortcuts after the scene is ready
        Platform.runLater(() -> setupKeyboardShortcuts());
    }

    private void setupSequenceBubbles() {
        // Listen to sequence changes and refresh the bubble list
        sequenceManager.getSequences().addListener((javafx.collections.ListChangeListener<Sequence>) change -> {
            refreshSequenceBubbles();
        });
        
        // Initial load
        refreshSequenceBubbles();
        
        // Enable keyboard shortcuts
        setupKeyboardShortcuts();
    }
    
    private void refreshSequenceBubbles() {
        sequenceContainer.getChildren().clear();
        
        for (Sequence sequence : sequenceManager.getSequences()) {
            Node bubble = createSequenceBubble(sequence);
            sequenceContainer.getChildren().add(bubble);
        }
    }
    
    private Node createSequenceBubble(Sequence sequence) {
        VBox bubble = new VBox(5);
        bubble.getStyleClass().add("sequence-bubble");
        
        // Sequence name
        Label nameLabel = new Label(sequence.getName());
        nameLabel.getStyleClass().add("sequence-name");
        
        // Sequence details
        Label detailsLabel = new Label(
            String.format("%d phases • %d min total", 
                sequence.getPhases().size(), 
                sequence.getTotalDuration())
        );
        detailsLabel.getStyleClass().add("sequence-details");
        
        bubble.getChildren().addAll(nameLabel, detailsLabel);
        
        // Click handler for selection
        bubble.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1) {
                selectedSequence.set(sequence);
                bubble.requestFocus(); // Focus for keyboard shortcuts
                refreshUI();
            } else if (event.getClickCount() == 2) {
                // Double-click to edit name
                editSequenceName(sequence);
            }
        });
        
        // Make bubble focusable for keyboard shortcuts
        bubble.setFocusTraversable(true);
        
        // Update selection state
        if (sequence == selectedSequence.get()) {
            bubble.getStyleClass().add("selected");
        }
        
        return bubble;
    }
    
    private void editSequenceName(Sequence sequence) {
        TextInputDialog dialog = new TextInputDialog(sequence.getName());
        dialog.setTitle("Edit Sequence Name");
        dialog.setHeaderText("Enter new sequence name:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty() && !name.trim().equals(sequence.getName())) {
                sequence.setName(name.trim());
                sequenceManager.updateSequence(sequence);
                refreshSequenceBubbles();
            }
        });
    }

    private void setupBindings() {
        // Bind button states to selection and timer state
        startPauseButton.disableProperty().bind(
            selectedSequence.isNull()
        );
        
        resetButton.disableProperty().bind(
            selectedSequence.isNull()
        );
        
        deleteSequenceButton.disableProperty().bind(
            selectedSequence.isNull()
        );
        
        addPhaseButton.disableProperty().bind(
            selectedSequence.isNull()
        );
        
        editPhaseButton.disableProperty().bind(
            selectedSequence.isNull().or(selectedPhase.isNull())
        );
        
        removePhaseButton.disableProperty().bind(
            selectedSequence.isNull().or(selectedPhase.isNull())
        );
        
        // Initialize button state
        updateStartPauseButton();
        
        // Listen to timer state changes to update button
        timerService.isRunningProperty().addListener((obs, oldVal, newVal) -> {
            System.out.println("Timer state changed: " + oldVal + " -> " + newVal);
            updateStartPauseButton();
        });
        
        // Auto-select first sequence if only one exists
        sequenceManager.getSequences().addListener((javafx.collections.ListChangeListener<Sequence>) change -> {
            if (sequenceManager.getSequences().size() == 1 && selectedSequence.get() == null) {
                selectedSequence.set(sequenceManager.getSequences().get(0));
                refreshUI();
            }
        });
        
        // Auto-select first sequence on startup if available
        if (!sequenceManager.getSequences().isEmpty() && selectedSequence.get() == null) {
            selectedSequence.set(sequenceManager.getSequences().get(0));
            refreshUI();
        }
    }

    private void setupEventHandlers() {
        // Sequence management
        newSequenceButton.setOnAction(e -> createNewSequence());
        deleteSequenceButton.setOnAction(e -> deleteSelectedSequence());
        
        // Timer control
        startPauseButton.setOnAction(e -> toggleStartPause());
        resetButton.setOnAction(e -> resetSelectedSequence());
        
        // Phase management
        addPhaseButton.setOnAction(e -> addNewPhase());
        editPhaseButton.setOnAction(e -> editSelectedPhase());
        removePhaseButton.setOnAction(e -> removeSelectedPhase());
    }
    
    private void setupKeyboardShortcuts() {
        // Add global keyboard shortcuts to the main scene
        if (sequenceContainer.getScene() != null) {
            sequenceContainer.getScene().setOnKeyPressed(event -> {
                handleKeyPress(event);
            });
        }
        
        // Also add to the main container as fallback
        sequenceContainer.setOnKeyPressed(event -> {
            handleKeyPress(event);
        });
        
        phaseListContainer.setOnKeyPressed(event -> {
            handleKeyPress(event);
        });
        
        // Make containers focusable
        sequenceContainer.setFocusTraversable(true);
        phaseListContainer.setFocusTraversable(true);
    }
    
    private void handleKeyPress(KeyEvent event) {
        if (event.getCode().toString().equals("DELETE")) {
        if (selectedPhase.get() != null) {
            deleteSelectedPhase();
        } else if (selectedSequence.get() != null) {
                deleteSelectedSequence();
            }
        } else if (event.getCode().toString().equals("F2")) {
            if (selectedSequence.get() != null) {
                editSequenceName(selectedSequence.get());
            }
        } else if (event.isControlDown()) {
            if (event.getCode().toString().equals("C")) {
                copySelectedSequence();
            } else if (event.getCode().toString().equals("V")) {
                pasteSequence();
            }
        }
    }

    private void createNewSequence() {
        TextInputDialog dialog = new TextInputDialog("New Sequence");
        dialog.setTitle("Create New Sequence");
        dialog.setHeaderText("Enter sequence name:");
        dialog.setContentText("Name:");
        
        Optional<String> result = dialog.showAndWait();
        result.ifPresent(name -> {
            if (!name.trim().isEmpty()) {
                Sequence newSequence = sequenceManager.createSequence(name.trim());
                selectedSequence.set(newSequence);
                refreshUI();
            }
        });
    }


    private void deleteSelectedSequence() {
        if (selectedSequence.get() == null) return;
        
        // Delete immediately without confirmation
        sequenceManager.removeSequence(selectedSequence.get());
        selectedSequence.set(null);
        refreshUI();
    }
    

    private void copySelectedSequence() {
        if (selectedSequence.get() != null) {
            copiedSequence = selectedSequence.get();
        }
    }
    
    private void pasteSequence() {
        if (copiedSequence != null) {
            sequenceManager.duplicateSequence(copiedSequence);
        }
    }

    private void toggleStartPause() {
        if (selectedSequence.get() == null) return;
        
        if (timerService.isRunning()) {
            // Currently running, so pause
            timerService.pauseSequence();
        } else {
            // Not running, so start (or resume if same sequence)
            timerService.startSequence(selectedSequence.get());
        }
    }
    
    private void updateStartPauseButton() {
        // Use Platform.runLater to ensure UI updates happen on the JavaFX thread
        javafx.application.Platform.runLater(() -> {
            // Clear all existing style classes first
            startPauseButton.getStyleClass().removeAll("success-button", "warning-button", "primary-button");
            
            boolean isRunning = timerService.isRunning();
            System.out.println("Updating button - isRunning: " + isRunning);
            
            if (isRunning) {
                startPauseButton.setText("Pause");
                startPauseButton.getStyleClass().add("warning-button");
                System.out.println("Button set to Pause with warning style");
            } else {
                startPauseButton.setText("Start");
                startPauseButton.getStyleClass().add("success-button");
                System.out.println("Button set to Start with success style");
            }
        });
    }


    private void resetSelectedSequence() {
        if (selectedSequence.get() != null) {
            // Clear the current sequence from timer service and reset the selected sequence
            timerService.clearCurrentSequence();
            selectedSequence.get().reset();
            refreshUI();
        }
    }

    private void addNewPhase() {
        if (selectedSequence.get() == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/phase-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Add New Phase");
            stage.setScene(scene);
            
            PhaseDialogController controller = loader.getController();
            controller.setSequence(selectedSequence.get());
            controller.setPhaseManager(sequenceManager);
            
            stage.showAndWait();
            refreshUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void editSelectedPhase() {
        if (selectedPhase.get() == null) return;
        
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/phase-dialog.fxml"));
            Scene scene = new Scene(loader.load());
            Stage stage = new Stage();
            stage.setTitle("Edit Phase");
            stage.setScene(scene);
            
            PhaseDialogController controller = loader.getController();
            controller.setSequence(selectedSequence.get());
            controller.setPhaseManager(sequenceManager);
            controller.setPhaseToEdit(selectedPhase.get());
            
            stage.showAndWait();
            refreshUI();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void removeSelectedPhase() {
        if (selectedPhase.get() == null || selectedSequence.get() == null) return;
        
        selectedSequence.get().removePhase(selectedPhase.get());
        sequenceManager.updateSequence(selectedSequence.get());
        selectedPhase.set(null);
        refreshUI();
    }
    
    private void deleteSelectedPhase() {
        removeSelectedPhase();
    }

    private void refreshUI() {
        refreshSequenceBubbles(); // Update selection states
        if (selectedSequence.get() != null) {
            refreshPhaseList();
            refreshCurrentPhaseDisplay();
        } else {
            clearPhaseList();
            clearCurrentPhaseDisplay();
        }
    }

    private void refreshPhaseList() {
        phaseListContainer.getChildren().clear();
        
        for (int i = 0; i < selectedSequence.get().getPhases().size(); i++) {
            Phase phase = selectedSequence.get().getPhases().get(i);
            Node phaseNode = createPhaseNode(phase, i);
            phaseListContainer.getChildren().add(phaseNode);
        }
    }

    private Node createPhaseNode(Phase phase, int index) {
        HBox phaseBox = new HBox(10);
        phaseBox.getStyleClass().add("phase-item");
        
        Label indexLabel = new Label(String.valueOf(index + 1));
        indexLabel.getStyleClass().add("phase-index");
        
        Label nameLabel = new Label(phase.getName());
        nameLabel.getStyleClass().add("phase-name");
        
        Label durationLabel = new Label(phase.getDurationMinutes() + " min");
        durationLabel.getStyleClass().add("phase-duration");
        
        Label statusLabel = new Label();
        statusLabel.textProperty().bind(
            Bindings.createStringBinding(() -> {
                if (phase.isCompleted()) return "✓";
                if (phase.isActive()) return "▶";
                // Check if this phase is prepared (ready to start)
                if (selectedSequence.get() != null && 
                    selectedSequence.get().getCurrentPhaseIndex() == index &&
                    !timerService.isRunning()) {
                    return "▶"; // Play triangle for prepared phase
                }
                return "○";
            }, phase.isCompletedProperty(), phase.isActiveProperty(), 
               selectedSequence, timerService.isRunningProperty())
        );
        statusLabel.getStyleClass().add("phase-status");
        
        HBox.setHgrow(nameLabel, Priority.ALWAYS);
        
        phaseBox.getChildren().addAll(indexLabel, nameLabel, durationLabel, statusLabel);
        
        // Add selection handling
        phaseBox.setOnMouseClicked(e -> {
            if (e.getClickCount() == 1) {
                selectedPhase.set(phase);
                phaseBox.requestFocus(); // Focus for keyboard shortcuts
                refreshUI();
            } else if (e.getClickCount() == 2) {
                // Double-click to prepare sequence from this phase (show as ready)
                if (selectedSequence.get() != null) {
                    int phaseIndex = selectedSequence.get().getPhases().indexOf(phase);
                    if (phaseIndex >= 0) {
                        timerService.prepareSequenceFromPhase(selectedSequence.get(), phaseIndex);
                    }
                }
            }
        });
        
        // Make phase box focusable for keyboard shortcuts
        phaseBox.setFocusTraversable(true);
        
        // Add drag and drop functionality
        setupDragAndDrop(phaseBox, phase, index);
        
        if (phase == selectedPhase.get()) {
            phaseBox.getStyleClass().add("selected");
        }
        
        return phaseBox;
    }
    
    private void setupDragAndDrop(HBox phaseBox, Phase phase, int index) {
        // Make the phase box draggable
        phaseBox.setOnDragDetected(event -> {
            if (selectedSequence.get() == null) return;
            
            Dragboard dragboard = phaseBox.startDragAndDrop(TransferMode.MOVE);
            ClipboardContent content = new ClipboardContent();
            content.putString(String.valueOf(index));
            dragboard.setContent(content);
            
            // Visual feedback
            phaseBox.getStyleClass().add("dragging");
            
            event.consume();
        });
        
        // Handle drag over
        phaseBox.setOnDragOver(event -> {
            if (event.getGestureSource() != phaseBox && event.getDragboard().hasString()) {
                event.acceptTransferModes(TransferMode.MOVE);
                phaseBox.getStyleClass().add("drag-over");
            }
            event.consume();
        });
        
        // Handle drag exit
        phaseBox.setOnDragExited(event -> {
            phaseBox.getStyleClass().remove("drag-over");
            event.consume();
        });
        
        // Handle drop
        phaseBox.setOnDragDropped(event -> {
            Dragboard dragboard = event.getDragboard();
            boolean success = false;
            
            if (dragboard.hasString()) {
                try {
                    int draggedIndex = Integer.parseInt(dragboard.getString());
                    int targetIndex = index;
                    
                    if (draggedIndex != targetIndex) {
                        reorderPhases(draggedIndex, targetIndex);
                        success = true;
                    }
                } catch (NumberFormatException e) {
                    // Invalid drag data
                }
            }
            
            event.setDropCompleted(success);
            phaseBox.getStyleClass().remove("dragging");
            phaseBox.getStyleClass().remove("drag-over");
            event.consume();
        });
        
        // Handle drag done
        phaseBox.setOnDragDone(event -> {
            phaseBox.getStyleClass().remove("dragging");
            phaseBox.getStyleClass().remove("drag-over");
            event.consume();
        });
    }
    
    private void reorderPhases(int fromIndex, int toIndex) {
        if (selectedSequence.get() == null) return;
        
        // Get the phase to move
        Phase phaseToMove = selectedSequence.get().getPhases().get(fromIndex);
        
        // Remove from original position
        selectedSequence.get().getPhases().remove(fromIndex);
        
        // Adjust target index if necessary
        if (toIndex > fromIndex) {
            toIndex--;
        }
        
        // Insert at new position
        selectedSequence.get().getPhases().add(toIndex, phaseToMove);
        
        // Update the sequence manager
        sequenceManager.updateSequence(selectedSequence.get());
        
        // Refresh the UI
        refreshUI();
    }

    private void refreshCurrentPhaseDisplay() {
        if (selectedSequence.get() != null) {
            // Update sequence title
            currentSequenceTitleLabel.setText(selectedSequence.get().getName());
            
            if (timerService.getCurrentSequence() == selectedSequence.get() && timerService.isRunning()) {
                // Timer is actively running
                Phase currentPhase = selectedSequence.get().getCurrentPhase();
                if (currentPhase != null) {
                    currentPhaseLabel.setText("Current: " + currentPhase.getName());
                    timeRemainingLabel.setText(currentPhase.getFormattedTime());
                    
                    int currentIndex = selectedSequence.get().getCurrentPhaseIndex();
                    int totalPhases = selectedSequence.get().getPhases().size();
                    sequenceProgressLabel.setText(
                        String.format("Phase %d of %d", currentIndex + 1, totalPhases)
                    );
                }
            } else if (timerService.getCurrentSequence() == selectedSequence.get() && !timerService.isRunning()) {
                // Timer is paused (not reset)
                Phase currentPhase = selectedSequence.get().getCurrentPhase();
                if (currentPhase != null) {
                    currentPhaseLabel.setText("Paused: " + currentPhase.getName());
                    timeRemainingLabel.setText(currentPhase.getFormattedTime());
                    
                    int currentIndex = selectedSequence.get().getCurrentPhaseIndex();
                    int totalPhases = selectedSequence.get().getPhases().size();
                    sequenceProgressLabel.setText(
                        String.format("Phase %d of %d", currentIndex + 1, totalPhases)
                    );
                }
            } else {
                // Sequence selected but not running - show first phase info (reset state)
                if (!selectedSequence.get().getPhases().isEmpty()) {
                    Phase firstPhase = selectedSequence.get().getPhases().get(0);
                    currentPhaseLabel.setText("Ready: " + firstPhase.getName());
                    timeRemainingLabel.setText(firstPhase.getFormattedTime());
                    sequenceProgressLabel.setText("Phase 1 of " + selectedSequence.get().getPhases().size());
                } else {
                    currentPhaseLabel.setText("No phases in sequence");
                    timeRemainingLabel.setText("--:--");
                    sequenceProgressLabel.setText("");
                }
            }
        } else {
            clearCurrentPhaseDisplay();
        }
    }

    private void clearPhaseList() {
        phaseListContainer.getChildren().clear();
    }

    private void clearCurrentPhaseDisplay() {
        currentSequenceTitleLabel.setText("No sequence selected");
        currentPhaseLabel.setText("No sequence running");
        timeRemainingLabel.setText("--:--");
        sequenceProgressLabel.setText("");
    }

    // TimerListener implementation
    @Override
    public void onSequenceStarted(Sequence sequence) {
        refreshUI();
        soundService.playNotification();
    }

    @Override
    public void onSequencePaused(Sequence sequence) {
        refreshUI();
    }

    @Override
    public void onSequencePrepared(Sequence sequence) {
        refreshUI();
    }

    @Override
    public void onSequenceStopped(Sequence sequence) {
        refreshUI();
    }

    @Override
    public void onSequenceCompleted(Sequence sequence) {
        refreshUI();
        soundService.playNotification();
    }

    @Override
    public void onPhaseStarted(Phase phase) {
        refreshUI();
        soundService.playNotification();
    }

    @Override
    public void onPhaseTick(Phase phase) {
        refreshCurrentPhaseDisplay();
    }

    @Override
    public void onPhaseCompleted(Phase phase) {
        refreshUI();
        soundService.playNotification();
    }

    public void dispose() {
        timerService.dispose();
        soundService.dispose();
    }
}
