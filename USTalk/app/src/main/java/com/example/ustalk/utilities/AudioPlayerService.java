package com.example.ustalk.utilities;

import android.media.MediaPlayer;
import android.net.Uri;

import java.io.File;
import java.io.IOException;

public class AudioPlayerService {
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private int currentPosition;
    private int duration;
    private final String audioSrc;

    public AudioPlayerService(String URLorURLString){
        this.mediaPlayer = new MediaPlayer();
        this.isPlaying = false;
        this.currentPosition = 0;
        this.duration = 0;
        audioSrc = URLorURLString;

        try {
            prepareAudioPlayer();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void prepareAudioPlayer() throws IOException {
        mediaPlayer.setDataSource(audioSrc);
        mediaPlayer.prepare();
        duration = mediaPlayer.getDuration();
        mediaPlayer.seekTo(currentPosition);
    }

    public void playAudio(OnPlayCallBack onPlayCallBack){
        mediaPlayer.start();
        isPlaying = true;

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            currentPosition = 0;
            mp.reset();
            try {
                prepareAudioPlayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
            onPlayCallBack.onFinished();
        });
    }

    public void playAudioFromURL(String URL, OnPlayCallBack onPlayCallBack){
        try{
            this.mediaPlayer = new MediaPlayer();
            mediaPlayer.setDataSource(URL);
            mediaPlayer.prepare();
            mediaPlayer.seekTo(currentPosition);
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(mp -> {
            isPlaying = false;
            currentPosition = 0;
            mp.reset();
            onPlayCallBack.onFinished();
        });
    }

    public void pauseAudio(){
        mediaPlayer.pause();
        currentPosition = mediaPlayer.getCurrentPosition();
        System.out.println(currentPosition);
        isPlaying = false;
    }

    public boolean isPlaying() { return isPlaying; }

    public long getCurrentPosition() {
        currentPosition = mediaPlayer.getCurrentPosition();
        return currentPosition;
    }

    public long getDuration() { return duration; }

    public void seekTo(int position) {
        currentPosition = position;
        mediaPlayer.seekTo(currentPosition);
    }

    public void clearPlayer() {
        if (isPlaying) mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
        currentPosition = 0;
        isPlaying = false;
    }

    public interface OnPlayCallBack{
        void onFinished();
    }
}
