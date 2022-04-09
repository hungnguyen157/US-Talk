package com.example.ustalk.utilities;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioService {
    private Context context;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private int currentPosition;
    private OnPlayCallBack onPlayCallBack;

    public AudioService(Context context){
        this.context = context;
        this.mediaPlayer = new MediaPlayer();
        this.isPlaying = false;
        this.currentPosition = 0;
    }

    public void playAudioFromURL(String URL, OnPlayCallBack onPlayCallBack){
        this.onPlayCallBack = onPlayCallBack;

        try{
            if (currentPosition == 0){
                if (mediaPlayer != null){
                    mediaPlayer.stop();
                    mediaPlayer.release();
                }
                mediaPlayer.setDataSource(URL);
                mediaPlayer.prepare();
            }
            else{
                mediaPlayer.seekTo(currentPosition);
            }
            mediaPlayer.start();
            isPlaying = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
             @Override
             public void onCompletion(MediaPlayer mp) {
                 mp.release();
                 onPlayCallBack.onFinished();
             }
         });
    }

    public void pauseAudio(){
        mediaPlayer.pause();
        currentPosition = mediaPlayer.getCurrentPosition();
        isPlaying = false;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public interface OnPlayCallBack{
        void onFinished();
    }
}
