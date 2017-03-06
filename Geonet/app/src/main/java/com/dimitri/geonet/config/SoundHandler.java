package com.dimitri.geonet.config;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;

import java.io.IOException;

/**
 * Created by Dimitri on 04/03/2017.
 */

public class SoundHandler {
    public MediaPlayer player;
    public SoundHandler(Context ctx, String route) {
        AssetFileDescriptor afd = null;
        try {
            afd = ctx.getAssets().openFd(route);

        player = new MediaPlayer();
            player.setDataSource(afd.getFileDescriptor(),afd.getStartOffset(),afd.getLength());
        player.prepare();
        player.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
