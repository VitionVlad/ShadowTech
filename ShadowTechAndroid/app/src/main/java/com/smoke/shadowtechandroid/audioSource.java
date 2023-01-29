package com.smoke.shadowtechandroid;

import android.content.Context;
import android.media.MediaPlayer;

public class audioSource {
    public MediaPlayer source;
    public void init(Context context, int pathtofile){
        source = MediaPlayer.create(context, pathtofile);
    }
    audioSource(Context context, int pathtofile){
        init(context, pathtofile);
    }
    audioSource(){}
    void play(){
        source.start();
    }
    void stop(){
        source.stop();
    }
}
