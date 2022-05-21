package com.music;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.Timer;

import javafx.util.Duration;

import javax.swing.*;

public class Controller implements Initializable {
    @FXML
    public Button previousBtn, playBtn, nextBtn, resetBtn, pauseBtn, openFile, exit;

    @FXML
    protected ComboBox<String> speedBox;

    @FXML
    public Slider slider;

    @FXML
    public ProgressBar progressBar;

    @FXML
    public Label songNameLabel;

    @FXML
    public MenuItem openMenuItem,resetMenuItem, quitMenuItem, aboutMenuItem;

    @FXML
    public ListView<String> songsList;

    @FXML
    public GridPane gridPane;
    int flag=0;
    public  Media media;
    public MediaPlayer mediaPlayer;
    private File directory;
    private File[] files;
    private ArrayList<File> songs;
    private int songNumber;
    private final int[] speeds = {25, 50, 75, 100, 125, 150, 175, 200};

    private Timer timer;
    public Boolean running = false;

    File songFile;
    
    String currentDirectory = "V:\\intern\\project\\music";

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        //  While no files are added,it will show the text-> FIVE STARS
        songNameLabel.setText("FIVE STARS");

        // An array list to store the audio files as objects.
        songs = new ArrayList<>();

        // For speeds. Audio File can be played at desired speeds.
        for (int speed : speeds) speedBox.getItems().add(Integer.toString(speed));

        // An action event to change the speed of audio file as user selects.
        speedBox.setOnAction(event -> speed());

