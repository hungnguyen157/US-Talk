package com.example.ustalk.utilities;

import android.content.Context;
import android.media.MediaPlayer;

import java.io.IOException;

public class AudioService {
    private Context context;
    private MediaPlayer mediaPlayer;
    private boolean isPlaying;
    private int currentPosition;

    public AudioService(Context context){
        this.context = context;
        this.mediaPlayer = new MediaPlayer();
        this.isPlaying = false;
        this.currentPosition = 0;
    }

    public void playAudioFromURL(String URL, OnPlayCallBack onPlayCallBack){
        if (mediaPlayer != null){
            try{
                if (currentPosition == 0) {
                    if (mediaPlayer.isPlaying()){
                        mediaPlayer.stop();
                        mediaPlayer.release();
                    }
                    mediaPlayer.setDataSource(URL);
                    mediaPlayer.prepare();
                }
                mediaPlayer.seekTo(currentPosition);
                mediaPlayer.start();
                isPlaying = true;
            } catch (IOException e) {
                e.printStackTrace();
            }

            mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    isPlaying = false;
                    currentPosition = 0;
                    mp.release();
                    onPlayCallBack.onFinished();
                }
            });
        }
    }

    public void pauseAudio(){
        mediaPlayer.pause();
        currentPosition = mediaPlayer.getCurrentPosition();
        isPlaying = false;
    }

    public boolean isPlaying(){
        return isPlaying;
    }

    public long getCurrentPostion(){ return currentPosition; }

    public long getDuration(){ return mediaPlayer.getDuration(); }

    public void seekTo(int position) {
        currentPosition = position;
        mediaPlayer.seekTo(currentPosition);
    }

    public interface OnPlayCallBack{
        void onFinished();
    }
}
