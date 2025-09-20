# Chain Timer App

A modern, interactive JavaFX application for creating and managing timer sequences with multiple phases. Built using Java OOP principles and the Observer design pattern for clean, modular code architecture.

## Features

### 🎯 **Sequence Management**
- Create, edit, delete, and duplicate timer sequences
- Persistent storage - sequences are automatically saved and restored
- Clean, organized interface for managing multiple sequences

### ⏱️ **Phase System**
- Add multiple phases (timers) to each sequence
- Each phase has a customizable name and duration (1 minute to 8 hours)
- Visual indicators show phase status (pending, active, completed)

### 🎵 **Smart Timer System**
- Automatic phase progression - when one phase completes, the next begins
- Sound notifications when phases start and complete
- Pause, resume, and stop functionality
- Real-time countdown display

### 🎨 **Modern UI**
- Clean, professional design with intuitive controls
- Responsive layout that adapts to different screen sizes
- Color-coded buttons and visual feedback
- Hover effects and smooth interactions

## Architecture & Design Patterns

### **Observer Pattern**
- `TimerService` acts as the subject, managing timer events
- `MainController` implements `TimerListener` to receive notifications
- Clean separation between timer logic and UI updates

### **MVC Architecture**
- **Models**: `Phase`, `Sequence` with JavaFX properties for reactive UI
- **Views**: FXML layouts with modern CSS styling
- **Controllers**: Handle user interactions and coordinate between services

### **Service Layer**
- `TimerService`: Manages countdown logic and phase transitions
- `SequenceManager`: Handles persistence and sequence operations
- `SoundService`: Manages notification sounds

## Project Structure

```
src/main/java/com/timer/
├── Main.java                          # Application entry point
├── controller/                        # UI controllers
│   ├── MainController.java           # Main application controller
│   └── PhaseDialogController.java    # Phase creation/editing dialog
├── model/                            # Data models
│   ├── Phase.java                    # Individual timer phase
│   └── Sequence.java                 # Collection of phases
└── service/                          # Business logic services
    ├── TimerService.java             # Timer countdown management
    ├── TimerListener.java            # Observer interface
    ├── SequenceManager.java          # Sequence persistence
    └── SoundService.java             # Audio notifications
```

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher

### Running the Application

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd chain-timer
   ```

2. **Build the project**
   ```bash
   mvn clean compile
   ```

3. **Run the application**
   ```bash
   mvn javafx:run
   ```

   Or build and run the JAR:
   ```bash
   mvn clean package
   java -jar target/chain-timer-1.0.0.jar
   ```

## Usage Guide

### Creating a New Sequence
1. Click the "New" button in the Sequences panel
2. Enter a name for your sequence
3. Click "Add Phase" to add timer phases
4. Configure each phase with a name and duration

### Running a Sequence
1. Select a sequence from the list
2. Click "Start" to begin the timer
3. Use "Pause" to pause/resume, "Stop" to end early
4. Click "Reset" to reset all phases to their initial state

### Managing Phases
- **Add Phase**: Click "Add Phase" and configure name/duration
- **Edit Phase**: Select a phase and click "Edit"
- **Remove Phase**: Select a phase and click "Remove"
- **Reorder**: Phases execute in the order they appear

### Example Workflow
Create a workout sequence:
1. **Warm-up**: 5 minutes
2. **Cardio**: 20 minutes  
3. **Strength**: 30 minutes
4. **Cool-down**: 5 minutes

The app will automatically progress through each phase, playing sounds and updating the display.

## Technical Details

### **JavaFX Properties**
- All models use JavaFX properties for automatic UI updates
- Reactive binding ensures UI stays synchronized with data

### **Persistence**
- Sequences are automatically saved to `sequences.dat`
- Data survives application restarts
- Serialization handles complex object graphs

### **Sound System**
- Built-in notification sounds for phase events
- Extensible for custom sound files
- Graceful fallback if audio is unavailable

### **Error Handling**
- Comprehensive validation for user inputs
- Graceful error handling with user-friendly messages
- Robust state management prevents invalid operations

## Customization

### **Styling**
- Modify `src/main/resources/css/styles.css` for custom themes
- CSS classes are well-organized and documented
- Responsive design considerations included

### **Sound Files**
- Replace default notification sounds in the `SoundService`
- Support for various audio formats through JavaFX Media

### **Phase Limits**
- Adjust duration limits in `PhaseDialogController`
- Current range: 1 minute to 8 hours

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes with proper JavaDoc
4. Ensure all tests pass
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Future Enhancements

- [ ] Drag-and-drop phase reordering
- [ ] Import/export sequence files
- [ ] Custom notification sounds per phase
- [ ] Timer presets and templates
- [ ] Multi-language support
- [ ] Dark/light theme toggle
- [ ] Statistics and usage tracking
- [ ] Cloud synchronization

---

**Built with ❤️ using Java, JavaFX, and modern software design principles**