        //  volume slider. To manipulate the volume of audio file.
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if(flag!=1){fileOpenAlert();return;}
            mediaPlayer.setVolume(slider.getValue() * 0.01);
        });

        //  progress bar. Show the run time of audio file
        progressBar.setStyle("-fx-accent : #00ff00");

        // On click event on progress bar to move forward or backward as user desired.
        progressBar.setOnMouseClicked(this::progressBarClick);//progressBar.setOnMouseClicked(mouseEvent -> progressBarClick(mouseEvent));

        //  song list view. on mouse click event-> plays the selected song in the list view
        songsList.setOnMouseClicked(mouseEvent ->selectedSong());

    }

    public void openFile() {

        //  JFileChooser is a swig component, that opens the files to select.
        JFileChooser openFileChooser = new JFileChooser(currentDirectory);

        // FileTypeFilter is class created by developer to filter the mp3 audio files only.
        openFileChooser.setFileFilter(new FileTypeFilter(".mp3", "Open MP3 Files Only!"));
        int result = openFileChooser.showOpenDialog(null);
        // If condition will be true when the dialog box with files has popped up.
        if (result == JFileChooser.APPROVE_OPTION) {
            // check weather any songs present in arrayList or not.
            // And if present they will get cleared and new songs will be added
            // Actually works when user open files for the second time at single run.
            if(songs!=null){songs.clear();}
            // if any audio file is running while opening the files for second or more times then,
            // media player has to stop the current playing audio file and timer has to restart.
            if(running){cancelTimer(); mediaPlayer.stop();}

            // SongFile will store the audio file which is selected by the user, when user opens folder or file.
            songFile = openFileChooser.getSelectedFile();
            // song name label will be changed from previous to present playing audio file.
            songNameLabel.setText(songFile.getName().substring(0,songFile.getName().length()-4));

            // directory will store the path of the present playing audio file.
            directory= new File(songFile.getParent());
            // files store the all type of files in the directory or folder.
            files=directory.listFiles();

            if( files != null){

                //  flag variable, that defines weather any audio files is opened or not.
                flag=1;

                // while files are not null, in beginning we clear the list view of the songs.
                songsList.getItems().clear();
                for (File file : files) {
                    // Check weather the file is mp3 or not.
                    if (file.getName().contains(".mp3")) {
                        // if the file is in the format of mp3 then it will be added to the songs arrayList.
                        songs.add(file);
                        // Simultaneously the name of the song will be added to the list view and displayed.
                        songsList.getItems().add(file.getName().substring(0,file.getName().length()-4));

                    }
                    // Terminates when no file left unchecked.
                }

                // Searches for selected audio file in file chooser with in the songs arrayList
                for(int i=0; i<=songs.size()-1;i++){
                    if(songFile.equals(songs.get(i))){
                        // Whenever they got matched it will assign the song to media player and get ready to play.
                        media = new Media(songFile.toURI().toString());
                        mediaPlayer = new MediaPlayer(media);
                        // Initially the volume of player is 50%
                        mediaPlayer.setVolume(0.5);
                        //Assigns the audio file number to songNumber.
                        songNumber=i;
                        // calling the function songPlay() to run or play the audio.
                        songPlay();
                    }
                }
            }
        }
    }

    @FXML
    public void songPlay() {

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}
        // if any song is already playing and calls for new song then we have to cancel the timer and restart it.
        if(running) {cancelTimer();}
        running = true;
        pauseBtn.setText("Pause");

        beginTimer();
        speed();
        // Adjusting the volume as per the value in the slider.
        mediaPlayer.setVolume(slider.getValue()*0.01);
        // playing the song which is already added in the media player.
        mediaPlayer.play();
    }

    private void progressBarClick(MouseEvent e) {

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1)return;

        // Retrieving the parent of the progress bar for future reference
        Parent parent = progressBar.getParent();
        // if parent is not null then we will send that event to the parent.
        if (parent != null) parent.fireEvent(e);
        // x variable will store the x-axis value of progress bar, where click event happen.
        double x= e.getX();
        // width contains the total length of the progress bar.
        double width=progressBar.getWidth();
        double progression = (x/width);
        //ms contains the milliseconds of audio file where click event happens.
        int ms= (int) (progression*mediaPlayer.getTotalDuration().toMillis());
        // Duration is a class which stores the double value,
        // Here it stores milliseconds where click event happen by the mouse
        Duration duration= new Duration(ms);
        // playing the audio file, from where ever mouse click event happen by mouse
        mediaPlayer.seek(duration);
    }

    private void selectedSong() {

        // This function is called when user click on the song in the list view.

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1)return;
        int temp=songNumber;
        // index of the selected song will be retrieved and stored in songNumber
        songNumber = songsList.getSelectionModel().getSelectedIndex();

        //Do nothing if user clicks on blank space in list view.
        if(songNumber>songs.size() || songNumber<0)return;
        // if user clicks on the audio file which is already playing then it will do nothing.
        // Means playing will continue without any disturb.
        if(temp==songNumber)return;

        // Have to stop the previously playing song firstly.
        mediaPlayer.stop();
        // Adding the selected song to media, with the help of songNumber. old media will be cleared.
        media = new Media(songs.get(songNumber).toURI().toString());
        // Adding the updated media to media player.
        mediaPlayer = new MediaPlayer(media);

        // Changing the song name label to present playing audio file or audio file which selected by user.
        songNameLabel.setText(songs.get(songNumber).getName().substring(0,songs.get(songNumber).getName().length()-4));
        // calling the song play function.
        songPlay();
    }

    @FXML
    public void previous() {

        // This method is called when user clicked on previous button.
        // Plays the preceding audio file to current playing audio file.

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}
        // i variable stores the pre audio file index for the present playing audio file.
        int i=songNumber==0 ? (songNumber=songs.size()-1):songNumber-- ;

        // stops the present playing audio file.
        mediaPlayer.stop();
        // if any song is already playing and calls for new song then we have to cancel the timer and restart it.
        if (running)cancelTimer();
        // Adding the selected song to media, with the help of songNumber. old media will be cleared.
        media = new Media(songs.get(songNumber).toURI().toString());
        // Adding the updated media to media player.
        mediaPlayer = new MediaPlayer(media);

        // Changing the song name label to present playing audio file or audio file which selected by user.
        songNameLabel.setText(songs.get(songNumber).getName().substring(0,songs.get(songNumber).getName().length()-4));
        // calling the song play function.
        songPlay();
    }

    @FXML
    public void next() {

        // This method is called when user clicked on next button.
        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}
        // i variable stores the next audio file index for the present playing audio file.
        int i = (songs.size() - 1 > songNumber) ? (songNumber++) : (songNumber = 0);

        // stops the present playing audio file.
        mediaPlayer.stop();
        // if any song is already playing and calls for new song then we have to cancel the timer and restart it.
        if (running)cancelTimer();
        // Adding the selected song to media, with the help of songNumber. old media will be cleared.
        media = new Media(songs.get(songNumber).toURI().toString());
        // Adding the updated media to media player.
        mediaPlayer = new MediaPlayer(media);
        // Changing the song name label to present playing audio file or audio file which selected by user.
        Platform.runLater(() -> songNameLabel.setText(songs.get(songNumber).getName().substring(0,songs.get(songNumber).getName().length()-4)));
        // calling the song play function.
        songPlay();
    }

    public void pause() {

        // This method is called when user clicks on the pause button
        // when user clicks pause button then text will change from pause to resume and vice versa.

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}

        // if any audio file is already playing and calls for new song then we have to cancel the timer.
        if (running){
            cancelTimer();
            running = false;
            // Pause the media player until user click on the resume button.
            mediaPlayer.pause();
            // Change the text in pause button from pause from to resume.
            pauseBtn.setText("Resume");
        }
        // if no audio file is running else part will be executed.
        else {
            // Change the text in pause button form resume to pause
            pauseBtn.setText("Pause");
            // Calling the song play method.
            songPlay();
        }
    }

    public void reset() {

        // This method is executed when user clicks reset button,
        // It will reset the current playing audio file, to play it from beginning.

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}
        // Progress bar is reset to initial position.
        progressBar.setProgress(0);
        // Change the duration of song to zero seconds,
        // So that it will play form the beginning.
        mediaPlayer.seek(Duration.seconds(0));
    }

    public void speed() {

        // This method is executed when user clicks on the speed combo box.
        // It has a dropdown list, that are added before(25,50,100,125,150,175,200)

        // If there is no file in audio track, then it will simply return void.
        if(flag!=1){fileOpenAlert();return;}
        // if the value of speedBox is null then audio file will be played normally.
        if (speedBox.getValue() == null) {
            mediaPlayer.setRate(1);
        } else {
            mediaPlayer.setRate(Integer.parseInt(String.valueOf(speedBox.getValue())) * 0.01);
        }
    }

    private void beginTimer() {
        timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            public void run() {
                running = true;
                try{
                double current = mediaPlayer.getCurrentTime().toSeconds();
                double end = mediaPlayer.getTotalDuration().toSeconds();
                progressBar.setProgress(current / end);
                if (current / end == 1) {
                    // nextBtn.fire();
                    cancelTimer();
                    nextBtn.fire();
                }}catch(Exception x){close();}
            }
        };
        timer.scheduleAtFixedRate(timerTask, 0, 1000);
    }

    private void cancelTimer() {
        // changing the value of variable running to false.
        running = false;
        // cancelling the timer
        timer.cancel();
    }

    private void fileOpenAlert() {

        // Whenever user clicks on the buttons like next, previous, pause/resume, speeds, volume slider..soon,
        // without opening any file, it will result in pop up box which shows select file first.
        // Alert class is with the type of warning
        Alert fileOpenAlert = new Alert(Alert.AlertType.WARNING);
        fileOpenAlert.setTitle("Alert");
        fileOpenAlert.setHeaderText("No file selected!");
        fileOpenAlert.setContentText("Open a file first");
        fileOpenAlert.show();
    }

    @FXML
    private void aboutApp() {

        // This method is called when user, click about option which is present in menu bar.
        // Initializing  Alert class as information type.
        Alert aboutAlert = new Alert(Alert.AlertType.INFORMATION);
        aboutAlert.setTitle("About");
        aboutAlert.setHeaderText("Music Player");
        aboutAlert.setContentText("It is a simple music player. Created by 5* ");
        aboutAlert.show();
    }

    public void close() {
        Platform.exit();
        System.exit(0);
    }

}