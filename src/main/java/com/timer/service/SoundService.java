package com.timer.service;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;
import java.net.URL;

/**
 * Service for playing notification sounds
 */
public class SoundService {
    private MediaPlayer mediaPlayer;
    private static final String DEFAULT_SOUND = "/sounds/notification.wav";

    public SoundService() {
        initializeSound();
    }

    private void initializeSound() {
        try {
            // Try to load a default sound file
            URL soundUrl = getClass().getResource(DEFAULT_SOUND);
            if (soundUrl != null) {
                Media media = new Media(soundUrl.toString());
                mediaPlayer = new MediaPlayer(media);
            }
        } catch (Exception e) {
            System.err.println("Could not initialize sound: " + e.getMessage());
        }
    }

    public void playNotification() {
        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
                mediaPlayer.play();
            } catch (Exception e) {
                System.err.println("Could not play sound: " + e.getMessage());
            }
        }
    }

    public void setSoundFile(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                Media media = new Media(file.toURI().toString());
                if (mediaPlayer != null) {
                    mediaPlayer.dispose();
                }
                mediaPlayer = new MediaPlayer(media);
            }
        } catch (Exception e) {
            System.err.println("Could not load sound file: " + e.getMessage());
        }
    }

    public void dispose() {
        if (mediaPlayer != null) {
            mediaPlayer.dispose();
        }
    }
}
