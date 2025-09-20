package com.timer.controller;

import com.timer.model.Phase;
import com.timer.model.Sequence;
import com.timer.service.SequenceManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Controller for the phase dialog (add/edit phases)
 */
public class PhaseDialogController {
    
    @FXML private TextField nameField;
    @FXML private Spinner<Integer> minutesSpinner;
    @FXML private Spinner<Integer> secondsSpinner;
    @FXML private ComboBox<String> soundComboBox;
    @FXML private Button noSoundButton;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    
    private Sequence sequence;
    private Phase phaseToEdit;
    private SequenceManager phaseManager;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        setupDurationSpinner();
        setupSoundSelection();
        setupValidation();
        setupEventHandlers();
    }

    private void setupDurationSpinner() {
        // Minutes spinner: 0 to 480 minutes (8 hours)
        SpinnerValueFactory<Integer> minutesFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 480, 5);
        minutesSpinner.setValueFactory(minutesFactory);
        minutesSpinner.setEditable(true);
        
        // Seconds spinner: 0 to 59 seconds
        SpinnerValueFactory<Integer> secondsFactory = 
            new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 59, 0);
        secondsSpinner.setValueFactory(secondsFactory);
        secondsSpinner.setEditable(true);
        
        // Handle manual input for minutes
        minutesSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= 0 && value <= 480) {
                    minutesSpinner.getValueFactory().setValue(value);
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        });
        
        // Handle manual input for seconds
        secondsSpinner.getEditor().textProperty().addListener((obs, oldValue, newValue) -> {
            try {
                int value = Integer.parseInt(newValue);
                if (value >= 0 && value <= 59) {
                    secondsSpinner.getValueFactory().setValue(value);
                }
            } catch (NumberFormatException e) {
                // Invalid input, ignore
            }
        });
        
        // Ensure at least 1 second total duration
        minutesSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == 0 && secondsSpinner.getValue() == 0) {
                secondsSpinner.getValueFactory().setValue(1);
            }
        });
        
        secondsSpinner.valueProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == 0 && minutesSpinner.getValue() == 0) {
                secondsSpinner.getValueFactory().setValue(1);
            }
        });
    }

    private void setupSoundSelection() {
        // Load available MP3 files from assets folder
        loadAvailableSounds();

        // Always include an "Import..." option at the end
        addImportOption();

        // If the only option is Import..., keep combo enabled
        soundComboBox.setDisable(false);

        // Handle selection of Import...
        soundComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.equals("Import...")) {
                // Revert selection temporarily to avoid keeping "Import..." selected
                soundComboBox.getSelectionModel().select(oldVal);
                handleImportSound();
            }
        });
        
        // No sound button handler
        noSoundButton.setOnAction(e -> {
            soundComboBox.getSelectionModel().clearSelection();
        });
    }
    
    private void loadAvailableSounds() {
        File assetsDir = new File("assets");
        if (assetsDir.exists() && assetsDir.isDirectory()) {
            File[] mp3Files = assetsDir.listFiles((dir, name) -> 
                name.toLowerCase().endsWith(".mp3"));
            
            if (mp3Files != null) {
                for (File mp3File : mp3Files) {
                    // Remove .mp3 extension and add to combo box
                    String soundName = mp3File.getName().substring(0, mp3File.getName().lastIndexOf('.'));
                    soundComboBox.getItems().add(soundName);
                }
            }
        }
        
        // If no MP3 files found, add a placeholder
        // Do not disable the combo; allow Import... even when empty
    }

    private void addImportOption() {
        if (!soundComboBox.getItems().contains("Import...")) {
            soundComboBox.getItems().add("Import...");
        }
    }

    private void handleImportSound() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Import Sound");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Audio Files (*.mp3)", "*.mp3")
        );

        File selectedFile = fileChooser.showOpenDialog(saveButton.getScene().getWindow());
        if (selectedFile == null) {
            return;
        }

        try {
            String importedBaseName = copyToAssetsWithAutoRename(selectedFile.toPath());
            refreshSoundListAndSelect(importedBaseName);
        } catch (IOException ex) {
            showError("Failed to import sound: " + ex.getMessage());
        }
    }

    private String copyToAssetsWithAutoRename(Path sourcePath) throws IOException {
        Path assetsDir = Path.of("assets");
        if (!Files.exists(assetsDir)) {
            Files.createDirectories(assetsDir);
        }

        String fileName = sourcePath.getFileName().toString();
        String base = fileName;
        String ext = "";
        int dot = fileName.lastIndexOf('.');
        if (dot != -1) {
            base = fileName.substring(0, dot);
            ext = fileName.substring(dot); // includes dot
        }

        Path target = assetsDir.resolve(fileName);
        int counter = 1;
        while (Files.exists(target)) {
            target = assetsDir.resolve(base + " (" + counter + ")" + ext);
            counter++;
        }

        Files.copy(sourcePath, target, StandardCopyOption.COPY_ATTRIBUTES);

        return target.getFileName().toString().replaceFirst("\\.mp3$", "");
    }

    private void refreshSoundListAndSelect(String baseNameWithoutExt) {
        soundComboBox.getItems().clear();
        loadAvailableSounds();
        addImportOption();
        soundComboBox.getSelectionModel().select(baseNameWithoutExt);
    }

    private void setupValidation() {
        // Save button is enabled only when name is not empty
        saveButton.disableProperty().bind(
            nameField.textProperty().isEmpty()
        );
    }

    private void setupEventHandlers() {
        saveButton.setOnAction(e -> savePhase());
        cancelButton.setOnAction(e -> closeDialog());
        
        // Handle Enter key in name field
        nameField.setOnAction(e -> savePhase());
    }

    public void setSequence(Sequence sequence) {
        this.sequence = sequence;
    }

    public void setPhaseManager(SequenceManager phaseManager) {
        this.phaseManager = phaseManager;
    }

    public void setPhaseToEdit(Phase phase) {
        this.phaseToEdit = phase;
        this.isEditMode = true;
        
        // Populate fields with existing data
        nameField.setText(phase.getName());
        
        // Convert total seconds to minutes and seconds
        int totalSeconds = phase.getTotalDurationSeconds();
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        minutesSpinner.getValueFactory().setValue(minutes);
        secondsSpinner.getValueFactory().setValue(seconds);
        
        // Set sound selection
        String endSound = phase.getEndSound();
        if (endSound != null && !endSound.isEmpty()) {
            soundComboBox.getSelectionModel().select(endSound);
        } else {
            soundComboBox.getSelectionModel().clearSelection();
        }
        
        // Update dialog title
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.setTitle("Edit Phase");
    }

    private void savePhase() {
        String name = nameField.getText().trim();
        int minutes = minutesSpinner.getValue();
        int seconds = secondsSpinner.getValue();
        int totalSeconds = minutes * 60 + seconds;
        String selectedSound = soundComboBox.getSelectionModel().getSelectedItem();
        
        if (name.isEmpty()) {
            showError("Name cannot be empty");
            return;
        }
        
        if (totalSeconds < 1) {
            showError("Duration must be at least 1 second");
            return;
        }
        
        try {
            if (isEditMode && phaseToEdit != null) {
                // Update existing phase
                phaseToEdit.setName(name);
                phaseToEdit.setDurationSeconds(totalSeconds);
                phaseToEdit.setEndSound(selectedSound != null ? selectedSound : "");
                phaseManager.updateSequence(sequence);
            } else {
                // Create new phase - use a temporary duration, then set the correct seconds
                Phase newPhase = new Phase(name, 1); // Temporary 1 minute
                newPhase.setDurationSeconds(totalSeconds); // Set the actual duration
                newPhase.setEndSound(selectedSound != null ? selectedSound : "");
                sequence.addPhase(newPhase);
                phaseManager.updateSequence(sequence);
            }
            
            closeDialog();
        } catch (Exception e) {
            showError("Failed to save phase: " + e.getMessage());
        }
    }

    private void closeDialog() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText("Validation Error");
        alert.setContentText(message);
        alert.showAndWait();
    }
}
