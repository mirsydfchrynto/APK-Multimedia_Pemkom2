package multimedia;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.BorderPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class PlayerController {

    @FXML
    private BorderPane root;
    @FXML
    private MediaView mediaView;
    @FXML
    private ListView<String> playlistView;
    @FXML
    private Button playPauseBtn;
    @FXML
    private Slider seekSlider;
    @FXML
    private Slider volumeSlider;
    @FXML
    private Label timeLabel;

    private final ObservableList<String> playlist = FXCollections.observableArrayList();
    private final List<String> sources = new ArrayList<>();
    private MediaPlayer player;
    private int index = -1;
    private static final Duration SEEK_STEP = Duration.seconds(10);

    private ChangeListener<Duration> currentTimeListener;

    @FXML
    public void initialize() {
        playlistView.setItems(playlist);

        // Initialize currentTimeListener once
        currentTimeListener = (obs, oldTime, newTime) -> {
            if (player != null && !seekSlider.isValueChanging()) {
                seekSlider.setValue(newTime.toSeconds());
                timeLabel.setText(formatTime(newTime, player.getMedia().getDuration()));
            }
        };

        volumeSlider.setValue(50);
        volumeSlider.valueProperty().addListener((obs, oldV, newV) -> {
            if (player != null) {
                player.setVolume(newV.doubleValue() / 100d);
            }
        });

        root.setOnDragOver(e -> {
            if (e.getDragboard().hasFiles()) {
                e.acceptTransferModes(TransferMode.COPY);
            }
            e.consume();
        });
        root.setOnDragDropped(this::handleDrop);
    }

    @FXML
    private void openFiles(ActionEvent ignored) {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Media Files", "*.mp4", "*.mp3", "*.wav"));
        List<File> files = fc.showOpenMultipleDialog(root.getScene().getWindow());
        addFiles(files);
    }

    @FXML
    private void removeSelected(ActionEvent ignored) {
        int selected = playlistView.getSelectionModel().getSelectedIndex();
        if (selected >= 0) {
            // Kalau lagu yang dihapus adalah yang sedang diputar
            if (selected == index && player != null) {
                player.currentTimeProperty().removeListener(currentTimeListener);
                player.stop();
                player.dispose();
                player = null;

                playlist.remove(selected);
                sources.remove(selected);

                if (!sources.isEmpty()) {
                    if (selected >= sources.size()) {
                        selected = sources.size() - 1;
                    }
                    play(selected);
                } else {
                    index = -1;
                    playPauseBtn.setText("▶");
                    timeLabel.setText("00:00 / 00:00");
                    seekSlider.setValue(0);
                }
            } else {
                // Jika yang dihapus bukan lagu yang sedang diputar
                playlist.remove(selected);
                sources.remove(selected);

                if (selected < index) {
                    index--;
                }
            }
        }
    }

    @FXML
    private void playPause(ActionEvent ignored) {
        togglePlay();
    }

    @FXML
    private void prev(ActionEvent ignored) {
        if (index > 0) {
            play(--index);
        }
    }

    @FXML
    private void next(ActionEvent ignored) {
        if (index < sources.size() - 1) {
            play(++index);
        }
    }

    @FXML
    private void seekBack(ActionEvent ignored) {
        seekRelative(SEEK_STEP.negate());
    }

    @FXML
    private void seekForward(ActionEvent ignored) {
        seekRelative(SEEK_STEP);
    }

    private void handleDrop(DragEvent e) {
        addFiles(e.getDragboard().getFiles());
        e.setDropCompleted(true);
        e.consume();
    }

    private void addFiles(List<File> files) {
        if (files == null || files.isEmpty()) {
            return;
        }
        for (File f : files) {
            sources.add(f.toURI().toString());
            playlist.add(f.getName());
        }
        if (player == null && !sources.isEmpty()) {
            play(0);
        }
    }

    private void play(int idx) {
        if (idx < 0 || idx >= sources.size()) {
            return;
        }

        if (player != null) {
            player.currentTimeProperty().removeListener(currentTimeListener);
            player.stop();
            player.dispose();
            player = null;
        }

        player = new MediaPlayer(new Media(sources.get(idx)));
        mediaView.setMediaPlayer(player);
        index = idx;
        playlistView.getSelectionModel().select(idx);

        player.currentTimeProperty().addListener(currentTimeListener);

        player.setOnReady(() -> {
            seekSlider.setMax(player.getMedia().getDuration().toSeconds());
            volumeSlider.setValue(player.getVolume() * 100);
            timeLabel.setText(formatTime(Duration.ZERO, player.getMedia().getDuration()));
        });

        seekSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (seekSlider.isValueChanging() && player != null) {
                player.seek(Duration.seconds(newVal.doubleValue()));
            }
        });

        player.setOnEndOfMedia(() -> next(null));

        player.play();
        playPauseBtn.setText("❚❚");
    }

    private void togglePlay() {
        if (player == null) {
            return;
        }
        if (player.getStatus() == MediaPlayer.Status.PLAYING) {
            player.pause();
            playPauseBtn.setText("▶");
        } else {
            player.play();
            playPauseBtn.setText("❚❚");
        }
    }

    private void seekRelative(Duration d) {
        if (player != null) {
            Duration newTime = player.getCurrentTime().add(d);
            if (newTime.lessThan(Duration.ZERO)) {
                newTime = Duration.ZERO;
            } else if (newTime.greaterThan(player.getMedia().getDuration())) {
                newTime = player.getMedia().getDuration();
            }
            player.seek(newTime);
        }
    }

    private String formatTime(Duration current, Duration total) {
        int cur = (int) Math.floor(current.toSeconds());
        int tot = (int) Math.floor(total.toSeconds());
        return String.format("%02d:%02d / %02d:%02d", cur / 60, cur % 60, tot / 60, tot % 60);
    }
}
