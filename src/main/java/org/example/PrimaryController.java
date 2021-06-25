package org.example;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.media.AudioSpectrumListener;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.stage.DirectoryChooser;
import javafx.util.Duration;

public class PrimaryController  implements Initializable {

    public MediaView mediaView;
    public Label labelPlaying;
    public ImageView pause_button;
    public Slider volume;
    public Slider slider;
    double x, y;
    public void panel_dragged(MouseEvent mouseEvent) {
        App.getInstance().getStage().setX(mouseEvent.getScreenX() + x);
        App.getInstance().getStage().setY(mouseEvent.getScreenY() + y);
    }

    public void panel_pressed(MouseEvent mouseEvent) {
        x = App.getInstance().getStage().getX() - mouseEvent.getScreenX();
        y = App.getInstance().getStage().getY() - mouseEvent.getScreenY();
    }

    public void exit(MouseEvent mouseEvent) {
        App.getInstance().getStage().close();
    }

    public void close(MouseEvent mouseEvent) {
        App.getInstance().getStage().setIconified(true);
    }

    ArrayList<Media> arrayList = new ArrayList<>();
    public void select_playlist(MouseEvent mouseEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        File dir = directoryChooser.showDialog(App.getInstance().getStage());
        loadMedia(dir);
        if(arrayList.size() > 0)
        {
            createMediaPlayer(arrayList.get(0));
        }

    }
    private MediaPlayer mediaPlayer;
    int mediaPlaying = 0;
    public void createMediaPlayer(Media media)
    {
        mediaPlayer = new MediaPlayer(media);
        mediaPlayer.setVolume(volume.getValue());
        mediaPlayer.play();
        pause_button.setImage(new Image("https://img.icons8.com/ios-glyphs/2x/ffffff/pause.png"));
        mediaView.setMediaPlayer(mediaPlayer);
        labelPlaying.setText(new File(media.getSource()).getName()
                .replace("%20"," ")
                .replace("%5",""));
        mediaPlayer.setOnEndOfMedia(new Runnable() {
            @Override
            public void run() {
                next_song();
            }
        });

        mediaPlayer.setAudioSpectrumListener(new AudioSpectrumListener() {
            @Override
            public void spectrumDataUpdate(double v, double v1, float[] floats, float[] floats1) {
                if(slider.isPressed())
                    return;

                int percents = (int) ((mediaPlayer.getCurrentTime().toMillis()/mediaPlayer.getMedia().getDuration().toMillis()) * 1000);
                slider.setValue(percents);
            }
        });


    }

    public void loadMedia(File dir)
    {
        if(dir == null)
            return;
        if(dir.isDirectory())
        {
            for (File f : dir.listFiles())
            {
                if(f.isFile())
                {
                    if(f.getName().contains(".mp3") || f.getName().contains(".wav"))
                    {
                        arrayList.add(new Media(f.toURI().toString()));
                    }
                }else if(f.isDirectory())
                {
                    loadMedia(f);
                }
            }
        }

    }

    private boolean isPaused = false;
    public void prev_song(MouseEvent mouseEvent) {
        mediaPlayer.dispose();
        mediaPlaying --;
        if(mediaPlaying < 0)
        {
            mediaPlaying = arrayList.size()-1;
        }
        createMediaPlayer(arrayList.get(mediaPlaying));
    }

    public void pause(MouseEvent mouseEvent) {
        if(mediaPlayer != null)
        {
            if(!isPaused)
            {

                pause_button.setImage(new Image("https://img.icons8.com/ios-glyphs/2x/ffffff/play.png"));
                mediaPlayer.pause();
                isPaused = true;

            }else if(isPaused)
            {
                pause_button.setImage(new Image("https://img.icons8.com/ios-glyphs/2x/ffffff/pause.png"));
                mediaPlayer.play();
                isPaused = false;
            }
        }
    }

    public void next_song() {
        mediaPlayer.dispose();
        mediaPlaying ++;
        if(arrayList.size() - 1 < mediaPlaying)
            mediaPlaying = 0;

        createMediaPlayer(arrayList.get(mediaPlaying));
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if(volume == null)
            return;
        volume.setMax(1f);
        volume.setValue(0.3);

        volume.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number onValue, Number newValue) {
                if(mediaPlayer == null)
                    return;
                mediaPlayer.setVolume(volume.getValue());
            }
        });

        if(slider == null)
            return;

        slider.setMax(1000);
        slider.setOnMouseReleased(mouseEvent -> {
            if(mediaPlayer == null)
                return;
            double d = (slider.getValue() / slider.getMax())* 100;

            mediaPlayer.stop();
            mediaPlayer.setStartTime(new Duration(mediaPlayer.getMedia().getDuration().toMillis() * (d/100)));
            mediaPlayer.play();

        });




    }
}
